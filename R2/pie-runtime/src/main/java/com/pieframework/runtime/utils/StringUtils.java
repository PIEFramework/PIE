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

public class StringUtils {

	public static Boolean empty(String x){
		if (x!=null && !x.equalsIgnoreCase("")){
			return false;
		}else{
			return true;
		}
	}
	
	public static Boolean empty(String ... x){
		Boolean empty=false;
		
		for (int i=0;i<x.length;i++){
			if (x[i]!=null && !x[i].equalsIgnoreCase("")){
				
			}else{
				return true;
			}	
		}
		return empty;
	}
	
	public static String localizedPath(String path){
				
		String result=path.replace('/', File.separatorChar).replace('\\', File.separatorChar);
		return result;
	}
	
	public static String doubleSeperatorPath(String path){
		
		String result=localizedPath(path);
		result=result.replace("/", "//");
		result=result.replaceAll("\\\\","\\\\\\\\");
		
		return result;
	}
	
}
