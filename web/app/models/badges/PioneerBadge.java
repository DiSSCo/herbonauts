package models.badges;

import models.User;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("PIONEER")
public class PioneerBadge extends Badge {

	public PioneerBadge(User user) {
		super(user);
	}

}
