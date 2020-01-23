package nl.unimaas.ids.autorml.mappers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.lang3.StringUtils;


public abstract class AbstractMapper implements MapperInterface {
	Connection connection;
	final static String ROW_NUM_NAME = "autor2rml_rownum";	
	private String baseUri;
	private String graphUri;
	
	private String mysqlSupport = "";
	
	public AbstractMapper(String jdbcUrl, String userName, String passWord, String baseUri, String graphUri) {
		this.baseUri = StringUtils.appendIfMissing(baseUri, "/"); 
		this.graphUri = graphUri;
		
		if(jdbcUrl.contains("mysql")){
			mysqlSupport = " ,(SELECT @row_number:=0) AS t";
		}
		
	}
	
	@Override
	public void close() throws SQLException {
		if(connection != null && !connection.isClosed())
			connection.close();
	}
	
	void generateMappingForTable(String table, String[] columns, PrintStream ps, String label, String outputDir) throws Exception {
		generateMappingForTable(table, columns, ps, label, null, outputDir);
	}
	
	// Generate the R2RML mappings for a table/file
	void generateMappingForTable(String table, String[] columns, PrintStream ps, String label, String prefix, String outputDir) throws Exception {
		PrintWriter upper = new PrintWriter(ps);
		PrintWriter lower = new PrintWriter(ps);

		upper.println("<#" + label + ">");
		upper.println("rr:logicalTable [ rr:sqlQuery \"\"\"");
		
		lower.println("rr:subjectMap [");
		lower.println("  rr:termType rr:IRI;");
		lower.println("  rr:template \"" + this.baseUri + cleanTableNameForUri(table) + "/{" + ROW_NUM_NAME + "}\";");
		lower.println("  rr:class <" + this.baseUri + cleanTableNameForUri(table) + ">;");
		lower.println("  rr:graph <" + this.graphUri + ">;");
		lower.println("];");

		upper.println("  select " + getSqlForRowNum());
		for (int i = 0; i < columns.length; i++) {
			String column = columns[i];
			upper.println("    , " + getSqlForColumn(column, i));

			lower.println("rr:predicateObjectMap [");
			lower.println("  rr:predicate <" + this.baseUri + "model/" + getColumnName(column) + ">;");
			lower.println("  rr:objectMap [ rr:column \"" + getColumnName(column) + "\" ];");
			lower.println("  rr:graph <" + this.graphUri + ">;");
			lower.println("];");
			System.out.println("Column mapped: " + getColumnName(column));
		}
		upper.println("  from " + table + mysqlSupport + ";");
		upper.println("\"\"\"];");

		lower.println(".");
		lower.println("\n");

		upper.flush();
		lower.flush();
		generateSparqlQuery(cleanTableNameForUri(table), columns, outputDir);
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
		if (StringUtils.startsWith(tableName, "/")){
			// Remove full path from Drill
			tableName = tableName.substring(tableName.lastIndexOf("/") + 1);
		}
		return StringUtils.removeStart(tableName, "/");
	}
	
	// Generate template SPARQL query based on input data structure
	private void generateSparqlQuery(String tableName, String[] columns, String outputDir) throws FileNotFoundException {
		// Get file path to create a file by table/file, only tested on AutoR2RML (TODO: test SQL tables support)               
		String tableSparqlPath = outputDir + "/sparql_mapping_templates/" + getTableSparqlPath(tableName, outputDir)  + ".rq";
		File sparqlQueryFile = new File(tableSparqlPath);
		sparqlQueryFile.getParentFile().mkdirs();
		// TODO: File not found here
		PrintStream ps = new PrintStream(new FileOutputStream(sparqlQueryFile));
		PrintWriter upper = new PrintWriter(ps);
		PrintWriter lower = new PrintWriter(ps);
		
		upper.println("PREFIX d2s: <https://w3id.org/d2s/model/>");
		upper.println("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>");
		upper.println("PREFIX owl: <http://www.w3.org/2002/07/owl#>");
		upper.println("PREFIX dc: <http://purl.org/dc/elements/1.1/>");
		upper.println("PREFIX dcterms: <http://purl.org/dc/terms/>");
		upper.println("PREFIX bl: <https://w3id.org/biolink/vocab/>");
		upper.println("PREFIX w3idvocab: <https://w3id.org/d2s/vocab/>");
		upper.println("INSERT {");
		upper.println("  GRAPH <?_output> {  ");
		upper.println("    # Attribute the retrieved data to your model properties");
		
		lower.println("} WHERE {");
		lower.println("  # SERVICE <?_service>  {  # Don't work on Virtuoso at the moment");
		lower.println("    GRAPH <?_input> {");
		lower.println("");
		
		for (int i = 0; i < columns.length; i++) {
			String columnName = getColumnName(columns[i]);
			if (i == 0) {
				upper.println("   ?" + columnName + "_uri a owl:Thing ;");
				upper.println("      dc:identifier ?" + columnName + " ;");
				lower.println("      ?row d2s:" + columnName + " ?" + columnName + " ;");
				lower.println("        a <" + this.baseUri + tableName + "> .");
				lower.println("");
				lower.println("      # Generate URI from ID");
				lower.println("      BIND ( iri(concat(\"https://w3id.org/d2s/data/\", md5(?" + columnName + "))) AS ?" + columnName + "_uri )");
				lower.println("");
			} else{
				if (i == columns.length - 1) {
					upper.println("      rdfs:label ?" + columnName + " .");
				} else {
					upper.println("      rdfs:label ?" + columnName + " ;");
				}
				lower.println("      OPTIONAL { ?row d2s:" + columnName + " ?" + columnName + " . }");
			}
		}
		upper.println("  }");
		
		lower.println("    }");
		lower.println("  # }");
		lower.println("}");
		
		upper.flush();
		lower.flush();
		ps.close();
	}

}
