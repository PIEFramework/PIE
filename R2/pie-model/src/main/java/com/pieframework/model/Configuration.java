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
package com.pieframework.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;



public class Configuration {

	private String home;
	private String data;
	private String username;
	private String password;
	private String javabin;
	private String bootstrap;
	private String jar;
	private String base;
	private String tmp;
	private String lib;
	private String ext;
	private String log;
	private File pieFile;
	private String p4user;
	private String p4client;
	private String p4serverURI;
	private String pathseparator;
	private Properties props;
	private String sshstore;
	private String verbosity;
	private String puttyexe;
	private Map<String,String> acton;
	private String shell;
	private String shellparam;
	private String shellquote;
	private String cli;
	private static Logger logger;
	private long defaultTimeout;
	private String skipRegistration;
	private String currentModels;
	
	
	private static class StaicConfigurationHolder { 
	     private static final Configuration INSTANCE = new Configuration("");
	}
	 
	public static Configuration getStaticInstance() {
		return StaicConfigurationHolder.INSTANCE;
	}
	
	public Configuration(String confLocation){
		 //Load configuration from a file
		 try {
			 Properties prop=new Properties();
			 File pieConf=null;

			 if (confLocation !=null && !confLocation.equalsIgnoreCase("")){
				 pieConf=new File(confLocation);
				 
			 }else{
				 pieConf=getPieConf(getOs());
			 }
			 
			 if(pieConf!=null && pieConf.exists()){
				 acton=new HashMap<String,String>();
				 pieFile=pieConf;
			 	 FileInputStream fin = new FileInputStream(pieConf);
				 BufferedInputStream iStream = new BufferedInputStream(fin);
				 prop.load(iStream);
				 this.props=prop;
				 data=prop.getProperty("pie.data");
				 username=prop.getProperty("pie.username");
				 password=prop.getProperty("pie.password");
				 bootstrap=prop.getProperty("pie.bootstrap");
				 jar=prop.getProperty("pie.jar");
				 javabin=prop.getProperty("pie.javabin");
				 tmp=prop.getProperty("pie.tmp");
				 lib=prop.getProperty("pie.lib");
				 ext=prop.getProperty("pie.ext");
				 log=prop.getProperty("pie.log");
				 p4user=prop.getProperty("pie.p4user");
				 p4client=prop.getProperty("pie.p4client");
				 p4serverURI=prop.getProperty("pie.p4serverURI");
				 pathseparator=prop.getProperty("pie.pathseparator");
				 sshstore=prop.getProperty("pie.sshstore");
				 base=javabin+" -jar "+jar+" ";
				 verbosity=prop.getProperty("pie.verbosity");
				 puttyexe=prop.getProperty("pie.puttyexe");
				 shell=prop.getProperty("pie.shell");
				 shellparam=prop.getProperty("pie.shellparam");
				 shellquote=prop.getProperty("pie.shellquote");
				 cli=prop.getProperty("pie.cli");
				 skipRegistration=prop.getProperty("pie.skipRegistration");
				 currentModels=prop.getProperty("pie.currentModels");
				 for(int i=0;i<10;i++){
					 String key="pie.acton["+i+"]";
					 String value=prop.getProperty(key);
					 if (value!=null && !value.equalsIgnoreCase("")){
						 acton.put(key,value);
					 }
				 }
				
				 iStream.close();
				 fin.close();
			 }else{
				 throw new RuntimeException("pie.conf is null or does not exist for file location:"+pieConf.getPath());
			 }
			 
			 
			 if (logger == null) {
					logger = Logger.getRootLogger();
					String logFile = "";
					if (this.getLog() != null && !this.getLog().equalsIgnoreCase("")) {
						logFile = this.getLog();
					} else {
						logFile=getDefaultLogLocation();
					}

					if (logFile != null && !logFile.equalsIgnoreCase("")) {
						RollingFileAppender appender1 = null;
						try {
							/*Enumeration<Appender> list = logger.getAllAppenders();
							while (list.hasMoreElements()) {
								System.out.println(list.nextElement().getName());
							}*/
							
							appender1 = new RollingFileAppender(logger.getAppender("pie").getLayout(), logFile);
							appender1.setMaxFileSize("4096KB");

						} catch (Exception e) {
							
							e.printStackTrace();
						}

						if (appender1 != null) {
							logger.addAppender(appender1);
						}else{
							java.lang.System.err.println("Failed initializing logging. Verify log4j.properties exists and set correctly.");
						}
					}
				}
			 
			 	try {
					defaultTimeout=Long.parseLong(prop.getProperty("pie.defaultTimeout").trim());
				} catch (Exception e) {
					logger.warn("defaultTimeout value set in pie.conf could not be converted to integer. Default to 0.");
					defaultTimeout=0;
				}
			 		
	
		 }catch (Exception e){
			 e.printStackTrace();
			 logger.error("Encountered errors during installation",e);
		 }
	 }
	
