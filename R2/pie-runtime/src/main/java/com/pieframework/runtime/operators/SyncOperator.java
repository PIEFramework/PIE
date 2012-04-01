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
import java.io.IOException;
import java.util.Map;

import org.soyatec.windowsazure.management.ServiceManagement;

import com.pieframework.model.Component;
import com.pieframework.model.Configuration;
import com.pieframework.model.Operator;
import com.pieframework.model.Request;
import com.pieframework.model.Status;
import com.pieframework.model.operations.Operation;
import com.pieframework.model.repository.ModelStore;
import com.pieframework.model.system.Role;
import com.pieframework.model.system.Service;
import com.pieframework.model.system.SubSystem;
import com.pieframework.model.system.System;
import com.pieframework.resources.Files;
import com.pieframework.runtime.core.ResourceLoader;
import com.pieframework.runtime.utils.ArtifactManager;
import com.pieframework.runtime.utils.azure.CscfgGenerator;
import com.pieframework.runtime.utils.azure.CsdefGenerator;
import com.pieframework.runtime.utils.azure.Cspack;

public class SyncOperator extends Operation implements Operator{

	public Status help(Component c,Request r){
		r.getStatus().addMessage("info", this.getClass().getSimpleName()+" will generate .csdef .cscfg and .cspkg files from the model and the binaries in resources. The generated files are stored on the local file system in paths specified by cscfg,csdef,cspkg.\nOptional inputs: cspack=<absolute_path_to_cspack.exe");
		return null;
	}
	
	@Override
	public Status process(SubSystem c, Request r) {
		Status status = new Status(r.getStatus().getVerbosity(), null, r.getCommand(), c.getId());
		
		//Download resources
		try {
			ArtifactManager artifactManager=new ArtifactManager();
			artifactManager.syncResources(c,true, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		status.addMessage("info",this.getClass().getSimpleName()+" completed for "+c.getId());
		
		return status;
	}

	@Override
	public Status process(Component c, Request r) {
		return c.handle(this, r);
	}

	@Override
	public Status process(System arg0, Request arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status process(Role arg0, Request arg1) {
		arg1.getStatus().addMessage("info", "Command "+arg1.getCommand()+" is not implemented for "+arg1.getQuery());
		return null;
	}

	@Override
	public Status process(Service arg0, Request arg1) {
		arg1.getStatus().addMessage("info", "Command "+arg1.getCommand()+" is not implemented for "+arg1.getQuery());
		return null;
	}

	@Override
	public Status validate(Component arg0, Request arg1) {
		arg1.getStatus().addMessage("info", "Command "+arg1.getCommand()+" is not implemented for "+arg1.getQuery());
		return null;
	}

	@Override
	public Status validate(System arg0, Request arg1) {
		arg1.getStatus().addMessage("info", "Command "+arg1.getCommand()+" is not implemented for "+arg1.getQuery());
		return null;
	}

	@Override
	public Status validate(SubSystem arg0, Request arg1) {
		arg1.getStatus().addMessage("info", "Command "+arg1.getCommand()+" is not implemented for "+arg1.getQuery());
		return null;
	}

	@Override
	public Status validate(Role arg0, Request arg1) {
		arg1.getStatus().addMessage("info", "Command "+arg1.getCommand()+" is not implemented for "+arg1.getQuery());
		return null;
	}

	@Override
	public Status validate(Service arg0, Request arg1) {
		arg1.getStatus().addMessage("info", "Command "+arg1.getCommand()+" is not implemented for "+arg1.getQuery());
		return null;
	}
}
