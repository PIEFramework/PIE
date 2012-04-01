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
package com.pieframework.resources;

import org.simpleframework.xml.Element;

import com.pieframework.model.resources.Resource;

public class AzureLocalStorage extends Resource{

	@Element
	private String name;
	
	@Element
	private String cleanOnRecycle;
	
	@Element
	private String sizeInMB;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCleanOnRecycle() {
		return cleanOnRecycle;
	}

	public void setCleanOnRecycle(String cleanOnRecycle) {
		this.cleanOnRecycle = cleanOnRecycle;
	}

	public String getSizeInMB() {
		return sizeInMB;
	}

	public void setSizeInMB(String sizeInMB) {
		this.sizeInMB = sizeInMB;
	}
	
	
}
