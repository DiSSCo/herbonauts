package fr.mnhn.herbonautes.tiles;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class HTTP {

	public static final InputStream get(String host) {
		
		try {
			
			URL url = new URL(host);
			URLConnection connection;
			connection = url.openConnection();
			return connection.getInputStream();
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
}
