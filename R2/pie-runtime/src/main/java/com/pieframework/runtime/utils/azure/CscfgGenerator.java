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
import java.security.cert.X509Certificate;

import org.mvel2.MVEL;
import org.soyatec.windowsazure.management.ServiceManagement;

import com.pieframework.model.Component;
import com.pieframework.model.Configuration;
import com.pieframework.model.Operator;
import com.pieframework.model.Request;
import com.pieframework.model.Status;
import com.pieframework.model.repository.ModelStore;
import com.pieframework.model.system.Dependency;
import com.pieframework.model.system.Model;
import com.pieframework.model.system.Role;
import com.pieframework.model.system.Service;
import com.pieframework.model.system.SubSystem;
import com.pieframework.resources.AzureHostedService;
import com.pieframework.resources.AzureKey;
import com.pieframework.resources.AzureRdp;
import com.pieframework.resources.Policy;
import com.pieframework.runtime.core.ResourceLoader;
import com.pieframework.runtime.operators.azure.PackageOperator;
import com.pieframework.runtime.utils.CertificateUtils;
import com.pieframework.runtime.utils.StringUtils;

public class CscfgGenerator {

	public File generate(SubSystem c, Request r,Operator o, String filePath,ServiceManagement sm) {
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath),"UTF8"));
			AzureHostedService ahs=(AzureHostedService) c.getResources().get("hostedService");
			printHeader(out,c.getId(),ahs.getOsFamily(),ahs.getOsVersion());
			printBody(out,sm,c,r.getStatus());
			printFooter(out);
			out.flush();
			out.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new File(filePath);
	}
	
	private void printFooter(BufferedWriter out) {
		
		try {
			out.append("</ServiceConfiguration>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void printHeader(BufferedWriter out,String id,String osFamily,String osVersion){
		
		try {
			out.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			out.append("<ServiceConfiguration serviceName=\""+id+"\" xmlns=\"http://schemas.microsoft.com/ServiceHosting/2008/10/ServiceConfiguration\"");
			if (osFamily!=null && !osFamily.equalsIgnoreCase("")){
				 out.append(" osFamily=\""+osFamily+"\" ");
			}
			
			if (osVersion!=null && !osVersion.equalsIgnoreCase("")){
				 out.append(" osVersion=\""+osVersion+"\" ");
			}
			
			out.append(" >");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
		
	private void printBody(BufferedWriter out,ServiceManagement sm,Component c,Status s) {
		Boolean enableForwarder=true;
		for (String id:c.getChildren().keySet()){
			
			if (c.getChildren().get(id) instanceof Role){
				Role role=(Role)c.getChildren().get(id);
				AzureRdp ardp=(AzureRdp) role.getResources().get("rdpAccount");
				AzureKey rdpKey=(AzureKey) role.getResources().get("rdpKey");
				AzureKey sslKey=(AzureKey) role.getResources().get("sslCert");
				Policy p=(Policy) role.getResources().get("provisionPolicy");
				
				if (p!=null && ardp!=null && rdpKey!=null){
					try {
						
						String encryptedPassword="";
						String thumbprintAlgo="";
						
						String rdpThumbprint="";
						String sslThumbprint="";

						String rdpCertFile=ResourceLoader.locate(rdpKey.getLocalPath());
						X509Certificate rdpCert=CertificateUtils.getCertificate(new File(rdpCertFile), rdpKey.getPassword(),rdpKey.getCertificateAlias());
						
						String sslCertFile=null;
						X509Certificate sslCert=null;
						if (sslKey!=null){
							sslCertFile=ResourceLoader.locate(sslKey.getLocalPath());
							String certAlias="";
							if (!StringUtils.empty(sslKey.getCertificateAlias())){
								certAlias=sslKey.getCertificateAlias();
							}
							sslCert=CertificateUtils.getCertificate(new File(sslCertFile), sslKey.getPassword(),certAlias);	
						}
						
						try {
							encryptedPassword=CertificateUtils.encryptPassword(ardp.getRdpPassword(),rdpCert);
							thumbprintAlgo=CertificateUtils.getThumbPrintAlgorithm();
							rdpThumbprint=CertificateUtils.getThumbPrint(rdpCert);
							if (sslCert!=null){
								sslThumbprint=CertificateUtils.getThumbPrint(sslCert);	
							}
						
						} catch ( NoClassDefFoundError e ){
							
							s.addMessage("warn", "Failed loading class libraries needed for encrypting the rdpPassword and generating certificate thumbprints for:"+role.getId()+
									".Verify the libraries are available in "+Configuration.getStaticInstance().getLib()+".The rdpPassword or thumbprints will remain un-initilaized.");
							Configuration.log().warn("Failed loading class libraries needed for encrypting the rdpPassword for:"+role.getId()+
									".Verify the libraries are available in "+Configuration.getStaticInstance().getLib(),e);
							
						} catch (Exception e1) {
							
							s.addMessage("warn","Encountered errors encrypting the rdpPassword and certificate thumbprints for:"+role.getId()+".The rdpPassword or thumbprints will remain un-initilaized.");
							Configuration.log().warn("Encountered errors encrypting the rdpPassword or certificate thumbrints for:"+role.getId(),e1);
							 
						}
						
						
						out.append("<Role name=\""+role.getId()+"\">");
						out.append("<Instances count=\""+p.getMinRunning()+"\" />");
						out.append("<ConfigurationSettings>");
						printGlobalSettings(out,sm,enableForwarder,role,ardp.getRdpUsername(),ardp.getRdpExpiration(),encryptedPassword);
						printServiceSettings(out,role);
						out.append("</ConfigurationSettings>");
						
						//print certificates
						out.append("<Certificates>");
						printCertificate(out,"Microsoft.WindowsAzure.Plugins.RemoteAccess.PasswordEncryption",rdpThumbprint,thumbprintAlgo);
						if (sslKey!=null && !StringUtils.empty(sslThumbprint)){
							printCertificate(out,sslKey.getCertificateName(),sslThumbprint,thumbprintAlgo);
						}
						out.append("</Certificates>");
						
						out.append("</Role>");
						enableForwarder=false;
					}catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	public void printCertificate(BufferedWriter out,String certName,String thumbprint, String thumbprintAlgo){
		
		try {
			out.append("<Certificate name=\""+certName+"\" thumbprint=\""+thumbprint+"\" thumbprintAlgorithm=\""+thumbprintAlgo+"\" />");
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printGlobalSettings(BufferedWriter out,ServiceManagement sm,Boolean enableForwarder,Role r, String user,String expiration,String encryptedPassword){
		
		try {	
			//out.append("<Setting name=\"Microsoft.WindowsAzure.Plugins.Diagnostics.ConnectionString\" value=\"UseDevelopmentStorage=true\" />");
			out.append("<Setting name=\"Microsoft.WindowsAzure.Plugins.RemoteAccess.Enabled\" value=\"true\" />");
			out.append("<Setting name=\"Microsoft.WindowsAzure.Plugins.RemoteAccess.AccountUsername\" value=\""+user+"\" />");
			out.append("<Setting name=\"Microsoft.WindowsAzure.Plugins.RemoteAccess.AccountEncryptedPassword\" value=\""+encryptedPassword+"\" />");
			out.append("<Setting name=\"Microsoft.WindowsAzure.Plugins.RemoteAccess.AccountExpiration\" value=\""+expiration+"\" />");
			if (enableForwarder){
				out.append("<Setting name=\"Microsoft.WindowsAzure.Plugins.RemoteForwarder.Enabled\" value=\"true\" />");	
			}
			
			AzureHostedService ahs=(AzureHostedService) r.getParent().getResources().get("hostedService");
			String storageName=ahs.getMgmtStorageName();
			
			String val="DefaultEndpointsProtocol=https;"+
			"AccountName="+storageName+";"+
			"AccountKey="+StorageManager.getStorageKey(sm, storageName);
			
			
			out.append("<Setting name=\"deploy.storage\" value=\""+val+"\" />");
			for (String p:r.getProps().keySet()){
				String propName=ResourceLoader.getResourceName(r.getProps().get(p));
				out.append("<Setting name=\""+p+"\"  value=\""+propName+"\" />");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void printServiceSettings(BufferedWriter out,Role role){
				
		
		try {
			
			
			for (String key:role.getChildren().keySet()){
				
				Service service=null;
				if (role.getChildren().get(key) instanceof Service){
					service=(Service)role.getChildren().get(key);
				}	
				
				if (service!=null){
					//TODO move this string to a common place for other operators to access
					String value=getServiceContainerId(service.getId());
					out.append("<Setting name=\""+service.getId()+".installContainer\" value=\""+value+"\" />");
					for (String id:service.getProps().keySet()){
						String propName=ResourceLoader.getResourceName(service.getProps().get(id));
						out.append("<Setting name=\""+service.getId()+"."+id+"\" value=\""+propName+"\" />");
					}
					
					if (service.getDependencies()!=null){
						for (Dependency d:service.getDependencies()){
							if (d.getTarget()!=null && d.getType()!=null){
								Object tmp = MVEL.getProperty(d.getTarget(),ModelStore.getCurrentModel());
								if (tmp instanceof Service){
									Service serviceDependency = (Service) tmp;
									for (String prop:serviceDependency.getProps().keySet()){
										String propName=serviceDependency.getProps().get(prop);
										out.append("<Setting name=\""+serviceDependency.getId()+"."+prop+"\" value=\""+propName+"\" />");
									}
								}
							}
						}
						
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static String getServiceContainerId(String sid){
		return "i-"+sid+"-service";
	}
}
