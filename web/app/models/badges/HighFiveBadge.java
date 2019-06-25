package models.badges;

import models.User;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("HIGH_FIVE")
public class HighFiveBadge extends Badge {

	public HighFiveBadge(User user) {
		super(user);
	}

}
