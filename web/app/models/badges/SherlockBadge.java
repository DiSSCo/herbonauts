package models.badges;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import models.User;

@Entity
@DiscriminatorValue("SHERLOCK")
public class SherlockBadge extends Badge {

	public SherlockBadge(User user) {
		super(user);
	}

}
