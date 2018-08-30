package models;

import notifiers.Mails;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Confirmation de changement de mail
 */
//@Entity
//@Table(name="H_EMAIL_CONFIRMATION")
public class EmailConfirmation extends Model {

	//@ManyToOne(fetch = FetchType.EAGER)
	//@JoinColumn(name = "USER_ID")
	private User user;

	//@Column(name="TOKEN")
	private String token;

	//@MaxSize(255)
	//@Required
	private String email;
	
	//@Column(name="CHANGE_DATE")
	private Date changeDate;

	public static EmailConfirmation findByToken(String token) {
		List<EmailConfirmation> list = find("token = ?", token).fetch();
		if (list.size() == 1) {
			return list.get(0);
		}
		return null;
	}

	public static void createAndSend(User user, String email) {
		EmailConfirmation confirmation = new EmailConfirmation();
		confirmation.user = user;
		confirmation.email = email;
		confirmation.token = UUID.randomUUID().toString();
		confirmation.changeDate = new Date();
		JPA.em().persist(confirmation);

		Mails.mailChangeConfirmation(confirmation);

	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getChangeDate() {
		return changeDate;
	}

	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}
}
