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

import org.soyatec.windowsazure.management.DeploymentSlotType;
import org.soyatec.windowsazure.management.ServiceManagement;

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
import com.pieframework.resources.AzureAccount;
import com.pieframework.resources.AzureKey;
import com.pieframework.runtime.core.ResourceLoader;
import com.pieframework.runtime.utils.StringUtils;
import com.pieframework.runtime.utils.azure.ServiceManagementFactory;

public class AzureBaseOperator extends Operation implements Operator {
	
	public Status process(Component c, Request r) {
		return c.handle(this, r);
	}

	public Status process(System arg0, Request arg1) {
		arg1.getStatus().addMessage("info", "Command "+arg1.getCommand()+" is not implemented for "+arg1.getQuery());
		return null;
	}

	public Status process(SubSystem arg0, Request arg1) {
		arg1.getStatus().addMessage("info", "Command "+arg1.getCommand()+" is not implemented for "+arg1.getQuery());
		return null;
	}

	public Status process(Role arg0, Request arg1) {
		arg1.getStatus().addMessage("info", "Command "+arg1.getCommand()+" is not implemented for "+arg1.getQuery());
		return null;
	}

	public Status process(Service arg0, Request arg1) {
		arg1.getStatus().addMessage("info", "Command "+arg1.getCommand()+" is not implemented for "+arg1.getQuery());
		return null;
	}
	
	public Status help(Component arg0, Request arg1) {
		arg1.getStatus().addMessage("info", "HELP is not implemented for '"+arg1.getCommand()+" "+arg1.getQuery()+"'");
		return null;
	}

	public ServiceManagement getServiceManagementInstance(SubSystem c) {

		Model m = ModelStore.getCurrentModel();
		
		AzureKey ak = (AzureKey) c.getResources().get("mgmtKey");
		AzureAccount aa = (AzureAccount) c.getResources().get("account");

		ServiceManagementFactory smf = new ServiceManagementFactory();
		String certAlias = ak.getCertificateAlias();
		String pfxFile = ResourceLoader.locate(ak.getLocalPath());
		String pfxPass = ak.getPassword();
		String subscriptionId = aa.getSubscriptionId();

		Configuration.log().debug(
				"ServiceManagement params " + certAlias + " " + subscriptionId
						+ " " + pfxFile + " " + pfxPass);

		ServiceManagement sm = smf.getServiceManagementInstance(certAlias,
				pfxFile, pfxPass, subscriptionId);
		return sm;
	}

	@Override
	public Status validate(Component c, Request r) {
		return c.eval(this, r);
	}

	@Override
	public Status validate(System arg0, Request arg1) {
		arg1.getStatus().addMessage("warn", "Command "+arg1.getCommand()+" does not have a validator for "+arg1.getQuery());
		return null;
	}

	@Override
	public Status validate(SubSystem arg0, Request arg1) {
		arg1.getStatus().addMessage("warn", "Command "+arg1.getCommand()+" does not have a validator for "+arg1.getQuery());
		return null;
	}

	@Override
	public Status validate(Role arg0, Request arg1) {
		arg1.getStatus().addMessage("warn", "Command "+arg1.getCommand()+" does not have a validator for "+arg1.getQuery());
		return null;
	}

	@Override
	public Status validate(Service arg0, Request arg1) {
		arg1.getStatus().addMessage("warn", "Command "+arg1.getCommand()+" does not have a validator for "+arg1.getQuery());
		return null;
	}
	
	public static DeploymentSlotType getSlot(Request r,Status status){
		String result=Request.findAttribute(r.getInput(), "slot");
		if (StringUtils.empty(result)){
			result=DeploymentSlotType.Production.name();
		}
		
		DeploymentSlotType slotType = null;
		if (!StringUtils.empty(result)) {
			String tier=result.substring(0,1).toUpperCase()+result.substring(1).toLowerCase();
			slotType = DeploymentSlotType.valueOf(tier);
		} else {
			status.addMessage("error", "Unknown slot type:" + result);
			throw new IllegalArgumentException("Unknown slot type:" + result
					+ ".Supported options are 'stage' and 'production'");
		}
		
		return slotType;
	}
}
