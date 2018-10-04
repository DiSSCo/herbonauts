package fr.mnhn.herbonautes.tiles;


import java.sql.ResultSet;
import java.sql.SQLException;


public class Specimen {

	private Long id; 
	
	private String institute;
	
	private String collection;
	
	private String code;
	
	private String url;
	
	public Long getId() { return id; }
	public String getInstitute() { return institute; }
	public String getCollection() { return collection; }
	public String getCode() { return code; }
	public String getUrl() { return url; }

	public String getStringPath() {
		StringBuffer sb = new StringBuffer()
			.append(institute).append('/')
			.append(collection).append('/')
			.append(code);
		return sb.toString();
	}
	
	public static final Specimen create(ResultSet rs) {
		if (rs == null) return null;
    	try {
    		Specimen specimen = new Specimen();
    		specimen.id = rs.getLong("ID");
    		specimen.institute = rs.getString("INSTITUTE");
    		specimen.collection = rs.getString("COLLECTION");
    		specimen.code = rs.getString("CODE");
    		specimen.url = rs.getString("SONNERATURL");
    		return specimen;
    	} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}	
}
