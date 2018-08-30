package models.alerts;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import models.Specimen;
import models.comments.SpecimenComment;
import models.contributions.Contribution;
import models.contributions.reports.ContributionReport;
import models.User;

import play.db.jpa.JPA;
import play.db.jpa.Model;

@Entity
@DiscriminatorValue("CONFLICT")
public class ConflictAlert extends Alert {

	@ManyToOne
	private Contribution contribution;
	
	
	public ConflictAlert(Contribution contribution) {
		this.setContribution(contribution);
//		this.setContributionInitiale(this.getUser(), this.getType(), contribution);
	}

	
	public void setContribution(Contribution contribution) {
		this.contribution = contribution;
	}

	public Contribution getContribution() {
		return contribution;
	}

	public Contribution contributionInitiale() {

	  Contribution contributionInitiale = null;
	 
	  
	  List<Contribution> contributionList = Contribution.find("user_id = ? and type = ? and specimen_id= ?", this.getUser().id, this.contribution.getType(), this.contribution.getSpecimen().getId()).fetch();
	  
	  //List<Contribution> contributionList = Contribution.find("select c from Contribution c where c.user = ?", getUser().getId()).fetch();

	  for (Contribution contribution : contributionList) {
      contributionInitiale = contribution;
    }

	  return contributionInitiale;
	}

	
}
