package com.pingunaut.maven.plugin;

import java.io.IOException;
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

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Mojo(name = "generateXml", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class GenerateXmlMojo extends AbstractWicketMessagesMojo {

    @Parameter(defaultValue = "messages.xlsx", property = "messagesFile", required = true)
    private String messagesFile;

    private final Map<PathAndLocale, Properties> messagesMap = new HashMap<>();

    @Override
    public void execute() throws MojoExecutionException {
        Workbook wb;
        try {
            wb = new XSSFWorkbook(Files.newInputStream(Paths.get(messagesFile)));
            Sheet sheet = wb.getSheetAt(0);

            Iterator<Row> rows = sheet.rowIterator();
            //get locales
            List<Locale> locales = getLocales(rows.next().cellIterator());

            //
            while (rows.hasNext()) {
                Row row = rows.next();
                String path = row.getCell(0).getStringCellValue();
                String key = row.getCell(1).getStringCellValue();
                for (int i = 0; i < locales.size(); i++) {
                    Cell cell = row.getCell(i+2);
                    if (cell != null) {
                        addMessage(path, locales.get(i), key, cell.getStringCellValue());
                    }
                }
            }
        } catch (IOException e) {
            getLog().error("Error reading excel file", e);
        }

        writeXml();
    }

    private void addMessage(String path, Locale locale, String key, String value) {
        Path filePath = buildFilePath(Paths.get(path), locale);
        if (!Files.exists(filePath)) {
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                getLog().error("Error creating xml file", e);
            }
        }

        PathAndLocale pathAndKey = new PathAndLocale(filePath, locale);
        messagesMap.putIfAbsent(pathAndKey, new Properties());
        Properties props = messagesMap.get(pathAndKey);
        props.put(key, value);
    }

    private Path buildFilePath(Path path, Locale locale) {
        //default messages file
        if (DEFAULT_LOCALE.equals(locale)) {
            return path;
        } else {
            String fileName = FilenameUtils.getBaseName(FilenameUtils.getBaseName(path.getFileName().toString()));
            return Paths.get(path.getParent().toString(), String.format("%s_%s%s", fileName, locale.toString(), fileExtension));
        }
    }

    private List<Locale> getLocales(Iterator<Cell> cells) {
        List<Locale> locales = new ArrayList<>();
        //ignore path and key column
        cells.next();
        cells.next();
        while (cells.hasNext()) {
            locales.add(new Locale(cells.next().getStringCellValue()));
        }
        return locales;
    }

    public void writeXml() {
        messagesMap.forEach((k, v) -> {
            try {
                v.storeToXML(Files.newOutputStream(k.getPath()), "", Charsets.UTF_8.toString());
                getLog().info(String.format("stored properties to %s", k.getPath().toString()));

            } catch (IOException e) {
                getLog().error("error while storing properties to xml", e);
            }
        });
    }
}