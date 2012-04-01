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
package com.pieframework.model;

import java.util.HashMap;

import com.pieframework.model.operations.Operation;
import com.pieframework.model.resources.Resource;

public interface Component {

	public String getId();

	public HashMap<String, String> getProps();

	public HashMap<String, Resource> getResources();

	public HashMap<String, Operation> getCommands();

	public Status handle(Operator o, Request r);

	public Status eval(Operator o, Request r);

	public HashMap<String, Component> getChildren();

	public Component getParent();

	public void setParent(Component c);

}
