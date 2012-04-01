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

import org.soyatec.windowsazure.management.HostedService;
import org.soyatec.windowsazure.management.ServiceManagement;

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

public class TerminateOperator extends AzureBaseOperator {

	public Status help(Component c,Request r){
		r.getStatus().addMessage("info", this.getClass().getSimpleName()+" removes the hosted service and associated ssl and rdp certificates. ");
		return null;
	}
	
		
	public Status process(SubSystem c, Request r) {
		Status status = new Status(r.getStatus().getVerbosity(), null, r
				.getCommand(), c.getId());

		// Get the Model's Hosted Service...
		AzureHostedService ahs = (AzureHostedService) c.getResources().get("hostedService");
		String hostedServiceName = ahs.getUrlPrefix();

		// Get service management API
		ServiceManagement sm = super.getServiceManagementInstance(c);

		int pollPeriod = 15;
		int timeout = 40;

		// Run operation to stage or production
		
		Boolean requestCompleted = true;
		try {
			sm.deleteHostedService(hostedServiceName);
			status.addMessage("info", this.getClass().getSimpleName()+" request completed.");
		} catch (Exception e) {
			requestCompleted = false;
			status.addMessage("error", this.getClass().getSimpleName()+" failed for " + c.getId()
					+ ".Inspect the pie.log for more information.");
			Configuration.log().error(this.getClass().getSimpleName()+" failed for " + c.getId(), e);
		}

		// Verify
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

				// Is the hosted service removed
				for (HostedService hs : sm.listHostedServices()) {
					if (hs.getName().equalsIgnoreCase(hostedServiceName)) {
						inProgress = true;
						status.addMessage("info", hs.getName()
								+ " is available.");
					}
				}
				counter++;
			}

			if (!inProgress) {
				status
						.addMessage("info",
								this.getClass().getSimpleName()+" completed the hostedService is removed.");
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
