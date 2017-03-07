package com.pingunaut.maven.plugin.wicketmessages;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * The Class GenerateXlsMojo generates an excel file from wicket messages
 * properties files.
 *
 * @author Martin Spielmann
 */
@Mojo(name = "generateXls", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class GenerateXlsMojo extends AbstractWicketMessagesMojo {

	/** The output file. */
	@Parameter(defaultValue = "messages.xlsx", property = "outputFile", required = true)
	private String outputFile;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		List<Path> files = new ListWicketMessagesMojo().listFiles(basedir, fileExtension);

		Map<PathAndKey, Map<Locale, String>> map = new HashMap<>();

		files.forEach(file -> {
			String fileName = FilenameUtils.getBaseName(FilenameUtils.getBaseName(file.getFileName().toString()));
			int indexOfFirstUnderscore = fileName.indexOf('_');

			loadProperties(file).forEach((k, v) -> {
				if (-1 == indexOfFirstUnderscore) {
					// default file
					addProperties(file, k, DEFAULT_LOCALE, v, map, fileExtension);
				} else {
					// other locale files
					String baseFileName = fileName.substring(0, indexOfFirstUnderscore);
					String localeName = fileName.substring(indexOfFirstUnderscore + 1, fileName.length());
					addProperties(Paths.get(file.getParent().toString(), baseFileName + fileExtension), k,
							new Locale(localeName), v, map, fileExtension);
				}
			});
		});

		Workbook workbook = append ? updateWorkbook(map) : createNewWorkbook(map);
		writeToFile(workbook);
	}

	/**
	 * Load properties.
	 *
	 * @param file
	 *            the file
	 * @return the properties
	 */
	private Properties loadProperties(final Path file) {
		Properties properties = new Properties();
		try {
			properties.loadFromXML(Files.newInputStream(file));
		} catch (IOException e) {
			getLog().error("Error while loading the properites", e);
		}
		return properties;
	}

	/**
	 * Adds the properties.
	 *
	 * @param file
	 *            the file
	 * @param key
	 *            the key
	 * @param locale
	 *            the locale
	 * @param value
	 *            the value
	 * @param map
	 *            the map
	 */
	private void addProperties(final Path file, final Object key, final Locale locale, final Object value,
			final Map<PathAndKey, Map<Locale, String>> map, String extension) {
		PathAndKey pathAndKey = new PathAndKey(basePath().relativize(file), key, isUsed(key, extension));
		map.putIfAbsent(pathAndKey, new HashMap<>());
		Map<Locale, String> localeMap = map.get(pathAndKey);
		localeMap.putIfAbsent(locale, value == null ? "" : value.toString());
	}

	private boolean isUsed(final Object key, String extension) {
		return GrepFiles.fileContainsRecursive(Paths.get(basedir), (String) key, getLog(), extension);
	}

	/**
	 * Creates a new workbook.
	 *
	 * @param map
	 *            the map of path, key, locales and properties
	 * @return the workbook
	 */
	private Workbook createNewWorkbook(final Map<PathAndKey, Map<Locale, String>> map) {
		int rowCounter = 0;

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet worksheet = workbook.createSheet("Worksheet");
		XSSFRow currentRow = worksheet.createRow(rowCounter++);

		int cellCounter = 0;
		List<Locale> locales = findAllLocales(map);

		// create header
		currentRow.createCell(cellCounter++).setCellValue("path");
		currentRow.createCell(cellCounter++).setCellValue("key");
		currentRow.createCell(cellCounter++).setCellValue("used");

		for (Locale locale : locales) {
			currentRow.createCell(cellCounter++).setCellValue(locale.toString());
		}

		// fill in values
		for (Entry<PathAndKey, Map<Locale, String>> e : map.entrySet()) {
			currentRow = worksheet.createRow(rowCounter++);
			fillNewRow(currentRow, e, locales);
		}

		return workbook;
	}

	/**
	 * Find all locales.
	 *
	 * @param xlsData
	 *            the locale props
	 * @return the list
	 */
	private List<Locale> findAllLocales(final Map<PathAndKey, Map<Locale, String>> xlsData) {
		return xlsData.values().stream().flatMap(m -> m.keySet().stream()).distinct()
				.sorted((o1, o2) -> o1.toString().compareTo(o2.toString())).collect(Collectors.toList());
	}

	/**
	 * Write to file.
	 *
	 * @param workbook
	 *            the workbook
	 */
	public void writeToFile(final Workbook workbook) {
		try (FileOutputStream fileOut = new FileOutputStream(outputFile)) {
			workbook.write(fileOut);
			fileOut.flush();
		} catch (IOException e) {
			getLog().error("Error while writing Excel file", e);
		}
	}

	/**
	 * Update workbook.
	 *
	 * @param map
	 *            the map
	 * @return the workbook
	 * @throws MojoFailureException
	 *             the mojo failure exception
	 */
	private Workbook updateWorkbook(final Map<PathAndKey, Map<Locale, String>> map) throws MojoFailureException {
		Workbook wb = null;
		try {
			wb = new XSSFWorkbook(Files.newInputStream(Paths.get(outputFile)));
			Sheet sheet = wb.getSheetAt(0);
			List<Locale> locales = findAllLocales(map);
			checkLocalesFromData(sheet, locales);

			// fill in values
			for (Entry<PathAndKey, Map<Locale, String>> e : map.entrySet()) {
				// check if path & key already existing within excel
				Row existingRow = findRow(sheet, e.getKey());
				if (existingRow != null) {
					int cellCounter = 3;
					for (Locale locale : locales) {
						existingRow.createCell(cellCounter).setCellValue(e.getValue().get(locale));
						cellCounter++;
					}
				} else {
					// create new row at the tables end
					Row newRow = sheet.createRow(sheet.getLastRowNum() + 1);
					fillNewRow(newRow, e, locales);
				}
			}
		} catch (IOException e) {
			getLog().error("Error reading excel file", e);
		}

		return wb;
	}

	/**
	 * Fill new row.
	 *
	 * @param newRow
	 *            the new row
	 * @param entry
	 *            the entry
	 * @param locales
	 *            the locales
	 */
	private void fillNewRow(final Row newRow, final Entry<PathAndKey, Map<Locale, String>> entry, final List<Locale> locales) {
		int cellCounter = 0;
		newRow.createCell(cellCounter++).setCellValue(entry.getKey().getPath().toString());
		newRow.createCell(cellCounter++).setCellValue(entry.getKey().getKey().toString());
		newRow.createCell(cellCounter++).setCellValue(entry.getKey().isUsed());

		for (Locale locale : locales) {
			newRow.createCell(cellCounter++).setCellValue(entry.getValue().get(locale));
		}

	}

	/**
	 * Check locales from data.
	 *
	 * @param sheet
	 *            the sheet
	 * @param locales
	 *            the locales
	 * @throws MojoFailureException
	 *             the mojo failure exception
	 */
	private void checkLocalesFromData(final Sheet sheet, final List<Locale> locales) throws MojoFailureException {
		List<Locale> existingLocales = new ArrayList<>();
		Iterator<Cell> cells = sheet.getRow(0).cellIterator();
		skipPathAndKeyCol(cells);
		cells.forEachRemaining(cell -> {
			existingLocales.add(new Locale(cell.getStringCellValue()));
		});

		if (!locales.equals(existingLocales)) {
			throw new MojoFailureException(String.format(
					"Existing Excel file does not contain the same locales like the provided properties. \n"
							+ "Please update your Excel file accordingly before you proceed. \n"
							+ "Locales in Excel: %s \n" + "Locales in properties: %s \n",
					Arrays.toString(existingLocales.toArray()), Arrays.toString(locales.toArray())));
		}
	}

	/**
	 * Find row.
	 *
	 * @param sheet
	 *            the sheet
	 * @param pac
	 *            the pac
	 * @return the row
	 */
	public static Row findRow(final Sheet sheet, final PathAndKey pac) {
		for (Row row : sheet) {
			if (row.getCell(0).getStringCellValue().equals(pac.getPath().toString())
					&& row.getCell(1).getStringCellValue().equals(pac.getKey())) {
				return row;
			}
		}
		return null;
	}

	/**
	 * Skip path and key col.
	 *
	 * @param cells
	 *            the cells
	 */
	private void skipPathAndKeyCol(final Iterator<Cell> cells) {
		cells.next();
		cells.next();
		cells.next();
	}
}
