package jobs;

import java.util.List;

import conf.Herbonautes;

import notifiers.Mails;

import models.User;
import models.alerts.Alert;

import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Every;
import play.jobs.Job;
import play.libs.Mail;
import play.mvc.Mailer;

/**
 * Job d'envoi d'alertes par mail
 */
@Every("60s")
public class AlertsMailSenderJob extends Job {

	@Override
	public void doJob() throws Exception {
		
		Logger.info("Envoi des notifications d'alerte par mail");
		
		List<User> users = 
			JPA.em().createQuery(
					"select distinct a.user " +
					"from Alert a " +
					"where a.emailSent = false", User.class)
					.getResultList();
		
		if (users == null || users.size() == 0) {
			Logger.info("Aucune notification à envoyer");
			return;
		}
		
		for (User user : users) {
			List<Alert> alerts = Alert.find("user.id = ? and emailSent = false", user.id).fetch();
			Mails.alerts(user, alerts);
			for (Alert alert : alerts) {
				alert.setEmailSent(true);
				alert.save();
			}
			
		}
		
	}
	
}
