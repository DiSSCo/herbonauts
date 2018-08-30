package models.alerts;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import models.Mission;
import models.Specimen;
import models.comments.SpecimenComment;
import models.contributions.Contribution;
import models.contributions.reports.ContributionReport;

@Entity
@DiscriminatorValue("ANNOUNCEMENT")
public class AnnouncementAlert extends Alert {

	@ManyToOne
	private Mission mission;
	
	public AnnouncementAlert(Mission mission) {
		this.setMission(mission);
	}

	public void setMission(Mission mission) {
		this.mission = mission;
	}

	public Mission getMission() {
		return mission;
	}
	
}
