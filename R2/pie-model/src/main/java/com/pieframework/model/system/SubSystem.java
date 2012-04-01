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

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.core.Commit;
import org.simpleframework.xml.core.Persist;

import com.pieframework.model.Component;
import com.pieframework.model.Operator;
import com.pieframework.model.Request;
import com.pieframework.model.Status;
import com.pieframework.model.operations.Operation;
import com.pieframework.model.resources.Resource;

public class SubSystem extends BaseComponent implements Component {

	@ElementList(name = "roles", inline = false, required = false)
	private ArrayList<Role> roleList = new ArrayList<Role>();

	private HashMap<String, Role> roles = new HashMap<String, Role>();

	@Commit
	private void commit() {
		for (Role role : roleList) {
			roles.put(role.getId(), role);
		}

		for (Resource resource : resourceList) {
			resources.put(resource.getId(), resource);
		}
		
		for (Operation command : commandList) {
			commands.put(command.getId(), command);
		}
	}

	@Persist
	private void prepare() {
		roleList = new ArrayList<Role>(roles.values());
		resourceList = new ArrayList<Resource>(resources.values());
		commandList = new ArrayList<Operation>(commands.values());
	}

	public HashMap<String, Role> getRoles() {
		return roles;
	}

	public void setRoles(HashMap<String, Role> roles) {
		this.roles = roles;
	}

	@Override
	public String toString() {
		ReflectionToStringBuilder
				.setDefaultStyle(ToStringStyle.MULTI_LINE_STYLE);
		return ReflectionToStringBuilder.toStringExclude(this, "list");
	}

	public Status handle(Operator o, Request r) {
		return o.process(this, r);
	}

	public Status eval(Operator o, Request r) {
		return o.validate(this, r);
	}

	@Override
	public HashMap<String, Component> getChildren() {
		HashMap<String, Component> children = new HashMap<String, Component>();
		children.putAll(this.getRoles());

		return children;
	}
}
