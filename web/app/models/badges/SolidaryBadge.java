package models.badges;

import models.User;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("SOLIDARY")
public class SolidaryBadge extends Badge {

	public SolidaryBadge(User user) {
		super(user);
	}

}
