/*
 *  Copyright 2012 National Instruments Corporation
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.pieframework.model.resources;

import org.simpleframework.xml.Element;

import com.pieframework.model.resources.Resource;

public class Artifact extends Resource{
	
	@Element
	private String artifactRepository;
	
	@Element (required=false)
	private String type;
	
	@Element (required=false)
	private String artifactAddress;
	
	@Element (required=false)
	private String pathFilter;
	
	@Element
	private String localPath;

	public String getArtifactRepository() {
		return artifactRepository;
	}

	public void setArtifactRepository(String artifactRepository) {
		this.artifactRepository = artifactRepository;
	}

	public String getArtifactAddress() {
		return artifactAddress;
	}

	public void setArtifactAddress(String artifactAddress) {
		this.artifactAddress = artifactAddress;
	}

	

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPathFilter() {
		return pathFilter;
	}

	public void setPathFilter(String pathFilter) {
		this.pathFilter = pathFilter;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}
	
}
