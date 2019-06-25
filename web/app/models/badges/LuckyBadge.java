package models.badges;

import models.User;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("LUCKY")
public class LuckyBadge extends Badge {

	public LuckyBadge(User user) {
		super(user);
	}

}
