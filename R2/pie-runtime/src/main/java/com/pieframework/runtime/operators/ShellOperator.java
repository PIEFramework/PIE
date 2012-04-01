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

public class ShellOperator extends Operation implements Operator{
	
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
			
			if (this.getArgs().get("COMMAND")!=null){
				//Build the command string
				String command=this.getArgs().get("COMMAND").getValue();
				String commandPath="";
				String commandResource="";
				if (this.getArgs().get("COMMAND_PACKAGE")!=null){
					commandResource=this.getArgs().get("COMMAND_PACKAGE").getValue();
				}
				
				if (!StringUtils.empty(commandResource) && ResourceLoader.isResource(commandResource)){
					String nameQuery=ResourceLoader.getResourceName(commandResource);
					String pathQuery=ResourceLoader.getResourcePath(commandResource);
					Files commandPackage=(Files) c.getResources().get(nameQuery);
					commandPath=ArtifactManager.generateDeployPath(commandPackage.getLocalPath(), true, pathQuery);
					commandPath=commandPath+File.separatorChar+command;
				}else{
					commandPath=command;
				}
				
				String options=getParams(this.getArgs(),r,c);
				String commandExec=commandPath+options;
				
				Configuration config=ModelStore.getCurrentModel().getConfiguration();
				CommandExec cmdExec=new CommandExec(commandExec,config.getDefaultTimeout(),config.getShell(),config.getShellparam(),config.getShellquote(),config.getCli());
				status.addMessage("info", cmdExec.getCmd());
				r.getStatus().addStatus(cmdExec.exec(r.getStatus()));
			}else{
				status.addMessage("error", "COMMAND argument is null for command:"+r.getCommand());
				throw new RuntimeException ("COMMAND argument is null for command:"+r.getCommand());
			}
		}else{
			status.addMessage("warn", r.getCommand()+": no arguments are defined, there is nothing to execute.");
			throw new RuntimeException ( r.getCommand()+": no arguments are defined, there is nothing to execute.");
		}
		
		return status;
	}

	private String getParams(LinkedHashMap<String, Arguement> args,Request r,Component c) {
		String result="";
		for (String key:args.keySet()){
			if (!key.equalsIgnoreCase("COMMAND") && !key.equalsIgnoreCase("COMMAND_PACKAGE")){
				if (StringUtils.empty(Request.findAttribute(r.getInput(), key))){
					result+=" "+evalResource(args.get(key).getValue(),c);
				}else{
					result+=" "+evalResource(Request.findAttribute(r.getInput(), key),c);
				}
				
			}
		}
		return result;
	}

	private static String evalResource(String arg,Component c) {
		String result=arg;
		
		if (ResourceLoader.isResource(arg)){
			String nameQuery=ResourceLoader.getResourceName(arg);
			String pathQuery=ResourceLoader.getResourcePath(arg);
			Files resourcePackage=(Files) c.getResources().get(nameQuery);
			List<File> flist=ResourceLoader.findPath(resourcePackage.getLocalPath(), pathQuery);
			if (flist.size()>1){
				throw new RuntimeException("Found duplicates searching for "+pathQuery+" results:"+flist.toString());
			}else{
				result=flist.get(0).getPath();
			}
		}
		
		return result;
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
