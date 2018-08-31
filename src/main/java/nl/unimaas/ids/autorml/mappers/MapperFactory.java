package nl.unimaas.ids.autorml.mappers;

import java.sql.SQLException;

public class MapperFactory {
	
	public static MapperInterface getMapper(String jdbcUrl, String userName, String passWord) throws SQLException, ClassNotFoundException {
		MapperInterface ret;
		if(jdbcUrl.startsWith("jdbc:drill:")) {
			ret = new DrillMapper(jdbcUrl, userName, passWord);
		} else {
			ret = new RDBMSMapper(jdbcUrl, userName, passWord);
		}
		return ret;
	}

}
