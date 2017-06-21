package com.pingunaut.maven.plugin.wicketmessages;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * The Class GenerateXmlMojo generates wicket properties xml files.
 *
 * @author Martin Spielmann
 *
 */
@Mojo(name = "generateXml", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class GenerateXmlMojo extends AbstractWicketMessagesMojo {

    /** The input file. */
    @Parameter(defaultValue = "messages.xlsx", property = "inputFile", required = true)
    private String inputFile;

    @Parameter(defaultValue = "false", property = "removeEmptyEntries", required = true)
    private boolean removeEmptyEntries;
    /*
     * (non-Javadoc)
     *
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException {
        final Map<PathAndLocale, Properties> dataFromExcel = extractDataFromExcel(inputFile);
        writeXml(dataFromExcel);
    }

    /**
     * Extract data from excel.
     *
     * @param inputFile
     *            the input file
     * @return the map
     */
    public Map<PathAndLocale, Properties> extractDataFromExcel(final String inputFile) {
        final Map<PathAndLocale, Properties> map = new HashMap<>();
        Workbook wb;
        try {
            wb = new XSSFWorkbook(Files.newInputStream(Paths.get(inputFile)));
            final Sheet sheet = wb.getSheetAt(0);

            final Iterator<Row> rows = sheet.rowIterator();
            // get locales
            final List<Locale> locales = getLocales(rows.next().cellIterator());

            //
            while (rows.hasNext()) {
                final Row row = rows.next();
                final String path = row.getCell(0).getStringCellValue();
                final String key = row.getCell(1).getStringCellValue();
                for (int i = 0; i < locales.size(); i++) {
                    final Cell cell = row.getCell(i + 3);
                    if (cell != null) {
                        addMessage(path, locales.get(i), key, cell.getStringCellValue(), map);
                    }
                }
            }
        } catch (final IOException e) {
            getLog().error("Error reading excel file", e);
        }
        return map;
    }

    /**
     * Adds the message.
     *
     * @param path
     *            the path
     * @param locale
     *            the locale
     * @param key
     *            the key
     * @param value
     *            the value
     * @param map
     *            the map
     */
    private void addMessage(final String path, final Locale locale, final String key, final String value,
            final Map<PathAndLocale, Properties> map) {
        final Path filePath = buildFilePath(Paths.get(path), locale);
        if (!Files.exists(filePath)) {
            try {
                Files.createFile(filePath);
            } catch (final IOException e) {
                getLog().error("Error creating xml file", e);
            }
        }

        final PathAndLocale pathAndKey = new PathAndLocale(filePath, locale);
        map.putIfAbsent(pathAndKey, new Properties());
        final Properties props = map.get(pathAndKey);
        props.put(key, value);
    }

    /**
     * Builds the file path.
     *
     * @param path
     *            the path
     * @param locale
     *            the locale
     * @return the path
     */
    private Path buildFilePath(final Path path, final Locale locale) {
        // default messages file
        if (DEFAULT_LOCALE.equals(locale)) {
            return path;
        } else {
            final String fileName = FilenameUtils.getBaseName(FilenameUtils.getBaseName(path.getFileName().toString()));
            return Paths.get(path.getParent().toString(),
                    String.format("%s_%s%s", fileName, locale.toString(), fileExtension));
        }
    }

    /**
     * Gets the locales.
     *
     * @param cells
     *            the cells
     * @return the locales
     */
    private List<Locale> getLocales(final Iterator<Cell> cells) {
        final List<Locale> locales = new ArrayList<>();
        // ignore path, key and used column
        cells.next();
        cells.next();
        cells.next();
        while (cells.hasNext()) {
            locales.add(new Locale(cells.next().getStringCellValue()));
        }
        return locales;
    }

    /**
     * Write xml file.
     *
     * @param map
     *            the map
     */
    public void writeXml(final Map<PathAndLocale, Properties> map) {
        map.forEach((pal, props) -> {
            try {
                if (append) {
                    getLog().info("Append to existing file");
                    Properties existingProperties = new Properties();
                    existingProperties.loadFromXML(Files.newInputStream(pal.getPath()));
                    existingProperties.putAll(props);
                    if(removeEmptyEntries){
                        existingProperties = getPropertiesWithoutEmptyEntries(existingProperties);
                    }
                    existingProperties.storeToXML(Files.newOutputStream(pal.getPath()), "", StandardCharsets.UTF_8.toString());
                } else {
                    if(removeEmptyEntries){
                        props = getPropertiesWithoutEmptyEntries(props);
                    }
                    props.storeToXML(Files.newOutputStream(pal.getPath()), "", StandardCharsets.UTF_8.toString());

                }
                getLog().info(String.format("stored properties to %s", pal.getPath().toString()));

            } catch (final IOException e) {
                getLog().error("error while storing properties to xml", e);
            }
        });
    }
    
    /**
     * Gets the properties without empty entries.
     *
     * @param props the props
     * @return the properties without empty entries
     */
    private Properties getPropertiesWithoutEmptyEntries(Properties props) {
        Properties propsWithoutEmptyEntries = new Properties();
        props.forEach((k,v)->{
            if(StringUtils.isNotEmpty(String.valueOf(v))){
                propsWithoutEmptyEntries.put(k, v);
            }
        });
        return propsWithoutEmptyEntries;
    }
}