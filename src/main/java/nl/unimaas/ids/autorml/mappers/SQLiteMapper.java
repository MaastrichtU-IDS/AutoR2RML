package nl.unimaas.ids.autorml.mappers;

import java.sql.SQLException;

public class SQLiteMapper extends RDBMSMapper {

	public SQLiteMapper(String jdbcUrl, String userName, String passWord) throws SQLException, ClassNotFoundException {
		super(jdbcUrl, userName, passWord);
	}
	
	@Override
	public String getSqlForRowNum() {
		return "rowid as " + ROW_NUM_NAME;
	}

}
