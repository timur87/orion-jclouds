package org.jclouds.orion.http.filters.create;

import org.jclouds.http.HttpException;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpRequestFilter;
import org.jclouds.orion.domain.OrionSpecificFileMetadata;
import org.jclouds.orion.http.functions.OrionSpecificObject2JSON;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * This class should be present before providing remaining filters
 * 
 * @author timur
 * 
 */
public class EmptyRequestFilter implements HttpRequestFilter {

	private final OrionSpecificFileMetadata metadata;
	private final OrionSpecificObject2JSON orionSpecificObject2JSON;

	@Inject
	public EmptyRequestFilter(OrionSpecificFileMetadata metadata,
			OrionSpecificObject2JSON orionSpecificObject2JSON) {
		this.metadata = Preconditions
				.checkNotNull(metadata, "metadata is null");
		this.orionSpecificObject2JSON = Preconditions.checkNotNull(
				orionSpecificObject2JSON, "orionSpecificObject2JSON is null");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jclouds.http.HttpRequestFilter#filter(org.jclouds.http.HttpRequest )
	 */
	@Override
	public HttpRequest filter(HttpRequest req) throws HttpException {
		req = req.toBuilder()
				.payload(this.orionSpecificObject2JSON.apply(metadata)).build();
		return req;
	}

}