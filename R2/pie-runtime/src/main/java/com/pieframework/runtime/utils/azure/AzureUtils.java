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
package com.pieframework.runtime.utils.azure;

import org.soyatec.windowsazure.management.OperationState;
import org.soyatec.windowsazure.management.OperationStatus;
import org.soyatec.windowsazure.management.ServiceManagement;

import com.pieframework.model.Status;

public class AzureUtils {

	public AzureUtils() {

	}
	
	public static String getHostedServiceFQDN(String hostname){
		return hostname+".cloudapp.net";
	}

	public static String getOperationStatus(String requestId, int pollPeriod,
			ServiceManagement sm, Status status, String operation) {
		String result = "";
		if (requestId != null && !requestId.equalsIgnoreCase("")) {
			Boolean finished = false;
			String state = "";
			while (!finished) {
				// wait 15s before checking status of the async request
				try {
					Thread.sleep(pollPeriod * 1000L);
				} catch (InterruptedException e) {
					throw new RuntimeException(e.getMessage(), e);
				}

				OperationStatus opStatus = sm.getOperationStatus(requestId);
				if (opStatus.getStatus() == OperationState.InProgress) {
					status.addMessage("info", operation
							+ " request is in-progress.");
					state = "in-progress";
				} else if (opStatus.getStatus() == OperationState.Failed) {
					status.addMessage("error", operation + " request failed.");
					status.addMessage("error", opStatus.getErrorMessage());
					finished = true;
					state = "failed";
				} else if (opStatus.getStatus() == OperationState.Succeeded) {
					status.addMessage("info", operation + " request succeeded");
					finished = true;
					state = "succeeded";
				} else {
					status.addMessage("error", operation
							+ " request completed with uknown state");
					finished = true;
					state = "unknown";
				}
			}
			result = state;
		}

		return result;
	}

	// public static String getHostedServiceAffinityGroup(SubSystem c){
	// String result="";
	// //result=csic.getParent().getAData().getAzureAccount().getNameToken();
	// String nodeId=c.getRegistry().getRootPath() + c.getId()+
	// "/environment/azure.service.affinitygroup";
	// result=c.getRegistry().getNodeData(nodeId);
	//		
	// if (result == null || result.equalsIgnoreCase("")){
	// throw new RuntimeException(
	// "Registry environment variable:azure.service.affinitygroup is null or empty.Check if the registry environment is setup properly."
	// );
	// }
	//		
	// return result;
	// }
	//	
	// public static String getHostedServiceFQDN(SubSystem c){
	// String result="";
	// try {
	// ServiceManagementFactory smf=new ServiceManagementFactory();
	// ServiceManagement sm = smf.getServiceManagementInstance(c);
	//			
	// result+=AzureUtils.getHostedServiceName(c)+".cloudapp.net";
	//			
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//		
	// return result;
	// }
	//	
	// public static String getStorageName(SubSystem c){
	// String result="";
	// String nodeId=c.getRegistry().getRootPath() + c.getId()+
	// "/environment/azure.storageaccount.mgmt.name";
	// result=c.getRegistry().getNodeData(nodeId);
	// //result=csic.getParent().getAData().getAzureAccount().getNameToken();
	//		
	// if (result == null || result.equalsIgnoreCase("")){
	// throw new RuntimeException(
	// "Registry environment variable:azure.storageaccount.mgmt.name is null or empty.Check if the registry environment is setup properly."
	// );
	// }
	//		
	// return result;
	// }
	//	
	// public static String getStorageURI(SubSystem c){
	// String result="";
	// result="http://"+getStorageName(c)+".blob.core.windows.net";
	// return result;
	// }
	//	
	// public static String getDeployContainerName(String var){
	//		
	// return "deploy"+var;
	// }
	//
	// public static String getDeployStorageDetails(SubSystem c) {
	// String result="";
	// ServiceManagementFactory smf=new ServiceManagementFactory();
	// ServiceManagement sm=smf.getServiceManagementInstance(c);
	// StorageManager storageManager=new
	// StorageManager(sm,AzureUtils.getStorageName(c));
	// result+="DefaultEndpointsProtocol=https;"+
	// "AccountName="+AzureUtils.getStorageName(c)+";"+
	// "AccountKey="+storageManager.getStorageKey(sm,
	// AzureUtils.getStorageName(c));
	// return result;
	// }
	//	
	// public static String getServiceInstallContainerName(String service) {
	// // TODO Auto-generated method stub
	// return "i-"+service;
	// }
}
