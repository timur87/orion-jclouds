package org.jclouds.orion.blobstore.functions.parsers.response;

import org.jclouds.http.HttpResponse;

import com.google.common.base.Function;

public class FileExistsResponseParser implements Function<HttpResponse, Boolean> {

	@Override
	public Boolean apply(HttpResponse response) {
		if (String.valueOf(response.getStatusCode()).startsWith("2")) {
			return true;
		}
		return false;
	}

}
