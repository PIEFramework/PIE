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
package com.pieframework.runtime.operators;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

import com.pieframework.model.Component;
import com.pieframework.model.Configuration;
import com.pieframework.model.Operator;
import com.pieframework.model.Request;
import com.pieframework.model.Status;
import com.pieframework.model.operations.Arguement;
import com.pieframework.model.operations.Operation;
import com.pieframework.model.repository.ModelStore;
import com.pieframework.model.system.Role;
import com.pieframework.model.system.Service;
import com.pieframework.model.system.SubSystem;
import com.pieframework.model.system.System;
import com.pieframework.resources.Files;
import com.pieframework.runtime.core.ResourceLoader;
import com.pieframework.runtime.execution.CommandExec;
import com.pieframework.runtime.utils.ArtifactManager;
import com.pieframework.runtime.utils.StringUtils;

public class ShellSequenceOperator extends Operation implements Operator{
	
	@Override
	public Status help(Component arg0, Request arg1) {
		arg1.getStatus().addMessage("info", this.getClass().getSimpleName()+" executes the command that is specified in the first argument of the args collection.");
		return null;
	}

	@Override
	public Status process(Component c, Request r) {
		return c.handle(this, r);
	}

	public Status process(System arg0, Request arg1) {
		arg1.getStatus().addMessage("info", "Command "+arg1.getCommand()+" is not implemented for "+arg1.getQuery());
		return null;
	}

	@Override
	public Status process(SubSystem c, Request r) {
		Status status=new Status(r.getStatus().getVerbosity(),null,r.getCommand(),c.getId());
		if (this.getArgs()!=null){
			String args="Params:\n";
			for (String arg:this.getArgs().keySet()){
				args+=arg+"="+this.getArgs().get(arg).getValue()+"\n";
			}
			status.addMessage("debug", args);
			
			Configuration config=ModelStore.getCurrentModel().getConfiguration();
			for (String key:this.getArgs().keySet()){
				String command=this.getArgs().get(key).getValue();
				CommandExec cmdExec=new CommandExec(command,config.getDefaultTimeout(),config.getShell(),config.getShellparam(),config.getShellquote(),config.getCli());
				status.addMessage("info", cmdExec.getCmd());
				r.getStatus().addStatus(cmdExec.exec(r.getStatus()));
			}
				
		}else{
			status.addMessage("warn", r.getCommand()+": no arguments are defined, there is nothing to execute.");
			throw new RuntimeException ( r.getCommand()+": no arguments are defined, there is nothing to execute.");
		}
		
		return status;
	}

	
	public Status process(Role arg0, Request arg1) {
		arg1.getStatus().addMessage("info", "Command "+arg1.getCommand()+" is not implemented for "+arg1.getQuery());
		return null;
	}

	public Status process(Service arg0, Request arg1) {
		arg1.getStatus().addMessage("info", "Command "+arg1.getCommand()+" is not implemented for "+arg1.getQuery());
		return null;
	}
	
	@Override
	public Status validate(Component arg0, Request arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status validate(System arg0, Request arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status validate(SubSystem arg0, Request arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status validate(Role arg0, Request arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status validate(Service arg0, Request arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
