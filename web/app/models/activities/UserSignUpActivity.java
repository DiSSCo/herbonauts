package models.activities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.Logger;

import models.User;
import models.badges.Badge;

@Entity
@DiscriminatorValue("SIGN_UP")
public class UserSignUpActivity extends Activity {
	
	@ManyToOne
	private User user;
	
	public UserSignUpActivity(User user) {
		super();
		this.setUser(user);
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}
	
}
