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

import org.soyatec.windowsazure.management.ServiceManagement;

import com.pieframework.model.Component;
import com.pieframework.model.Configuration;
import com.pieframework.model.Request;
import com.pieframework.model.Status;
import com.pieframework.model.system.SubSystem;
import com.pieframework.resources.Files;
import com.pieframework.runtime.core.ResourceLoader;
import com.pieframework.runtime.utils.azure.CscfgGenerator;
import com.pieframework.runtime.utils.azure.CsdefGenerator;
import com.pieframework.runtime.utils.azure.Cspack;

/**
 * Operator to run the cspack utility.
 * 
 * @author whackett
 * 
 */
public class PackageOperator extends AzureBaseOperator {

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.pieframework.runtime.operators.azure.AzureBaseOperator#help(com.
	 * pieframework.model.Component, com.pieframework.model.Request)
	 */
	public Status help(Component c, Request r) {
		r
				.getStatus()
				.addMessage(
						"info",
						this.getClass().getSimpleName()
								+ " will generate .csdef .cscfg and .cspkg files from the model and the binaries in resources. The generated files are stored on the local file system in paths specified by cscfg,csdef,cspkg.\nOptional inputs: cspack=<absolute_path_to_cspack.exe");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.pieframework.runtime.operators.azure.AzureBaseOperator#process(com
	 * .pieframework.model.system.SubSystem, com.pieframework.model.Request)
	 */
	@Override
	public Status process(SubSystem c, Request r) {
		Status status = new Status(r.getStatus().getVerbosity(), null, r
				.getCommand(), c.getId());
		ServiceManagement sm = super.getServiceManagementInstance(c);

		// Generate csdef. Returns a file.
		File csdef = null;
		try {
			CsdefGenerator csdefGen = new CsdefGenerator();
			Files csdefResource = (Files) c.getResources()
					.get("azureArtifacts");
			String csdefFileName = ResourceLoader.locate(csdefResource
					.getLocalPath())
					+ File.separatorChar + "\\" + c.getId() + ".csdef" + "";
			csdef = csdefGen.generate(c, r, this, csdefFileName);
			status.addMessage("info", "Generated file " + csdef.getPath());
		} catch (Exception e) {
			Configuration.log().error(e.getMessage(), e);
		}

		// Generate cscfg. Returns a file.
		File cscfg = null;
		try {
			CscfgGenerator cscfgGen = new CscfgGenerator();
			Files cscfgResource = (Files) c.getResources()
					.get("azureArtifacts");
			String cscfgFileName = ResourceLoader.locate(cscfgResource
					.getLocalPath())
					+ File.separatorChar + "\\" + c.getId() + ".cscfg" + "";
			cscfg = cscfgGen.generate(c, r, this, cscfgFileName, sm);
			status.addMessage("info", "Generated file " + cscfg.getPath());
		} catch (Exception e) {
			Configuration.log().error(e.getMessage(), e);
		}

		// Package with cspack.
		if (cscfg != null && csdef != null) {
			try {
				Cspack cspack = new Cspack();
				
				Files cspkgResource = (Files) c.getResources().get(
						"azureArtifacts");
				
				String cspkgFileName = ResourceLoader.locate(cspkgResource
						.getLocalPath())
						+ File.separatorChar + "\\" + c.getId() + ".cspkg" + "";
				
				status.addStatus(cspack.createPackage(csdef, c, r,
						cspkgFileName));
			} catch (Exception e) {
				Configuration.log().error(e.getMessage(), e);
			}
		} else {
			status.addMessage("error",
					"Failed to generate cscfg and csdef files.");
		}

		// TODO ?Cleanup downloaded artifacts?

		return status;
	}
}
