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
import org.apache.log4j.Level;

public class CompileLogLevel extends Level{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 887243543488138659L;
	public static final CompileLogLevel COMPILE = new CompileLogLevel(15000, "COMPILE",0);

	protected CompileLogLevel(int level, String levelStr, int syslogEquivalent) {
		super(level, levelStr, syslogEquivalent);
	}
	
	public static CompileLogLevel toLevel(int val, Level defaultLevel)
    {
		return COMPILE;
    }

    public static CompileLogLevel toLevel(String sArg, Level defaultLevel)
    {
        return COMPILE;
    }


}

