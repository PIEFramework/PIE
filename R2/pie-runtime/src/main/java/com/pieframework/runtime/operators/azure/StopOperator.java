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


import org.soyatec.windowsazure.management.AsyncResultCallback;
import org.soyatec.windowsazure.management.Deployment;
import org.soyatec.windowsazure.management.DeploymentSlotType;
import org.soyatec.windowsazure.management.InstanceStatus;
import org.soyatec.windowsazure.management.RoleInstance;
import org.soyatec.windowsazure.management.ServiceManagement;
import org.soyatec.windowsazure.management.UpdateStatus;

import com.pieframework.model.Component;
import com.pieframework.model.Configuration;
import com.pieframework.model.Request;
import com.pieframework.model.Status;
import com.pieframework.model.repository.ModelStore;
import com.pieframework.model.system.Model;
import com.pieframework.model.system.SubSystem;
import com.pieframework.resources.AzureAccount;
import com.pieframework.resources.AzureHostedService;
import com.pieframework.resources.AzureKey;
import com.pieframework.runtime.core.ResourceLoader;
import com.pieframework.runtime.utils.StringUtils;
import com.pieframework.runtime.utils.azure.AzureUtils;
import com.pieframework.runtime.utils.azure.DefaultAsyncCallback;



public class StopOperator extends AzureBaseOperator {

	public Status help(Component c,Request r){
		r.getStatus().addMessage("info", this.getClass().getSimpleName()+" stops the VMs that were setup by the 'create' command. The operator accepts -i slot='stage|production' which identifies the deployment target.");
		return null;
	}
	
	
	@Override
	public Status process(SubSystem c, Request r) {
		Status status = new Status(r.getStatus().getVerbosity(), null, r
				.getCommand(), c.getId());

		
		AzureHostedService ahs = (AzureHostedService) c.getResources().get("hostedService");

		String hostedServiceName = ahs.getUrlPrefix();

		// Get service management API
		ServiceManagement sm = super.getServiceManagementInstance(c);
		DeploymentSlotType slotType = super.getSlot(r, status);

		// Try to start the deployment
		String result = "";
		Boolean requestCompleted = true;
		status.addMessage("info", this.getClass().getSimpleName()+" in progress " + c.getId());
		try {
			AsyncResultCallback callback = new DefaultAsyncCallback();
			result = sm.updateDeplymentStatus(hostedServiceName, slotType,
					UpdateStatus.Suspended, callback);
		} catch (Exception e) {
			requestCompleted = false;
			status.addMessage("error", this.getClass().getSimpleName()+" failed for " + c.getId()
					+ ".Inspect the pie.log for more information.");
			Configuration.log().error(this.getClass().getSimpleName()+" failed for " + c.getId(), e);
		}

		int pollPeriod = 15;
		int timeout = 40;
		AzureUtils.getOperationStatus(result, pollPeriod, sm, status, this.getClass().getSimpleName());

		// Verify that all are stopped
		Boolean stopInProgress = true;
		int counter = 0;
		if (requestCompleted) {
			while (stopInProgress && counter < timeout) {
				// wait before checking status of the async request
				stopInProgress = false;

				try {
					Thread.sleep(pollPeriod * 1000L);
				} catch (InterruptedException e) {
					throw new RuntimeException(e.getMessage(), e);
				}

				Deployment dp = sm.getDeployment(hostedServiceName, slotType);
				for (RoleInstance roleInst : dp.getRoleInstances()) {
					if (roleInst.getInstanceStatus() != InstanceStatus.Stopped) {
						stopInProgress = true;
					}
					status.addMessage("info", roleInst.getInstanceName()
							+ " is " + roleInst.getInstanceStatus().name());
				}
				counter++;
			}

			if (!stopInProgress) {
				status.addMessage("info",
						this.getClass().getSimpleName()+" completed and all instances are down.");
			}

			if (counter >= timeout) {
				status
						.addMessage(
								"warn",
								this.getClass().getSimpleName()+" is still in-progress after "
										+ timeout
										* pollPeriod
										+ " seconds. PIE is disconnecting from the stop operation, which will continue running.");
			}
		}
		status.act(true);
		return status;
	}
}
