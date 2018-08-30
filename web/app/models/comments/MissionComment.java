package models.comments;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import models.Mission;
import models.User;

@Entity
@DiscriminatorValue("MISSION")
public class MissionComment extends Comment {

	@ManyToOne
	private Mission mission;
	
	public List<User> getIncludedUsers() {
		return User.find("select distinct c.user " +
				"from MissionComment c " +
				"where c.mission = ?", getMission()).fetch();
	}

	public void setMission(Mission mission) {
		this.mission = mission;
	}

	public Mission getMission() {
		return mission;
	}
	
}
