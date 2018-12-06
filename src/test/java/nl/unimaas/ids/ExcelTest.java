package nl.unimaas.ids;

import junit.framework.TestCase;
import nl.unimaas.ids.autorml.AutoR2RML;
import nl.unimaas.ids.autorml.mappers.MapperFactory;
import nl.unimaas.ids.autorml.mappers.MapperInterface;

import javax.validation.constraints.Null;
import java.io.File;

public class ExcelTest extends TestCase {

    public void testHelp() throws Throwable {
        // Inject the file into Apache Drill
        // Config of DRILL
//        MapperInterface mapper = MapperFactory.getMapper("jdbc:drill:drillbit=localhost:31010", null, null, "http://example.com", "http://wur.nl");
//        mapper.generateMapping(System.out, false, "/data/");
        // Run R2RML to convert whatever is in there to RDF

        if (new File("/data/").exists()) {

            AutoR2RML.main(new String[]{"--jdbcurl", "jdbc:drill:drillbit=localhost:31010", "-d", "/data/"});
        }
    }
}
