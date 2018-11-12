package nl.unimaas.ids.autorml.mappers;

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
import java.util.List;

import org.apache.commons.text.CaseUtils;

import nl.unimaas.ids.util.PrefixPrintWriter;

public class DrillMapper extends AbstractMapper implements MapperInterface {
	Connection connection;
	final static List<String> acceptedFileTypes = Arrays.asList(new String[] { "csv", "tsv", "psv" });

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
			String[] columns = getColumnNames(filePath);
			printFirstFiveLines(filePath, ps);
			
			String table = "dfs.root.`" + filePath + "`";
			
			generateMappingForTable(table, columns, ps, ("Mapping" + count++));
			
			for(int i=0; i<columns.length; i++) 
				columns[i] = "Column" + (i+1);
			generateMappingForTable(table, columns, ps, ("Mapping" + count++), "# ");
		}

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
		
		return line.substring(2, line.length() - 2).split("\",\"");
	}


	private List<String> getFilesRecursivelyAsList(Connection connection, String path, boolean recursive)
			throws Exception {
		List<String> ret = new ArrayList<>();

		Statement st = connection.createStatement();
		String sql = "show files in dfs.root.`" + path + "`";

		ResultSet rs = st.executeQuery(sql);

		String fileName = null;
		String filePath = null;
		boolean isDirectory = false;
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
				else if (fileName.contains(".")
						&& acceptedFileTypes.contains(fileName.substring(fileName.lastIndexOf(".") + 1)))
					ret.add(filePath);
			}
			rs.close();
		}

		return ret;

	}
	
	@Override
	public String getColumnName(String column) {
		// Remove all parenthesis
		column = column.replaceAll("[()]", " ");
		return CaseUtils.toCamelCase(column, true, new char[] { '-' });
	}
	
 	@Override
	public String getSqlForRowNum() {
		return "row_number() over (partition by filename) as " + ROW_NUM_NAME;
	}
 	
 	@Override
	public String getSqlForColumn(String column, int index) {
		return "columns[" + index + "] as `" + getColumnName(column) + "`";
	}

}
