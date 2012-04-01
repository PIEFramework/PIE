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
import java.util.LinkedHashMap;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

import com.pieframework.model.operations.Arguement;

@Root(name = "command")
public class Command {

	@Attribute
	private String id;

	@ElementList(name = "args", inline = false, required = false)
	private ArrayList<Arguement> list;

	private LinkedHashMap<String, Arguement> args = new LinkedHashMap<String, Arguement>();

	@Commit
	private void commit() {
		for (Arguement o : list) {
			args.put(o.getId(), o);
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LinkedHashMap<String, Arguement> getArgs() {
		return args;
	}

	public void setArgs(LinkedHashMap<String, Arguement> args) {
		this.args = args;
	}

}
