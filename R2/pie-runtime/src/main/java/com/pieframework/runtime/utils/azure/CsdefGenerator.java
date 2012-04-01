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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

import org.apache.commons.io.FilenameUtils;
import org.mvel2.MVEL;

import com.pieframework.model.Component;
import com.pieframework.model.Configuration;
import com.pieframework.model.PIEID;
import com.pieframework.model.Request;
import com.pieframework.model.Status;
import com.pieframework.model.operations.Operation;
import com.pieframework.model.repository.ModelStore;
import com.pieframework.model.resources.Resource;
import com.pieframework.model.system.Dependency;
import com.pieframework.model.system.Model;
import com.pieframework.model.system.Role;
import com.pieframework.model.system.Service;
import com.pieframework.model.system.SubSystem;
import com.pieframework.resources.AzureEndpoint;
import com.pieframework.resources.AzureKey;
import com.pieframework.resources.AzureLocalStorage;
import com.pieframework.resources.Files;
import com.pieframework.resources.Policy;
import com.pieframework.runtime.core.ResourceLoader;
import com.pieframework.runtime.utils.ArtifactManager;
import com.pieframework.runtime.utils.StringUtils;
import com.pieframework.runtime.utils.TimeUtils;
import com.pieframework.runtime.utils.Zipper;


public class CsdefGenerator {
	
