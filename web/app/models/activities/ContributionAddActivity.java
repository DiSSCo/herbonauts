package models.activities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import models.Mission;
import models.Specimen;
import models.User;
import models.contributions.Contribution;

@Entity
@DiscriminatorValue("CONTRIBUTION_ADD")
public class ContributionAddActivity extends Activity {
	
	@ManyToOne
	private User user;
	
	@ManyToOne
	private Mission mission;
	
	@ManyToOne
	private Contribution contribution;
	
	@ManyToOne
	private Specimen specimen;

	public ContributionAddActivity(Contribution contribution) {
		super();
		this.setUser(contribution.getUser());
		this.setMission(contribution.getMission());
		this.setSpecimen(contribution.getSpecimen());
		this.setContribution(contribution);
	}

	public void setContribution(Contribution contribution) {
		this.contribution = contribution;
	}

	public Contribution getContribution() {
		return contribution;
	}

	public void setSpecimen(Specimen specimen) {
		this.specimen = specimen;
	}

	public Specimen getSpecimen() {
		return specimen;
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
