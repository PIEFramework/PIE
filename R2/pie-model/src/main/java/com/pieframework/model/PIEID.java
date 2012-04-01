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

import java.util.Scanner;

import com.pieframework.model.system.Role;
import com.pieframework.model.system.Service;
import com.pieframework.model.system.SubSystem;
import com.pieframework.model.system.System;

public class PIEID {

	public static String getFullId(String query,Component c){
		
		String result="";
		Scanner scanner = new Scanner(query);
		scanner.useDelimiter("/");
		int counter=0;
		while (scanner.hasNext()) {
			final String[] nameValue = scanner.next().split(":");
			
			if (nameValue.length == 0 || nameValue.length > 2) {
				throw new IllegalArgumentException("Illegal format");
			}
			
			result+=scanner.delimiter().toString()+nameValue[0];
			counter++;
			if (c instanceof Service && counter > 3){
				return result;
			}else if (c instanceof Role && counter > 2){
				return result;
			}else if (c instanceof SubSystem && counter > 1){
				return result;
			}else if (c instanceof System && counter > 0){
				return result;
			}
		}
		
		return result;
	}
	
	public static String getFullInstanceId(String query,Component c){
		
		String result="";
		Scanner scanner = new Scanner(query);
		scanner.useDelimiter("/");
		int counter=0;
		while (scanner.hasNext()) {
						
			result+=scanner.delimiter().toString()+scanner.next();
			counter++;
			if (c instanceof Service && counter > 3){
				return result;
			}else if (c instanceof Role && counter > 2){
				return result;
			}else if (c instanceof SubSystem && counter > 1){
				return result;
			}else if (c instanceof System && counter > 0){
				return result;
			}
		}
		
		return result;
	}
	
	public static String getInstanceId(String query,Component c){
		
		String result="";
		Scanner scanner = new Scanner(query);
		scanner.useDelimiter("/");
		int counter=0;
		while (scanner.hasNext()) {
			final String[] nameValue = scanner.next().split(":");
			
			if (nameValue.length == 0 || nameValue.length > 2) {
				throw new IllegalArgumentException("Illegal format");
			}
			
			if (nameValue.length >1){
				result=nameValue[1];
			}
			
			counter++;
			if (c instanceof Service && counter > 3){
				return result;
			}else if (c instanceof Role && counter > 2){
				return result;
			}else if (c instanceof SubSystem && counter > 1){
				return result;
			}else if (c instanceof System && counter > 0){
				return result;
			}
		}
		
		return result;
	}
	
public static String getId(String query,Component c){
		
		String result="";
		Scanner scanner = new Scanner(query);
		scanner.useDelimiter("/");
		int counter=0;
		while (scanner.hasNext()) {
			String tmp=scanner.next();
			final String[] nameValue = tmp.split(":");
			
			if (nameValue.length > 2) {
				throw new IllegalArgumentException("Illegal format");
			}
			
			if (nameValue.length > 0){
				result=nameValue[0];	
			}
			
			
			counter++;
			if (c instanceof Service && counter > 3){
				return result;
			}else if (c instanceof Role && counter > 2){
				return result;
			}else if (c instanceof SubSystem && counter > 1){
				return result;
			}else if (c instanceof System && counter > 0){
				return result;
			}
		}
		
		return result;
	}
	
}
