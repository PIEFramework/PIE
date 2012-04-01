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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class ClasspathAppender {

	   private static final Class[] urlParams = new Class[]{URL.class};
	   public static void appendPath(URL newURL) throws
	       IOException {
	       URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
	       try {
	           Method meth = URLClassLoader.class.getDeclaredMethod("addURL", urlParams);
	           meth.setAccessible(true);
	           meth.invoke(classLoader, new Object []{newURL});
	       } catch (Throwable err) {
	           throw new IOException();
	       }
	   }

	   public static void appendPath(String newPath)
	       throws IOException {
	       appendPath(new File(newPath));
	   }

	   public static void appendPath(File fileObj)
	       throws IOException {
	       appendPath(fileObj.toURL());
	   }
}

