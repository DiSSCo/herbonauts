package fr.mnhn.herbonautes.tiles;


import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IO {

	public static void writeFile(InputStream in, File file) {
		FileOutputStream writeFile = null;
		file.getParentFile().mkdirs();
		try {
			writeFile = new FileOutputStream(file);
			
			byte[] buffer = new byte[1024];
	        int read;

	        while ((read = in.read(buffer)) > 0)
	            writeFile.write(buffer, 0, read);
	        writeFile.flush();
			
		} catch (FileNotFoundException e) {
			throw new RuntimeException("unable to write file " + file, e);
		} catch (IOException e) {
			throw new RuntimeException("unable to write file " + file, e);
		} finally {
			quietClose(writeFile);
			quietClose(in);
		}

	
	}
	
	
	public static void quietClose(Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (IOException e) {
				// Mal
			}
		}
	}
	
}
