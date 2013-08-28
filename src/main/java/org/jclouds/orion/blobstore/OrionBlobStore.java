package org.jclouds.orion.blobstore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.internal.BaseBlobStore;
import org.jclouds.blobstore.options.CreateContainerOptions;
import org.jclouds.blobstore.options.GetOptions;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.blobstore.util.BlobUtils;
import org.jclouds.collect.Memoized;
import org.jclouds.domain.Credentials;
import org.jclouds.domain.Location;
import org.jclouds.location.Provider;
import org.jclouds.orion.OrionApi;
import org.jclouds.orion.OrionUtils;
import org.jclouds.orion.blobstore.functions.BlobPropertiesToBlobMetadata;
import org.jclouds.orion.blobstore.functions.BlobToOrionBlob;
import org.jclouds.orion.config.constans.OrionConstantValues;
import org.jclouds.orion.domain.BlobType;
import org.jclouds.orion.domain.OrionBlob;
import org.jclouds.orion.domain.OrionBlob.Factory;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.inject.Inject;

public class OrionBlobStore extends BaseBlobStore {

    private final OrionApi api;
    private final String workspaceName;
    private final BlobToOrionBlob blob2OrionBlob;
    private final BlobPropertiesToBlobMetadata blobProps2BlobMetadata;
    private final BlobUtils blobUtils;
    private final Factory orionBlobProvider;

    @Inject
    protected OrionBlobStore(BlobStoreContext context,
	    @Provider Supplier<Credentials> creds, BlobUtils blobUtils,
	    OrionApi api, Supplier<Location> defaultLocation,
	    @Memoized Supplier<Set<? extends Location>> locations,
	    BlobToOrionBlob blob2OrionBlob,
	    BlobPropertiesToBlobMetadata blobProps2BlobMetadata,
	    OrionBlob.Factory orionBlobProvider) {
	super(context, blobUtils, defaultLocation, locations);
	this.blobUtils = blobUtils;
	this.api = Preconditions.checkNotNull(api, "api is null");
	this.workspaceName = Preconditions.checkNotNull(creds.get(),
		"creds is null").identity;
	this.blob2OrionBlob = Preconditions.checkNotNull(blob2OrionBlob,
		"blob2OrionBlob is null");
	this.blobProps2BlobMetadata = Preconditions.checkNotNull(
		blobProps2BlobMetadata, "blobProps2BlobMetadata is null");
	this.orionBlobProvider = Preconditions.checkNotNull(orionBlobProvider,
		"orionBlobProvider is null");
    }

    @Override
    public boolean blobExists(String container, String blobName) {
	return api.blobExits(getUserLocation(), container,
		OrionUtils.getFilePath(blobName));
    }

    @Override
    public BlobMetadata blobMetadata(String container, String blobName) {
	String parentPath = OrionUtils.fetchParentPath(blobName);
	// Blob names must not start with a "/" since they are relative paths
	// they will be automatically removed in case it starts with that
	// Get the blob name
	// Convert the blob name to it's metadata file name and fetch it
	return blobProps2BlobMetadata
		.apply(api.getMetadata(getUserLocation(), container,
			parentPath, OrionUtils.getMetadataFileName(OrionUtils
				.fetchName(blobName))));
    }

    @Override
    public boolean containerExists(String container) {
	return api.containerExists(getUserLocation(), container);
    }

    @Override
    public void deleteContainer(String container) {
	api.deleteContainerMetadata(getUserLocation(), container);
	api.deleteContainer(getUserLocation(), container);

    }

    @Override
    public boolean createContainerInLocation(Location location, String container) {
	return this.api.createContainerAsAProject(getUserLocation(), container);
    }

    @Override
    public boolean createContainerInLocation(Location arg0, String arg1,
	    CreateContainerOptions arg2) {
	// TODO Auto-generated method stub
	throw new IllegalStateException("Not yet implemented.");
    }

    @Override
    protected boolean deleteAndVerifyContainerGone(String container) {
	api.deleteContainerMetadata(getUserLocation(), container);
	return api.deleteContainer(getUserLocation(), container);
    }

    @Override
    public Blob getBlob(String arg0, String arg1, GetOptions arg2) {
	// TODO Auto-generated method stub
	throw new IllegalStateException("Not yet implemented.");
    }

    private String getUserLocation() {
	return this.workspaceName;
    }

    @Override
    public PageSet<? extends StorageMetadata> list() {
	// TODO Auto-generated method stub
	throw new IllegalStateException("Not yet implemented.");
    }

    @Override
    public PageSet<? extends StorageMetadata> list(String arg0,
	    ListContainerOptions arg1) {
	// TODO Auto-generated method stub
	throw new IllegalStateException("Not yet implemented.");
    }

    @Override
    public String putBlob(String container, Blob blob) {
	OrionBlob orionBlob = this.blob2OrionBlob.apply(blob);
	// Copy temporarily the inputstream otherwise JVM closes the stream
	ByteArrayOutputStream tempOutputStream = new ByteArrayOutputStream();
	try {
	    IOUtils.copy(blob.getPayload().getInput(), tempOutputStream);
	} catch (IOException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}

	ArrayList<String> pathList = new ArrayList<String>(
		Arrays.asList(orionBlob.getProperties().getParentPath()
			.split(OrionConstantValues.PATH_DELIMITER)));
	createPathRecursively(container, pathList);

	api.createBlob(getUserLocation(), container, orionBlob.getProperties()
		.getParentPath(), orionBlob);
	// put contents in case it has some contents
	if (orionBlob.getProperties().getType() == BlobType.FILE_BLOB) {
	    orionBlob.setPayload(tempOutputStream.toByteArray());
	    api.putBlob(getUserLocation(), container, orionBlob.getProperties()
		    .getParentPath(), orionBlob);
	}

	createMetadata(container, orionBlob);
	return null;
    }

    @Override
    public String putBlob(String container, Blob blob, PutOptions arg2) {
	return putBlob(container, blob);
    }

    @Override
    public void removeBlob(String container, String blobName) {
	api.removeBlob(getUserLocation(), container,
		OrionUtils.getFilePath(blobName));
    }

    private boolean createMetadata(String container, OrionBlob blob) {

	return api.createMetadataFolder(getUserLocation(), container, blob
		.getProperties().getParentPath())
		&&
		// Create metadata file
		api.createMetadata(getUserLocation(), container, blob
			.getProperties().getParentPath(), OrionUtils
			.getMetadataFileName(blob.getProperties().getName())) &&
		// Add metadata file contents
		api.putMetadata(getUserLocation(), container, blob
			.getProperties().getParentPath(), blob);

    }

    /**
     * Create the non existing paths starting from index 0
     * 
     * @param container
     * @param pathArray
     */
    private void createPathRecursively(String container, List<String> pathArray) {
	if (pathArray.size() == 1) {
	    return;
	}
	String parentPath = "";
	int startIndex = 0;

	for (String path : pathArray) {
	    if (!api.folderExists(getUserLocation(), container, path)) {
		break;
	    }
	    startIndex++;
	    parentPath = parentPath + path;
	}
	for (; startIndex < pathArray.size(); startIndex++) {
	    putBlob(container,
		    blobUtils.blobBuilder().payload("")
			    .name(parentPath + pathArray.get(startIndex))
			    .type(StorageType.FOLDER).build());
	    parentPath = parentPath + pathArray.get(startIndex)
		    + OrionConstantValues.PATH_DELIMITER;
	}
    }

}
