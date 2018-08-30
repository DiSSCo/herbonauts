package models.activities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.Logger;

import models.User;
import models.badges.Badge;

@Entity
@DiscriminatorValue("BADGE_WIN")
public class BadgeWinActivity extends Activity {
	
	@ManyToOne
	private User user;
	
	@ManyToOne
	private Badge badge;

	public BadgeWinActivity(Badge badge) {
		super();
		this.setUser(badge.getUser());
		this.setBadge(badge);
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public void setBadge(Badge badge) {
		this.badge = badge;
	}

	public Badge getBadge() {
		return badge;
	}
	
}
