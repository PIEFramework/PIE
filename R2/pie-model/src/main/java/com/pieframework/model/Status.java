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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Status {

	private String status;
	private String statusCode;
	private String commandString;
	private String actor;
	private List<Status> children;
	private String verbosity;
	private Map<String,String> verbosityMap;
	private ArrayList<ArrayList<String>> messages;
	private Map<String,String> actOn;
	final static String ERROR="error";
	final static String INFO="info";
	final static String DEBUG="debug";
	final static String COMPILE="compile";
	final static String WARN="warn";
	final static String OK="ok";
	final static String COMPILED="compiled";
	final static String STATUS="status";
	private String logLocation;
	
		
	public Status (String verbosity,String actions,String command,String currentActor) 
	{
		this.setStatus("");
		this.setStatusCode("");
		this.verbosity=verbosity;
		this.initVerbosity(verbosity);
		this.messages=new ArrayList<ArrayList<String>>();
		this.children=new ArrayList<Status>();
		this.commandString=command;
		this.actor=currentActor;
		this.actOn=new HashMap<String,String>();
		this.actOn.put("*","print");
		
	}
	
	public static void trace(){
		//System.setProperty("file.separator", "/");
		File myFile=new File("/opt/apps/test");
		
		Configuration.log().trace("filesep "+File.separator+" "+System.getProperty("file.separator"));
		Properties props=System.getProperties();
		Enumeration<String> keys=(Enumeration<String>) props.propertyNames();
		while (keys.hasMoreElements()){
			String key=keys.nextElement();
			Configuration.log().trace(key+" : "+props.get(key));
			
		}
		
		for (Thread x : Thread.getAllStackTraces().keySet()) {
			for (StackTraceElement stackEl : x.getStackTrace()) {
				Configuration.log().trace(x.getId() + " " + stackEl.getClassName()
						+ " " + stackEl.getMethodName() + " "
						+ stackEl.getLineNumber() + " " + x.getState().name());
			}

		}
	
	}

	public String getStatusRequest(){
		String inputs="verbosity="+this.getVerbosity()+",";
		
		if (this.actOn!=null){
			int count=0;
			for (String key:this.getActOn().keySet()){
				inputs+="acton["+count+"]="+key+":"+this.getActOn().get(key)+",";
				count++;
			}
		}
		
		return inputs;
	}
	
	public void updateStatus(){
		//roll up status based on children state and the current status messages collected
		//If any warnings set status to warn
		//If any errors set status to error
		//Otherwise OK
		int warn=0;
		int error=0;
		int compile=0;
		
		//check messages that the current status object has accumulated
		for (ArrayList<String> keyValue:this.getMessages()){
			if (keyValue.get(0).equalsIgnoreCase(WARN)){
				warn++;
			}
			
			if (keyValue.get(0).equalsIgnoreCase(ERROR)){
				error++;
			}
			
			if (keyValue.get(0).equalsIgnoreCase(COMPILED)){
				compile++;
			}
		}
		
		for (Status stat:this.getChildren()){
			if (stat==null){
				
			}else{
				if (stat.getStatus().equalsIgnoreCase(WARN)){
					warn++;
				}
				
				if (stat.getStatus().equalsIgnoreCase(ERROR)){
					error++;
				}
				
				if (stat.getStatus().equalsIgnoreCase(COMPILED)){
					compile++;
				}
			}
		}
		
		if(error >0){
			this.setStatus(ERROR);
		}else if (warn >0){
			this.setStatus(WARN);
		}else{
			this.setStatus(OK);
		}
			
		Configuration.log().debug(error+" "+warn+" "+compile);
		
	}
	
	
	public void prompt(){
		//TODO:Open a prompt to proceed or exit
		
	}
	
	public void act(Boolean act){
		this.updateStatus();
		
		if (act && (this.getChildren().size()>0 || this.getMessages().size()>0)){
			this.log("status", this.getStatus().toUpperCase()+" "+this.getActor()+" "+this.getCommandString());
			this.actOn("status",this.getStatus());
		}
			
	}
	
	public void log(String type,String message){
		if (type.equalsIgnoreCase(ERROR)){
			Configuration.log().error(message);
		}else if (type.equalsIgnoreCase(INFO)){
			Configuration.log().info(message);
		}else if (type.equalsIgnoreCase(DEBUG)){
			Configuration.log().debug(message);
		}else if (type.equalsIgnoreCase(COMPILE)){
			Configuration.log().log(CompileLogLevel.COMPILE, message);
		}else if (type.equalsIgnoreCase(WARN)){
			Configuration.log().warn(message);
		}else if (type.equalsIgnoreCase(STATUS)){
			Configuration.log().log(StatusLogLevel.STATUS, message);
		}
	}
	
	public void print(String level,String value){
		//Format the message and print
		//Add a time stamp
		Date current = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String timestamp = format.format(current);
		String sep=" | ";
		
		if (!value.equalsIgnoreCase("") && value!=null) {
			if (this.getVerbosityMap()!=null){
				Boolean skip = this.getVerbosityMap().containsKey("!"+level);
				Boolean levelMatch=this.getVerbosityMap().containsKey(level);
				
				//System.out.println(skip +" "+levelMatch +" "+level+" "+value+" "+this.getVerbosityMap().keySet());
				Boolean skipAction = this.getVerbosityMap().containsKey("!"+this.commandString);
				Boolean skipActor = this.getVerbosityMap().containsKey("!"+this.getActor());
				if (!skip){
					if (levelMatch || this.getVerbosityMap().containsKey("*")) {
						if (level.equalsIgnoreCase("status")){
							if ((skipActor || skipAction) && value.equalsIgnoreCase("ok")){
								
							}else{
								System.out.println(timestamp + sep
										+ "[" + level.toUpperCase()+ " = "+value.toUpperCase()+"]"+sep
										+ this.getCommandString() + sep
										+ this.getActor() + sep+"completed");	
							}
						}else if (level!=null ){
							System.out.println(timestamp + sep
									+ "[" + level.toUpperCase()+ "]\t "+sep
									+ this.getCommandString() + sep 
									+ this.getActor() + sep
									+ value );	
						}
						
					}
				}
				
			
			}
			
		}
			
	}
	
	public void addMessage(String type,String message){
		ArrayList<String> msg=new ArrayList<String>();
		msg.add(type);
		msg.add(message);
		this.getMessages().add(msg);
		this.updateStatus();
		this.log(type, message);
		this.actOn(type, message);
	}
	
	public void addActOn(String action,String statusLevel){
		this.getActOn().put(action, statusLevel);
	}
	
	public void actOn(String type,String value){
		//Evaluate the act on Map and perform an operation. If the status contains! skip the operation.
		String currentStatus=this.getStatus();
		if (currentStatus!=null && !currentStatus.equalsIgnoreCase("")){
			Boolean skip=this.getActOn().containsKey("!"+currentStatus);
			
			if (!skip){
				// Is there a print for the currentStatus
				//System.out.println("current status "+currentStatus +" "+this.getActOn().containsKey("*") + " "+this.getActOn().keySet());
				String action = this.getActOn().get(currentStatus);
				if (action != null && !action.equalsIgnoreCase("")
						&& action.equalsIgnoreCase("print")) {
					this.print(type, value);
				}

				// Do we have a wildcard for print action
				if (this.getActOn().containsKey("*") && this.getActOn().get("*").equalsIgnoreCase("print")) {
					this.print(type, value);
				}
				
			}	
		}	
			
	}
	
	public void addStatus(Status status){
		this.getChildren().add(status);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public List<Status> getChildren() {
		return children;
	}

	public void setChildren(List<Status> children) {
		this.children = children;
	}

	public void initVerbosity(String verbosity) {
		if (verbosity!=null && !verbosity.equalsIgnoreCase("")){
			String[] tmp=verbosity.split(":");
			Map<String,String> verbMap=new HashMap<String,String>();
			for (String s:tmp){
				verbMap.put(s, "");
			}
			this.verbosityMap = verbMap;
		}
		
	}
	

	public String getVerbosity() {
		return verbosity;
	}

	public void setVerbosity(String verbosity) {
		this.verbosity = verbosity;
	}

	public Map<String, String> getVerbosityMap() {
		return verbosityMap;
	}

	public void setVerbosityMap(Map<String, String> verbosityMap) {
		this.verbosityMap = verbosityMap;
	}

	public ArrayList<ArrayList<String>> getMessages() {
		return messages;
	}

	public void setMessages(ArrayList<ArrayList<String>> messages) {
		this.messages = messages;
	}

	public Map<String, String> getActOn() {
		return actOn;
	}

	public void setActOn(Map<String, String> actOn) {
		this.actOn = actOn;
	}

	public String getCommandString() {
		return commandString;
	}

	public void setCommandString(String commandString) {
		this.commandString = commandString;
	}

	public String getActor() {
		return actor;
	}

	public void setActor(String actor) {
		this.actor = actor;
	}	
}

