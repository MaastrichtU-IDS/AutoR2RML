package nl.unimaas.ids;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import junitx.framework.FileAssert;
import nl.unimaas.ids.autorml.mappers.MapperFactory;
import nl.unimaas.ids.autorml.mappers.MapperInterface;


public class TestR2RMLDrill {

	private static String connectionURL = "jdbc:drill:drillbit=localhost:31010";
	private static MapperInterface mapper;
	
	/* Not working. Try to start Apache Drill Docker at start of test
	@ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file("src/test/resources/docker-compose.yml")
            .build();
	*/
	
	// Get mapper before running tests
	@BeforeClass
	public static void init() throws Exception {
		mapper = MapperFactory.getMapper(connectionURL, "", "", "http://test/", "http://graph/test");
	}

	// Generate mapping file for a simple TSV file with columns id, name and date
    @Test
	public void testTsv() throws Exception {
    	// Put files in /data/data2services-tests to be processed by Apache Drill (which runs on /data)
    	FileUtils.copyFile(new File("src/test/resources/validation-basic_test.tsv"), new File("/data/data2services-tests/validation-basic_test.tsv"));
		
    	PrintStream ps = new PrintStream(new FileOutputStream(new File("/data/data2services-tests/mappings.trig")));

		// Pass the Path to process in Drill
		mapper.generateMapping(ps, true, "/data/data2services-tests");
		mapper.close();
		
		// Check if the generated mapping file is equal to the reference mapping file
		FileAssert.assertEquals(new File("src/test/resources/validation-reference.trig"), new File("/data/data2services-tests/mappings.trig"));
	}
	
}

