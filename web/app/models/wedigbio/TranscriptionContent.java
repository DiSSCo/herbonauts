/**
 * 
 */
package models.wedigbio;

/**
 * Les valeurs retranscrites
 * seul le cas de la geolocalisation est implémenté
 * @author schagnoux
 *
 */
public class TranscriptionContent {
	private String latitude;
	private String longitude;
	public TranscriptionContent(String lat, String lng) {
		latitude=lat;
		longitude=lng;
	}
	public String getLat() {
		return latitude;
	}
	public void setLat(String latitude) {
		this.latitude = latitude;
	}
	public String getLong() {
		return longitude;
	}
	public void setLong(String longitude) {
		this.longitude = longitude;
	}
}
