package models;

import notifiers.Mails;
import play.Play;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="H_USER_INVITATION")
public class UserInvitation extends Model {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "FROM_USER_ID")
	private User fromUser;

	@Column(name = "TO_EMAIL")
	private String toEmail;

	@Column(name = "TOKEN")
	private String token;

	@Column(name = "INVITATION_DATE")
	private Date date;

	public static void createAndSend(User from, String to) {
		UserInvitation invitation = new UserInvitation();
		invitation.fromUser = from;
		invitation.toEmail = to;
		invitation.token = UUID.randomUUID().toString();
		invitation.date = new Date();

		JPA.em().persist(invitation);

		Mails.invitation(invitation);
	}


	public static UserInvitation findByToken(String token) {
		List<UserInvitation> invitations = UserInvitation.find("token = ?", token).fetch();
		if (invitations.size() > 0) {
			return invitations.get(0);
		}
		return null;
	}

	public User getFromUser() {
		return fromUser;
	}

	public void setFromUser(User fromUser) {
		this.fromUser = fromUser;
	}

	public String getToEmail() {
		return toEmail;
	}

	public void setToEmail(String toEmail) {
		this.toEmail = toEmail;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
