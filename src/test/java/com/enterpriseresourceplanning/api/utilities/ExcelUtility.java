package com.enterpriseresourceplanning.api.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Apache POI utility for Excel-driven API test data.
 */
public final class ExcelUtility {

	private static final Logger LOG = LogManager.getLogger(ExcelUtility.class);
	private static final DataFormatter FORMATTER = new DataFormatter();

	private ExcelUtility() {
	}

	public static List<Map<String, String>> readSheet(String classpathResource, String sheetName) {
		List<Map<String, String>> rows = new ArrayList<>();
		try (InputStream stream = ExcelUtility.class.getClassLoader().getResourceAsStream(classpathResource)) {
			if (stream == null) {
				throw new IllegalArgumentException("Excel not found: " + classpathResource);
			}
			try (Workbook workbook = new XSSFWorkbook(stream)) {
				Sheet sheet = workbook.getSheet(sheetName);
				if (sheet == null) {
					throw new IllegalArgumentException("Sheet not found: " + sheetName);
				}
				Row header = sheet.getRow(0);
				if (header == null) {
					return rows;
				}
				List<String> headers = readRow(header);
				for (int i = 1; i <= sheet.getLastRowNum(); i++) {
					Row row = sheet.getRow(i);
					if (row == null) {
						continue;
					}
					Map<String, String> data = new HashMap<>();
					for (int col = 0; col < headers.size(); col++) {
						data.put(headers.get(col), readCell(row.getCell(col)));
					}
					rows.add(data);
				}
			}
			LOG.info("Loaded {} rows from {}#{}", rows.size(), classpathResource, sheetName);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read Excel: " + classpathResource, e);
		}
		return rows;
	}

	private static List<String> readRow(Row row) {
		List<String> values = new ArrayList<>();
		for (int col = 0; col < row.getLastCellNum(); col++) {
			values.add(readCell(row.getCell(col)));
		}
		return values;
	}

	private static String readCell(Cell cell) {
		return cell == null ? "" : FORMATTER.formatCellValue(cell).trim();
	}

}
