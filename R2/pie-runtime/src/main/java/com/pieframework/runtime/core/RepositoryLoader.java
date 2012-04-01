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
package com.pieframework.runtime.core;

import org.mvel2.MVEL;

import com.pieframework.model.repositories.Repository;
import com.pieframework.model.system.Model;
import com.pieframework.runtime.utils.StringUtils;

public class RepositoryLoader {

	public static Repository load(String query,Model m){
		if (!StringUtils.empty(query)){
			Object result = MVEL.getProperty("repositories[\""+ query + "\"]".toString(), m);
			if (result!=null){
				//System.out.println(result.toString());
				return (Repository) result;
			}
		}
		return null;
	}
}
