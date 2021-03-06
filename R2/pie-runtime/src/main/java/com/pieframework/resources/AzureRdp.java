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

public class AzureRdp extends Resource{

	@Element
	private String rdpUsername;
	
	@Element
	private String rdpPassword;
	
	@Element 
	private String rdpExpiration;

	public String getRdpUsername() {
		return rdpUsername;
	}

	public void setRdpUsername(String rdpUsername) {
		this.rdpUsername = rdpUsername;
	}

	public String getRdpPassword() {
		return rdpPassword;
	}

	public void setRdpPassword(String rdpPassword) {
		this.rdpPassword = rdpPassword;
	}

	public String getRdpExpiration() {
		return rdpExpiration;
	}

	public void setRdpExpiration(String rdpExpiration) {
		this.rdpExpiration = rdpExpiration;
	}
	
	
	
}
