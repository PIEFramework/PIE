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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Request {

	private String command = "";
	private String query = "";
	private String input = "";
	private Status status =null;

	public Request(String command, String query, String input, Status status) {
		this.command = command;
		this.query = query;
		this.input = input;
		this.status = status;
	}

	public static String findAttribute(String input, String attribute) {

		String result = null;
		
		Pattern pattern = Pattern.compile(attribute+"=(.*?),");
		Matcher matcher = pattern.matcher(input+",");
		if (matcher.find()) {
		    return matcher.group(1);
		}

		return result;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	
}
