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


import org.soyatec.windowsazure.management.Deployment;
import org.soyatec.windowsazure.management.DeploymentSlotType;
import org.soyatec.windowsazure.management.HostedService;
import org.soyatec.windowsazure.management.RoleInstance;
import org.soyatec.windowsazure.management.ServiceManagement;

import com.pieframework.resources.AzureAccount;
import com.pieframework.resources.AzureHostedService;
import com.pieframework.resources.AzureKey;
import com.pieframework.runtime.core.ResourceLoader;
import com.pieframework.model.Component;
import com.pieframework.model.Configuration;
import com.pieframework.model.Request;
import com.pieframework.model.Status;
import com.pieframework.model.repository.ModelStore;
import com.pieframework.model.system.Model;
import com.pieframework.model.system.SubSystem;


public class StatusOperator extends AzureBaseOperator {

	public Status help(Component c,Request r){
		r.getStatus().addMessage("info", this.getClass().getSimpleName()+" returns status of VMs deployed to the hosted service in Azure.");
		return null;
	}
	
	@Override
	public Status validate(SubSystem c, Request r) {
		Status status=new Status(r.getStatus().getVerbosity(),null,r.getCommand(),c.getId());
		return status;
	}
	
	@Override
	public Status process(SubSystem c, Request r) {
		Status status=new Status(r.getStatus().getVerbosity(),null,r.getCommand(),c.getId());
		String hsUrl = ((AzureHostedService) c.getResources().get("hostedService")).getUrlPrefix();
		ServiceManagement sm=super.getServiceManagementInstance(c);
		
		
		String hostedService="";
		for (HostedService hs:sm.listHostedServices()){
			Configuration.log().debug(hs.getName()+" "+hs.getUrl());
			if (hs.getName().equalsIgnoreCase(hsUrl)){
				status.addMessage("info", c.getId()+" is available.");
				hostedService=hs.getName();
			}
		}
		
		if (!hostedService.equalsIgnoreCase("")){
			String header="instanceId : roleID : status : slot";
			java.lang.System.out.println(header);
			Deployment dpProd=sm.getDeployment(hsUrl,  DeploymentSlotType.Production);
			listDeploymentDetails(dpProd,status,c.getId(),"production");
			Deployment dpStage=sm.getDeployment(hsUrl,  DeploymentSlotType.Staging);
			listDeploymentDetails(dpStage,status,c.getId(),"stage");
			
		}else{
			status.addMessage("info",c.getId()+":"+hsUrl+" was not found and there is no status information available for it.");
		}
		
		status.act(true);
		return status;
	}

	
	public static void listDeploymentDetails(Deployment dp,Status status,String id,String slot){
		if (dp!=null){
			if (dp.getRoleInstances()!=null){
				for (RoleInstance roleInst:dp.getRoleInstances()){
					java.lang.System.out.println( "\t"+
							""+roleInst.getInstanceName()+
							" : "+roleInst.getRoleName()+
							" : "+roleInst.getInstanceStatus().name()+
							" : "+slot+
							"");
				}
			}
			
		}else{
			status.addMessage("info","There are no VMs setup for "+id+" in "+slot+" slot.");
		}
	}

}
