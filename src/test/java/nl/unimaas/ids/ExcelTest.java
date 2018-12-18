package nl.unimaas.ids;

import java.io.File;

import junit.framework.TestCase;
import nl.unimaas.ids.autorml.AutoR2RML;

//public class ExcelTest extends TestCase {
public class ExcelTest {

    public void testExcel() throws Throwable {

        if (new File("/data/").exists()) {
            AutoR2RML.main(new String[]{"--jdbcurl", "jdbc:drill:drillbit=localhost:31010", "-d", "/data/", "--baseuri", "http://wur.nl/", "--outputfile", "dev1.trig"}); // , "--debug"
        }
    }

    public void testHelp() throws Throwable {
        AutoR2RML.main(new String[]{"--help"});
    }

    // TODO identify where /data/config.properties comes from
    public void testTSV() throws Exception {
        String filePath = "/data/";
        if (new File(filePath).exists()) {
            AutoR2RML.main(new String[]{"--jdbcurl", "jdbc:drill:drillbit=localhost:31010", "-d", filePath, "--baseuri", "http://wur.nl/", "--outputfile", "/data/mapping.ttl"}); // , "--debug" "--outputfile", "mapping.ttl"
            String command = "docker run -dit --rm -p 8047:8047 -p 31010:31010 --name drill -v /data:/data:ro apache-drill\n" +
                    "docker run -it --rm --link drill:drill -v /data:/data r2rml /data/config.properties\n";
            System.err.println("Execute:\n" + command);
        }
    }
}
