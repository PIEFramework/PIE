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
import org.soyatec.windowsazure.management.AsyncResultCallback;
import org.soyatec.windowsazure.management.OperationStatus;




public class DefaultAsyncCallback implements AsyncResultCallback {

	public void onError(Object arg0) {
		// TODO Auto-generated method stub
		
	}

	public void onSuccess(Object arg0) {
		// TODO Auto-generated method stub
		
	}

	/*
	@Override
	public void onError(Object result) {
		
		String responseInfo=this.getResponseInfo(result);
		Configuration.log().error(responseInfo);
		throw new RuntimeException("Operation failed:"+responseInfo);

	}

	@Override
	public void onSuccess(Object result) {
		String responseInfo=this.getResponseInfo(result);
		Configuration.log().info(responseInfo);
	}
	
	public String  getResponseInfo(Object result){
		String msg="";
		if (result instanceof OperationStatus){
			
			msg=" errorCode:"+((OperationStatus)result).getErrorCode()+
						" errorMessage:"+((OperationStatus) result).getErrorMessage()+
						" httpCode:"+((OperationStatus) result).getHttpCode()+
						" requestId:"+((OperationStatus) result).getRequestId();
		}
		return msg;
	}
*/
}
