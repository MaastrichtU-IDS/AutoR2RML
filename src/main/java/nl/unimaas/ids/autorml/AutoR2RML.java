package nl.unimaas.ids.autorml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import nl.unimaas.ids.autorml.mappers.MapperFactory;
import nl.unimaas.ids.autorml.mappers.MapperInterface;
import picocli.CommandLine;

public class AutoR2RML {
	final static String ROW_NUM_NAME = "ROWNUM_PER_FILE";
	final static List<String> acceptedFileTypes = Arrays.asList(new String[] {"csv", "tsv", "psv"});

	public static void main(String[] args) throws Exception {
		try {
			CliOptions cli = CommandLine.populateCommand(new CliOptions(), args);
			if(cli.help) 
				printUsageAndExit();
		
			MapperInterface mapper = MapperFactory.getMapper(cli.jdbcurl, cli.userName, cli.passWord);
			
			PrintStream ps = System.out;
			if(cli.outputFilePath!=null)
				ps = new PrintStream(new FileOutputStream(new File(cli.outputFilePath)));
			
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
