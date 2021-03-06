package nl.unimaas.ids.autorml.mappers;

import java.sql.SQLException;

public class MapperFactory {
	
	public static MapperInterface getMapper(String jdbcUrl, String userName, String passWord, String baseUri, String graphUri, String columnHeaderString) throws SQLException, ClassNotFoundException {
		MapperInterface ret;
		if(jdbcUrl.startsWith("jdbc:drill:")) {
			ret = new DrillMapper(jdbcUrl, userName, passWord, baseUri, graphUri, columnHeaderString);
		} else if (jdbcUrl.startsWith("jdbc:sqlite:")) {
			ret = new SQLiteMapper(jdbcUrl, userName, passWord, baseUri, graphUri);
		} else if (jdbcUrl.startsWith("jdbc:mysql")) {
			ret = new MySQLMapper(jdbcUrl, userName, passWord, baseUri, graphUri);
		} else {
			ret = new RDBMSMapper(jdbcUrl, userName, passWord, baseUri, graphUri);
		}
		return ret;
	}

}
