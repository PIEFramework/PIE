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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.pieframework.model.Component;
import com.pieframework.model.Configuration;
import com.pieframework.model.Status;
import com.pieframework.model.repository.ModelStore;
import com.pieframework.model.resources.Resource;
import com.pieframework.resources.Files;
import com.pieframework.runtime.core.RepositoryLoader;
import com.pieframework.runtime.core.ResourceLoader;
import com.pieframework.model.repositories.ArtifactRepository;


public class ArtifactManager {
	

	public Map<String,String> syncResources(Component c,Boolean recursive, Boolean forceUpdate){
		//Load children first
		if (recursive){
			if (c.getChildren()!=null){
				for (String cid:c.getChildren().keySet()){
					this.syncResources(c.getChildren().get(cid), recursive, forceUpdate);
				}
			}
		}
		
		if (c.getResources()!=null){
			for (String key:c.getResources().keySet()){
				//Download artifacts from repository
				Resource r=c.getResources().get(key);
				if (r instanceof Files){
					Files fr=(Files) r;
					String filter=fr.getPathFilter();
					String localPath=ResourceLoader.locate(StringUtils.localizedPath(fr.getLocalPath()));
					ArtifactRepository repo=(ArtifactRepository) RepositoryLoader.load(fr.getArtifactRepository(), ModelStore.getCurrentModel());
					if (repo!=null){
						Map<String, String> downloadList=null;
						try {
							downloadList = repo.download(fr, null, forceUpdate);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						if (downloadList!=null){
							for (String virtual:downloadList.keySet()){
								copy(downloadList.get(virtual),filter,localPath,virtual);
							}
						}
						
					}else{
						throw new RuntimeException("Repository:"+fr.getArtifactRepository()+" could not be found.");
					}
					
				}
			}	
		}
		return null;
	}

	private void copy(String download, String filter, String localPath,String virtualPath) {
		
		/*System.out.println("download:"+download+
							"\nfilter:"+filter+
							"\nlocalPath:"+localPath+
							"\nvirtualPath:"+virtualPath);
		*/
		boolean match=true;
		if (!StringUtils.empty(filter)){
			match=FilenameUtils.wildcardMatch(FilenameUtils.separatorsToSystem(download), filter);
		}
		
		if (match){
					
			File destinationVirtualFile=new File(FilenameUtils.separatorsToSystem(virtualPath));
			File destination=new File(localPath+destinationVirtualFile.getParent());
			destination.mkdirs();
			
			if (destination.exists()){
				File downloadFile=new File(download);
				try {
					FileUtils.copyFileToDirectory(downloadFile, destination);
					File copiedFile=new File(destination.getPath()+File.separatorChar+destinationVirtualFile.getName());
					if (copiedFile.exists()){
						System.out.println("file copied:"+copiedFile+" crc:"+FileUtils.checksumCRC32(copiedFile));
					}else{
						//TODO error
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}else{
				//TODO error
		}
			
		}
	}
	
	public static String generateDeployPath(String basePath,boolean unArchive,String query) {
		String result=null;
		
		//Locate the files in the local artifact cache
		Configuration.log().debug("artifact location:"+basePath+" query:"+query);
		List<File> flist=ResourceLoader.findPath(basePath, query);
		
		//System.out.println(flist.size() +" "+basePath +" "+query);
		
		if (!flist.isEmpty()){
			String archivePath=getArchivePath(flist);
			if (!StringUtils.empty(archivePath)){
				//Create a temp location
				File archiveFile=new File(archivePath);
				String tmpPath=ResourceLoader.getDeployRoot()
				+ResourceLoader.getResourceName(query)
				+File.separatorChar
				+TimeUtils.getCurrentTimeStamp()
				+File.separatorChar
				+FilenameUtils.getName(archiveFile.getParent());
				
				File tmpDir=new File(tmpPath);
				if (tmpDir.isDirectory() && !tmpDir.exists()){
					tmpDir.mkdirs();
				}
				
				//Unarchive the file
				try {
					Zipper.unzip(archivePath, tmpPath);
					result=tmpPath;
				} catch (Exception e) {
					//TODO throw error
					e.printStackTrace();
				}
				
			}else {
				result=getCommonPathRoot(flist);
			}
		}else{
			throw new RuntimeException("Failed locating path for artifact. searchroot:"+basePath+" query:"+query);
		}
		return result;
	}

	private static String getCommonPathRoot(List<File> flist) {
		
		String result="";
		int shortestPathSize=0;
		Map<String,File> commonPath=new HashMap<String,File>();
		
		for (File f:flist){
			
			String[] tmp=f.getPath().split("\\\\");
			if (tmp.length == 0){
				tmp=f.getPath().split("/");
			}
			
			if (shortestPathSize==0){
				shortestPathSize=tmp.length;	
			}
			
			if (tmp.length <= shortestPathSize){
				shortestPathSize=tmp.length;
				commonPath.put(f.getPath(),f);
				result=f.getPath();
			}
			
		}
		
		if (commonPath.size() > 1){
			throw new RuntimeException("There is more than one root directory provided in file list.");
		}
		
		return result;
	}

	private static String getArchivePath(List<File> flist) {
		
		if (!flist.isEmpty()){
			if (flist.size() == 1 && flist.get(0).isFile() && FilenameUtils.isExtension(flist.get(0).getName(), "zip")){
				return flist.get(0).getPath();
			}
		}

		return null;
	}

/*
	public void cleanupArtifacts(Map<String, File> artifactMap) {
	
		for (String service:artifactMap.keySet()){
			File dir=artifactMap.get(service);
			FileUtils.delete(new File(dir.getParent()));
		}
		
	}

	*/
}
