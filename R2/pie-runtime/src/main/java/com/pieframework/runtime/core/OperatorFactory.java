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

import com.pieframework.model.Component;
import com.pieframework.model.Operator;
import com.pieframework.model.system.Model;

public class OperatorFactory {

	public static Operator getInstance(Component c, String command, Model model) {
		Object result = MVEL.getProperty("commands[\""+command+"\"]",c);
		Operator o=null;
		if (result!=null){
			Object classResult=MVEL.getProperty("operations[\""+result.toString()+"\"]",model);	
			if (classResult instanceof Operator){
				o=(Operator)classResult;
			}
		}
		return o;
	}

}
