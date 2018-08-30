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
@DiscriminatorValue("SPECIMEN_COMMENT")
public class SpecimenCommentAlert extends Alert {

	@ManyToOne
	private SpecimenComment specimenComment;
	
	public SpecimenCommentAlert(SpecimenComment comment) {
		this.setSpecimenComment(comment);
	}

	public void setSpecimenComment(SpecimenComment specimenComment) {
		this.specimenComment = specimenComment;
	}

	public SpecimenComment getSpecimenComment() {
		return specimenComment;
	}

	
	
}
