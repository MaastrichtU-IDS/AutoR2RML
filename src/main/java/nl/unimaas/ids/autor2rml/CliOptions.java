package nl.unimaas.ids.autor2rml;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "autodrill")
public class CliOptions {
	
	@Option(names = { "-?", "--help" }, usageHelp = true, description = "Display a help message")
	boolean help = false;
	
	@Option(names= {"-j", "--jdbcurl"}, description = "Required. The URL for the Jdbc connector. E.g.: jdbc:drill:drillbit=localhost:31010", required = true)
	String jdbcurl = null;
	
	@Option(names= {"-r", "--recursive"}, description = "Process subDirectories recursively")
	boolean recursive = false;
	
	@Option(names= {"-u", "--username"}, description = "Username for database login, if needed")
	String username = null;
	
	@Option(names= {"-p", "--password"}, description = "Password for database username, if needed.")
	String password = null;
	
	@Option(names = {"-o", "--outputfile"}, description = "Path to the file where the mappings will be stored. If empty, then mappings go to System.out" )
	String outputFilepath = null;
	
	@Option(names= {"-d", "--directory"}, description = "Base directory to scan for structured files with Apache Drill. Needs to be under the dir scanned by Apache Drill running (/data by default)")
	String baseDir;
	
	@Option(names = {"-g", "--graph"}, description = "URL of the Graph the nquads will belong to. If empty, it will be generated." )
	String outputGraph = null;
	
	@Option(names= {"-b", "--baseUri"}, description = "Base URI used to built the dataset URIs. Default: http://kraken/")
	String baseUri = "http://kraken.semanticscience.org/";

}
