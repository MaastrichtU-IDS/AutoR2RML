package nl.unimaas.ids.autorml.mappers;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import nl.unimaas.ids.util.PrefixPrintWriter;

public abstract class AbstractMapper implements MapperInterface {
	Connection connection;
	final static String ROW_NUM_NAME = "autor2rml_rownum";	
	private String baseUri;
	
	private String graph;
	
	public AbstractMapper(String jdbcUrl, String userName, String passWord, String baseUri) {
		this.baseUri = baseUri;
		if (this.baseUri.lastIndexOf("/") != this.baseUri.length()-1) {
			// Add / at end of base URI
			this.baseUri = this.baseUri + "/";
		}
		this.graph = this.baseUri + "graph/" + UUID.randomUUID() + "/"; 
	}
	
	@Override
	public void close() throws SQLException {
		if(connection != null && !connection.isClosed())
			connection.close();
	}
	
	void generateMappingForTable(String table, String[] columns, PrintStream ps, String label) throws Exception {
		generateMappingForTable(table, columns, ps, label, null);
	}
	
	@SuppressWarnings("resource")
	void generateMappingForTable(String table, String[] columns, PrintStream ps, String label, String prefix) throws Exception {
		PrintWriter upper = new PrefixPrintWriter(ps, prefix);
		PrintWriter lower = new PrefixPrintWriter(ps, prefix);

		upper.println("<#" + label + ">");
		upper.println("rr:logicalTable [ rr:sqlQuery \"\"\"");

		lower.println("rr:subjectMap [");
		lower.println("  rr:termType rr:IRI;");
		lower.println("  rr:template \"" + this.baseUri + cleanTableNameForUri(table) + "/{" + ROW_NUM_NAME + "}\";");
		lower.println("  rr:class <" + this.baseUri + cleanTableNameForUri(table) + ">;");
		lower.println("  rr:graph <" + this.graph + ">;");
		lower.println("];");

		upper.println("  select " + getSqlForRowNum());
		for (int i = 0; i < columns.length; i++) {
			String column = columns[i];

			upper.println("    , " + getSqlForColumn(column, i));

			lower.println("rr:predicateObjectMap [");
			lower.println("  rr:predicate <" + this.baseUri + "" + cleanTableNameForUri(table) + "/" + getColumnName(column) + ">;");
			lower.println("  rr:objectMap [ rr:column \"" + getColumnName(column) + "\" ];");
			lower.println("  rr:graph <" + this.graph + ">;");
			lower.println("];");
		}
		upper.println("  from " + table + ";");
		upper.println("\"\"\"];");

		lower.println(".");
		lower.println("\n");

		upper.flush();
		lower.flush();

	}
	
	void generateNamespaces(PrintStream ps) {
		ps.println("@prefix rr: <http://www.w3.org/ns/r2rml#>.");
	}
	
	private String cleanTableNameForUri(String tableName) {
		if(!tableName.contains("`"))
			return tableName;
		
		int i1 = tableName.indexOf("`");
		int i2 = tableName.indexOf("`", i1+1);
		tableName = tableName.substring(i1 + 1, i2);
		return StringUtils.removeStart(tableName, "/");
	}

}
