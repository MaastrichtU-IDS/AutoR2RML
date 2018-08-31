package nl.unimaas.ids.autorml.mappers;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.text.CaseUtils;

import nl.unimaas.ids.util.PrefixPrintWriter;

public abstract class AbstractMapper implements MapperInterface {
	final static String ROW_NUM_NAME = "ROWNUM_PER_FILE";
	final static List<String> acceptedTsvFileTypes = Arrays.asList(new String[] { "csv", "tsv", "psv" });
	
	Connection connection;
	
	public AbstractMapper() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void close() throws SQLException {
		if(connection != null && !connection.isClosed())
			connection.close();
	}
	
	void generateMapping(String table, String[] columns, PrintStream ps, String label) throws Exception {
		generateMapping(table, columns, ps, label, null);
	}
	
	@SuppressWarnings("resource")
	void generateMapping(String table, String[] columns, PrintStream ps, String label, String prefix) throws Exception {
		PrintWriter upper = new PrefixPrintWriter(ps, prefix);
		PrintWriter lower = new PrefixPrintWriter(ps, prefix);


		upper.println("<#" + label + ">");
		upper.println("rr:logicalTable [ rr:sqlQuery \"\"\"");

		lower.println("rr:subjectMap [");
		lower.println("  rr:termType rr:IRI;");
		lower.println("  rr:template \"http://kraken" + table + "/{" + ROW_NUM_NAME + "}\";");
		lower.println("];");

		upper.println("  select row_number() over (partition by filename) as " + ROW_NUM_NAME);
		for (int i = 0; i < columns.length; i++) {
			String column = columns[i];
			String columnName = CaseUtils.toCamelCase(column, true, new char[] { '-' });

			upper.println("    , columns[" + i + "] as `" + columnName + "`");

			lower.println("rr:predicateObjectMap [");
			lower.println("  rr:predicate http://kraken" + table + "/has" + columnName + ";");
			lower.println("  rr:objectMap [ rr:column \"" + columnName + "\" ];");
			lower.println("];");
		}
		upper.println("  from\n    " + table + ";");
		upper.println("\"\"\"];");

		lower.println(".");
		lower.println("\n");

		upper.flush();
		lower.flush();

	}

}