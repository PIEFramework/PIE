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
package com.pieframework.runtime.operators.azure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.soyatec.windowsazure.management.DeploymentSlotType;
import org.soyatec.windowsazure.management.ServiceManagement;

import com.pieframework.model.Component;
import com.pieframework.model.Configuration;
import com.pieframework.model.Operator;
import com.pieframework.model.PIEID;
import com.pieframework.model.Request;
import com.pieframework.model.Status;
import com.pieframework.model.operations.Operation;
import com.pieframework.model.repository.ModelStore;
import com.pieframework.model.system.Role;
import com.pieframework.model.system.Service;
import com.pieframework.model.system.SubSystem;
import com.pieframework.model.system.System;
import com.pieframework.resources.AzureHostedService;
import com.pieframework.resources.AzureRdp;
import com.pieframework.runtime.core.ResourceLoader;
import com.pieframework.runtime.execution.CommandExec;
import com.pieframework.runtime.utils.azure.AzureUtils;

public class RemoteDesktopOperator extends AzureBaseOperator{



	public Status process(Role c, Request r) {
		Status status = new Status(r.getStatus().getVerbosity(), null, r.getCommand(), c.getId());
		DeploymentSlotType slotType = super.getSlot(r, status);
		String instanceId=PIEID.getInstanceId(r.getQuery(), c);
		String connectionFilePath=generateConnectionFile(c,instanceId,slotType.name());
		String rdpCommand="mstsc";
		String rdpOptions="/f";
		rdpCommand+=" "+connectionFilePath+" "+rdpOptions;
		
		Configuration config=ModelStore.getCurrentModel().getConfiguration();
		CommandExec cmdExec=new CommandExec(rdpCommand,config.getDefaultTimeout(),config.getShell(),config.getShellparam(),config.getShellquote(),config.getCli());
		r.getStatus().addStatus(cmdExec.exec(r.getStatus()));
		
		return status;
	}


	@Override
	public Status help(Component c, Request r) {
		r.getStatus().addMessage("info", this.getClass().getSimpleName()+" will generate a RDP connection file and launch Remote Desktop application for the specified instance.");
		return null;
	}
	
	private String generateConnectionFile(Role role,String instanceId,String slot) {
		String rdpDirString=ModelStore.getCurrentModel().getConfiguration().getTmp()+"azure"+File.separatorChar+"rdp";
		String connectionFileName=role.getParent().getId()+"-"+slot+"-"+role.getId()+"_IN_"+instanceId+".rdp";
		File connectionFile=new File(rdpDirString+File.separatorChar+connectionFileName);
		File rdpDir=new File(rdpDirString);
		AzureHostedService ahs = (AzureHostedService) role.getParent().getResources().get("hostedService");
		AzureRdp ardp=(AzureRdp) role.getResources().get("rdpAccount");
		String username=ardp.getRdpUsername();
		String fqdn=AzureUtils.getHostedServiceFQDN(ahs.getUrlPrefix());
		
		try {
			if (!rdpDir.exists()){
				rdpDir.mkdirs();
			}
			
			BufferedWriter out = new BufferedWriter(new FileWriter(connectionFile.getPath()));
			out.write("full address:s:"+fqdn);
			out.newLine();
			out.write("username:s:"+username);
			out.newLine();
			out.write("LoadBalanceInfo:s:Cookie: mstshash="+role.getId()+"#"+role.getId()+"_IN_"+instanceId+"#Microsoft.WindowsAzure.Plugins.RemoteAccess.Rdp");
			out.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return connectionFile.getPath();
	}

}
