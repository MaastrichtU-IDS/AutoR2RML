package nl.unimaas.ids.autorml.mappers;

import java.io.PrintStream;

public interface MapperInterface {
	
	public void close() throws Exception;

	public void generateMapping(PrintStream out, boolean recursive, String baseDir, String outputDir) throws Exception;
	
	public String getColumnName(String column);
	
	public String getSqlForRowNum();
	
	public String getSqlForColumn(String column, int index);

	public String getTableSparqlPath(String tableName, String baseDir);

}
