package models.comments;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import models.Specimen;
import models.User;

@Entity
@DiscriminatorValue("SPECIMEN")
public class SpecimenComment extends Comment {

	@ManyToOne
	private Specimen specimen;
	
	public List<User> getIncludedUsers() {
		return User.find("select distinct c.user " +
				"from SpecimenComment c " +
				"where c.specimen = ?", getSpecimen()).fetch();
	}

	public void setSpecimen(Specimen specimen) {
		this.specimen = specimen;
	}

	public Specimen getSpecimen() {
		return specimen;
	}
	
}
