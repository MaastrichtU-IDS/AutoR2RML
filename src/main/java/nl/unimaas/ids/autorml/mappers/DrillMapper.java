package nl.unimaas.ids.autorml.mappers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.text.CaseUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import nl.unimaas.ids.autorml.AutoR2RML;
import nl.unimaas.ids.util.PrefixPrintWriter;

public class DrillMapper extends AbstractMapper implements MapperInterface {
	final static List<String> acceptedFileTypes = Arrays.asList(new String[] { "csv", "tsv", "psv","xlsx" });

	public DrillMapper(String jdbcUrl, String userName, String passWord, String baseUri, String graphUri) throws SQLException, ClassNotFoundException {
		super(jdbcUrl, userName, passWord, baseUri, graphUri);
		Class.forName("org.apache.drill.jdbc.Driver");
		connection = DriverManager.getConnection(jdbcUrl, userName, passWord);
	}


	public void generateMapping(PrintStream ps, boolean recursive, String path) throws Exception {

		if (path.endsWith("/"))
			path = path.substring(0, path.length() - 1);

		List<String> filepaths = getFilesRecursivelyAsList(connection, path, recursive);

		int count = 1;

		generateNamespaces(ps);

		for (String filepath : filepaths) {
			if (!new File(filepath).getName().startsWith("~")) {
				AutoR2RML.logger.debug("Analyzing: " + filepath);

				// If this is an xlsx
				if (filepath.endsWith(".xlsx")) {
					ArrayList<String> fileSheets = xlsxToTSV(new File(filepath));
					for (String fileSheet : fileSheets) {
						AutoR2RML.logger.debug("Analyzing excel sheet: " + fileSheet);
						count = generateMappingForFile(ps, count, fileSheet);
					}
				} else {
					count = generateMappingForFile(ps, count, filepath);
				}
			}
		}
	}

	private int generateMappingForFile(PrintStream ps, int count, String filepath) throws Exception {
		String[] columns = getColumnNames(filepath);
		printFirstThreeLines(filepath, ps);

		String table = "dfs.root.`" + filepath + "`";

		generateMappingForTable(table, columns, ps, ("Mapping" + count++));

		// Generate generic columns name (Column1)
		for (int i = 0; i < columns.length; i++) {
			columns[i] = "Column" + (i + 1);
		}
		//generateMappingForTable(table, columns, ps, ("Mapping" + count++), "# ");
		return count;
	}

	/**
	 * Requires an XLSX file and it will return a list of files each representing an excel sheet in TSV format
	 * @param xlsxFile the Excel file
	 * @return List of excel sheet files in TSV format
	 * @throws IOException when the excel file is in an odd format e.g. the ~$<fileName> format.
	 */
	private ArrayList<String> xlsxToTSV(File xlsxFile) throws IOException {
		ArrayList<String> fileSheets = new ArrayList<>();

		// A Special XLSX file that is created when opening the file and is "hidden" but not detected as such
		if (xlsxFile.getName().startsWith("~$"))
			return fileSheets;

		AutoR2RML.logger.error("XLSX file detected: " + xlsxFile.getAbsolutePath() + ". Converting it to TSV...");
		Workbook wb = WorkbookFactory.create(xlsxFile);
		Iterator<Sheet> sheetIterator = wb.sheetIterator();


		// TODO skip excel sheets starting with #?
		while (sheetIterator.hasNext()) {
			StringBuilder data = new StringBuilder();
			Sheet sheet = sheetIterator.next();
			String sheetName = sheet.getSheetName();

			if (!sheetName.startsWith("#")) {
				// TODO write after each line to the file or if x size is reached
				File outputFile = new File(xlsxFile + ".sheet_" + sheetName + ".tsv");
				BufferedWriter bwr = new BufferedWriter(new FileWriter(outputFile));
				fileSheets.add(outputFile.getAbsolutePath());

				boolean header = true;
                AutoR2RML.logger.info("Parsing sheet: " + sheet.getSheetName());
				for (Row row : sheet) {
                    if (row.getCell(0) != null && !row.getCell(0).getStringCellValue().startsWith("#")) {
						String rowData = "";
						if (row.getLastCellNum() != -1) {
							for (int j = 0; j < row.getLastCellNum(); j++) {
								Cell cell = row.getCell(j);
								// Cannot use standard cell iterator as it skips blank cells
								if (cell == null) {
									rowData += "" + "\t";
								} else {
									switch (cell.getCellType()) {
										case BOOLEAN:
											rowData += cell.getBooleanCellValue() + "\t";
											break;
										case NUMERIC:
											rowData += cell.getNumericCellValue() + "\t";
											break;
										case STRING:
											rowData += cell.getStringCellValue() + "\t";
											break;
										case BLANK:
											rowData += "" + "\t";
											break;
										default:
											rowData += cell + "\t";
									}
								}
							}
							String check = rowData.trim();
							if (check.length() > 0) {
								// Add first column with original excel name
								if (header) {
									rowData = "FileOrigin\t" + rowData;
									header = false;
								} else {
									rowData = xlsxFile.getName() + "\t" + rowData;
								}
								rowData += "\n";
								data.append(rowData);

								// Debug printing TODO enable logger
							/*if (rowData.trim().length() > 100)
								System.err.println(row.getLastCellNum() + "\t" + rowData.trim().substring(1,100) + "...");
							else
								System.err.println(row.getLastCellNum() + "\t" + rowData.trim());*/

								// Write per row to file
								bwr.write(data.toString());
								data = new StringBuilder();
							}
						}
					}
				}
				bwr.close();
			}
		}
		return fileSheets;
	}


