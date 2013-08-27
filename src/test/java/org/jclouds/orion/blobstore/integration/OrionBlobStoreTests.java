package org.jclouds.orion.blobstore.integration;

import java.util.Calendar;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "OrionApiMetadataTest")
public class OrionBlobStoreTests {

	private BlobStore blobStore;

	@Test
	protected void containerExists() throws Exception {
		String container = "Container+"
				+ Calendar.getInstance().getTimeInMillis();
		blobStore.createContainerInLocation(null, container);
		Assert.assertTrue(blobStore.containerExists(container),
				"Container SHOULD exist");
		Assert.assertTrue(!blobStore.containerExists(String.valueOf(Calendar
				.getInstance().getTimeInMillis())),
				"Container SHOULD NOT exist");
	}

	@Test
	protected void createContainer() throws Exception {
		blobStore.createContainerInLocation(null, "Container+"
				+ Calendar.getInstance().getTimeInMillis());
	}

	@Test
	protected void deleteContainer() throws Exception {
		String container = "Container+"
				+ Calendar.getInstance().getTimeInMillis();
		blobStore.deleteContainer(container);
		blobStore.createContainerInLocation(null, container);
		Assert.assertTrue(blobStore.containerExists(container),
				"Container SHOULD exist");
		Assert.assertTrue(!blobStore.containerExists(String.valueOf(Calendar
				.getInstance().getTimeInMillis())),
				"Container SHOULD NOT exist");
	}

	@Test
	protected void clearContainer() throws Exception {
		String container = "Container+"
				+ Calendar.getInstance().getTimeInMillis();
		blobStore.createContainerInLocation(null, container);
		Assert.assertTrue(blobStore.containerExists(container),
				"Container SHOULD exist");
		Assert.assertTrue(!blobStore.containerExists(String.valueOf(Calendar
				.getInstance().getTimeInMillis())),
				"Container SHOULD NOT exist");
	}

	@Test
	protected void putBlob() throws Exception {
		String container = "Container+"
				+ Calendar.getInstance().getTimeInMillis();
		blobStore.createContainerInLocation(null, container);
		Assert.assertTrue(blobStore.containerExists(container),
				"Container SHOULD exist");
		Assert.assertTrue(!blobStore.containerExists(String.valueOf(Calendar
				.getInstance().getTimeInMillis())),
				"Container SHOULD NOT exist");
		String blobName = "/level1/level2/Blob+"
				+ Calendar.getInstance().getTimeInMillis();
		Blob blob = blobStore.blobBuilder(blobName).build();
		blob.setPayload("PutBlobTest");
		blobStore.putBlob(container, blob);
	}

	@Test
	protected void getBlobMetadata() throws Exception {
		String container = "Container+"
				+ Calendar.getInstance().getTimeInMillis();
		blobStore.createContainerInLocation(null, container);
		String blobName = "/level1/level2/Blob+"
				+ Calendar.getInstance().getTimeInMillis();
		Blob blob = blobStore.blobBuilder(blobName).build();
		blob.setPayload("PutBlobTest");
		blob.getMetadata().getUserMetadata().put("test", "test");
		blobStore.putBlob(container, blob);
		BlobMetadata metadata = blobStore.blobMetadata(container, blobName);
		Assert.assertEquals(metadata.getUserMetadata().containsKey("test"),
				true, "user metadata is not there");

	}

	@BeforeSuite
	protected void setUp() throws Exception {
		BlobStoreContext context = ContextBuilder.newBuilder("orionblob")
				.endpoint("http://127.0.0.1:8080")
				.credentials("timur", "123456").build(BlobStoreContext.class);
		// create a container in the default location
		blobStore = context.getBlobStore();
	}
}
