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
package com.pieframework.model.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;

public class DefaultSerializer {

	private static final String XML_PROLOG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	public static <T> T read(Class<? extends T> type, InputStream inputstream)
			throws IOException, SimpleSerializerException {
		Serializer serializer = new Persister();
		// TODO return null?
		T p = null;
		try {
			p = serializer.read(type, inputstream);
		} catch (Exception e) {
			throw new SimpleSerializerException(e);
		} finally {
			inputstream.close();
		}
		return p;
	}

	public static void write(Object p, OutputStream stream)
			throws SimpleSerializerException {
		Format format = new Format(2, XML_PROLOG);
		Serializer serializer = new Persister(format);
		try {
			serializer.write(p, stream);
		} catch (Exception e) {
			throw new SimpleSerializerException(e);
		}
	}
}
