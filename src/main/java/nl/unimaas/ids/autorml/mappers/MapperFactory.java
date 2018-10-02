package nl.unimaas.ids.autorml.mappers;

import java.sql.SQLException;

public class MapperFactory {
	
	public static MapperInterface getMapper(String jdbcUrl, String userName, String passWord, String baseUri) throws SQLException, ClassNotFoundException {
		MapperInterface ret;
		if(jdbcUrl.startsWith("jdbc:drill:")) {
			ret = new DrillMapper(jdbcUrl, userName, passWord, baseUri);
		} else if (jdbcUrl.startsWith("jdbc:sqlite:")) {
			ret = new SQLiteMapper(jdbcUrl, userName, passWord, baseUri);
		} else {
			ret = new RDBMSMapper(jdbcUrl, userName, passWord, baseUri);
		}
		return ret;
	}

}
