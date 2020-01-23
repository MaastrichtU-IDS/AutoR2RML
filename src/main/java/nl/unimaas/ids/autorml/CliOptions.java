package nl.unimaas.ids.autorml;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "autor2rml")
public class CliOptions {

	@Option(names = {"-h", "-?", "--help" }, usageHelp = true, description = "Display a help message")
	boolean help = false;

	@Option(names = {"--debug"}, description = "Enabling debug mode")
	boolean debug = false;

	@Option(names= {"-r", "--recursive"}, description = "process subDirectories recursively")
	boolean recursive = false;

	// e.g. jdbc:drill:drillbit=localhost:31010
	@Option(names= {"-j", "--jdbcurl"}, description = "Connect to drill host", required = true)
	String jdbcurl = null;

	@Option(names= {"-u", "--username"}, description = "Username for login if not empty")
	String userName = null;

	@Option(names= {"-p", "--password"}, description = "Password for Username")
	String passWord = null;

	@Option(names = {"-o", "--outputfile"}, description = "Path to the file where the mappings will be stored. If empty, then mappings go to System.out" )
	String outputFilePath = null;

	@Option(names = {"-b", "--baseuri"}, description = "Base URI used to generate triples. Default: https://w3id.org/d2s/" )
	String baseUri = "https://w3id.org/d2s/";

	@Option(names = {"-g", "--graphuri"}, description = "Graph URI for the generated triples. Default: https://w3id.org/d2s/" )
	String graphUri = "https://w3id.org/d2s/graph/autor2rml";

	@Option(names = {"-d", "--directory"}, description = "Base directory to scan for structured files")
	String baseDir;
	
	@Option(names = {"-c", "--column-header"}, description = "Comma separated label of the columns for tabular files processing. The entire file will be considered as data. e.g. id,name,col3")
	String columnHeaderString;

	CliOptions(String[] args) {
		try {
			CliOptions cliOptions = CommandLine.populateCommand(this, args);
			if (cliOptions.help) {
				new CommandLine(this).usage(System.out);
				System.exit(0);
			}
		} catch (CommandLine.ParameterException pe) {
			System.out.println(pe.getMessage());
			new CommandLine(this).usage(System.out);
			// System.out.println("  * required parameter");
			System.exit(64);
		}
	}
}