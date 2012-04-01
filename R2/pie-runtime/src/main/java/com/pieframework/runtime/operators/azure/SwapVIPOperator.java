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
import com.pieframework.model.system.SubSystem;
import com.pieframework.resources.AzureHostedService;
import com.pieframework.runtime.utils.azure.AzureUtils;
import com.pieframework.runtime.utils.azure.DefaultAsyncCallback;

public class SwapVIPOperator extends AzureBaseOperator {

	private static final int DEFAULT_POLLING_INTERVAL = 15;

	public Status help(Component c, Request r) {
		r
				.getStatus()
				.addMessage(
						"info",
						this.getClass().getSimpleName()
								+ " swaps the Staging deployment with the Production deployment.");
		return null;
	}

	@Override
	public Status validate(SubSystem c, Request r) {
		Status status = new Status(r.getStatus().getVerbosity(), null, r
				.getCommand(), c.getId());
		return status;
	}

	@Override
	public Status process(SubSystem c, Request r) {
		Status status = new Status(r.getStatus().getVerbosity(), null, r
				.getCommand(), c.getId());

		String hsUrl = ((AzureHostedService) c.getResources().get(
				"hostedService")).getUrlPrefix();

		ServiceManagement sm = super.getServiceManagementInstance(c);

		Deployment stagingDeployment = sm.getDeployment(hsUrl,
				DeploymentSlotType.Staging);

		if (stagingDeployment == null) {
			throw new RuntimeException("Staging deployment must exist.");
		}

		Deployment productionDeployment = sm.getDeployment(hsUrl,
				DeploymentSlotType.Production);

		// If there is no Production deployment, we move Staging into the empty
		// production slot
		if (productionDeployment == null) {
			productionDeployment = stagingDeployment;
		}

		AsyncResultCallback callback = new DefaultAsyncCallback();

		String deployResult = "";

		try {

			deployResult = sm.swapDeployment(hsUrl, productionDeployment
					.getName(), stagingDeployment.getName(), callback);

			String opStatus = AzureUtils.getOperationStatus(deployResult,
					DEFAULT_POLLING_INTERVAL, sm, status, "Swap VIP");

			status.addMessage("info", this.getClass().getSimpleName()
					+ " completed.");

		} catch (Exception e) {
			status.addMessage("error", this.getClass().getSimpleName()
					+ " failed for " + c.getId()
					+ ".Inspect the pie.log for more information.");
			Configuration.log().error(
					this.getClass().getSimpleName() + " failed for "
							+ c.getId(), e);
		}

		status.act(true);
		return status;
	}
}