	@SuppressWarnings("resource")
	private void printFirstThreeLines(String filePath, PrintStream ps) throws SQLException {
		Statement st = connection.createStatement();
		String sql = "select * from dfs.root.`" + filePath + "` limit 3";

		PrintWriter pw = new PrefixPrintWriter(ps, "# ");

		ResultSet rs = st.executeQuery(sql);
		while (rs.next()) {
			pw.println(rs.getString(1));
		}
		pw.flush();
	}


	private String[] getColumnNames(String filePath) throws SQLException {
		Statement st = connection.createStatement();
		String sql = "select * from dfs.root.`" + filePath + "` limit 1";

		ResultSet rs = st.executeQuery(sql);

		String line;
		if (rs.next())
			line = rs.getString(1);
		else
			throw new InvalidParameterException("File \"" + filePath + "\" seems to be empty" );
//		AutoR2RML.logger.info(line);
		return line.substring(2, line.length() - 2).split("\",\"");
	}

	private List<String> getFilesRecursivelyAsList(Connection connection, String path, boolean recursive)
			throws Exception {
		List<String> ret = new ArrayList<>();

		Statement st = connection.createStatement();
		String sql = "show files in dfs.root.`" + path + "`";

		ResultSet rs = st.executeQuery(sql);

		String fileName;
		String filePath;
		boolean isDirectory;
		// TODO: do it more properly. Stackoverflow question
		// If the user pass directly a file
		if (path.contains(".")
				&& acceptedFileTypes.contains(path.substring(path.lastIndexOf(".") + 1))) {
			ret.add(path);
		} else {
			while (rs.next()) {
				fileName = rs.getString(1);
				isDirectory = rs.getBoolean(2);

				filePath = path + "/" + fileName;

				if (isDirectory && recursive)
					ret.addAll(getFilesRecursivelyAsList(connection, filePath, true));
				else if (fileName.contains(".") && acceptedFileTypes.contains(fileName.substring(fileName.lastIndexOf(".") + 1)))
					ret.add(filePath);
			}
			rs.close();
		}
		return ret;
	}

	@Override
	public String getColumnName(String column) {
		// Remove all parenthesis and capitalize first letter
		column = column.replaceAll("[()]", " ");
		column = column.replaceAll("(\\\\r|\\\\n)", "");
		return CaseUtils.toCamelCase(column, true, new char[] { '-' });
	}

	@Override
	public String getSqlForRowNum() {
		return "row_number() over (partition by filename) as " + ROW_NUM_NAME;
	}

	@Override
	public String getSqlForColumn(String column, int index) {
		// Avoid generating triples with empty values
		return "NULLIF(trim(columns[" + index + "]), '') as `" + getColumnName(column) + "`";
	}

}
