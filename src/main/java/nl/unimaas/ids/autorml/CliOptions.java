package nl.unimaas.ids.autorml;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "autor2rml")
public class CliOptions {
	
	@Option(names = { "-?", "--help" }, usageHelp = true, description = "display a help message")
	boolean help = false;
	
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
	
	@Option(names = {"-b", "--baseuri"}, description = "Base URI used to generate triples. Default: http://data2services/" )
	String baseUri = "http://data2services/";
	
	@Option(names = {"-g", "--graphuri"}, description = "Graph URI for the generated triples. Default: http://data2services/graph/autor2rml" )
	String graphUri = "http://data2services/graph/autor2rml";
	
	@Option(names = {"-d", "--directory"}, description = "Base directory to scan for structured files" )
	String baseDir;

}
