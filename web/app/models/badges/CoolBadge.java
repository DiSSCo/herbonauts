package models.badges;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import models.User;

@Entity
@DiscriminatorValue("COOL")
public class CoolBadge extends Badge {

	public CoolBadge(User user) {
		super(user);
	}

}
