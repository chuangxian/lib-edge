package myzd.domain;


import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class MyClass {
	public static void main(String[] args) throws IOException {
		InputStream in = new URL( "http://commons.apache.org" ).openStream();
		IOUtils.toString(in);
	}
}