	public File generate(SubSystem c, Request r,Operation o,String csdefFilename){
		
		
		File configFile=new File(csdefFilename);
		if (!configFile.getParentFile().exists()){
			try {
				if (configFile.getParentFile().mkdirs()){
					
				}else{
					Configuration.log().error("Failed creating directory for:"+configFile.getPath());
				}
				
			} catch (Exception e) {
				Configuration.log().error("Failed creating directory for:"+configFile.getPath(),e);
			}
		}
		
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile),"UTF8"));
			printHeader(out,r,c);
			printBody(out,o,c);
			printFooter(out);
			out.flush();
			out.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return configFile;
	}
	
	private void printHeader(BufferedWriter out,Request r,Component c){
		String header="<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
						"<ServiceDefinition name=\""+c.getId()+"\" xmlns=\"http://schemas.microsoft.com/ServiceHosting/2008/10/ServiceDefinition\">";
		
		try {
			out.write(header);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void printFooter (BufferedWriter out){
		String footer="</ServiceDefinition>";
		try {
			out.append(footer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printBody(BufferedWriter out,Operation o,Component c){
		//print roles
		Boolean includeForwarder=true;
		int counter=0;
		
		for (String id:c.getChildren().keySet()){
			Component role=c.getChildren().get(id);
			if ( role.getProps().get("type")!=null){
				String roletype=role.getProps().get("type");
				if (roletype!=null && roletype.equalsIgnoreCase("WebRole")){
					try {
						printWebRoleContent(out,(Role) role,counter,includeForwarder);
						includeForwarder=false;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else if(roletype!=null && roletype.equalsIgnoreCase("WorkerRole")){
					try {
						printWorkerRoleContent(out,(Role) role,counter,includeForwarder);
						includeForwarder=false;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void printWorkerRoleContent(BufferedWriter out, Role role,
			int counter, Boolean includeForwarder) {
		try {
			Policy p = (Policy) role.getResources().get("provisionPolicy");

			out.append("<WorkerRole name=\"" + role.getId() + "\"  vmsize=\""
					+ p.getSize() + "\" >");

			List<String> epList = printEndpoints(out, role);
			printLocalResources(out, role);
			printCertificates(out, role);
			printStartupTasks(out, role);
			printImports(out, role, includeForwarder);
			printSettings(out, role, includeForwarder);
			
			out.append("</WorkerRole>");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private int printWebRoleContent(BufferedWriter out, Role role,int counter,boolean includeForwarder){
		
		
		try {
			Policy p = (Policy) role.getResources().get("provisionPolicy");

			out.append("<WebRole name=\"" + role.getId() + "\"  vmsize=\""
					+ p.getSize() + "\" >");

			List<String> epList = printEndpoints(out, role);
			printLocalResources(out, role);
			printCertificates(out, role);
			printStartupTasks(out, role);
			printImports(out, role, includeForwarder);
			printSettings(out, role, includeForwarder);

			String staticDir = "";
			// Is this the rootContext application?
			for (String id : role.getChildren().keySet()) {
				Service srv = (Service) role.getChildren().get(id);

				if (srv.getProps().get("rootContext") != null
						&& srv.getProps().get("rootContext").equalsIgnoreCase(
								"/")) {
					// Files staticContent=(Files)
					// s.getResources().get("appPackage");
					String fullQuery = srv.getProps().get("package");
					String nameQuery = ResourceLoader
							.getResourceName(fullQuery);
					String pathQuery = ResourceLoader
							.getResourcePath(fullQuery);
					Files staticContent = (Files) srv.getResources().get(
							nameQuery);
					staticDir = ArtifactManager.generateDeployPath(
							staticContent.getLocalPath(), true, pathQuery);
				}
			}

			if (StringUtils.empty(staticDir)) {
				// create a default empty directory
			}

			if (!StringUtils.empty(staticDir)) {
				try {

					out.append("<Sites>");
					out.append("<Site name=\"" + role.getId()
							+ "\" physicalDirectory=\"" + staticDir + "\">");
					for (String key : role.getChildren().keySet()) {

						Service service = null;
						if (role.getChildren().get(key) instanceof Service) {
							service = (Service) role.getChildren().get(key);
						}

						if (service != null) {
							if (service.getProps().get("type") != null) {
								if (service.getProps().get("type")
										.equalsIgnoreCase("application")
										&& !service.getProps().get(
												"rootContext")
												.equalsIgnoreCase("/")) {

									String fQuery = service.getProps().get(
											"package");
									String nQuery = ResourceLoader
											.getResourceName(fQuery);
									String pQuery = ResourceLoader
											.getResourcePath(fQuery);
									Files appContent = (Files) service
											.getResources().get(nQuery);
									String serviceArtifactDir = ArtifactManager
											.generateDeployPath(appContent
													.getLocalPath(), true,
													pQuery);
									String rootContext = service.getProps()
											.get("rootContext");

									if (!StringUtils.empty(rootContext,
											serviceArtifactDir)) {
										out
												.append("<VirtualApplication name=\""
														+ rootContext
														+ "\" physicalDirectory=\""
														+ serviceArtifactDir
														+ "\" />");
									} else {
										throw new RuntimeException(
												"Web application "
														+ service.getId()
														+ " must contain app.rootContext property and physicalDirectory cannot be null.");
									}
								}
							}
						}

					}

					// print bindings
					out.append("<Bindings>");
					if (epList != null && !epList.isEmpty()) {
						for (String id : epList) {
							out.append("<Binding name=\"" + id
									+ "\" endpointName=\"" + id + "\" />");
						}
					}
					out.append("</Bindings>");
					out.append("</Site>");
					out.append("</Sites>");
					out.append("</WebRole>");

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return counter;
	}
	
	private void printSettings(BufferedWriter out,Role r,boolean includeForwarder){
		try {
			out.append("<ConfigurationSettings>");
			printGlobalSettings(out,r,includeForwarder);
			Map<String,Service> settingsList=new HashMap<String,Service>();
			
			for (String key:r.getChildren().keySet()){
				Service service=null;
				if (r.getChildren().get(key) instanceof Service){
					service=(Service) r.getChildren().get(key);
					for (String attr:service.getProps().keySet()){
						settingsList.put(service.getId()+"."+attr,service);
					}
					
					if (service.getDependencies()!=null){
						for (Dependency d:service.getDependencies()){
							if (d.getTarget()!=null && d.getType()!=null){
								Object tmp = MVEL.getProperty(d.getTarget(),ModelStore.getCurrentModel());
								if (tmp instanceof Service){
									Service serviceDependency = (Service) tmp;
									for (String prop:serviceDependency.getProps().keySet()){
										settingsList.put(serviceDependency.getId()+"."+prop,service);
									}
								}
							}
						}
					}
					settingsList.put(service.getId()+".installContainer", service);		
				}
			}
			
			for (String setting:settingsList.keySet()){
				out.append("<Setting name=\""+setting+"\" />");
			}
			out.append("</ConfigurationSettings>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void printStartupTasks(BufferedWriter out,Role r){
		//print startup tasks (bootstrap services)
		try {
			List<String> startupTasks=new ArrayList<String>();
			for (String key:r.getChildren().keySet()){
				Service service=null;
				if (r.getChildren().get(key) instanceof Service){
					service=(Service)r.getChildren().get(key);
					if (service!=null && service.getProps().get("type").equalsIgnoreCase("bootstrap")){
						if (service.getProps().get("startupTask")!=null){
							startupTasks.add(service.getProps().get("startupTask"));	
						}
					}
				}	
			}
			
			out.append("<Startup>");
			for (String task:startupTasks){
				out.append("<Task commandLine=\""+task+"\" executionContext=\"elevated\" taskType=\"background\"></Task>");
			}
			out.append("</Startup>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private List<String> printEndpoints(BufferedWriter out,Role r){
		//print Endpoints
		List<String> epList=null;
		try {
			AzureKey ssl=(AzureKey) r.getResources().get("sslCert");
			AzureEndpoint ap=(AzureEndpoint) r.getResources().get("endpoints");
			String certName=ssl.getCertificateName();
			out.append("<Endpoints>");
			if (ap!=null){
				epList=new ArrayList<String>();
				for (String port:ap.getEndpoints().keySet()){
					String protocol=ap.getEndpoints().get(port);
					String id=protocol+port;
					out.append("<InputEndpoint name=\""+id+"\" protocol=\""+protocol+"\" port=\""+port+"\" ");
					if (protocol.equalsIgnoreCase("https")){
						if (!StringUtils.empty(certName)){
							out.append(" certificate=\""+certName+"\" ");	
						}else{
							throw new RuntimeException("Certificate name is not configured in resource "+ssl.getId());
						}
					}
					out.append("/>");
					
					//Save the endpoint in the map
					epList.add(id);
				}
			}
			
			out.append("</Endpoints>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return epList;
	}
	
	private void printLocalResources(BufferedWriter out,Role r){
		try {
			out.append("<LocalResources>");
			out.append("<LocalStorage name=\"localInstallDirectory\" cleanOnRoleRecycle=\"true\" sizeInMB=\"4096\" />");
			for (String key:r.getResources().keySet()){
				Resource res=r.getResources().get(key);
				if (res instanceof AzureLocalStorage){
					AzureLocalStorage ls=(AzureLocalStorage) res;
					out.append("<LocalStorage name=\""+ls.getName()+"\" cleanOnRoleRecycle=\""+ls.getCleanOnRecycle()+"\" sizeInMB=\""+ls.getSizeInMB()+"\" />");
				}
			}
			out.append("</LocalResources>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void printCertificates(BufferedWriter out,Role r){
		//print certificates
		try {
			AzureKey ssl=(AzureKey) r.getResources().get("sslCert");
			String certName=ssl.getCertificateName();
			out.append("<Certificates>");
			out.append("<Certificate name=\""+certName+"\" storeLocation=\"LocalMachine\" storeName=\"My\" />");
			out.append("</Certificates>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void printImports(BufferedWriter out,Role r,boolean includeForwarder){
		//print imports
		try {
			out.append("<Imports>");
			//out.append("<Import moduleName=\"Diagnostics\" />");
			out.append("<Import moduleName=\"RemoteAccess\" />");
			if (includeForwarder){
				out.append("<Import moduleName=\"RemoteForwarder\" />");
			}
			out.append("</Imports>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void printGlobalSettings(BufferedWriter out,Role r,boolean enableForwarder) {
		try {	
			out.append("<Setting name=\"deploy.storage\" />");
			for (String p:r.getProps().keySet()){
				out.append("<Setting name=\""+p+"\" />");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
