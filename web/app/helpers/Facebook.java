package helpers;

/**
 * Classe utilitaire pour Facebook
 */
public class Facebook {

	/**
	 * URL de l'image de profil Facebook
	 */
	public static String profilePictureURL(String username) {
		return "http://graph.facebook.com/" + username + "/picture";
	}
	
	
	/**
	 * URL de l'image de profil Facebook
	 */
	public static String profileLargePictureURL(String username) {
		return "http://graph.facebook.com/" + username + "/picture?type=large";
	}
}
