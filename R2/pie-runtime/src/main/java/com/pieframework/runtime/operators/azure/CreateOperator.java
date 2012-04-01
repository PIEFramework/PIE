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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.codec.binary.Base64;
import org.soyatec.windowsazure.management.AffinityGroup;
import org.soyatec.windowsazure.management.Certificate;
import org.soyatec.windowsazure.management.CertificateFormat;
import org.soyatec.windowsazure.management.HostedService;
import org.soyatec.windowsazure.management.HostedServiceProperties;
import org.soyatec.windowsazure.management.ServiceManagement;

import com.pieframework.model.Component;
import com.pieframework.model.Configuration;
import com.pieframework.model.PIEID;
import com.pieframework.model.Request;
import com.pieframework.model.Status;
import com.pieframework.model.repository.ModelStore;
import com.pieframework.model.resources.Resource;
import com.pieframework.model.system.Model;
import com.pieframework.model.system.SubSystem;
import com.pieframework.resources.AzureAccount;
import com.pieframework.resources.AzureHostedService;
import com.pieframework.resources.AzureKey;
import com.pieframework.runtime.core.ResourceLoader;
import com.pieframework.runtime.utils.CertificateUtils;
import com.pieframework.runtime.utils.StringUtils;

public class CreateOperator extends AzureBaseOperator{

	
	public Status help(Component c,Request r){
		r.getStatus().addMessage("info", this.getClass().getSimpleName()+" will create a hosted service in azure and upload any certificates associated with the hosted service.");
		return null;
	}
	
	
	@Override
	public Status process(SubSystem c, Request r) {
		Status status = new Status(r.getStatus().getVerbosity(), null, r.getCommand(), c.getId());
		ServiceManagement sm = super.getServiceManagementInstance(c);
		
	
		AzureHostedService ahs = (AzureHostedService) c.getResources().get("hostedService");
		String hsUrl = ahs.getUrlPrefix();
		String hsLabel=PIEID.getFullInstanceId(r.getQuery(), c);
		
		if (sm != null) {
			List<HostedService> hss = sm.listHostedServices();
			Boolean found = false;
			if (hss != null && hss.size() > 0) {
				for (HostedService service : hss) {
					try {
						// System.out.println("Service name is : " +
						// service.getName()+" "+service.getUrl());
						HostedServiceProperties serviceProps = sm.getHostedServiceProperties(service.getName(),	true);
						if (serviceProps.getLabel().equalsIgnoreCase(hsLabel)) {
							status.addMessage("info", "SubSystem "+ hsLabel + " already exists.");
							found = true;
						}
					} catch (Exception e) {
						Configuration.log().error(e);
					}
				}
			}

			if (!found) {
				// We do not have any hosted services for the subsystem.
				// create one
				try {
					HostedServiceProperties serviceProps = new HostedServiceProperties();
					serviceProps.setLabel(hsLabel);
					//TODO need to get the description from the component
					serviceProps.setDescription("description of the subsystem");

					String affinityGroup = ahs.getAffinityGroup();
					String location = ahs.getLocation();

					Boolean foundAffinityGroup = false;
					if (affinityGroup != null
							&& !affinityGroup.equalsIgnoreCase("")) {
						for (AffinityGroup ag : sm.listAffinityGroups()) {
							String decodedLabel = new String(Base64.decodeBase64(ag.getLabel().getBytes()));
							if (decodedLabel.equalsIgnoreCase(affinityGroup)) {
								serviceProps.setAffinityGroup(ag.getName());
								status.addMessage("info","using affinity group:"+ affinityGroup);
								foundAffinityGroup = true;
								break;
							}
						}
					}

					if (!foundAffinityGroup) {
						serviceProps.setLocation(location);
					}

					serviceProps.setName(hsUrl);
					sm.createHostedService(serviceProps);
					status.addMessage("info", "Completed creating "	+ hsUrl);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			try {
				List<AzureKey> keyList=new ArrayList<AzureKey>();
				for (String key:c.getChildren().keySet()){
					
					AzureKey rdp=(AzureKey) c.getChildren().get(key).getResources().get("rdpKey");
					AzureKey ssl=(AzureKey) c.getChildren().get(key).getResources().get("sslCert");
					if (ssl!=null) {
						keyList.add(ssl);
					}
					
					if (rdp!=null){
						keyList.add(rdp);
					}
				}
				addCertificates(hsUrl,status,sm,keyList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
		} else {
			status.addMessage(
							"error",
							this.getClass().getSimpleName()+" failed. Verify your management keys have been setup correctly.");
		}

		status.act(true);
		return status;
	}

	private void addCertificates(String hsUrl, Status status,ServiceManagement sm, List<AzureKey> keys) {
		
		List<Certificate> certList=sm.listCertificates(hsUrl);
				
		for (AzureKey ak:keys){
			String cert=ResourceLoader.locate(ak.getLocalPath());
			String certAlias="";
			if (!StringUtils.empty(ak.getCertificateAlias())){
				certAlias=ak.getCertificateAlias();
			}
			
			X509Certificate certificate=CertificateUtils.getCertificate(new File(cert), ak.getPassword(), certAlias);
			Boolean exists=false;
			for (Certificate c:certList){
				if (c.getThumbprint().equalsIgnoreCase(CertificateUtils.getThumbPrint(certificate))){
					status.addMessage("info", "Certificate "+ak.getId()+" with thumbprint:"+CertificateUtils.getThumbPrint(certificate)+" and DN:"+certificate.getIssuerDN()+" exists.");
					exists=true;
				}
			}
			
			if (!exists){
				try {
					File certificateFile=new File(cert);
					FileInputStream fin=new FileInputStream(certificateFile);
					byte data[]=new byte[(int)certificateFile.length()];
					fin.read(data);
					fin.close();
							
					sm.addCertificate(hsUrl, data, CertificateFormat.Pfx,ak.getPassword());
					status.addMessage("info", "Certificate "+ak.getId()+" created. thumbprint:"+CertificateUtils.getThumbPrint(certificate)+" and DN:"+certificate.getIssuerDN());
				}  catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
					
	}
	 
	
	
}
