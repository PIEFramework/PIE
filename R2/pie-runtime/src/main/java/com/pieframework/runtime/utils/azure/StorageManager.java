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
package com.pieframework.runtime.utils.azure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.DigestInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.soyatec.windowsazure.authenticate.StorageAccountInfo;
import org.soyatec.windowsazure.blob.BlobStorageClient;
import org.soyatec.windowsazure.blob.IBlobContainer;
import org.soyatec.windowsazure.blob.IBlobContents;
import org.soyatec.windowsazure.blob.IBlobProperties;
import org.soyatec.windowsazure.blob.IBlockBlob;
import org.soyatec.windowsazure.blob.IContainerAccessControl;
import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.internal.util.NameValueCollection;
import org.soyatec.windowsazure.management.ServiceManagement;
import org.soyatec.windowsazure.management.StorageAccountKey;

import com.pieframework.model.Configuration;
import com.pieframework.runtime.utils.TimeUtils;



public class StorageManager {
	public enum Access {Public, Private;}
	private BlobStorageClient storage;
	
	public StorageManager(){
		
	}
	
	public StorageManager(ServiceManagement sm, String storageName) {
		Boolean usePathStyleURIs=false;
		String storageKey=getStorageKey(sm,storageName);
		StorageAccountInfo sa=new StorageAccountInfo(URI.create("http://blob.core.windows.net"),usePathStyleURIs,storageName,storageKey);
		BlobStorageClient storage = BlobStorageClient.create(sa);
		this.setStorage(storage);
	}

	public static String getStorageKey(ServiceManagement sm,String storageAccountName){
		StorageAccountKey sk=null;
		
		try {
			sk = sm.getStorageAccountKeys(storageAccountName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String storageKey="";
		if (sk!=null){
			storageKey=sk.getPrimaryKey();
		}
		
		return storageKey;
	}
	
	public Map<String,String> uploadFiles(String containerName,String accessControl,Boolean overwrite,String ... filesToUpload){
		Map<String,String> result=new HashMap<String,String>();
		
		IBlobContainer bc=this.createContainer(containerName, accessControl);
		String path=bc.getProperties().getUri().toString();
		
		for (String fileToUpload:filesToUpload){
			IBlockBlob blob=this.createBlob(bc, fileToUpload,overwrite);
			String info="URI:"+path+"/"+blob.getProperties().getName()+" crc32:"+blob.getProperties().getMetadata().getSingleValue("crc32");
			result.put(fileToUpload,info);	 
		}
		
		return result;
	}
	
	public String uploadFile(File fileToUpload,String containerName,String accessControl,Boolean overwrite){
		String result="";
		
		if (this.getStorage()!=null){
			
			try {
				//Create container
				IBlobContainer bc=this.createContainer(containerName, accessControl);
				result+="URI:"+bc.getProperties().getUri();
				
				//Create blobs
				IBlockBlob blob=this.createBlob(bc, fileToUpload.getPath(),overwrite);
				result+="/"+blob.getProperties().getName()+" crc32:"+blob.getProperties().getMetadata().getSingleValue("crc32")+" md5:"+blob.getProperties().getContentMD5();
				
			} catch (StorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}else{
			
		}
		
		return result;
	}

	public IBlobContainer createContainer(String containerName,String accessControl){
		IBlobContainer bc=null;
		//Check if container does not exist create it
		try{
			if (!this.getStorage().isContainerExist(containerName)){
				IContainerAccessControl access=null;
				if (accessControl.equalsIgnoreCase("public")){
					 access=IContainerAccessControl.Public;
				}else{
					 access=IContainerAccessControl.Private;
				}
				
				//Create container
				bc=this.getStorage().createContainer(containerName, null,access);
				Configuration.log().info("Created container:"+bc.getName());
			}else{
				bc=this.getStorage().getBlobContainer(containerName);
			}
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bc;
	}
	
	public IBlockBlob createBlob(IBlobContainer bc,String fileToUpload,Boolean overwrite){
		IBlockBlob blob=null;
		File upload=new File(fileToUpload);
		String fileName=upload.getName();
		try{
			
			String checksum=this.calculateChecksum(upload)+"";
			IBlobContents blobContents = BlobStorageClient.createBlobContents(upload);
			IBlobProperties blobProperties = BlobStorageClient.createBlobProperties(fileName);
			
			NameValueCollection metadata=new NameValueCollection();
			metadata.put("crc32",checksum );
			blobProperties.setMetadata(metadata);
			
			if (!bc.isBlobExist(blobProperties.getName())){
				blob=bc.createBlockBlob(blobProperties, blobContents);
				
			}else if (overwrite){
				blob=bc.updateBlockBlob(blobProperties, blobContents);
			}else{
				blob=bc.getBlockBlobReference(blobProperties.getName());
			}
			
			//fileStream.close();
			
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return blob;
	}

	public BlobStorageClient getStorage() {
		return storage;
	}

	public void setStorage(BlobStorageClient storage) {
		this.storage = storage;
	}

	public Long calculateChecksum(File file){
		
		Long result=0L;
		try {
			Checksum checksum;
			FileInputStream is=new FileInputStream(file);
			byte data[]=new byte[2048];
			while(is.read(data) >= 0) {}
			checksum = new CRC32();
			checksum.update(data, 0, data.length);
			is.close();
			result=checksum.getValue();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public String getLatestDeployContainer(File pack,File cfg) {
		String result="";
		Long checksumPack=this.calculateChecksum(pack);
		Long checksumCfg=this.calculateChecksum(cfg);
		String cspackName=pack.getName();
		String cfgName=cfg.getName();
		String timestamp=TimeUtils.getToday();
		
		
		for (IBlobContainer bc:this.storage.listBlobContainers()){
			if (bc.getName().startsWith("deploy"+timestamp)){
				IBlockBlob blobPack=bc.getBlockBlobReference(cspackName);
				IBlockBlob blobCfg=bc.getBlockBlobReference(cfgName);
				
				String crcPack="";
				String crcCfg="";
				if (blobPack.getProperties().getMetadata().containsKey("crc32")){
					crcPack=blobPack.getProperties().getMetadata().getSingleValue("crc32");
				}	
				
				if (blobCfg.getProperties().getMetadata().containsKey("crc32")){
					crcCfg=blobCfg.getProperties().getMetadata().getSingleValue("crc32");
				}	
				
				if (String.valueOf(checksumPack).equalsIgnoreCase(crcPack) && String.valueOf(checksumCfg).equalsIgnoreCase(crcCfg)){
					result=bc.getName();
					break;
				}
			}
		}
		
		return result;
	}
	
}
