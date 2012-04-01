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
import org.soyatec.windowsazure.management.ServiceManagement;

import com.pieframework.model.Component;
import com.pieframework.model.Configuration;
import com.pieframework.model.Request;
import com.pieframework.model.Status;
import com.pieframework.model.repository.ModelStore;
import com.pieframework.model.system.Model;
import com.pieframework.model.system.SubSystem;
import com.pieframework.resources.AzureHostedService;
import com.pieframework.runtime.core.ResourceLoader;
import com.pieframework.runtime.utils.StringUtils;
import com.pieframework.runtime.utils.azure.AzureUtils;
import com.pieframework.runtime.utils.azure.DefaultAsyncCallback;

public class UndeployOperator extends AzureBaseOperator {

	public Status help(Component c,Request r){
		r.getStatus().addMessage("info", this.getClass().getSimpleName()+" removes the application deployment and VMs that were created for it. The operator accepts -i slot='stage|production' which identifies the deployment target.");
		return null;
	}
	
	
	public Status process(SubSystem c, Request r) {
		Status status = new Status(r.getStatus().getVerbosity(), null, r
				.getCommand(), c.getId());

		AzureHostedService ahs = (AzureHostedService) c.getResources().get("hostedService");
		String hostedServiceName = ahs.getUrlPrefix();

		// Get service management API
		ServiceManagement sm = super.getServiceManagementInstance(c);
		DeploymentSlotType slotType = super.getSlot(r, status);

		int pollPeriod = 15;
		int timeout = 40;

		// Run operation to stage or production
		AsyncResultCallback callback = new DefaultAsyncCallback();
		String result = "";
		Boolean requestCompleted = true;
		try {
			result = sm.deleteDeployment(hostedServiceName, slotType, callback);
			status.addMessage("info",
					this.getClass().getSimpleName()+" request completed with status:"
							+ AzureUtils.getOperationStatus(result, pollPeriod,
									sm, status, this.getClass().getSimpleName() ));

		} catch (Exception e) {
			requestCompleted = false;
			status.addMessage("error", this.getClass().getSimpleName()+" failed " + c.getId()
					+ ".Inspect the pie.log for more information.");
			Configuration.log().error(this.getClass().getSimpleName()+" failed " + c.getId(), e);
		}

		// Verify that all are terminated
		Boolean inProgress = true;
		int counter = 0;
		if (requestCompleted) {
			while (inProgress && counter < timeout) {
				// wait before checking status of the async request
				inProgress = false;
				try {
					Thread.sleep(pollPeriod * 1000L);
				} catch (InterruptedException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
				Deployment dp = sm.getDeployment(hostedServiceName, slotType);
				if (dp != null) {
					status.addMessage("info", dp.getStatus().name());
					inProgress = true;
				}
				counter++;
			}

			if (!inProgress) {
				status
						.addMessage("info",
								this.getClass().getSimpleName()+" completed all VMs have been removed.");
			}

			if (counter >= timeout) {
				status
						.addMessage(
								"warn",
								this.getClass().getSimpleName()+" is still in-progress after "
										+ timeout
										* pollPeriod
										+ " seconds. PIE is disconnecting from the terminate operation, which will continue running.");
			}
		}

		status.act(true);
		return status;
	}
}