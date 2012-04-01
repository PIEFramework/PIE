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
package com.pieframework.model.query;

import java.util.ArrayList;
import java.util.List;

public class ComponentQueryBuilder {

	private final List<String> list = new ArrayList<String>();

	private enum component {
		systems, subSystems, roles, services
	};

	public ComponentQueryBuilder add(String queryTerm) {
		list.add(queryTerm);
		return this;
	}

	public String build() {
		StringBuilder builder = new StringBuilder();

		String[] queryTerms = list.toArray(new String[list.size()]);
		String template = "%s[\"%s\"]";

		for (int i = 0; i < queryTerms.length; i++) {
			builder.append(String.format(template, component.values()[i],
					queryTerms[i]));
			if (i < queryTerms.length - 1) {
				builder.append(".");
			}
		}
		return builder.toString();
	}
}
