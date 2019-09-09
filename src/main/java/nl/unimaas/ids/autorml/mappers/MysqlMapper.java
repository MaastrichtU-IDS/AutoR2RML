package nl.unimaas.ids.autorml.mappers;

import java.sql.SQLException;

public class MysqlMapper extends RDBMSMapper {

	public MysqlMapper(String jdbcUrl, String userName, String passWord, String baseUri, String graphUri) throws SQLException, ClassNotFoundException {
		super(jdbcUrl, userName, passWord, baseUri, graphUri);
	}
	
	@Override
	public String getSqlForRowNum() {
		return "(@row_number:=@row_number + 1) AS " + ROW_NUM_NAME;
	}

}
