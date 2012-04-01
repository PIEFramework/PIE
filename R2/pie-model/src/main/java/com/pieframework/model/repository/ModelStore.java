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
package com.pieframework.model.repository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableException;
import org.mvel2.MVEL;
import org.mvel2.templates.TemplateRuntime;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.pieframework.model.Component;
import com.pieframework.model.Configuration;
import com.pieframework.model.PIEID;
import com.pieframework.model.Request;
import com.pieframework.model.operations.Operations;
import com.pieframework.model.query.ComponentQueryBuilder;
import com.pieframework.model.repositories.Repositories;
import com.pieframework.model.resources.Resources;
import com.pieframework.model.system.Model;
import com.pieframework.model.system.System;
import com.pieframework.model.util.DefaultSerializer;
import com.pieframework.model.util.SimpleSerializerException;

public class ModelStore {

	private static Model model = null;

	private static final String PATH_DELIMITER = "/";
	private static final String NAME_VALUE_DELIMITER = ":";
	private static final String SYSTEM_FILE_NAME = "system.xml";
	// private static final String RESOURCES_FILE_NAME = "resources.xml";
	// private static final String OPERATIONS_FILE_NAME = "operations.xml";
	private static final String REPOSITORIES_FILE_NAME = "repositories.xml";

	public static Model loadModel(Configuration config, Request request,
			File fileRoot, String instance) throws IOException,
			NestableException {
		if (fileRoot == null || !fileRoot.isDirectory()) {
			throw new IllegalArgumentException(
					"Cannot build Model from given directory.");
		}

		try {
			System system = getSystem(new File(fileRoot + PATH_DELIMITER
					+ SYSTEM_FILE_NAME));

			Repositories repos = getRepositories(new File(fileRoot
					+ PATH_DELIMITER + REPOSITORIES_FILE_NAME));

			model = new Model().withSystem(system).withRepositories(repos)
					.withModelDirectory(fileRoot.getPath()).withConfiguration(
							config).withRequest(request);

			File instanceFile = new File(fileRoot + PATH_DELIMITER
					+ "instances" + PATH_DELIMITER + instance + ".txt");
			if (instanceFile != null && instanceFile.canRead()) {
				applyInstanceOverrides(model, instanceFile);
			}

			// replace @{expression} tags in model
			model = evaluateTemplateExpressions(model);

			// relink the system
			linkComponents(model.getSystem());

			// ////////Experiment////

			// /////////////////////

		} catch (SimpleSerializerException e) {
			Configuration.log().error(
					"Could not parse Model. " + e.getMessage());
			throw new NestableException("Could not parse Model.", e);
		} catch (ParserConfigurationException e) {
			Configuration.log().error(
					"Could not parse Model. " + e.getMessage());
			throw new NestableException("Could not parse Model.", e);
		} catch (SAXException e) {
			Configuration.log().error(
					"Could not parse Model. " + e.getMessage());
			throw new NestableException("Could not parse Model.", e);
		} catch (TransformerException e) {
			Configuration.log().error(
					"Could not parse Model. " + e.getMessage());
			throw new NestableException("Could not parse Model.", e);
		}
		return model;
	}

