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
import java.util.concurrent.ConcurrentLinkedQueue;

import com.pieframework.model.Configuration;

public class CommandOutputThread extends Thread implements Callable {

	private ConcurrentLinkedQueue<String> outQueue;

	public CommandOutputThread() {
		this.outQueue = new ConcurrentLinkedQueue<String>();
	}

	public void enqueue(String msg) {
		this.outQueue.offer(msg);
	}

	public int qsize() {
		return this.outQueue.size();
	}

	public String dequeue() {
		return this.outQueue.poll();
	}

	public void run() {
		
		try {
			call();
		} catch (Exception e) {
			Configuration.log().error(e.getMessage(),e);
		}
			
	}
	
	public void processMessages(){
		// process all available messages in the queue
		while (!this.outQueue.isEmpty()) {
			String line=this.dequeue();
			if (line != null && !line.endsWith(System.getProperty("line.separator"))) {
					System.out.println(line);
				} else
					System.out.print(line);
			
		}
	}

	public Boolean isDone() {
		return this.outQueue.isEmpty();
	}

	public void processRemaining() {

		while (!this.outQueue.isEmpty()) {
			String line=outQueue.poll();
			if (line!=null && !line.endsWith( System.getProperty("line.separator"))){				
				System.out.println(line);
			}else
				System.out.print(line);
				
		}
	}

	public ConcurrentLinkedQueue<String> getOutQueue() {
		return outQueue;
	}

	public void setOutQueue(ConcurrentLinkedQueue<String> outQueue) {
		this.outQueue = outQueue;
	}

	public Integer call() throws Exception {
		while (true) {
			// process all available messages in the queue
			this.processMessages();
			try {
				Thread.sleep(10L);
			} catch (InterruptedException e) {
				//Flush remaining messages
				this.processMessages();
				return 0;
			}
		}
	}

}
