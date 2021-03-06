package com.maxsoft.testngtestresultsanalyzer;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static com.maxsoft.testngtestresultsanalyzer.Constants.*;

/**
 * Project Name    : maxsoft-test-results-analyzer
 * Developer       : Osanda Deshan
 * Version         : 1.0.0
 * Date            : 7/2/2021
 * Time            : 7:45 PM
 * Description     : This is the Excel file generator class that is used to generate the Excel file
 **/

public class ExcelFileGenerator {

    private static final Logger logger = Logger.getLogger(ExcelFileGenerator.class.getName());
    private static final XSSFWorkbook workbook = new XSSFWorkbook();
    private static final CellStyle cellStyle = workbook.createCellStyle();
    private static final CellStyle headerRowCellStyle = workbook.createCellStyle();
    private static final Font headerRowFont = workbook.createFont();

    public static void generateExcel(String allTestsWorkSheetName, Map<String, Object[]> allTestsExcelDataMap,
                                     String failedTestsWorkSheetName, Map<String, Object[]> failedTestsExcelDataMap,
                                     String skippedTestsWorkSheetName, Map<String, Object[]> skippedTestsExcelDataMap,
                                     String filePath) {

        // Prepare data object for each worksheet to write to the Excel file
        prepareExcelDataMapToWriteToTheExcelFile(allTestsWorkSheetName, allTestsExcelDataMap);
        prepareExcelDataMapToWriteToTheExcelFile(failedTestsWorkSheetName, failedTestsExcelDataMap);
        prepareExcelDataMapToWriteToTheExcelFile(skippedTestsWorkSheetName, skippedTestsExcelDataMap);

        autoSizeColumns();

        // Prepare bar charts and pie charts for each worksheet to write to the Excel file
        ChartHelper chartHelper = new ChartHelper(workbook);
        chartHelper.generateCharts(FAILURE_ANALYSIS, failedTestsExcelDataMap, FAILED_TEST_ANALYSIS_BAR_CHART_NAME,
                FAILED_TEST_ANALYSIS_PIE_CHART_NAME, FAILED_TEST_COUNT, FAILED_REASON);
        chartHelper.generateCharts(SKIPPED_ANALYSIS, skippedTestsExcelDataMap, SKIPPED_TEST_ANALYSIS_BAR_CHART_NAME,
                SKIPPED_TEST_ANALYSIS_PIE_CHART_NAME, SKIPPED_TEST_COUNT, SKIPPED_REASON);

        // Write the workbook as an Excel file
        try {
            File excelFile = new File(filePath);
            final boolean isParentFileCreationSuccessful = excelFile.getParentFile().mkdirs();
            logger.info("Is test analysis reports directory exists? " + !isParentFileCreationSuccessful);
            FileOutputStream out = new FileOutputStream(excelFile);
            workbook.write(out);
            out.close();
            logger.info("Excel file has successfully generated at '" + filePath + "'");
        } catch (IOException e) {
            logger.severe("Excel file generation failed. \n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void prepareExcelDataMapToWriteToTheExcelFile(String workSheetName, Map<String, Object[]> excelDataMap) {

        if (excelDataMap.size() > 1) {

            // Set header row font and cell style
            headerRowFont.setBold(true);
            headerRowCellStyle.setFont(headerRowFont);
            headerRowCellStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerRowCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerRowCellStyle.setBorderBottom(BorderStyle.MEDIUM);
            headerRowCellStyle.setBorderTop(BorderStyle.MEDIUM);
            headerRowCellStyle.setBorderRight(BorderStyle.MEDIUM);
            headerRowCellStyle.setBorderLeft(BorderStyle.MEDIUM);

            // Set other rows' cell style
            cellStyle.setBorderBottom(BorderStyle.MEDIUM);
            cellStyle.setBorderTop(BorderStyle.MEDIUM);
            cellStyle.setBorderRight(BorderStyle.MEDIUM);
            cellStyle.setBorderLeft(BorderStyle.MEDIUM);
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.TOP);

            XSSFSheet workSheet = workbook.createSheet(workSheetName);
            int rowId = 0;

            for (String key : excelDataMap.keySet()) {
                XSSFRow row = workSheet.createRow(rowId++);
                Object[] objectArr = excelDataMap.get(key);
                int cellId = 0;

                for (Object obj : objectArr) {
                    XSSFCell cell = row.createCell(cellId++);
                    if (isNaturalNumber((String) obj)) {
                        cell.setCellValue(Integer.parseInt((String) obj));
                    } else {
                        cell.setCellValue((String) obj);
                    }
                    if (key.equals("1")) {
                        cell.setCellStyle(headerRowCellStyle);
                    } else {
                        cell.setCellStyle(cellStyle);
                    }
                }
            }
        }
    }

    private static void autoSizeColumns() {
        int numberOfSheets = ((Workbook) ExcelFileGenerator.workbook).getNumberOfSheets();
        IntStream.range(0, numberOfSheets).mapToObj(((Workbook) ExcelFileGenerator.workbook)::getSheetAt)
                .filter(sheet -> sheet.getPhysicalNumberOfRows() > 0).forEach(sheet -> {
                    Row row = sheet.getRow(sheet.getFirstRowNum());
                    Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        int columnIndex = cell.getColumnIndex();
                        sheet.autoSizeColumn(columnIndex);
                        int currentColumnWidth = sheet.getColumnWidth(columnIndex);
                        sheet.setColumnWidth(columnIndex, Math.min(currentColumnWidth, 33000));
                    }
                });
    }

    private static boolean isNaturalNumber(String input) {
        return input != null && Pattern.compile("^\\d+$").matcher(input).matches();
    }
}
