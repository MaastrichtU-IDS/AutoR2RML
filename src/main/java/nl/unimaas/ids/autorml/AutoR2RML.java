package nl.unimaas.ids.autorml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

//import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import nl.unimaas.ids.autorml.mappers.MapperFactory;
import nl.unimaas.ids.autorml.mappers.MapperInterface;

public class AutoR2RML {

	final public static Logger logger = Logger.getLogger(AutoR2RML.class);
	
	public static void main(String[] args) throws Exception {
		//logger.setLevel(Level.INFO);
		CliOptions cli = new CliOptions(args);

		MapperInterface mapper = MapperFactory.getMapper(cli.jdbcurl, cli.userName, cli.passWord, cli.baseUri, cli.graphUri);

		PrintStream ps = System.out;
		if (cli.outputFilePath != null)
			ps = new PrintStream(new FileOutputStream(new File(cli.outputFilePath)));

		mapper.generateMapping(ps, cli.recursive, cli.baseDir);

		mapper.close();
	}
}
