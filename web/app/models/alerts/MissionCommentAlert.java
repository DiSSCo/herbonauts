package models.alerts;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import models.Mission;
import models.Specimen;
import models.comments.MissionComment;
import models.comments.SpecimenComment;
import models.contributions.Contribution;
import models.contributions.reports.ContributionReport;

@Entity
@DiscriminatorValue("MISSION_COMMENT")
public class MissionCommentAlert extends Alert {

	@ManyToOne
	private MissionComment missionComment;
	
	public MissionCommentAlert(MissionComment comment) {
		this.setMissionComment(comment);
	}

	public void setMissionComment(MissionComment missionComment) {
		this.missionComment = missionComment;
	}

	public MissionComment getMissionComment() {
		return missionComment;
	}
	
}
