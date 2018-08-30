/**
 * 
 */
package models.wedigbio;

/**
 * Le contributeur Wedigbio 
 * Correspond au User herbonautes
 * @author schagnoux
 *
 */
public class Contributor {
	String decimalLatitude;  //coordonnées
	String decimalLongitude; //coordonnées
	String transcriber;		 // pseudo
	public Contributor(String la, String lo, String userLogin) {
		this.decimalLatitude=la;
		this.decimalLongitude=lo;
		this.transcriber=userLogin;
	}
	public String getDecimalLatitude() {
		return decimalLatitude;
	}
	public String getDecimalLongitude() {
		return decimalLongitude;
	}
	public String getTranscriber() {
		return transcriber;
	}

}
