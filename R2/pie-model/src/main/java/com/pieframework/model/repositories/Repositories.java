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
package com.pieframework.model.repositories;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;
import org.simpleframework.xml.core.Persist;

@Root(strict = false)
public class Repositories {

	@ElementList(name = "repositories", inline = true, required = false)
	private ArrayList<Repository> list;

	private HashMap<String, Repository> repositories = new HashMap<String, Repository>();

	@Commit
	private void commit() {
		for (Repository r : list) {
			repositories.put(r.getId(), r);
		}
	}

	@Persist
	private void prepare() {
		list = new ArrayList<Repository>(repositories.values());
	}

	public HashMap<String, Repository> getRepositories() {
		return repositories;
	}

	public void setRepositories(HashMap<String, Repository> repositories) {
		this.repositories = repositories;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		ReflectionToStringBuilder
				.setDefaultStyle(ToStringStyle.MULTI_LINE_STYLE);
		return ReflectionToStringBuilder.toStringExclude(this, "list");
	}
}
