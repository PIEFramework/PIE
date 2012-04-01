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

public class FindExecutable  
{  
     public static File find(String exe)  
     {  
        return findExecutableOnPath(exe);  
     }  
    
     private static File findExecutableOnPath(String executableName)  
     {  
         String systemPath = System.getenv("PATH");  
         String[] pathDirs = systemPath.split(File.pathSeparator);  
    
         File fullyQualifiedExecutable = null;  
         for (String pathDir : pathDirs)  
         {  
             File file = new File(pathDir, executableName);  
             if (file.isFile())  
             {  
                 fullyQualifiedExecutable = file;  
                 break;  
             }  
         }  
         return fullyQualifiedExecutable;  
     }  
 }  