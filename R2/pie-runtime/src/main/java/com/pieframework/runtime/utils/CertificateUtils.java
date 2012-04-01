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
package com.pieframework.runtime.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.commons.codec.binary.Base64;

import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


public class CertificateUtils {

	public static String encryptPassword(String rdpPassword, X509Certificate certificate) {
		Security.addProvider(new BouncyCastleProvider());
		String encryptedPassword="";
		//get PrivateKey And certificate from pfx file
		try {
			
			certificate.checkValidity();
			
			CMSEnvelopedDataGenerator envDataGen = new CMSEnvelopedDataGenerator();
			envDataGen.addKeyTransRecipient(certificate);
			CMSProcessable envData = new CMSProcessableByteArray(rdpPassword.getBytes());
			CMSEnvelopedData enveloped = envDataGen.generate(envData,CMSEnvelopedDataGenerator.DES_EDE3_CBC, "BC");
			byte[] data=enveloped.getEncoded();
			encryptedPassword=new String(Base64.encodeBase64(data));
			
		}  catch (Exception e) {
			e.printStackTrace();
		} 
		
		return encryptedPassword;
	}
	
	public static X509Certificate getCertificate(File certificateFile, String pass,String certAlias){
		X509Certificate certificate=null;
		
		try {
			FileInputStream cert=new FileInputStream(certificateFile);
			KeyStore pfxStore=KeyStore.getInstance("pkcs12");
			pfxStore.load(cert, pass.toCharArray());
			if (StringUtils.empty(certAlias) && pfxStore.size() > 0){
				certAlias=pfxStore.aliases().nextElement();
			}
			certificate=(X509Certificate) pfxStore.getCertificate(certAlias);
			cert.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 

		return certificate;
	}
	
	public static String getThumbPrint(X509Certificate certificate){
		
		String thumbPrint="";
		try {
			
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] der = certificate.getEncoded();
			md.update(der);
			byte[] digest = md.digest();
			
			char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

			StringBuffer buf = new StringBuffer(digest.length * 2);

			for (int i = 0; i < digest.length; ++i) {
				buf.append(hexDigits[(digest[i] & 0xf0) >> 4]);
				buf.append(hexDigits[digest[i] & 0x0f]);
			}

			thumbPrint=buf.toString();
			
		} catch (Exception e) {
			e.printStackTrace();
		}  
		return thumbPrint;
	}

	public static String getThumbPrintAlgorithm(){
		
		return "sha1";	
	}
}
