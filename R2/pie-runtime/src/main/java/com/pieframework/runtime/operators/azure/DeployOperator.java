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


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.soyatec.windowsazure.management.AsyncResultCallback;
import org.soyatec.windowsazure.management.Deployment;
import org.soyatec.windowsazure.management.DeploymentConfiguration;
import org.soyatec.windowsazure.management.DeploymentSlotType;
import org.soyatec.windowsazure.management.DeploymentStatus;
import org.soyatec.windowsazure.management.InstanceStatus;
import org.soyatec.windowsazure.management.OperationState;
import org.soyatec.windowsazure.management.OperationStatus;
import org.soyatec.windowsazure.management.RoleInstance;
import org.soyatec.windowsazure.management.ServiceManagement;
import org.soyatec.windowsazure.management.StorageAccountKey;
import org.soyatec.windowsazure.management.UpdateStatus;

import com.pieframework.model.Component;
import com.pieframework.model.Configuration;
import com.pieframework.model.Operator;
import com.pieframework.model.Request;
import com.pieframework.model.Status;
import com.pieframework.model.operations.Operation;
import com.pieframework.model.repository.ModelStore;
import com.pieframework.model.system.Model;
import com.pieframework.model.system.Role;
import com.pieframework.model.system.Service;
import com.pieframework.model.system.SubSystem;
import com.pieframework.model.system.System;
import com.pieframework.resources.AzureHostedService;
import com.pieframework.resources.Files;
import com.pieframework.runtime.core.ResourceLoader;
import com.pieframework.runtime.utils.StringUtils;
import com.pieframework.runtime.utils.azure.AzureNames;
import com.pieframework.runtime.utils.azure.AzureUtils;
import com.pieframework.runtime.utils.azure.DefaultAsyncCallback;
import com.pieframework.runtime.utils.azure.StorageManager;



public class DeployOperator  extends AzureBaseOperator{

	public Status help(Component c,Request r){
		r.getStatus().addMessage("info", this.getClass().getSimpleName()+" will create a new application deployment. By default it will use most recently staged application data. To override the default behavior use -i container=<value> option. All VMs will be in stopped state after the operation completes. The operator accepts -i slot='stage|production' which identifies the deployment target.");
		return null;
	}
	
	public Status process(SubSystem c, Request r) {
		Status status=new Status(r.getStatus().getVerbosity(),null,r.getCommand(),c.getId());
		DeploymentSlotType slotType = super.getSlot(r, status);
		
		String container=Request.findAttribute(r.getInput(), "container");;
		//Get service management
		
		AzureHostedService ahs = (AzureHostedService) c.getResources().get("hostedService");
		ServiceManagement sm=super.getServiceManagementInstance(c);
		
		//Getting the azure artifacts
		String cspackFile=null;
		String cscfgFile=null;
		Files azureArtifacts=(Files) c.getResources().get("azureArtifacts");
		List<File> list=ResourceLoader.findPath(azureArtifacts.getLocalPath(), "*.cspkg");
		if (list.get(0)!=null && list.get(0).exists() && list.get(0).isFile()){
			cspackFile=list.get(0).getPath();
		}
		list=ResourceLoader.findPath(azureArtifacts.getLocalPath(), "*.cscfg");
		if (list.get(0)!=null && list.get(0).exists() && list.get(0).isFile()){
			cscfgFile=list.get(0).getPath();
		}
		
				
		if (StringUtils.empty(container) && ahs!=null){
			//Find the latest deploy container and do a checksum comparison with the local package
			//If they match it is safe to use otherwise leave container empty
			StorageManager storageManager=new StorageManager(sm,ahs.getMgmtStorageName());
			container=storageManager.getLatestDeployContainer(new File(cspackFile),new File(cscfgFile));
			if (!StringUtils.empty(container)){
				status.addMessage("info","Container was not explicitly provided. There is an existing container:"+container+" with matching cspkg that will be used.");
			}
		}
		
		if (container!=null && !container.equalsIgnoreCase("")){

			int pollPeriod=15;
			
			String containerURI=AzureNames.getStorageURI(ahs.getMgmtStorageName(), container);
			String blobURI=containerURI+"/"+c.getId()+".cspkg";
			
			status.addMessage("info","Using container:"+container+" "+blobURI);
			//Run deployment to stage or production
			AsyncResultCallback callback=new DefaultAsyncCallback();
			DeploymentConfiguration config=new DeploymentConfiguration(container,blobURI,cscfgFile,container);
			String deployResult="";
			try {
				deployResult = sm.createDeployment(ahs.getUrlPrefix(), slotType, config, callback);
			} catch (Exception e) {
				status.addMessage("error", this.getClass().getSimpleName()+" failed for "+c.getId()+".Inspect the pie.log for more information.");
				Configuration.log().error(this.getClass().getSimpleName()+" failed for "+c.getId(), e);
			}
			status.addMessage("info", this.getClass().getSimpleName()+" completed with status:"+AzureUtils.getOperationStatus(deployResult, pollPeriod, sm, status, "Create"));
			
		}else{
			status.addMessage("error", this.getClass().getSimpleName()+" failed for "+c.getId()+". The create command requires -i container=value option.");
			Configuration.log().error(this.getClass().getSimpleName()+" failed for "+c.getId()+". The create command requires -i container=value option.");
		}
		
		status.act(true);
		return status;
	}


}
