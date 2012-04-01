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
import java.io.IOException;
import java.util.jar.JarFile;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.pieframework.model.Component;
import com.pieframework.model.Configuration;
import com.pieframework.model.Operator;
import com.pieframework.model.PIEID;
import com.pieframework.model.Request;
import com.pieframework.model.Status;
import com.pieframework.model.repository.ModelStore;
import com.pieframework.model.system.Model;
import com.pieframework.model.system.System;
import com.pieframework.runtime.utils.StringUtils;

public class run {

	public static void main(String[] args) {

		java.lang.System.out.println("Welcome to PIE!");

		// Parse commandline options
		CmdLineOptions options = new CmdLineOptions();
		CmdLineParser parser = parseArgs(options, args);
		if (parser != null) {
			if (options.install) {
				Installer.run(options.input);
			} else if (options.version) {
				getVersion();
			} else if (options.help && options.command == null) {
				parser.printUsage(java.lang.System.out);
			} else {
				// Load configuration and proceed running normal commands
				String confLocation = Request.findAttribute(options.input,
						"conf");
				Configuration conf = null;
				if (!StringUtils.empty(confLocation)) {
					conf = new Configuration(confLocation);
				} else {
					conf = Configuration.getStaticInstance();
				}

				if (conf == null) {
					Configuration.log().error("Failed loading pie.conf");
					throw new RuntimeException("Failed loading pie.conf");
				}

				// Load external libs
				ExtClassLoader loader = new ExtClassLoader();
				loader.load(conf.getLib());

				Status status = new Status(conf.getVerbosity(), null,
						options.command, "run");

				if (!StringUtils.empty(options.command, options.query)) {
					Request r = new Request(options.command, options.query,
							options.input, status);

					try {
						String instanceId = PIEID.getInstanceId(r.getQuery(),
								new System());
						String modelDir = ModelStore.getModelDir(r.getQuery(),
								conf);
						java.lang.System.out.println("Loading model from:"
								+ modelDir);
						ModelStore.loadModel(conf, r, new File(modelDir),
								instanceId);
						Model model = ModelStore.getCurrentModel();

						if (model != null) {
							// Load the appropriate component and operator
							Component c = ModelStore.find(model, r.getQuery());
							Operator o = (Operator) c.getCommands().get(
									r.getCommand());

							if (options.help) {
								o.help(c, r);
							} else {
								if (o != null) {
									// TODO Eval the operation
									// o.validate(c, r);

									// Execute the operation
									Status opStatus = o.process(c, r);

									// TODO add notification to external system

								} else {
									status.addMessage("error", "Operation:"
											+ r.getCommand()
											+ " is not defined for component:"
											+ r.getQuery());
								}
							}
						} else {
							status.addMessage("error", "Model is null");
						}
					} catch (Exception e) {
						status.addMessage("error",
								"Failed executing the command:"
										+ r.getCommand());
						Configuration.log().error(
								"Failed executing the command:"
										+ r.getCommand() + " for target:"
										+ r.getQuery(), e);
					}
				} else {
					printUsage();
					parser.printUsage(java.lang.System.out);
				}
			}
		}
	}

	public static CmdLineParser parseArgs(CmdLineOptions options, String[] args) {
		CmdLineParser parser = new CmdLineParser(options);

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			java.lang.System.err.println(e.getMessage());
			parser.printUsage(java.lang.System.err);
			parser = null;
		}

		return parser;
	}

	public static boolean printUsage(CmdLineOptions options, String[] args) {
		CmdLineParser parser = new CmdLineParser(options);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {

		}
		printUsage();
		parser.printUsage(java.lang.System.out);
		return true;
	}

	public static void printUsage() {
		java.lang.System.out.println(" Usage\t\t\t  : java -jar pie.jar <command> <query> -i <input> -u <user> -p <password>");
	}

	private static void getVersion() {
		try {
			JarFile jf = new JarFile(new File(run.class.getProtectionDomain()
					.getCodeSource()
					.getLocation()
					.getPath()));
			if (jf != null) {

				for (Object attr : jf.getManifest()
						.getMainAttributes()
						.keySet()) {
					if (attr.toString().equalsIgnoreCase("Built-By")
							|| attr.toString().equalsIgnoreCase("Build-Date")
							|| attr.toString().equalsIgnoreCase("Version")) {
						java.lang.System.out.println(attr.toString()
								+ "="
								+ jf.getManifest()
										.getMainAttributes()
										.get(attr)
										.toString());
					}
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
