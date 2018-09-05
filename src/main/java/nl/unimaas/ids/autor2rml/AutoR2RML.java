package nl.unimaas.ids.autor2rml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import nl.unimaas.ids.autor2rml.mappers.MapperFactory;
import nl.unimaas.ids.autor2rml.mappers.MapperInterface;
import picocli.CommandLine;

public class AutoR2RML {

	public static void main(String[] args) throws Exception {
		try {
			CliOptions cli = CommandLine.populateCommand(new CliOptions(), args);
			if(cli.help) 
				printUsageAndExit();
		
			MapperInterface mapper = MapperFactory.getMapper(cli.jdbcurl, cli.username, cli.password, cli.outputGraph, cli.baseUri);
			
			PrintStream ps = System.out;		
			if(cli.outputFilepath!=null) {
				File outputFile = new File(cli.outputFilepath);
				ps = new PrintStream(new FileOutputStream(outputFile));
			}
				
			mapper.generateMapping(ps, cli.recursive, cli.baseDir);
			
			mapper.close();
		} catch (Throwable e) {
			printUsageAndExit(e);
		}

	}
	
	private static void printUsageAndExit() {
		printUsageAndExit(null);
	}
	
	private static void printUsageAndExit(Throwable e) {
		CommandLine.usage(new CliOptions(), System.out);
		if(e == null)
			System.exit(0);
		e.printStackTrace();
		System.exit(-1);
	}
	
	

}
