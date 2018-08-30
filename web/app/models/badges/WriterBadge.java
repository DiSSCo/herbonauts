package models.badges;

import models.User;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("WRITER")
public class WriterBadge extends Badge {

	public WriterBadge(User user) {
		super(user);
	}

}
