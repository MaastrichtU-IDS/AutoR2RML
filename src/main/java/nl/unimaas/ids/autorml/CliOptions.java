package nl.unimaas.ids.autorml;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "autodrill")
public class CliOptions {
	
	@Option(names = { "-?", "--help" }, usageHelp = true, description = "display a help message")
	boolean help = false;
	
	@Option(names= {"-r", "--recursive"}, description = "process subDirectories recursively")
	boolean recursive = false;
	
	// e.g. jdbc:drill:drillbit=localhost:31010
	@Option(names= {"-j", "--jdbcurl"}, description = "Connect to drill host", required = true)
	String jdbcurl = null;
	
	@Option(names= {"-u", "--username"}, description = "Username for login if not empty")
	String username = null;
	
	@Option(names= {"-p", "--password"}, description = "Password for Username")
	String password = null;
	
	@Option(names = {"-o", "--outputfile"}, description = "Path to the file where the mappings will be stored. If empty, then mappings go to System.out" )
	String outputFilepath = null;
	
	
	@Parameters(paramLabel="DIRECTORY", description = "Base directory to scan for structured files. Needs to be under the dir scanned by Apache Drill running (/data by default)")
	String baseDir;

}
