package nl.unimaas.ids.autorml.mappers;

import java.io.*;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import oadd.org.apache.commons.collections.iterators.ArrayListIterator;
import org.apache.commons.text.CaseUtils;

import nl.unimaas.ids.util.PrefixPrintWriter;
import org.apache.poi.ss.usermodel.*;

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

		List<String> filePaths = getFilesRecursivelyAsList(connection, path, recursive);

		int count = 1;
		
		generateNamespaces(ps);
		
		for (String filePath : filePaths) {
			System.err.println("Analyzing: " + filePath);

			// If this is an xlsx
			if (filePath.endsWith(".xlsx")) {
				ArrayList<String> fileSheets = xlsxToTSV(new File(filePath));
				for (String fileSheet : fileSheets) {
					count = getCount(ps, count, filePath, fileSheet);
				}
			} else {
				count = getCount(ps, count, filePath, filePath);
			}
		}

	}

	private int getCount(PrintStream ps, int count, String filePath, String fileSheet) throws Exception {
		String[] columns = getColumnNames(fileSheet);
		printFirstFiveLines(fileSheet, ps);

		String table = "dfs.root.`" + filePath + "`";

		generateMappingForTable(table, columns, ps, ("Mapping" + count++));

		for (int i = 0; i < columns.length; i++)
			columns[i] = "Column" + (i + 1);
//		generateMappingForTable(table, columns, ps, ("Mapping" + count++), "# ");
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

		System.err.println("XLSX file detected: " + xlsxFile);
		Workbook wb = WorkbookFactory.create(new FileInputStream(xlsxFile.getAbsolutePath()));
		Iterator<Sheet> sheetIterator = wb.sheetIterator();


		// TODO skip excel sheets starting with #?
		while (sheetIterator.hasNext()) {
			StringBuffer data = new StringBuffer();
			Sheet sheet = sheetIterator.next();
			String sheetName = sheet.getSheetName();
			if (!sheetName.startsWith("#")) {
				Iterator<Row> rowIterator = sheet.iterator();
				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					Iterator<Cell> cellIterator = row.cellIterator();
					while (cellIterator.hasNext()) {
						Cell cell = cellIterator.next();

						switch (cell.getCellType()) {
							case BOOLEAN:
								data.append(cell.getBooleanCellValue() + "\t");
								break;
							case NUMERIC:
								data.append(cell.getNumericCellValue() + "\t");
								break;
							case STRING:
								data.append(cell.getStringCellValue() + "\t");
								break;
							case BLANK:
								data.append("" + "\t");
								break;
							default:
								data.append(cell + "\t");
						}
					}
					data.append("\n");
				}
				File outputFile = new File(xlsxFile + ".sheet_" + sheetName + ".tsv");
				BufferedWriter bwr = new BufferedWriter(new FileWriter(outputFile));
				fileSheets.add(outputFile.getAbsolutePath());
				bwr.write(data.toString());
				bwr.close();
			}
		}
		return fileSheets;
	}


	@SuppressWarnings("resource")
	private void printFirstFiveLines(String filePath, PrintStream ps) throws SQLException {
		Statement st = connection.createStatement();
		String sql = "select * from dfs.root.`" + filePath + "` limit 5";
		
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

		String line = null;
		if (rs.next()) 
			line = rs.getString(1);
		else
			throw new InvalidParameterException("File \"" + filePath + "\" seems to be empty" );
		System.err.println(line);
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
