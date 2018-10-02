package nl.unimaas.ids.autorml.mappers;

import java.sql.SQLException;

public class SQLiteMapper extends RDBMSMapper {

	public SQLiteMapper(String jdbcUrl, String userName, String passWord, String baseUri) throws SQLException, ClassNotFoundException {
		super(jdbcUrl, userName, passWord, baseUri);
	}
	
	@Override
	public String getSqlForRowNum() {
		return "rowid as " + ROW_NUM_NAME;
	}

}