	public static File getPieConf(String os){
		File pieConf=null;
		//pieConf is located relative to the executing jar file
		File jarFile=new File(Configuration.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		String home=jarFile.getParent();
		
		if (os==null || os.equalsIgnoreCase("")){
			os=Configuration.getOs();
		}
		
		if (os.contains("indow")){
			pieConf=new File( home+"\\conf\\pie.conf");
		}else if (os.contains("inux")){
			pieConf=new File( home+"/conf/pie.conf");
		}else if (os.contains("cygwin")){
			pieConf=new File( home+"\\\\\\\\conf\\\\\\\\pie.conf");
		}else {
			throw new RuntimeException("PIE does not support '"+os+"'");
		}
		
		return pieConf;
	}
		
	public static String getOs(){
		String os=java.lang.System.getProperty("os.name");
		if (os.contains("indow")){
			os="windows";
		}else if (os.contains("inux")){
			os="linux";
		}else if (os.contains("cygwin")){
			os="cygwin";
		}else{
			os="";
		}
		return os;
	}

	public static String getDefaultLogLocation(){
		if (java.lang.System.getProperty("os.name").contains("indow")) {
			return "c:\\logs\\pie\\pie.log";
		} else {
			return "/opt/apps/logs/pie/pie.log";
		}
	}
	
	
	
	public long getDefaultTimeout() {
		return defaultTimeout;
	}

	public void setDefaultTimeout(long defaultTimeout) {
		this.defaultTimeout = defaultTimeout;
	}

	public static Logger getLogger(){
		return logger;
	}
	
	public static Logger log(){
		return logger;
	}
	 
	public String getCli() {
		return cli;
	}



	public void setCli(String cli) {
		this.cli = cli;
	}

	

	public String getSkipRegistration() {
		return skipRegistration;
	}

	public void setSkipRegistration(String skipRegistration) {
		this.skipRegistration = skipRegistration;
	}

	public String getShell() {
		return shell;
	}



	public void setShell(String shell) {
		this.shell = shell;
	}



	public String getShellparam() {
		return shellparam;
	}



	public void setShellparam(String shellparam) {
		this.shellparam = shellparam;
	}



	public String getShellquote() {
		return shellquote;
	}



	public void setShellquote(String shellquote) {
		this.shellquote = shellquote;
	}



	public Map<String, String> getActon() {
		return acton;
	}



	public void setActon(Map<String, String> acton) {
		this.acton = acton;
	}



	public String getVerbosity() {
		return verbosity;
	}



	public void setVerbosity(String verbosity) {
		this.verbosity = verbosity;
	}



	public Properties getProps() {
		return props;
	}

	public void setProps(Properties props) {
		this.props = props;
	}

	public Configuration getConfiguration(String file)
	 {
		 return new Configuration(file);
	  }

	public String getPathseparator() {
		return pathseparator;
	}

	public void setPathseparator(String pathseparator) {
		this.pathseparator = pathseparator;
	}

	public File getPieFile() {
		return pieFile;
	}

	public String getHome() {
		return home;
	}

	public String getData() {
		return data;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getJavabin() {
		return javabin;
	}

	public String getBootstrap() {
		return bootstrap;
	}

	public String getJar() {
		return jar;
	}
	
	public String getBase() {
		return base;
	}

	public String getTmp() {
		return tmp;
	}

	public String getLog() {
		return log;
	}

	public String getP4user() {
		return p4user;
	}

	public void setP4user(String p4user) {
		this.p4user = p4user;
	}

	public String getP4client() {
		return p4client;
	}

	public void setP4client(String p4client) {
		this.p4client = p4client;
	}

	public String getP4serverURI() {
		return p4serverURI;
	}

	public void setP4serverURI(String p4serveruri) {
		p4serverURI = p4serveruri;
	}

	public String getSshstore() {
		return sshstore;
	}

	public void setSshstore(String sshstore) {
		this.sshstore = sshstore;
	}



	public String getPuttyexe() {
		return puttyexe;
	}



	public void setPuttyexe(String puttyexe) {
		this.puttyexe = puttyexe;
	}

	public String getLib() {
		return lib;
	}

	public void setLib(String lib) {
		this.lib = lib;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	public void setTmp(String tmp) {
		this.tmp = tmp;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public String getCurrentModels() {
		return currentModels;
	}

	public void setCurrentModels(String currentModels) {
		this.currentModels = currentModels;
	}
	
	

	public static String getCurrentModelVersion(String modelName,String modelString) {
		String result="";
		Scanner scanner = new Scanner(modelString);
		scanner.useDelimiter(",");
		while (scanner.hasNext()) {
			String tmp=scanner.next();
			
			final String[] nameValue = tmp.split(":");
			
			if (nameValue.length == 0 || nameValue.length > 2) {
				throw new IllegalArgumentException("Illegal format");
			}
			
			if (nameValue[0].equalsIgnoreCase(modelName)){
				if (nameValue.length >1){
					result=nameValue[1];
				}	
			}
		}
		
		return result;
	}
}
