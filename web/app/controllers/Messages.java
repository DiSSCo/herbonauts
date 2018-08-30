package controllers;

import notifiers.Mails;
import models.User;

/**
 * Envoi d'un message aux utilisateurs
 */
public class Messages extends Application {


	/**
	 * Formulaire de message
	 */
	public static void blank(String login) {
		User from = Security.connected();
		User to = User.findByLogin(login);
		
		// Messages uniquement avec un chef de mission
		if (!from.isLeader() && !to.isLeader()) {
			forbidden();
		}
		
		render(to);
	}
	
	/**
	 * Envoi du message
	 */
	public static void send(String login, String title, String message) {
		User from = Security.connected();
		User to = User.findByLogin(login);
		
		// Messages uniquement avec un chef de mission
		if (!from.isLeader() && !to.isLeader()) {
			forbidden();
		}
		
		Mails.message(from, to, message);
		
		flash.success(play.i18n.Messages.get("sent.message"));
		Users.show(login);
	}
	
}
