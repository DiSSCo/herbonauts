package models.activities;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import models.Specimen;
import models.User;

@Entity
@DiscriminatorValue("COMMENT_SPECIMEN")
public class CommentSpecimenActivity extends Activity {
	
	@ManyToOne
	private User user;
	
	@ManyToOne
	private Specimen specimen;

	public CommentSpecimenActivity(User author, Specimen specimen) {
		super();
		this.setUser(author);
		this.setSpecimen(specimen);
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public void setSpecimen(Specimen specimen) {
		this.specimen = specimen;
	}

	public Specimen getSpecimen() {
		return specimen;
	}
	
}
