package nl.unimaas.ids.autorml.mappers;

import java.sql.SQLException;

public class MapperFactory {
	
	public static MapperInterface getMapper(String jdbcUrl, String username, String password, String outputGraph, String baseUri) throws SQLException, ClassNotFoundException {
		MapperInterface ret;
		if(jdbcUrl.startsWith("jdbc:drill:")) {
			ret = new DrillMapper(jdbcUrl, username, password, outputGraph, baseUri);
		} else {
			ret = new RDBMSMapper(jdbcUrl, username, password, outputGraph, baseUri);
		}
		return ret;
	}

}
