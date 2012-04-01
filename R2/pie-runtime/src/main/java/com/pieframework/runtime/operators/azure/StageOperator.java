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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.soyatec.windowsazure.management.ServiceManagement;

import com.pieframework.model.Component;
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
import com.pieframework.runtime.utils.TimeUtils;
import com.pieframework.runtime.utils.azure.AzureNames;
import com.pieframework.runtime.utils.azure.CscfgGenerator;
import com.pieframework.runtime.utils.azure.StorageManager;

public class StageOperator extends AzureBaseOperator{

	public Status help(Component c,Request r){
		r.getStatus().addMessage("info", this.getClass().getSimpleName()+" uploads the azure configuration and package files to azure storage to be ready for deployment. The stage command will print the name of the container where packages were uploaded, this output can be used for the create step.  ");
		return null;
	}

	public Status process(SubSystem c, Request r) {
		Status status=new Status(r.getStatus().getVerbosity(),null,r.getCommand(),c.getId());
		
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
		
		
		//Get service management
		AzureHostedService ahs = (AzureHostedService) c.getResources().get("hostedService");
		ServiceManagement sm=super.getServiceManagementInstance(c);
		String timestamp=TimeUtils.getCurrentTimeStamp();
		String containerName=AzureNames.getServiceDeployContainerName(timestamp);
		
		String storageName=ahs.getMgmtStorageName();
		
		//Stage Azure package and configuration into the azure deployment storage account
		StorageManager storageManager=new StorageManager(sm,storageName);
		status.addMessage("info", "Uploading to azure storage:"+cscfgFile);
		status.addMessage("info", "Uploading to azure storage:"+cspackFile);
		Map<String,String> results=storageManager.uploadFiles(containerName,"",true,cscfgFile,cspackFile);
		status.addMessage("info","Created container="+containerName);
		
		
		//Stage bootstrap services		
		String serviceContainerName="";
		try {
			for (String key:c.getRoles().keySet()){
				Role role=c.getRoles().get(key);
				for (String sid:role.getServices().keySet()){
					Service service=role.getServices().get(sid);
					if (!StringUtils.empty(service.getProps().get("type"))&& service.getProps().get("type").equalsIgnoreCase("bootstrap")){
					
						serviceContainerName=CscfgGenerator.getServiceContainerId(sid);
						String installer=service.getProps().get("package");
						String installerName=ResourceLoader.getResourceName(installer);
						String fileQuery=ResourceLoader.getResourcePath(installer);
						Files installerPackage=(Files) service.getResources().get(installerName);
						
						//java.lang.System.out.println(installerPackage.getLocalPath()+" "+service.getResources().size()+" "+installerName+" "+fileQuery+" ");
						if (installerPackage!=null){
							List<File> flist=ResourceLoader.findPath(installerPackage.getLocalPath(), fileQuery);
							
							if (flist.size()==1 && flist.get(0).exists() && flist.get(0).isFile()){
								results.putAll(storageManager.uploadFiles(serviceContainerName,"",true,flist.get(0).getPath()));	
							}else if (flist.size()== 0){ 
								throw new RuntimeException("File NOT found "+fileQuery+" . Searching in directory:"+installerPackage.getLocalPath()+" for resource:"+installer);
							}else{
								throw new RuntimeException("Found several copies of the application package, please remove the duplicates:"+flist.toString());
							}
						}else{
							//TODO error
						}
					}else{
						//TODO error
					}
					
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (String result:results.keySet()){
			status.addMessage("info", "Created blob:"+results.get(result));
		}
		
		status.act(true);
		return status;
	}
}
