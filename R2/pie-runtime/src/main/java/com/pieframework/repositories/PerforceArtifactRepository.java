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
package com.pieframework.repositories;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.simpleframework.xml.core.Commit;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.ConfigException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.NoSuchObjectException;
import com.perforce.p4java.exception.RequestException;
import com.perforce.p4java.exception.ResourceException;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.IServerInfo;
import com.perforce.p4java.server.ServerFactory;
import com.pieframework.model.Status;
import com.pieframework.model.repositories.ArtifactRepository;
import com.pieframework.model.repositories.Repository;
import com.pieframework.model.resources.Artifact;
import com.pieframework.runtime.utils.StringUtils;

public class PerforceArtifactRepository extends Repository implements
		ArtifactRepository {

	private String uri;
	private String user;
	private String client;
	private IServer server;
	private IClient p4client;

	public enum Types {
		FILESPEC, LABEL
	};

	@Commit
	private void commit() {
		this.setUri(getProps().get("uri"));
		this.setUser(getProps().get("username"));
		this.setClient(getProps().get("client"));
		try {
			this.setServer(ServerFactory.getServer(uri, null));
		} catch (ConnectionException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (NoSuchObjectException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (ConfigException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (ResourceException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public PerforceArtifactRepository() {
		super();
	}

	public Boolean connect() {
		IServerInfo info;
		try {
			this.getServer().connect();
			info = this.getServer().getServerInfo();
			if (info != null) {
				this.getServer().setUserName(this.getUser());
				this.getServer().login("");
				p4client = this.getServer().getClient(this.getClient());

				if (p4client.getDescription() != null) {
					this.getServer().setCurrentClient(p4client);
				}

				return true;
			} else
				return false;
		} catch (ConnectionException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (RequestException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (AccessException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (ConfigException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public Boolean disconnect() {
		if (this.getServer() != null) {
			try {
				this.getServer().disconnect();
				return true;
			} catch (ConnectionException e) {
				throw new RuntimeException(e.getMessage(), e);
			} catch (AccessException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		} else
			return false;
	}

	public List<IFileSpec> diffFiles(String path) {
		List<IFileSpec> filespec = FileSpecBuilder.makeFileSpecList(path);
		List<IFileSpec> diffspec = null;
		try {
			diffspec = this.getServer().getCurrentClient().getDiffFiles(
					filespec, 10000, true, false, false, false, false, true,
					false);

		} catch (ConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return diffspec;
	}

	public Map<String, String> download(Artifact artifact, Status status,
			Boolean forceUpdate) {
		String artifactAddress = artifact.getArtifactAddress();
		String localPath = artifact.getLocalPath();

		if (this.idFormat(artifact).equals(Types.FILESPEC) && !StringUtils.empty(artifactAddress)) {
			return this.downloadFileSpec(artifactAddress, localPath, status,
					forceUpdate);
		}

		return null;
	}

	public Map<String, String> downloadFileSpec(String artifactAddress,
			String localPath, Status status, Boolean forceUpdate) {
		Map<String, String> localFileList = new HashMap<String, String>();

		this.connect();
		try {

			// Create a fileSpec with the Path.
			List<IFileSpec> fsList = FileSpecBuilder
					.makeFileSpecList(artifactAddress);
			// Get the full list from the Server Depot
			List<IFileSpec> fileList = this.getServer().getDepotFiles(fsList,
					false);
			List<IFileSpec> validSyncList = FileSpecBuilder
					.getValidFileSpecs(fileList);
			List<IFileSpec> invalidSyncList = FileSpecBuilder
					.getInvalidFileSpecs(fileList);
			List<IFileSpec> syncList = this.getServer().getCurrentClient()
					.sync(validSyncList, forceUpdate, false, false, false);

			if (invalidSyncList != null) {
				String errors = "";
				for (IFileSpec fs : invalidSyncList) {
					errors += "error: " + artifactAddress
							+ " cannot be synced beacuse it is in state:"
							+ fs.getOpStatus()
							+ ".Verify the file exists in p4.";
				}

				if (!errors.equalsIgnoreCase("")) {
					// status.addMessage("error",errors);
				}

			}

			if (syncList != null) {
				// status.addMessage("debug","p4 syncing "+artifactAddress);
				System.out.println("p4 syncing " + artifactAddress);
				for (IFileSpec fileSpec : syncList) {
					if (fileSpec != null) {
						if (fileSpec.getOpStatus() == FileSpecOpStatus.VALID) {
							localFileList.put(FilenameUtils
									.separatorsToSystem(fileSpec
											.getDepotPathString()), fileSpec
									.getClientPathString());
							System.out.println("info: sync completed for "
									+ fileSpec.getDepotPathString());
						} else {
							CharSequence cs = "up-to-date.";
							if (!fileSpec.getStatusMessage().contains(cs)
									&& status != null) {
								status.addMessage("error", ""
										// System.out.println(""
										+ fileSpec.getOpStatus()
										+ " sync failed for " + artifactAddress
										+ " with error message "
										+ fileSpec.getStatusMessage());
							} else {
								String pathFromMessage = getPathFromStatusMessage(fileSpec
										.getStatusMessage());
								localFileList.put(pathFromMessage, this
										.getServer().getCurrentClient()
										.getRoot()
										+ pathFromMessage);
							}
						}
					}
				}
				// status.addMessage("info",localFileList.size()+
				// " files synced from p4 to local client.");
			}
		} catch (ConnectionException e1) {
			throw new RuntimeException(e1.getMessage(), e1);
		} catch (RequestException e1) {
			throw new RuntimeException(e1.getMessage(), e1);
		} catch (AccessException e1) {
			throw new RuntimeException(e1.getMessage(), e1);
		}

		this.disconnect();
		return localFileList;
	}

	public Types idFormat(Artifact artifact) {
		if (artifact.getType() != null) {
			if (artifact.getType().equalsIgnoreCase("filespec")) {
				return Types.FILESPEC;
			} else if (artifact.getType().equalsIgnoreCase("label")) {
				return Types.LABEL;
			}
		}

		return null;
	}

	public static String getPathFromStatusMessage(String message) {
		String result = message.substring(1, message.lastIndexOf("#")).replace(
				'/', File.separatorChar);
		return result;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public IServer getServer() {
		return server;
	}

	public void setServer(IServer server) {
		this.server = server;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

}
