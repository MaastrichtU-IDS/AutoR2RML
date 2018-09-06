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
	final static List<String> acceptedTsvFileTypes = Arrays.asList(new String[] { "csv", "tsv", "psv" });

	public DrillMapper(String jdbcUrl, String username, String password, String outputGraph, String baseUri) throws SQLException, ClassNotFoundException {
		// TODO: Class.forName should not be necessary any more
		Class.forName("org.apache.drill.jdbc.Driver"); 
		connection = DriverManager.getConnection(jdbcUrl, username, password);
		this.baseUri = baseUri;
		this.outputGraph = outputGraph;
	}

	public void generateMapping(PrintStream ps, boolean recursive, String path)
			throws Exception {
		if (path.endsWith("/"))
			path = path.substring(0, path.length() - 1);

		generateImportAndGraph(ps, path);
		
		List<String> filePaths = getFilesRecursivelyAsList(connection, path, recursive);

		int count = 1;
		
		for (String filePath : filePaths) {
			String[] columns = getColumnNames(filePath);
			printFirstFiveLines(filePath, ps);
			
			String fromQuery = "dfs.root.`" + filePath + "`";
			
			// TODO: one of these count++ need to be removed and use only simple count
			generateR2RML(filePath, fromQuery, columns, ps, ("Mapping" + count));
			
			for(int i=0; i<columns.length; i++) 
				columns[i] = "Column" + (i+1);
			generateR2RML(filePath, fromQuery, columns, ps, ("Mapping" + count++), "# ");
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
		while (rs.next()) {
			fileName = rs.getString(1);
			isDirectory = rs.getBoolean(2);

			filePath = path + "/" + fileName;

			if (isDirectory && recursive)
				ret.addAll(getFilesRecursivelyAsList(connection, filePath, true));
			else if (fileName.contains(".")
					&& acceptedTsvFileTypes.contains(fileName.substring(fileName.lastIndexOf(".") + 1)))
				ret.add(filePath);
		}
		rs.close();

		return ret;

	}

	@Override
	public String getColumnName(String column) {
		return CaseUtils.toCamelCase(column, true, new char[] { '-' });
	}

	@Override
	public String getSqlForRowNum(String tableName, String firstColumn) {
		return "row_number() over (partition by filename) as " + ROW_NUM_NAME;
	}

	@Override
	public String getSqlForColumn(String column, int index) {
		return "columns[" + index + "] as `" + column + "`";
	}


}
