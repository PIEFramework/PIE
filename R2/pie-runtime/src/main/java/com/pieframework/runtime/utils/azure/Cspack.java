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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.pieframework.model.Component;
import com.pieframework.model.Configuration;
import com.pieframework.model.Request;
import com.pieframework.model.Status;
import com.pieframework.model.repository.ModelStore;
import com.pieframework.model.resources.Resource;
import com.pieframework.model.system.Role;
import com.pieframework.model.system.Service;
import com.pieframework.resources.Files;
import com.pieframework.runtime.core.ResourceLoader;
import com.pieframework.runtime.execution.CommandExec;
import com.pieframework.runtime.utils.FindExecutable;
import com.pieframework.runtime.utils.StringUtils;
import com.pieframework.runtime.utils.Zipper;


public class Cspack {

	
	public Status createPackage(File csdef, Component c,Request r, String packageFilename) {
		
		List<File> tmpFiles=new ArrayList<File>();
		File packageFile=new File(packageFilename);
		if (!packageFile.getParentFile().exists()){
			try {
				File dir=new File(packageFile.getParentFile().getPath());
				dir.mkdirs();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		
		String cspackExe = Request.findAttribute(r.getInput(), "cspack");
		File cspackFile=null;
		if (!StringUtils.empty(cspackExe)){
			cspackFile=new File(cspackExe);
			if (!cspackFile.exists()){
				throw new RuntimeException("cspack file:"+cspackFile.getPath()+" does not exist!");
			}
			cspackExe="\""+cspackExe+"\"";
			
		}else {
			//is cspack.exe is in the PATH
			cspackFile=FindExecutable.find("cspack.exe");
			if (cspackFile!=null && cspackFile.exists()){
				cspackExe="\""+cspackFile.getPath()+"\"";
			}else{
				throw new RuntimeException("cspack.exe is not in the PATH, and was not explicitly provided with -i cspack=<exe_path>");
			}
		}
	
		//Create cspack commandline
		String cspackCommand="";
		//String cspackExe="\""+Configuration.getStaticInstance().getExt()+"cspack\\cspack.exe\" ";
		//String cspackExe="C:\\\"Program Files\"\\\"Windows Azure SDK\"\\v1.4\\bin\\cspack.exe";
		File destinationDir=new File(packageFile.getParent());
		String cspackOptions=" "+csdef.getPath()+" "+" /out:"+packageFile.getPath()+ " "+ this.generateRoleDirectories(c,destinationDir,tmpFiles)+ " "+this.generateRoleProperties(c,destinationDir,tmpFiles);
		cspackCommand=cspackExe+cspackOptions;
		
		r.getStatus().addMessage("info",cspackCommand + " "+destinationDir);
		//Execute cspack command
		Configuration config=ModelStore.getCurrentModel().getConfiguration();
		CommandExec cmdExec=new CommandExec(cspackCommand,config.getDefaultTimeout(),config.getShell(),config.getShellparam(),config.getShellquote(),config.getCli());
		r.getStatus().addMessage("info","Generating "+packageFile.getPath());
		r.getStatus().addStatus(cmdExec.exec(r.getStatus()));
		
		//Cleanup any temp files
		/*for (File f:tmpFiles){
			f.delete();
		}*/

		return r.getStatus();
	}

	
	private String generateRoleDirectories(Component c, File dir, List<File> tmpFiles) {
		String result="";
			
		for (String key:c.getChildren().keySet()){
			if (c.getChildren().get(key) instanceof Role){
				Role role=(Role) c.getChildren().get(key);
				
				if (role.getProps().get("type")!=null ){
					if (dir.exists() && dir.isDirectory()){
						File roleDir =new File(dir.getPath()+File.separatorChar+role.getId());
						roleDir.mkdir();
						tmpFiles.add(roleDir);
						result+=" /role:"+role.getId()+";"+roleDir.getPath();
						
						if (role.getProps().get("roleEntryPointDLL")!=null ){
						
							//Stage role binaries
							stageRoleBinaries(role,roleDir);
							
							//Locate the entry dll
							List<File> flist=ResourceLoader.findExactPath(roleDir, role.getProps().get("roleEntryPointDLL"));
							if (flist!=null && flist.size()>0){
								if (flist.size() == 1){
									result+=";"+flist.get(0).getName();	
								}else{
									throw new RuntimeException("Found multiple matches for entrypoint dll:"+role.getProps().get("roleEntryPointDLL")+" in "+roleDir.getPath());
								}
							}else{
								throw new RuntimeException("Found 0 matches for entrypoint dll:"+role.getProps().get("roleEntryPointDLL")+" in "+roleDir.getPath());
							}
						}
						
						//Copy startup tasks to the bin directory of the role
						stageStartupTasks(role,roleDir);
					}	
				}
			}
		}
		
		return result;
	}

	private void stageRoleBinaries(Role role, File roleDir) {
		File roleBin=null;
		if (role.getProps().get("type")!=null){
			roleBin=new File(roleDir.getPath()+File.separatorChar);
			roleBin.mkdir();
					
		if (roleBin!=null){
			for (String key:role.getChildren().keySet()){
				
				if (role.getChildren().get(key) instanceof Service){
					Service s=(Service) role.getChildren().get(key);
					if (s.getProps().get("type")!=null && s.getProps().get("type").equalsIgnoreCase("application") ){			
						String query=s.getProps().get("package");
						if (query!=null){
							String fQuery=s.getProps().get("package");
							String nQuery=ResourceLoader.getResourceName(fQuery);
							String pQuery=ResourceLoader.getResourcePath(fQuery);
							Files bootstrap= (Files) s.getResources().get(nQuery);
							if (bootstrap !=null){
								List<File> flist=ResourceLoader.findPath(bootstrap.getLocalPath(), pQuery);
								if (flist.size()==1 
										&& flist.get(0).exists() 
										&& FilenameUtils.isExtension(flist.get(0).getPath(),"zip" )){
									
									try {
										Zipper.unzip(flist.get(0).getPath(),roleBin.getPath());
										if (role.getProps().get("type").equals("WebRole")){
											//Cleanup non-bin files
											for (File f:roleBin.listFiles()){
												if (!f.getName().equalsIgnoreCase("bin")){
													FileUtils.forceDelete(f);
												}
											}
											
											//Move contents of bin dir into the current directory
											File binDir=new File(roleBin.getPath()+File.separatorChar+"bin");
											if (binDir.exists()){
												for (File f:binDir.listFiles()){
													if (f.isDirectory()){
														FileUtils.copyDirectory(f, roleBin);
													}else{
														FileUtils.copyFileToDirectory(f, roleBin);
													}
												}
												FileUtils.forceDelete(binDir);
											}
										}
									} catch (ZipException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}	
						}
					}
				}
			}
		}
		}
	}


	private void stageStartupTasks(Role role,File roleDir) {
		
		File roleBin=null;
		if (role.getProps().get("type")!=null){
			if (role.getProps().get("type").equals("WebRole")){
				roleBin=new File(roleDir.getPath()+File.separatorChar+"bin");
			}else if (role.getProps().get("type").equals("WorkerRole")){
				roleBin=new File(roleDir.getPath()+File.separatorChar);
			}
		}
		
		//Locate startup tasks and copy payloads into roleDir payloads	
		for (String key:role.getChildren().keySet()){
			
			if (role.getChildren().get(key) instanceof Service){
				Service s=(Service) role.getChildren().get(key);
				if (s.getProps().get("type")!=null && s.getProps().get("type").equalsIgnoreCase("bootstrap") ){
					
					roleBin.mkdir();
					String query=s.getProps().get("installer");
					if (query!=null){
						String fQuery=s.getProps().get("installer");
						String nQuery=ResourceLoader.getResourceName(fQuery);
						String pQuery=ResourceLoader.getResourcePath(fQuery);
						Files bootstrap= (Files) s.getResources().get(nQuery);
						if (bootstrap !=null){
							List<File> flist=ResourceLoader.findPath(bootstrap.getLocalPath(), pQuery);
							if (flist.size()==1 
									&& flist.get(0).exists() 
									&& FilenameUtils.isExtension(flist.get(0).getPath(),"zip" )){
								
								try {
									Zipper.unzip(flist.get(0).getPath(),roleBin.getPath());
								} catch (ZipException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						
					}
				}
			}
		}
	}

	private String generateRoleProperties(Component c, File destinationDir, List<File> tmpFiles) {
		// /rolePropertiesFile:DefaultWebApp1;role.prop /rolePropertiesFile:bApp2;role.prop
		String result="";
		
		for (String key:c.getChildren().keySet()){
			if (c.getChildren().get(key) instanceof Role){
				Role role=(Role) c.getChildren().get(key);
				
				if (role.getProps().get("targetFramework")!=null ){
					byte[] data=role.getProps().get("targetFramework").getBytes();
					String roleId=role.getId();
					File propFile=new File(destinationDir.getPath()+File.separatorChar+roleId+".property");
					try {
						FileOutputStream out=new FileOutputStream(propFile);
						tmpFiles.add(propFile);
						out.write(data);
						out.flush();
						out.close();
						
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					result+=" /rolePropertiesFile:"+roleId+";"+propFile.getPath();
				}
			}
		}
		
		return result;
	}
}
