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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.mvel2.MVEL;
import com.pieframework.model.Component;
import com.pieframework.model.Configuration;
import com.pieframework.model.Status;
import com.pieframework.model.repository.ModelStore;
import com.pieframework.model.resources.Resource;
import com.pieframework.model.system.Model;
import com.pieframework.runtime.utils.StringUtils;

public class ResourceLoader {
	
	public static Resource load(String query,Component c,Model m){
		
		if (c!=null){
			
			String resourceName=getResourceName(c.getProps().get(query));
			Object result = MVEL.getProperty("resources[\""+ resourceName + "\"]".toString(), m);
			if (result!=null){
				//System.out.println(result.toString());
				return (Resource) result;
			}else{
				//System.out.println("null:"+c.getId()+" "+c.getProps().get(query));
				return load(query,c.getParent(),m);
			}	
		}
		return null;
	}
	
	
	public static List<Resource> loadAll(Resource r, Component c, Model m) {
		
		List<Resource> rlist=new ArrayList<Resource>();
		for (String id:c.getProps().keySet()){
			String resourceName=getResourceName(c.getProps().get(id));
			Object result = MVEL.getProperty("resources[\""+resourceName + "\"]".toString(), m);
									
			if (result!=null && r.getClass().equals(result.getClass())){
				rlist.add((Resource)result);
			}
		}
		
		return rlist;
	}
	
	public static String getResourceName(String input) {
		if (input!=null){
			String[] tmp=input.split("\\[\"");
			if (tmp.length > 1){
				input=tmp[0];
			}
		}
		
		return input;
	}
	
	public static String getResourcePath(String input){
		String[] tmp=input.split("\\[\"");
		if (tmp.length > 1){
			String[] tmp1=tmp[1].split("\"\\]");
			//System.out.println(tmp1[0] +" "+tmp1.length);
			if (tmp1.length >0){
				return tmp1[0];
			}
		}
		return null;
	}
	
	/*public static List<File> findPath(String basePath,String query){
		
		String path=getResourcePath(query);
		List<File> flist=new ArrayList<File>();
		
		if (StringUtils.empty(path)){
			path=basePath;
		}
		
		//System.out.println(path+" "+basePath+" "+query);
		if (!StringUtils.empty(basePath,path)){
			String root=locate(basePath);
			path=FilenameUtils.separatorsToSystem(path);
			path="*"+path+"*";
			
			//System.out.println(path+" "+root);
			File rootDir=new File(root);
			try {
				for (File f:listFilesAndDirectories(rootDir)){
					//System.out.println(f.getPath());
					if (FilenameUtils.wildcardMatch(f.getPath(),path)){
						flist.add(f);
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return flist;
	}*/
	
	public static List<File> findPath(String basePath,String query){
		
		
		List<File> flist=new ArrayList<File>();
		
		if (!StringUtils.empty(basePath,query)){
			String root=locate(basePath);
			query=FilenameUtils.separatorsToSystem(query);
			query="*"+query+"*";
			
			//System.out.println(query+" "+root);
			File rootDir=new File(root);
			try {
				for (File f:listFilesAndDirectories(rootDir)){
					//System.out.println(f.getPath());
					if (FilenameUtils.wildcardMatch(f.getPath(),query)){
						flist.add(f);
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return flist;
	}
	
	public static List<File> findExactPath(File dir,String query){
		
		List<File> flist=new ArrayList<File>();
		
		if (!StringUtils.empty(query) && dir.exists()){
			String root=dir.getPath();
			query=FilenameUtils.separatorsToSystem(query);
			String wildCard="*"+query+"*";
			//System.out.println(query+" "+root);
			File rootDir=new File(root);
			try {
				for (File f:listFilesAndDirectories(rootDir)){
					//System.out.println(f.getPath());
					
					if (FilenameUtils.wildcardMatch(f.getPath(),wildCard)){
						if (f.getName().equals(query)){
							flist.add(f);
						}
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return flist;
	}
	
	static public List<File> listFilesAndDirectories(File aStartingDir) throws FileNotFoundException {
		    List<File> result = getFileListingNoSort(aStartingDir);
		    Collections.sort(result);
		    return result;
	}

	static private List<File> getFileListingNoSort(File aStartingDir) throws FileNotFoundException {
		    List<File> result = new ArrayList<File>();
		    File[] filesAndDirs = aStartingDir.listFiles();
		    List<File> filesDirs = Arrays.asList(filesAndDirs);
		    for(File file : filesDirs) {
		      result.add(file); 
		      if ( ! file.isFile() ) {
		        List<File> deeperList = getFileListingNoSort(file);
		        result.addAll(deeperList);
		      }
		    }
		    return result;
	}
	
	
	public static String locate(String url){
		
		return FilenameUtils.separatorsToSystem(ModelStore.getCurrentModel().getModelDirectory()+File.separatorChar+"resources"+url);
	}


	public static void validate(Map<String, Resource> validation,String componentId) {
		for (String key:validation.keySet()){
			//System.out.println(componentId+" "+key+" "+validation.get(key));
			if (validation.get(key)== null){ 
				throw new RuntimeException("Resource:"+key+" is missing for "+componentId);
				//System.out.println("throwing exception for "+key+" "+componentId);
			}
		}
		
	}
	
	public static String getDeployRoot(){
		return ModelStore.getCurrentModel().getConfiguration().getTmp()+"deploy"+File.separatorChar;
	}


	public static boolean isResource(String command) {
		if (getResourcePath(command)!=null){
			return true;
		}
		return false;
	}

}
