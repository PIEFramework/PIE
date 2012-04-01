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

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
public class CmdLineOptions {


	@Argument(required = false, index = 0, metaVar="COMMAND",usage = "Command that will be executed for the target component.")
	public String command;

	@Argument(required = false, index = 1, metaVar="TARGET",usage = "Component query string that will locate a target component(s).")
	public String query;
	
	@Option(name = "--input", aliases = { "--i","-i" }, usage = "A list of arguments that will be provided to the command.")
	public String input;

	@Option(name = "--user", aliases = { "--u","-u" }, usage = "User to authenticate.")
	public String user;
	
	@Option(name = "--password", aliases = { "--p","-p" }, usage = "Password to authenticate with.")
	public String password;
	
	@Option(name = "--help", aliases = { "--h", "-?" }, usage = "Shows Usage.")
	public boolean help;
	
	@Option(name = "--version", aliases = { "--v" }, usage = "Shows PIE version details.")
	public boolean version;
	
	@Option(name = "--hush", aliases = { }, usage = "Suppresses sending notifications to campfire.")
	public boolean hush;
	
	@Option(name = "--install", aliases = { }, usage = "Runs the installer which will generate all required directories and default configuration.")
	public boolean install;
}
