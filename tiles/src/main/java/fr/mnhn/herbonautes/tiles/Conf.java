package fr.mnhn.herbonautes.tiles;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Conf {

	public static String IMAGES_ROOT_DIRECTORY; 
	public static String DB_DRIVER_CLASS; 
	public static String DB_URL; 
	public static String DB_USER; 
	public static String DB_PASSWORD; 
	public static Integer CROP_WIDTH;
	public static Integer CROP_HEIGHT;
	
	public static void init(String fileName) {
		Properties prop = new Properties();
		 
    	try {
    		//prop.load(ClassLoader.getSystemResourceAsStream("conf.properties"));
 
    		prop.load(new FileInputStream(fileName));
    		
    		IMAGES_ROOT_DIRECTORY = prop.getProperty("images.root.dir"); 
    		DB_DRIVER_CLASS = prop.getProperty("db.driver.class");
    		DB_URL = prop.getProperty("db.url"); 
    		DB_USER = prop.getProperty("db.user"); 
    		DB_PASSWORD = prop.getProperty("db.password"); 
    		CROP_WIDTH = Integer.valueOf(prop.getProperty("crop.width"));
    		CROP_HEIGHT = Integer.valueOf(prop.getProperty("crop.height"));
 
    	} catch (IOException e) {
    		throw new RuntimeException("Impossible de charger la configuration", e);
        }
    	
	}
	
}
