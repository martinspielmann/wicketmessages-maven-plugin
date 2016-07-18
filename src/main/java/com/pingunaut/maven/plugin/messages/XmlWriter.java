package com.pingunaut.maven.plugin.messages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;


public class XmlWriter {


    public Properties parseXlsFile(String xlsFileName) {
        try {
            Properties properties = new Properties();
            FileInputStream fileInputStream = new FileInputStream(xlsFileName);
            HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);
            HSSFSheet worksheet = workbook.getSheetAt(0);
            HSSFRow headerRow = worksheet.getRow(0);
            HSSFRow workingRow = null;
            HSSFRow currentRow = null;
            HSSFRow rowAfterCurrentRow = null;
            int columnCounter = 4;
            int rowCounter = 1;

            HSSFCell currentHeaderCell = headerRow.getCell(columnCounter);
            HSSFCell currentKeyCell = null;
            String currentHeaderCellValue = null;
            String currentTranlastionCellValue = null;


            //gehe durch die headerrow und hole dir die spalten mit _landesk�rzel
            if (currentHeaderCell != null) {
                currentHeaderCellValue = currentHeaderCell.getStringCellValue();
            }
            while (currentHeaderCellValue != null && (!currentHeaderCellValue.equals("")) && currentHeaderCellValue.startsWith("_")) {

                currentRow = worksheet.getRow(rowCounter);
                rowAfterCurrentRow = worksheet.getRow(rowCounter + 1);

                while (currentRow != null || rowAfterCurrentRow != null) {

                    workingRow = null;

                    if (currentRow != null) {
                        if (currentRow.getCell(2) != null) {
                            workingRow = currentRow;
                        }
                    } else if (rowAfterCurrentRow != null) {
                        if (rowAfterCurrentRow.getCell(2) != null) {
                            workingRow = rowAfterCurrentRow;
                            rowCounter = rowCounter + 1;
                        }
                    }

                    if (workingRow != null) {
                        //hole den Key
                        currentKeyCell = workingRow.getCell(2);
                        if (currentKeyCell != null) {
                            //hole den �bersetzten Text
                            if (workingRow.getCell(columnCounter) != null) {
                                if (workingRow.getCell(columnCounter).getStringCellValue() != null) {
                                    currentTranlastionCellValue = workingRow.getCell(columnCounter).getStringCellValue();
                                }
                            } else {
                                currentTranlastionCellValue = "";
                            }

                            //only properties with values are written
                            if (!currentTranlastionCellValue.equals("") && currentTranlastionCellValue != null) {
                                properties.setProperty(currentKeyCell.getStringCellValue(), currentTranlastionCellValue);
                            }
                        }
                    }
                    rowCounter = rowCounter + 1;
                    currentRow = worksheet.getRow(rowCounter);
                    rowAfterCurrentRow = worksheet.getRow(rowCounter + 1);
                }

                this.writeXml(currentHeaderCellValue, properties);
                properties.clear();
                columnCounter = columnCounter + 1;
                rowCounter = 1;
                currentHeaderCell = headerRow.getCell(columnCounter);

                //den n�chsten header cellvalue holen
                if (currentHeaderCell != null) {
                    currentHeaderCellValue = currentHeaderCell.getStringCellValue();
                } else {
                    currentHeaderCellValue = null;
                }
            }

            return properties;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void writeXml(String fileNamePostfix, Properties properties) {

        try {
            File file = new File("WicketApplication" + fileNamePostfix + ".properties.xml");
            FileOutputStream fileOut = new FileOutputStream(file);
            properties.storeToXML(fileOut, "");
            fileOut.close();

            FilePostProcessor fpp = new FilePostProcessor();
            fpp.doPostProcessing(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

