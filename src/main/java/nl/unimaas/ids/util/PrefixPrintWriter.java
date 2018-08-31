package nl.unimaas.ids.util;

import java.io.OutputStream;
import java.io.PrintWriter;

public class PrefixPrintWriter extends PrintWriter {

	String prefix = "";
	
	public PrefixPrintWriter(OutputStream out, String...prefix) {
		super(out);
		if(prefix.length>0 && prefix[0]!=null)
			this.prefix = prefix[0];
	}
	
	@Override
	public void println(String x) {
		super.println(prefix + x);
	}
	

}
