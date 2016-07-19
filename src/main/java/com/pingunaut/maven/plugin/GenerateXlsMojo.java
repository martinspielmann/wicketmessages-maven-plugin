package com.pingunaut.maven.plugin;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Mojo(name = "generateXls", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class GenerateXlsMojo extends AbstractWicketMessagesMojo {

	@Parameter(defaultValue = "messages.xlsx", property = "outputFile", required = true)
    private String outputFile;
	
	private final Map<PathAndKey, Map<Locale, String>> localeProps = new HashMap<>();

	@Override
	public void execute() throws MojoExecutionException {
		List<Path> files = new ListWicketMessagesMojo().listFiles(basedir, fileExtension);

		files.forEach(file -> {
			String fileName = FilenameUtils.getBaseName(FilenameUtils.getBaseName(file.getFileName().toString()));
			int indexOfFirstUnderscore = fileName.indexOf('_');

			loadProperties(file).forEach((k, v) -> {
				if (-1 == indexOfFirstUnderscore) {
					// default file
					addProperties(file, k, DEFAULT_LOCALE, v);
				} else {
					// other locale files
					String baseFileName = fileName.substring(0, indexOfFirstUnderscore);
					String localeName = fileName.substring(indexOfFirstUnderscore + 1, fileName.length());
					addProperties(Paths.get(file.getParent().toString(), baseFileName + fileExtension), k,
							new Locale(localeName), v);
				}
			});
		});

		writeToFile(buildWorkbook(localeProps));
	}

	private Properties loadProperties(Path file) {
		Properties properties = new Properties();
		try {
			properties.loadFromXML(Files.newInputStream(file));
		} catch (IOException e) {
			getLog().error("Error while loading the properites", e);
		}
		return properties;
	}

	private void addProperties(Path file, Object key, Locale locale, Object value) {
		PathAndKey pathAndKey = new PathAndKey(basePath().relativize(file), key);
		localeProps.putIfAbsent(pathAndKey, new HashMap<>());
		Map<Locale, String> localeMap = localeProps.get(pathAndKey);
		localeMap.putIfAbsent(locale, value == null ? "" : value.toString());
	}

	private Workbook buildWorkbook(Map<PathAndKey, Map<Locale, String>> localeProps) {
		int rowCounter = 0;

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet worksheet = workbook.createSheet("Worksheet");
		XSSFRow currentRow = worksheet.createRow(rowCounter++);

		int cellCounter = 0;
		List<Locale> locales = findAllLocales(localeProps);

		// create header
		currentRow.createCell(cellCounter++).setCellValue("path");
		currentRow.createCell(cellCounter++).setCellValue("key");
		for (Locale locale : locales) {
			currentRow.createCell(cellCounter++).setCellValue(locale.toString());
		}

		//fill in values
		for (Entry<PathAndKey, Map<Locale, String>> e : localeProps.entrySet()) {
			currentRow = worksheet.createRow(rowCounter++);
			cellCounter = 0;
			currentRow.createCell(cellCounter++).setCellValue(e.getKey().getPath().toString());
			currentRow.createCell(cellCounter++).setCellValue(e.getKey().getKey().toString());

			for (Locale locale : locales) {
				currentRow.createCell(cellCounter++).setCellValue(e.getValue().get(locale));
			}
		}
		
		return workbook;
	}

	private List<Locale> findAllLocales(Map<PathAndKey, Map<Locale, String>> localeProps) {
		return localeProps.values().stream().flatMap(m -> m.keySet().stream()).distinct()
				.sorted((o1, o2) -> o1.toString().compareTo(o2.toString())).collect(Collectors.toList());
	}
	
	public void writeToFile(Workbook workbook) {
        try (FileOutputStream fileOut = new FileOutputStream(outputFile)) {
            workbook.write(fileOut);
            fileOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
