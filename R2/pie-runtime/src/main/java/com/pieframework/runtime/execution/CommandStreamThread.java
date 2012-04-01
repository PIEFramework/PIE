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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.pieframework.model.Configuration;

public class CommandStreamThread  extends Thread implements Callable {
	private InputStream iStream;
	private String cPrompt;
	private CommandOutputThread out;
	private BufferedReader reader;
	private InputStreamReader streamReader;

	CommandStreamThread(InputStream is, String cPrompt,CommandOutputThread outputThread) {
		this.iStream = is;
		this.cPrompt = cPrompt;
		this.out = outputThread;
		this.streamReader=new InputStreamReader(this.iStream);
		this.reader = new BufferedReader(streamReader);

	}
	
	public void run(){
		
		try {
			call();
		} catch (Exception e) {
			Configuration.log().error(e.getMessage(),e);
		}
	}
	
	public CommandOutputThread getOut() {
		return out;
	}

	public void setOut(CommandOutputThread out) {
		this.out = out;
	}

	public BufferedReader getReader() {
		return reader;
	}

	public void setReader(BufferedReader reader) {
		this.reader = reader;
	}

	public InputStreamReader getStreamReader() {
		return streamReader;
	}

	public void setStreamReader(InputStreamReader streamReader) {
		this.streamReader = streamReader;
	}

	public InputStream getIStream() {
		return iStream;
	}

	public void setIStream(InputStream stream) {
		iStream = stream;
	}

	public String getCPrompt() {
		return cPrompt;
	}

	public void setCPrompt(String prompt) {
		cPrompt = prompt;
	}

	public Integer call() throws Exception {
		String linesep = System.getProperty("line.separator");
		String line = null;
		if (this.getCPrompt()!=null && !this.getCPrompt().equalsIgnoreCase(""))
			this.out.enqueue(this.getCPrompt());
		
			try {
				while (!reader.ready()){
					try {
						Thread.sleep(10L);
					} catch (InterruptedException e) {
						reader.close();
						return 1;
					}
				}

				while ((line = reader.readLine()) != null ) {
						this.out.enqueue(line);
						int in;
						String lastLine = "";
						char[] buffer = new char[linesep.length()];
						while (!reader.ready()){
							try {
								Thread.sleep(10L);
							} catch (InterruptedException e) {
								reader.close();
								return 1;
							}
						}
						while ((in = reader.read(buffer)) != -1 ) {
							String bufferValue = String.valueOf(buffer, 0, in);
							lastLine += bufferValue;
							if (bufferValue.equalsIgnoreCase(linesep)){
								break;
							}
						}
						this.out.enqueue(lastLine);
				}
				reader.close();
				
			} catch (IOException e) {
				Configuration.log().warn("Command output interrupted.",e);
				return 1;
			}
			
		return 0;
	}

}
