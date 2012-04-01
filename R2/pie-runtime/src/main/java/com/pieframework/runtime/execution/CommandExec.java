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
package com.pieframework.runtime.execution;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.pieframework.model.Configuration;
import com.pieframework.model.Status;

public class CommandExec {
	//Lookup inputs and the command to be executed
	private String cmd;
	private long timeout;
	private String shell;
	private String shellParam;
	private String shellQuote;
	private String cli;
	private ExecutorService executor;

	private Integer timedCall(Callable c, long timeout, TimeUnit timeUnit)
    throws InterruptedException, ExecutionException, TimeoutException
    {
		FutureTask task = new FutureTask(c);
		this.executor.execute(task);
		int result=0;
		//executor.execute(task);
		
		if ( timeout == 0 ){
			result=(Integer) task.get();
		}else{
			result=(Integer) task.get(timeout, timeUnit);
		}
		
		return result;
    }

		
	public CommandExec(String cmd,Long timeout,String shell,String shellParam,String shellQuote,String cli){
		this.setCmd(cmd);
		this.setTimeout(timeout);
		this.shell=shell;
		this.shellParam=shellParam;
		this.shellQuote=shellQuote;
		this.cli=cli;
		this.executor=Executors.newFixedThreadPool(1);
		
		
	}
	
	public Status exec(Status parentStatus){
		
		Status status=null;
		Integer exitCode=-1;
		if (this.getCmd()!=null && !this.getCmd().equalsIgnoreCase("")){
			
			status = new Status(parentStatus.getVerbosity(), null,this.getCli() ,this.getCmd());
			
			try {
				Process shell;
				ProcessBuilder pb=null;
				if (this.getCli()!=null){
					
					//Avoid double boxing
					if (this.getCli().equalsIgnoreCase("windows")){
						//System.out.println(this.getShell()+" "+this.getShellParam()+" "+this.getShellQuote()+"commandStart: "+cmd+" :commandEnd "+this.getShellQuote());
						if (cmd.startsWith("\"") && cmd.endsWith("\"")){
							pb= new ProcessBuilder(this.getShell(),this.getShellParam(),cmd);
						}
						else{
							cmd=this.getShellQuote()+cmd+this.getShellQuote();
							pb= new ProcessBuilder(this.getShell(),this.getShellParam(),cmd);
						}
					}else if (this.getCli().equalsIgnoreCase("linux")){
						//Have to pass the command string without quotes to process builder for bash shell to work properly
						if (cmd.startsWith("\"") && cmd.endsWith("\"")){
							cmd=cmd.substring(1, cmd.length()-1);
							if (cmd.startsWith("\"") && cmd.endsWith("\"")){
								cmd=cmd.substring(1, cmd.length()-1);
							}
							//System.out.println(this.getShell()+" "+this.getShellParam()+" "+this.getShellQuote()+"commandStart: "+cmd+" :commandEnd "+this.getShellQuote());
							pb= new ProcessBuilder(this.getShell(),this.getShellParam(),cmd);
						}
						else{
							//cmd=this.getShellQuote()+cmd+this.getShellQuote();
							//System.out.println(this.getShell()+" "+this.getShellParam()+" "+this.getShellQuote()+"commandStart: "+cmd+" :commandEnd "+this.getShellQuote());
							pb= new ProcessBuilder(this.getShell(),this.getShellParam(),cmd);
						}
					} else if (this.getCli().equalsIgnoreCase("cygwin")){
						//Have to pass the command string without quotes to process builder for bash shell to work properly
						if (cmd.startsWith("\"") && cmd.endsWith("\"")){
							cmd=cmd.substring(1, cmd.length()-1);
							pb= new ProcessBuilder(this.getShell(),this.getShellParam(),cmd);
						}
						else{
							cmd=this.getShellQuote()+cmd+this.getShellQuote();
							pb= new ProcessBuilder(this.getShell(),this.getShellParam(),cmd);
						}
					}
					status.addMessage("debug", " debugmsg "+cmd+" : "+this.getShell()+" : "+this.getShellParam()+" : "+this.getCli());
					
				}else{
					status.addMessage("warn","CLI not recognized "+this.getCli()+"."+cmd+" will be executed without a shell");
					pb = new ProcessBuilder(cmd);
				}
				
				//Initialize the process that will carry out the command
				shell = pb.start();
						
				//Create a thread to handle output from each message producing thread
				CommandOutputThread outThread=new CommandOutputThread();
				//setup error and output stream threads
				String lineSep="";
				CommandStreamThread eStream = new CommandStreamThread(shell.getErrorStream(), lineSep,outThread); 
				CommandStreamThread oStream = new CommandStreamThread(shell.getInputStream(), lineSep,outThread);
				//setup a worker thread so we can time the process out when we need
				CommandWorkerThread worker=new CommandWorkerThread(shell);
				
				try {
					oStream.start();
					eStream.start();
					outThread.start();
					exitCode=timedCall(worker, this.getTimeout(), TimeUnit.SECONDS);
				} catch (Exception e) {
					status.addMessage("error", "Command is still running after the set timeout of "+this.getTimeout()+" seconds. PIE is disconnecting from the command.");
					Configuration.log().error("Command still running  after "+this.getTimeout(),e);
					try {
						//Stop all threads and terminated the process
						outThread.interrupt();
						oStream.interrupt();
						eStream.interrupt();
						shell.destroy();
					} catch (Exception e1) {
						status.addMessage("error","Failed to properly terminate the running command.");
						Configuration.log().error(e1.getMessage(),e1);
					}
				}
				
				//Stop all threads and terminated the process
				outThread.interrupt();
				oStream.interrupt();
				eStream.interrupt();
				shell.destroy();
				this.executor.shutdown();
				if( exitCode > 0){
					status.addMessage("error","Exit code:"+exitCode );
				}else
					status.addMessage("info","Exit code:"+exitCode );
			}
			catch (Exception e) {
				status.addMessage("error","Encountered errors executing the command "+cmd);
				Configuration.log().error(e.getMessage(),e);
				return status;
			}
		}
		return status;
	}
	

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public String getShell() {
		return shell;
	}

	public void setShell(String shell) {
		this.shell = shell;
	}

	public String getShellParam() {
		return shellParam;
	}

	public void setShellParam(String shellParam) {
		this.shellParam = shellParam;
	}

	public String getShellQuote() {
		return shellQuote;
	}

	public void setShellQuote(String shellQuote) {
		this.shellQuote = shellQuote;
	}

	public String getCli() {
		return cli;
	}

	public void setCli(String cli) {
		this.cli = cli;
	}
	
	
}

