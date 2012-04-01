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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.pieframework.model.Configuration;
import com.pieframework.model.Request;
import com.pieframework.runtime.utils.StringUtils;
import com.pieframework.runtime.utils.TimeUtils;


public class Installer {
	public static final String USERNAME = "pieadmin";
	public static final String PASSWORD = "LVd0tc0m";
	public static final String WINHOME = "c:\\pie2\\";
	public static final String LINHOME = "/opt/apps/pie2/";
	public static final String CYGDRIVE = "/cygdrive/c";
	public static final String CYGHOME = "c:\\\\\\\\pie2\\\\\\\\";

	public static Properties generateProps(String inputs){
		
		Properties prop=new Properties();
		String os=Configuration.getOs();
		String home=Request.findAttribute(inputs, "home");
				
		if (os.contains("indow")){
			if (StringUtils.empty(home)){
				home=WINHOME;
			}
			home+="\\";
			prop.setProperty("pie.javabin","\""+getWindowsPath(System.getProperty("java.home")+File.separator+"bin"+File.separator+"java.exe")+"\"");
			prop.setProperty("pie.data", home+"data"+File.separator);
			prop.setProperty("pie.tmp", home+"tmp"+File.separator);
			prop.setProperty("pie.lib", home+"lib"+File.separator);
			prop.setProperty("pie.ext", home+"ext"+File.separator);
			prop.setProperty("pie.log", "c:\\logs\\pie\\pie.log");
			prop.setProperty("pie.bootstrap", home+"conf"+File.separator+"bootstrap.properties");
			prop.setProperty("pie.pathseparator",File.separator);
			prop.setProperty("pie.jar",home+"pie.jar");
			prop.setProperty("pie.home",home);
			prop.setProperty("pie.config",getPieConf(os,home).getPath());
			prop.setProperty("pie.shell","cmd.exe");
			prop.setProperty("pie.shellparam","/c");
			prop.setProperty("pie.shellquote","\"");
			
		}else if (os.contains("inux")){
			if (StringUtils.empty(home)){
				home=LINHOME;
			}
			
			home+="/";
			prop.setProperty("pie.javabin",System.getProperty("java.home")+"/"+"bin"+"/"+"java");
			prop.setProperty("pie.data", home+"data"+"/");
			prop.setProperty("pie.tmp", home+"tmp"+"/");
			prop.setProperty("pie.lib", home+"lib"+"/");
			prop.setProperty("pie.ext", home+"ext"+"/");
			prop.setProperty("pie.log", "/opt/apps/logs/pie/pie.log");
			prop.setProperty("pie.bootstrap", home+"conf"+"/"+"bootstrap.properties");
			prop.setProperty("pie.pathseparator","/");
			prop.setProperty("pie.jar",home+"pie.jar");
			prop.setProperty("pie.home",home);
			prop.setProperty("pie.config",getPieConf(os,home).getPath());
			prop.setProperty("pie.shell","bash");
			prop.setProperty("pie.shellparam","-lc");
			prop.setProperty("pie.shellquote","\"");
			
		}else if (os.equalsIgnoreCase("cygwin")){
			if (StringUtils.empty(home)){
				home=CYGHOME;
			}
			home+="\\\\\\\\";
			prop.setProperty("pie.javabin","java");
			prop.setProperty("pie.data", home+"data"+"\\\\\\\\");
			prop.setProperty("pie.tmp", home+"tmp"+"\\\\\\\\");
			prop.setProperty("pie.lib", home+"lib"+"\\\\\\\\");
			prop.setProperty("pie.ext", home+"ext"+"\\\\\\\\");
			prop.setProperty("pie.log", "c:\\\\\\\\logs\\\\\\\\pie\\\\\\\\pie.log");
			prop.setProperty("pie.bootstrap", home+"conf"+"\\\\\\\\"+"bootstrap.properties");
			prop.setProperty("pie.pathseparator","\\\\\\\\");
			prop.setProperty("pie.jar",home+"pie.jar");
			prop.setProperty("pie.home",home);
			prop.setProperty("pie.config",getPieConf(os,home).getPath());
			prop.setProperty("pie.shell","bash");
			prop.setProperty("pie.shellparam","-lc");
			prop.setProperty("pie.shellquote","\"");
		}else{
			throw new RuntimeException("Installer failed. environment "+os+" is not supported.");
		}
			
		prop.setProperty("pie.username", "pieadmin");
		prop.setProperty("pie.password", "LVd0tc0m");
		prop.setProperty("pie.p4user","<username>");
		prop.setProperty("pie.p4serverURI","p4java://<server>:<port>");
		prop.setProperty("pie.p4client","<p4_local_client>");
		prop.setProperty("pie.sshstore", "<keystore>");
		prop.setProperty("pie.cli", os);
		prop.setProperty("pie.puttyexe","<path_to_putty>");
		prop.setProperty("pie.verbosity", "*:!compile:!handler:!delegator:!debug:!ssh");
		prop.setProperty("pie.acton[0]", "*:print");
		prop.setProperty("pie.defaultTimeout", "180");
		prop.setProperty("pie.currentModels", "<systemId>:<modelVersion>");
		
		return prop;	
	}
	
	public static String getWindowsPath(String path){
		return path.replaceAll(java.util.regex.Matcher.quoteReplacement("\\"),"\\\\\\\\");
	}
	
