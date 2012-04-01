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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.soyatec.windowsazure.management.ServiceManagement;
import org.soyatec.windowsazure.management.ServiceManagementRest;

import com.pieframework.runtime.utils.StringUtils;

public class ServiceManagementFactory {

	public ServiceManagementFactory() {
		System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
	}

	public ServiceManagement getServiceManagementInstance(String certAlias,
			String pfxFile, String pfxPass, String subscriptionId) {
		ServiceManagement sm = null;
		if (!StringUtils.empty(certAlias, pfxFile, pfxPass, subscriptionId)) {

			String trustedStorePass = pfxPass;
			String keyStoreFile = pfxFile + ".key.jks";
			String keyStorePass = pfxPass;
			String trustedStoreFileLocation = pfxFile + ".trusted.jks";

			try {
				// Load key stores
				setupKeyStores(keyStoreFile, keyStorePass,
						trustedStoreFileLocation, trustedStorePass, pfxFile,
						pfxPass, certAlias);
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			try {
				sm = new ServiceManagementRest(subscriptionId, keyStoreFile,
						keyStorePass, trustedStoreFileLocation,
						trustedStorePass, certAlias);
				// add the certificate to Azure subscription
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return sm;

	}

	public void setupKeyStores(String keystoreFile, String keystorePass,
			String trustedStore, String trustedStorePass, String pfxPrivateKey,
			String pfxPrivateKeyPass, String certAlias) {

		File pfxKeyFile = new File(pfxPrivateKey);
		File jksStoreFile = new File(keystoreFile);
		File trustedStoreFile = new File(trustedStore);
		String defaultTrustedCAPassword = "changeit";

		if (!jksStoreFile.exists()) {
			// Create private key store
			try {
				KeyStore pfxStore = KeyStore.getInstance("pkcs12");
				KeyStore jksStore = KeyStore.getInstance("jks");
				char[] pfxPass = pfxPrivateKeyPass.toCharArray();
				char[] jksPass = keystorePass.toCharArray();
				pfxStore.load(new FileInputStream(pfxKeyFile), pfxPass);
				jksStore.load(null, jksPass);

				Enumeration eAliases = pfxStore.aliases();
				int n = 0;
				List<String> list = new ArrayList<String>();
				if (!eAliases.hasMoreElements()) {
					throw new Exception(
							"Certificate does not contain any aliases.");
				}
				while (eAliases.hasMoreElements()) {
					String strAlias = (String) eAliases.nextElement();
					// System.out.println("Alias " + n++ + ": " + strAlias);
					if (pfxStore.isKeyEntry(strAlias)) {
						// System.out.println("Adding key for alias " +
						// strAlias);
						Key key = pfxStore.getKey(strAlias, pfxPass);
						Certificate[] chain = pfxStore
								.getCertificateChain(strAlias);

						if (certAlias != null)
							strAlias = certAlias;

						jksStore.setKeyEntry(strAlias, key, jksPass, chain);
						list.add(strAlias);
					}
				}

				OutputStream out = new FileOutputStream(jksStoreFile);
				jksStore.store(out, jksPass);
				out.close();
			} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (!trustedStoreFile.exists()) {
			// Create trusted keystore
			try {
				String filename = System.getProperty("java.home")
						+ "/lib/security/cacerts".replace('/',
								File.separatorChar);
				FileInputStream is = new FileInputStream(filename);
				KeyStore keystore = KeyStore.getInstance(KeyStore
						.getDefaultType());
				keystore.load(is, defaultTrustedCAPassword.toCharArray());
				OutputStream trustedStoreStream = new FileOutputStream(
						trustedStoreFile);
				keystore.store(trustedStoreStream, trustedStorePass
						.toCharArray());
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (KeyStoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (CertificateException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}
}
