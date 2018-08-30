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
import models.comments.MissionComment;
import models.contributions.Contribution;

import play.db.jpa.Model;

@Entity
@DiscriminatorValue("COMMENT_MISSION")
public class CommentMissionActivity extends Activity {
	
	@ManyToOne
	private Mission mission;
	
	@ManyToOne
	private User user;

	public CommentMissionActivity(User author, Mission mission) {
		super();
		this.setMission(mission);
		this.setUser(author);
	}

	public void setMission(Mission mission) {
		this.mission = mission;
	}

	public Mission getMission() {
		return mission;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}
	
}