	protected static String componentToString(Object component)
			throws SimpleSerializerException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DefaultSerializer.write(component, out);
		return out.toString();
	}

	protected static String evalAsTemplate(Object object)
			throws SimpleSerializerException {
		String template = componentToString(object);
		template = StringUtils.replace(template, "&quot;", "\"");
		return (String) TemplateRuntime.eval(template, model);
	}

	public static Model evaluateTemplateExpressions(Model model)
			throws SimpleSerializerException, IOException {

		System system = model.getSystem();
		String systemTemplate = evalAsTemplate(system);
		System newSystem = DefaultSerializer.read(System.class, IOUtils
				.toInputStream(systemTemplate));

		// Resources resources = new Resources();
		// resources.setResources(model.getResources());
		// String resourcesTemplate = evalAsTemplate(resources);
		// Resources newResources = DefaultSerializer.read(Resources.class,
		// IOUtils.toInputStream(resourcesTemplate));

		// Operations ops = new Operations();
		// ops.setOperations(model.getOperations());
		// String opsTemplate = evalAsTemplate(ops);
		// ops = DefaultSerializer.read(Operations.class, IOUtils
		// .toInputStream(opsTemplate));

		Repositories repos = new Repositories();
		repos.setRepositories(model.getRepositories());
		String repoTemplate = evalAsTemplate(repos);
		repos = DefaultSerializer.read(Repositories.class, IOUtils
				.toInputStream(repoTemplate));

		// TODO should probably clone model
		Model returnModel = new Model().withSystem(newSystem)
				.withModelDirectory(model.getModelDirectory())
				.withRepositories(repos).withConfiguration(
						model.getConfiguration()).withRequest(
						model.getRequest());

		return returnModel;
	}

	public static Model getCurrentModel() {
		if (model != null) {
			return model;
		} else {
			throw new RuntimeException("Model is null. Please load.");
		}
	}

	public static List<ExpressionEntry> parseInstanceFile(File file)
			throws IOException {
		List<ExpressionEntry> retList = new ArrayList<ExpressionEntry>();
		LineIterator it = FileUtils.lineIterator(file);
		try {
			while (it.hasNext()) {
				String line = it.nextLine();
				if (StringUtils.startsWith(line, "#")) {
					// ignore comments
				} else {
					String expression = StringUtils.trimToEmpty(StringUtils
							.substringBefore(line, "="));
					String value = StringUtils.trimToEmpty(StringUtils
							.substringAfter(line, "="));
					if (StringUtils.isNotEmpty(expression)
							&& StringUtils.isNotEmpty(value)) {
						retList.add(new ExpressionEntry().withExpression(
								expression).withValue(value));
					}
				}
			}
		} finally {
			it.close();
		}
		return retList;
	}

	private static void applyInstanceOverrides(Model model, File instanceFile)
			throws IOException {
		List<ExpressionEntry> list = parseInstanceFile(instanceFile);
		for (ExpressionEntry e : list) {
			MVEL.setProperty(model, e.getExpression(), e.getValue());
		}
	}

	/**
	 * @param xmlFile
	 * @param writer
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerException
	 */
	protected static void processXIncludes(File xmlFile, Writer writer)
			throws ParserConfigurationException, SAXException, IOException,
			TransformerException {

		final InputStream xml = new FileInputStream(xmlFile);

		// This sets the base where XIncludes are resolved
		InputSource i = new InputSource(xml);
		i.setSystemId(xmlFile.toURI().toString());

		// Configure Document Builder
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setXIncludeAware(true);
		factory.setNamespaceAware(true);
		factory.setFeature(
				"http://apache.org/xml/features/xinclude/fixup-base-uris",
				false);
		DocumentBuilder docBuilder = factory.newDocumentBuilder();

		if (!docBuilder.isXIncludeAware()) {
			throw new IllegalStateException();
		}

		// Parse the InputSource
		Document doc = docBuilder.parse(i);

		// output
		Source source = new DOMSource(doc);
		Result result = new StreamResult(writer);
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.transform(source, result);
	}

	/**
	 * @param file
	 * @return
	 * @throws SimpleSerializerException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws TransformerException
	 */
	public static System getSystem(File file) throws SimpleSerializerException,
			IOException, ParserConfigurationException, SAXException,
			TransformerException {

		if (file == null || !file.canRead()) {
			throw new IllegalArgumentException(
					"Cannot build Model from given file.");
		}

		StringWriter writer = new StringWriter();
		processXIncludes(file, writer);

		// Deserialize a System File
		System system;
		system = DefaultSerializer.read(System.class, IOUtils
				.toInputStream(writer.toString()));

		linkComponents(system);

		return system;
	}

	private static void linkComponents(Component c) {
		if (c.getChildren() != null) {
			for (String id : c.getChildren().keySet()) {
				c.getChildren().get(id).setParent(c);
				linkComponents(c.getChildren().get(id));
			}
		}
	}

	public static Resources getResources(File file)
			throws SimpleSerializerException, IOException,
			ParserConfigurationException, SAXException, TransformerException {

		if (file == null || !file.canRead()) {
			throw new IllegalArgumentException(
					"Cannot build Model from given file.");
		}

		StringWriter writer = new StringWriter();
		processXIncludes(file, writer);

		// Deserialize a System File
		Resources resources;
		resources = DefaultSerializer.read(
				com.pieframework.model.resources.Resources.class, IOUtils
						.toInputStream(writer.toString()));

		return resources;
	}

	public static Operations getOperations(File file)
			throws SimpleSerializerException, IOException,
			ParserConfigurationException, SAXException, TransformerException {

		if (file == null || !file.canRead()) {
			throw new IllegalArgumentException(
					"Cannot build Model from given file.");
		}

		StringWriter writer = new StringWriter();
		processXIncludes(file, writer);

		// Deserialize a System File
		Operations operations = DefaultSerializer.read(
				com.pieframework.model.operations.Operations.class, IOUtils
						.toInputStream(writer.toString()));

		return operations;
	}

	public static Repositories getRepositories(File file)
			throws SimpleSerializerException, IOException,
			ParserConfigurationException, SAXException, TransformerException {

		if (file == null || !file.canRead()) {
			throw new IllegalArgumentException(
					"Cannot build Model from given file.");
		}

		StringWriter writer = new StringWriter();
		processXIncludes(file, writer);

		// Deserialize a System File
		Repositories repos = DefaultSerializer.read(Repositories.class, IOUtils
				.toInputStream(writer.toString()));

		return repos;
	}

	protected static String parse(String query) {
		ComponentQueryBuilder builder = new ComponentQueryBuilder();

		Scanner scanner = new Scanner(query);
		scanner.useDelimiter(PATH_DELIMITER);

		while (scanner.hasNext()) {
			final String[] nameValue = scanner.next().split(
					NAME_VALUE_DELIMITER);
			if (nameValue.length == 0 || nameValue.length > 2) {
				throw new IllegalArgumentException("Illegal format");
			}
			builder.add(nameValue[0]);
		}
		return builder.build();
	}

	public static Component find(Model model, String query) {
		String expression = parse(query);
		return (Component) MVEL.getProperty(expression, model);
	}

	public static String getModelDir(String query, Configuration config) {
		// Load the model based on the query
		System tmp = new System();
		String modelString = config.getCurrentModels();
		String modelName = PIEID.getId(query, tmp);
		String modelIdVersion = modelName + File.separatorChar
				+ config.getCurrentModelVersion(modelName, modelString);
		String modelDir = FilenameUtils.separatorsToSystem(config.getData())
				+ modelIdVersion;

		return modelDir;
	}
}
