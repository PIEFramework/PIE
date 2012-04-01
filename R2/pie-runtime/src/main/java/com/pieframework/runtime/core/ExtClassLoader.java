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
package com.pieframework.runtime.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import com.pieframework.model.Configuration;

public class ExtClassLoader {
	
	public ExtClassLoader(){
		
	}
	
	public void load(String libDir){
		//Add external libs to the classpath
		if (libDir!=null){
			File lib=new File(libDir);
			
			if (lib.isDirectory()){
				for (File libFile:lib.listFiles()){
					if (libFile.isFile()){
						int dotPos = libFile.getPath().lastIndexOf(".");
					    String extension = libFile.getPath().substring(dotPos);
					    if (extension.equalsIgnoreCase(".jar")){
					    	
					    	try {
								ClasspathAppender.appendPath(libFile);
							} catch (IOException e) {
								Configuration.log().error("Failed loading library:"+libFile.getPath());
							}
					    }
					}
				}
				  
			}
		}else{
			Configuration.log().error("PIE lib directory location is null. Verify pie.lib is configured in pie.conf");
		}
		
	}
}
