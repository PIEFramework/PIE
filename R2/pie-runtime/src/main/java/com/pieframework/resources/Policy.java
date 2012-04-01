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

public class Policy extends Resource{
	@Element
	private String location;
	
	@Element
	private String size;
	
	@Element
	private int minRunning;
	
	@Element
	private int maxInstances;
	
	@Element
	private int minIdle;
	
	@Element
	private int maxIdle;
	
	@Element
	private int minSpare;
	
	@Element
	private int maxSpare;
	
	@Element
	private int pollInterval;

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public int getMinRunning() {
		return minRunning;
	}

	public void setMinRunning(int minRunning) {
		this.minRunning = minRunning;
	}

	public int getMaxInstances() {
		return maxInstances;
	}

	public void setMaxInstances(int maxInstances) {
		this.maxInstances = maxInstances;
	}

	public int getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}

	public int getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}

	public int getMinSpare() {
		return minSpare;
	}

	public void setMinSpare(int minSpare) {
		this.minSpare = minSpare;
	}

	public int getMaxSpare() {
		return maxSpare;
	}

	public void setMaxSpare(int maxSpare) {
		this.maxSpare = maxSpare;
	}

	public int getPollInterval() {
		return pollInterval;
	}

	public void setPollInterval(int pollInterval) {
		this.pollInterval = pollInterval;
	}
	
	
}
