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

import com.pieframework.model.system.Role;
import com.pieframework.model.system.Service;
import com.pieframework.model.system.SubSystem;
import com.pieframework.model.system.System;

public interface Operator {

	public Status process(Component c, Request r);
	
	public Status validate(Component c,Request r);
	
	public Status help(Component c, Request r);

	public Status process(System c, Request r);
	
	public Status process(SubSystem c, Request r);

	public Status process(Role c, Request r);

	public Status process(Service c, Request r);
	
	public Status validate(System c,Request r);

	public Status validate(SubSystem c,Request r);
	
	public Status validate(Role c,Request r);
	
	public Status validate(Service c,Request r);

}