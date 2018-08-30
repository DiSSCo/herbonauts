package helpers;

import java.util.List;

import play.db.jpa.JPA;

import models.User;

/**
 * Classe utilitaire utilisateurs
 */
public class UserUtils {

	public static String getUnusedLogin(String attempt) {

		if (attempt == null) {
			throw new NullPointerException();
		}
		
		List<String> logins = 
			JPA.em()
				.createQuery("select lower(login) from User where lower(login) like :attempt")
				.setParameter("attempt", attempt.toLowerCase().trim() + "%")
				.getResultList();

		if (logins == null || !logins.contains(attempt.trim())) {
			return attempt.trim();
		}
		
		
		boolean found = false;
		int suffix = 1;
		String unusedLogin = null;
		
		while (!found) {
			unusedLogin = attempt.trim() + "." + (++suffix);
			found = !logins.contains(unusedLogin.toLowerCase());
			
		}
		
		return unusedLogin;
	}
	
}
