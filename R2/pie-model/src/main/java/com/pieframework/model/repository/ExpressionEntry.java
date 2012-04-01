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
package com.pieframework.model.repository;


public class ExpressionEntry {

	private String expression;
	private String value;

	public ExpressionEntry() {
		super();
	}

	public ExpressionEntry(String expression, String value) {
		super();
		this.expression = expression;
		this.value = value;
	}

	public String getExpression() {
		return expression;
	}

	public String getValue() {
		return value;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ExpressionEntry withExpression(String expression) {
		this.expression = expression;
		return this;
	}

	public ExpressionEntry withValue(String value) {
		this.value = value;
		return this;
	}
}
