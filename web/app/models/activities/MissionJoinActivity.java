package models.activities;

import java.util.Date;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import models.Mission;
import models.User;
import models.contributions.Contribution;

import play.db.jpa.Model;

@Entity
@DiscriminatorValue("MISSION_JOIN")
public class MissionJoinActivity extends Activity {
	
	@ManyToOne
	private User user;
	
	@ManyToOne
	private Mission mission;

	public MissionJoinActivity(User user, Mission mission) {
		super();
		this.setUser(user);
		this.setMission(mission);
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public void setMission(Mission mission) {
		this.mission = mission;
	}

	public Mission getMission() {
		return mission;
	}
	
}
