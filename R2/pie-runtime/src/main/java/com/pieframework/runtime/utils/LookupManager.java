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
package com.pieframework.runtime.utils;

import java.util.regex.Pattern;


public class LookupManager {
	enum Lookups {
	    cloudlet, cloudletinstance, os, role,roleid, roleinstance, services, securitygroups, awsaccount
	  };
	  
	  /*
	
	
	public static String lookupEnvVaraibles(String lookup,ServiceComponent sc){
		Registry reg=sc.getParent().getParent().getParent().getRegistry();
		
		if (lookup!=null && !lookup.equalsIgnoreCase("")){
			Pattern matcher = Pattern.compile("\\$\\{");
			String[] vars = matcher.split(lookup);
			String rootEnv=reg.getRootPath()+sc.getParent().getParent().getParent().getId()+"/"+"environment";
			if (vars.length>1){
				lookup=vars[1].substring(0, vars[1].length()-1);
				String[] tmp=lookup.split("\\.");
				if (tmp.length>1){
					if (tmp[0].equalsIgnoreCase("environment")){
						try {
							//System.out.println(lookup+" "+rootEnv+ " "+tmp[1]);
							String var=tmp[1];
							for (int i=2;i<tmp.length;i++){
								var+="."+tmp[i];
							}
							lookup=reg.getNodeData(rootEnv+"/"+var);
						} catch (Exception e) {
							lookup="";
						}
					}
				}
			}
		}
		
		Configuration.log().debug(lookup);
		return lookup;
	}
	
	public static String lookupVariables(String inputs,ServiceComponent sc,Status parentStatus){
		
		String resolvedInputs=inputs;
		Pattern matcher = Pattern.compile("\\$\\{");
		if (inputs != null) {
			String[] vars = matcher.split(inputs);

			if (vars.length > 0) {
				resolvedInputs = vars[0];
				String resolvedVars = "";

				// Skip the first element as they are literals
				for (int c = 1; c < vars.length; c++) {

					// The variable string may contain more than just the variable we want to lookup so use space as a delimiter
					String[] var1 = vars[c].split(" ");
					parentStatus.addMessage("debug", "number of variables:"+var1.length);
					if (var1.length > 0) {
						String[] readyVar=var1[0].split("\\}");
						//Lookup the actual variable
						if (readyVar.length > 0){
							parentStatus.addMessage("debug", "number of tokens:"+readyVar.length);
							resolvedVars += lookup(readyVar[0], sc,parentStatus);
							//Append anything after the } if it exists
							if (readyVar.length > 1){
								resolvedVars +=readyVar[1];
							}
						}
						
						//Append any remaining literals
						for (int i = 1; i < var1.length; i++) {
							resolvedVars += " "+var1[i] ;
						}
					}
				}
				resolvedInputs += resolvedVars;
			}

		}
		
		Configuration.log().trace(resolvedInputs);
		parentStatus.addMessage("debug", "input :'"+inputs+"' result:'"+resolvedInputs+"'");
		return resolvedInputs;
			
	}

	
	private static String lookup(String var,ServiceComponent sc,Status parentStatus){
		String result="";
		
		String[] parts=var.split("\\.");
		if (parts.length>1){
			if (parts[0].equalsIgnoreCase("model")){
				
				Lookups lookup=Lookups.valueOf(parts[1]);
				//cloudlet, cloudletinstance, os, role, roleinstance, services, securitygroups, awsaccount
				switch (lookup) {
					case cloudlet:
						result+=sc.getParent().getParent().getParent().getParent().getCsData().getId()+parts[1].replace(Lookups.cloudlet.toString(),"");
					break;
					case cloudletinstance:
						result+=sc.getParent().getParent().getParent().getId()+parts[1].replace(Lookups.cloudletinstance.toString(),"");
					break;
					case os:
						result+=sc.getParent().getParent().getImageData().getOs()+parts[1].replace(Lookups.os.toString(),"");
					break;
					case role:
						result+=sc.getParent().getParent().getRoleData().getId()+parts[1].replace(Lookups.role.toString(),"");
					break;
					case roleid:
						result+=sc.getParent().getParent().getId()+parts[1].replace(Lookups.roleid.toString(),"");
					break;
					case roleinstance:
						result+=sc.getParent().getId()+parts[1].replace(Lookups.roleinstance.toString(),"");
					break;
					case services:
						String tmp="";
						for (String s:sc.getParent().getRoleData().getServicesDeployment().getServiceRefList())
							tmp+=s+" ";
						result+=tmp+parts[1].replace(Lookups.services.toString(),"");
					break;
					case securitygroups:
						String tmp1="";
						
						for (String s:sc.getParent().getRoleData().getSecurityGroups().getSecurityGroupMap().keySet())
							tmp1+=s+" ";
						
						result+=tmp1+parts[1].replace(Lookups.securitygroups.toString(),"");
					break;
					case awsaccount:
						result+=sc.getParent().getParent().getParent().getParent().getAData().getAccount().getOwner()+parts[1].replace(Lookups.awsaccount.toString(),"");					
					break;
					
					default: result+="noop";
				}
			}else if (parts[0].equalsIgnoreCase("environment")){
				result+=LookupManager.lookupEnvVaraibles(var, sc);
			}else{
				result=var;
			}
			
		}else{
			result=var;
		}
		
		parentStatus.addMessage("debug", "lookup :'"+var+"' '"+result+"'");
		Configuration.log().trace(result);
		return result;
	}
	*/
}
