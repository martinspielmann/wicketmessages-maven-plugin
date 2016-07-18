package com.pingunaut.maven.plugin.messages;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.pingunaut.maven.plugin.PathAndKey;

public class XlsWriter {
    int rowCounter = 1;

    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet worksheet = workbook.createSheet("Worksheet");
    // index from 0,0... cell A1 is cell(0,0)
    XSSFRow currentRow = worksheet.createRow(0);

    public XlsWriter(Map<PathAndKey, Map<Locale, String>> localeProps) {
        int i = 0;
        List<Locale> locales = localeProps.values().stream().flatMap(m -> m.keySet().stream()).distinct()
                .sorted((o1, o2) -> o1.toString().compareTo(o2.toString()))
                .collect(Collectors.toList());

        // create header
        XSSFCell cellA1 = currentRow.createCell(i++);
        cellA1.setCellValue("path");
        XSSFCell cellB1 = currentRow.createCell(i++);
        cellB1.setCellValue("key");
        // create locale columns
        for (Locale locale : locales) {
            XSSFCell cell = currentRow.createCell(i++);
            cell.setCellValue(locale.toString());
        }


        for (Entry<PathAndKey, Map<Locale, String>> e : localeProps.entrySet()) {
            currentRow = worksheet.createRow(rowCounter);

            XSSFCell currentPathCell = currentRow.createCell(0);
            currentPathCell.setCellValue(e.getKey().getPath().toString());

            XSSFCell currentFileCell = currentRow.createCell(1);
            currentFileCell.setCellValue(e.getKey().getKey().toString());

            int j = 2;
            for (Locale locale : locales) {
                XSSFCell cell = currentRow.createCell(j++);
                cell.setCellValue(e.getValue().get(locale));
            }

            rowCounter = rowCounter + 1;
        }
    }

    public void writeToFile() {
        try (FileOutputStream fileOut = new FileOutputStream("messages.xlsx")) {
            workbook.write(fileOut);
            fileOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
