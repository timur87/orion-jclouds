/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.orion.blobstore.functions.parsers;

import java.util.List;

import org.jclouds.http.HttpResponse;
import org.jclouds.orion.blobstore.functions.ListMetadataToChildrenList;
import org.jclouds.orion.domain.OrionChildMetadata;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * @author timur
 * 
 */
public class FolderListParser implements Function<HttpResponse, List<OrionChildMetadata>> {

	private final ListMetadataToChildrenList listMetadata2ChildrenList;

	@Inject
	public FolderListParser(ListMetadataToChildrenList listMetadata2ChildrenList) {
		this.listMetadata2ChildrenList = Preconditions.checkNotNull(listMetadata2ChildrenList,
		      "listMetadata2ChildrenList is null");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.common.base.Function#apply(java.lang.Object)
	 */
	@Override
	public List<OrionChildMetadata> apply(HttpResponse res) {
		return listMetadata2ChildrenList.apply(res);
	}
}
