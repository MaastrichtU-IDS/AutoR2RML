package nl.unimaas.ids.autorml.mappers;

import java.io.PrintStream;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RDBMSMapper extends AbstractMapper implements MapperInterface {

	public RDBMSMapper(String jdbcUrl, String userName, String passWord) throws SQLException, ClassNotFoundException {
		super(jdbcUrl, userName, passWord);
		Class.forName("org.sqlite.JDBC");
		Class.forName("org.postgresql.Driver");
		connection = DriverManager.getConnection(jdbcUrl, userName, passWord);
	}


	@Override
	public void generateMapping(PrintStream out, boolean recursive, String baseDir) throws Exception {
		DatabaseMetaData md = connection.getMetaData();
		ResultSet rs = md.getTables(null, null, "%", new String[] { "TABLE" });
		
		generateNamespaces(out);
		while (rs.next()) {
		  String table = rs.getString(3);
		  ResultSet rs2 = md.getColumns(null, null, table, null);
		  List<String> columns = new ArrayList<>();
		  while(rs2.next())
			  columns.add(rs2.getString(4));
		  
		  String[] col = (String[]) columns.toArray(new String[0]);
		  generateMappingForTable(table, col, System.out, "test");
		  
		}
		
	}
	
	@Override
	public String getColumnName(String column) {
		return column;
	}
	
 	@Override
	public String getSqlForRowNum() {
		return "rownum as " + ROW_NUM_NAME;
	}
 	
 	@Override
	public String getSqlForColumn(String column, int index) {
		return column;
	}

}
