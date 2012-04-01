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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class Zipper {

	private List<File> files;
	private File rootDir;
	
	public Zipper(String dir){
		File folder = new File(dir);
		List<File> fileList=new ArrayList<File>();
        this.setFiles(fileList);
        this.setRootDir(folder);
        
        gatherFiles(folder, this.getFiles());
	}

	public Zipper() {
		// TODO Auto-generated constructor stub
	}

	private void gatherFiles(File folder, List<File> list) {
	        folder.setReadOnly();
	        File[] files = folder.listFiles();
	        for(int j = 0; j < files.length; j++) {
	            if(files[j].isDirectory()){
	            	gatherFiles(files[j], list);	
	            }else{
	            	list.add(files[j]);
	            }
	        }
	}
	
	public List<File> getFiles() {
		return files;
	}
	public void setFiles(List<File> files) {
		this.files = files;
	}
	
	public static void unzip(String zipFile,String toDir) throws ZipException,IOException {

		int BUFFER = 2048;
		File file = new File(zipFile);

		ZipFile zip = new ZipFile(file);
		String newPath = toDir;

		File toDirectory=new  File(newPath);
		if (!toDirectory.exists()){
			toDirectory.mkdir();
		}
		Enumeration zipFileEntries = zip.entries();

		// Process each entry
		while (zipFileEntries.hasMoreElements()) {
		// grab a zip file entry
			ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();

			String currentEntry = entry.getName();
		    File destFile = new File(newPath, FilenameUtils.separatorsToSystem(currentEntry));
		    //System.out.println(currentEntry);
		    File destinationParent = destFile.getParentFile();

		    // create the parent directory structure if needed
		    destinationParent.mkdirs();
		    if (!entry.isDirectory()) {
	            BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
	            int currentByte;
	            // establish buffer for writing file
	            byte data[] = new byte[BUFFER];

	            // write the current file to disk
	            FileOutputStream fos = new FileOutputStream(destFile);
	            BufferedOutputStream dest = new BufferedOutputStream(fos,
	                            BUFFER);

	            // read and write until last byte is encountered
	            while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
	                    dest.write(data, 0, currentByte);
	            }
	            dest.flush();
	            dest.close();
	            is.close();
		    }
		}
		zip.close();
    
}

	
	public static void zip (String zipFile,Map<String,File> flist){
		byte[] buf = new byte[1024]; 
		try { 
		// Create the ZIP file 
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile)); 
		
		// Compress the files 
		for (String url:flist.keySet()) { 
			FileInputStream in = new FileInputStream(flist.get(url).getPath()); 
			// Add ZIP entry to output stream. Zip entry should be relative
			out.putNextEntry(new ZipEntry(url));
			// Transfer bytes from the file to the ZIP file 
			int len; 
			while ((len = in.read(buf)) > 0) { 
				out.write(buf, 0, len); 
			} 
			// Complete the entry 
			out.closeEntry(); 
			in.close(); 
		} 
		
		// Complete the ZIP file 
		out.close(); 
		} catch (Exception e) { 
			throw new RuntimeException("Encountered errors zipping file "+zipFile,e);
		} 
	}

	public File getRootDir() {
		return rootDir;
	}

	public void setRootDir(File rootDir) {
		
		//Append a trailing slash if the directory does not contain one
		if(rootDir.getPath().endsWith(File.separator))
			this.rootDir = rootDir;
		else{
			String newDirPath=rootDir.getPath()+File.separator;
			this.rootDir=new File(newDirPath);
		}
	}

	public String relativePath(File rootDir,File fileToZip){
		String relPath="";
		int startIndex=rootDir.getPath().length();
		relPath=fileToZip.getPath().substring(startIndex);
		return relPath;
	}
	
	
	
}
