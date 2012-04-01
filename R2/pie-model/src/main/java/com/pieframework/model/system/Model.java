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
package com.pieframework.model.system;

import java.util.HashMap;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.pieframework.model.Configuration;
import com.pieframework.model.Request;
import com.pieframework.model.repositories.Repositories;
import com.pieframework.model.repositories.Repository;

/**
 * @author whackett
 * 
 */
public class Model {

	private HashMap<String, System> systems = new HashMap<String, System>();

	private HashMap<String, Repository> repositories = new HashMap<String, Repository>();

	private String modelDirectory;

	private Configuration configuration;

	private Request request;

	public HashMap<String, System> getSystems() {
		return systems;
	}

	public void setSystems(HashMap<String, System> systems) {
		this.systems = systems;
	}

	public Model withSystem(System system) {

		HashMap<String, com.pieframework.model.system.System> systemMap = new HashMap<String, com.pieframework.model.system.System>();
		systemMap.put(system.getId(), system);
		setSystems(systemMap);

		return this;
	}

	public Model withRepositories(Repositories repos) {
		repositories = repos.getRepositories();
		return this;
	}

	public Model withModelDirectory(String path) {
		modelDirectory = path;
		return this;
	}

	public Model withConfiguration(Configuration config) {
		configuration = config;
		return this;
	}

	public HashMap<String, Repository> getRepositories() {
		return repositories;
	}

	public void setRepositories(HashMap<String, Repository> repositories) {
		this.repositories = repositories;
	}

	public String getModelDirectory() {
		return modelDirectory;
	}

	public void setModelDirectory(String modelDirectory) {
		this.modelDirectory = modelDirectory;
	}

	public System getSystem() {
		// there can only one - otherwise there's an error
		if (getSystems().size() > 1) {
			throw new RuntimeException(
					"Cannot have more than one system in model.");
		}
		return getSystems().entrySet().iterator().next().getValue();
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}
	
	public Model withRequest(Request request) {
		this.request = request;
		return this;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		ReflectionToStringBuilder
				.setDefaultStyle(ToStringStyle.MULTI_LINE_STYLE);
		return ReflectionToStringBuilder.toString(this);
	}

	
}
