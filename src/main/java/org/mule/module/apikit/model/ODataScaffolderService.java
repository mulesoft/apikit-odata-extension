/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.mule.module.apikit.spi.ScaffolderService;
import org.mule.tools.apikit.Scaffolder;

public class ODataScaffolderService implements ScaffolderService {

  private final static List<String> apiExtensions = Arrays.asList(".yaml", ".raml", ".yml");
  private final static List<String> appExtensions = Arrays.asList(".xml");

  private static final String LIBRARIES_FOLDER = "/libraries";
  
  private static final String LIBRARIES_ODATA_RAML = "/libraries/odataLibrary.raml";
  
	public void executeScaffolder(List<File> ramlFiles, File appDir, File domainDir, String muleVersion) {
		List<String> ramlsWithExtensionEnabledPaths = processDataModelFiles(ramlFiles);
		List<String> ramlFilePaths = retrieveFilePaths(ramlFiles, apiExtensions);
		ramlFilePaths.addAll(ramlsWithExtensionEnabledPaths);
		List<String> muleXmlFiles = retrieveFilePaths(appDir, appExtensions);
		SystemStreamLog log = new SystemStreamLog();
		String domain = null;
		if (domainDir != null) {
			List<String> domainFiles = retrieveFilePaths(domainDir, appExtensions);
			if (domainFiles.size() > 0) {
				domain = domainFiles.get(0);
				if (domainFiles.size() > 1) {
					log.info("There is more than one domain file inside of the domain folder. The domain: " + domain + " will be used.");
				}
			}
		}
		Scaffolder scaffolder;
		try {
			scaffolder = Scaffolder.createScaffolder(log, appDir, ramlFilePaths, muleXmlFiles, domain, muleVersion, ramlsWithExtensionEnabledPaths);
		} catch (Exception e) {
			throw new RuntimeException("Error executing scaffolder", e);
		}
		scaffolder.run();
	}

	private List<String> retrieveFilePaths(File dir, final List<String> extensions) {
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("File " + dir.getName() + " must be a directory");
		}
		return retrieveFilePaths(new ArrayList<File>(Arrays.asList(dir.listFiles())), extensions);
	}

	private List<String> retrieveFilePaths(List<File> files, List<String> extensions) {
		List<String> filePaths = new ArrayList<String>();
		if (files != null) {
			for (File file : files) {
				if (containsValidExtension(file, extensions)) {
					filePaths.add(file.getAbsolutePath());
				}
			}
		}
		return filePaths;
	}

	private boolean isODataModel(File file) {
		try {
			String path = file.getCanonicalPath();
			return path.contains("api" + File.separator + "odata.raml");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean containsValidExtension(File file, List<String> extensions) {
		for (String extension : extensions) {
			if (file.getName().toLowerCase().endsWith(extension)) {
				return true;
			}
		}
		return false;
	}

	private List<String> processDataModelFiles(List<File> files) {
		List<String> ramlFilePaths = new ArrayList<String>();
		if (files != null) {
			for (File file : files) {
				if (isODataModel(file)) {
					ramlFilePaths.addAll(generateRamlFilesFromODataModel(file));
				}
			}
		}
		return ramlFilePaths;
	}

	private List<String> generateRamlFilesFromODataModel(File model) {
		
		List<String> ramlFiles = new ArrayList<String>();
		ramlFiles.add(generateApiRamlFromODataModel(model).getAbsolutePath());
		ramlFiles.addAll(copyRamlTemplateFiles(model));
		
		return ramlFiles;
	}

	private File generateApiRamlFromODataModel(File model) {
		RamlGenerator ramlGenerator = new RamlGenerator();
		File raml = null;

		try {
			String ramlContents = ramlGenerator.generate(new FileInputStream(model));
			String path = model.getCanonicalPath().replace("odata.raml", "api.raml");
			raml = FileUtils.stringToFile(path, ramlContents);
		} catch (Exception e) {
			e.printStackTrace();
			SystemStreamLog log = new SystemStreamLog();
			log.error("Error: " + e.getMessage());
			throw new RuntimeException("Error: " + e.getMessage());
		}

		return raml;
	}

	private List<String> copyRamlTemplateFiles(File model) {
		List<String> ramlFiles = new ArrayList<String>();

		try {
			FileUtils.createFolder(model.getParentFile().getAbsolutePath() + LIBRARIES_FOLDER);
			ramlFiles.add(FileUtils.exportResource(LIBRARIES_ODATA_RAML, model.getParentFile().getAbsolutePath() + LIBRARIES_ODATA_RAML));

		} catch (Exception e) {
			e.printStackTrace();
			SystemStreamLog log = new SystemStreamLog();
			log.error("Error: " + e.getMessage());
			throw new RuntimeException("Error: " + e.getMessage());
		}

		return ramlFiles;
	}
}