	public static File getPieConf(String os,String home){
		File pieConf=null;
		//pieConf is located relative to the pie home directory
		//The default homes are constants in this file
		if (os==null || os.equalsIgnoreCase("")){
			os=Configuration.getOs();
		}
		
		if (os.contains("indow")){
			if (home==null || home.equalsIgnoreCase("")){
				home=WINHOME;
			}
			pieConf=new File( home+"conf\\pie.conf");
		}else if (os.contains("inux")){
			if (home==null || home.equalsIgnoreCase("")){
				home=LINHOME;
			}
			pieConf=new File( home+"conf/pie.conf");
		}else if (os.contains("cygwin")){
			if (home==null || home.equalsIgnoreCase("")){
				home=CYGHOME;
			}
			pieConf=new File( home+"conf\\\\\\\\pie.conf");
		}else {
			throw new RuntimeException("PIE does not support '"+os+"'");
		}
		
		return pieConf;
	}
	
	
	public static String getJarPath(){
		//Get the location of the jar file that we specified
		String jarPath=run.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		
		//Strip the first character of the Class url if this is windows
		if(Configuration.getOs().equalsIgnoreCase("windows")){
			jarPath=jarPath.substring(1);
		}
		
		return jarPath;
	}
	
	public static void createConfigFile(Properties prop){
		
		try {
			File pieConfig=new File(prop.getProperty("pie.config"));
			//Backup the config file if it exists
			if (pieConfig.exists()){
				File backup=new File(pieConfig.getPath()+"."+TimeUtils.getCurrentTimeStamp());
				FileUtils.copyFile(pieConfig, backup);
			}
			
			//Create/Overwrite the config file
			OutputStream out=new FileOutputStream(pieConfig);
			prop.store(out, getComments());
			System.out.println("Created config file: "+pieConfig.getPath());		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void run(String inputs){
		//Generate configuration file if one does not exists and overwrite is not specified
		Properties prop=generateProps(inputs);
		
		System.out.println("Running installer for pie for "+Configuration.getOs());
		//Based on config create directories
		List<File> dirs=new ArrayList<File>();
	
		dirs.add(new File(prop.getProperty("pie.home")));
		dirs.add(new File(prop.getProperty("pie.lib")));
		dirs.add(new File(prop.getProperty("pie.tmp")));
		dirs.add(new File(prop.getProperty("pie.data")));
		dirs.add(new File(prop.getProperty("pie.ext")));
		dirs.add(new File( new File(prop.getProperty("pie.bootstrap")).getParent()));
		dirs.add(new File( new File(prop.getProperty("pie.log")).getParent()));
				
		for (File f:dirs){
			//System.out.println(f.getPath());
			f.mkdirs();
		}
		
		//Create the pie config file and insert all the properties we created
		createConfigFile(prop);
		
		//Copy the current pie.jar in the right place according to config
		File currentPieJar=new File(getJarPath());
		File newPieJar=new File(prop.getProperty("pie.jar"));
		
		try {
			if (!newPieJar.getPath().equalsIgnoreCase(currentPieJar.getPath())){
				FileUtils.copyFile(currentPieJar,newPieJar);	
			}
			
			if (newPieJar.exists() && newPieJar.isFile()){
				System.out.println("info: created exe file "+newPieJar.getPath());
			}else{
				throw new RuntimeException("error: failed creating file "+newPieJar.getPath());
			}
		} catch (IOException e) {
			throw new RuntimeException("error: failed creating file "+newPieJar.getPath(),e);
		}
	}
	
	public static String getComments(){
		return ""+"\n pie.data	= The directory where model data is stored which pie.jar looks it up at runtime. This value can get overriden by providing 'd <data_path>' or '-data <data_path>' to the pie.jar command line."
		+"\n pie.username	= Username for accessing and running pie commands. This value can get overriden by providing '-u <username>' or '-username <username>' to the pie.jar command line."
		+"\n pie.password	= Password to access pie.jar. This value can get overriden by providing 'p <password>' or '-password <password>' to the pie.jar command line."
		+"\n pie.javabin	= Location of where the java binary is used for pie commands execution."
		+"\n pie.bootstrap= The bootstrap.properties the IP and port address for the Registry services that pie.jar will be looking up at runtime."
		+"\n pie.jar	= Location of the pie.jar"
		+"\n pie.lib	= Location of external libraries that will be loaded."
		+"\n pie.tmp	= Temp directory where pie.jar commands will place temp files."
		+"\n pie.log	= Pie log file."
		+"\n pie.sshstore= Directory where sshKeys will be stored for remote access."
		+"\n pie.verbosity	= Level of verbosity of the client output. Possible options are null,status,error,warn,debug,*. The verbosity string is : delimited. Example 'pie.verbosity=error:status:info' "
		+"\n pie.acton[i]= The acton map specifies what action to be taken when a certain condition is encounters. Possible options are ok,warn,error,*. Currently only print action is allowed. Example: 'pie.acton[0]=ok:print,pie.acton[1]=error:print"
		+"\n pie.p4user	= P4 user that the local client will use to connect to p4."
		+"\n pie.p4serverURI = P4 URI for the p4 server. Example URI: p4java://p4server.domain.com:1666"
		+"\n pie.defaultTimeout = It controls when the pie client will disconnect from the remote shell or locally executed command.Default value is 60 seconds"
		+"\n pie.shell = Controls what is the local shell environment for the pie client."
		+"\n pie.shellparam = The shell parameters that will be passed to the shell (local or remote) when running an exe command. Example: -lc"
		+"\n pie.shellquote = The character to use for quoting the command when wrapped by the shell. Example: \" "
		+"\n pie.currentModels = Defines what are the model versions that will be use by default."
		+"\n pie.p4client	= P4 client name.";
	}	
}
