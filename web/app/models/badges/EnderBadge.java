package models.badges;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import models.User;

@Entity
@DiscriminatorValue("ENDER")
public class EnderBadge extends Badge {

	public EnderBadge(User user) {
		super(user);
	}

}
