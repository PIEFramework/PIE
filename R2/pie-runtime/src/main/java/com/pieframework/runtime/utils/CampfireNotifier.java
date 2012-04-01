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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.pieframework.model.Configuration;

import sun.misc.BASE64Encoder;


public class CampfireNotifier {

	public static boolean notify(String msg,String url,String user,String password){
		HttpClient client = new HttpClient();
		PostMethod post = new PostMethod(url);

		 // stuff the Authorization request header
	    byte[] encodedPassword = ( user+":"+password).getBytes();
	    BASE64Encoder encoder = new BASE64Encoder();
	    post.setRequestHeader("Authorization", "Basic " + encoder.encode( encodedPassword ));

		try {
			RequestEntity entity = new StringRequestEntity("<message><body>"+msg+"</body></message>","application/xml","UTF-8");
			post.setRequestEntity(entity);
			client.executeMethod(post);
			String responseMsg="httpStatus: " + post.getStatusCode()+" "+
								"Content-type: "+ post.getResponseHeader("Content-Type")+" "+post.getResponseBodyAsString();
			Configuration.log().info(responseMsg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}
}
