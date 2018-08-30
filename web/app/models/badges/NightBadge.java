package models.badges;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import models.User;

@Entity
@DiscriminatorValue("NIGHT")
public class NightBadge extends Badge {

	public NightBadge(User user) {
		super(user);
	}

}
