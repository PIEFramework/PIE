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

import java.util.ArrayList;
import java.util.HashMap;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementMap;

import com.pieframework.model.Component;
import com.pieframework.model.operations.Operation;
import com.pieframework.model.resources.Resource;

public class BaseComponent {

	@Attribute
	protected String id;
	
	@ElementMap(required = false, name = "props", entry = "prop", key = "id", attribute = true, keyType = String.class, valueType = String.class)
	private HashMap<String, String> props = new HashMap<String, String>();
	
	@ElementList(name = "resources", inline = false, required = false)
	protected ArrayList<Resource> resourceList = new ArrayList<Resource>();
	
	@ElementList(name = "commands", inline = false, required = false)
	protected ArrayList<Operation> commandList = new ArrayList<Operation>();
	
	protected HashMap<String, Operation> commands = new HashMap<String, Operation>();
	
	@Element(required = false)
	private Access access;
	
	@ElementList(name = "dependencies", inline = false, required = false)
	private ArrayList<Dependency> dependencies;
	
	private Component parent;
	
	protected HashMap<String, Resource> resources = new HashMap<String, Resource>();

	public BaseComponent() {
		super();
	}

	public HashMap<String, Resource> getResources() {
		return resources;
	}

	public void setResources(HashMap<String, Resource> resources) {
		this.resources = resources;
	}

	public HashMap<String, String> getProps() {
		return props;
	}

	public void setProps(HashMap<String, String> props) {
		this.props = props;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public HashMap<String, Operation> getCommands() {
		return commands;
	}

	public void setCommands(HashMap<String, Operation> commands) {
		this.commands = commands;
	}

	public ArrayList<Dependency> getDependencies() {
		return dependencies;
	}

	public void setDependencies(ArrayList<Dependency> dependencies) {
		this.dependencies = dependencies;
	}

	public Component getParent() {
		return parent;
	}

	public void setParent(Component parent) {
		this.parent = parent;
	}
}