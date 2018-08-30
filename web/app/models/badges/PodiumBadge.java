package models.badges;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import models.User;

@Entity
@DiscriminatorValue("PODIUM")
public class PodiumBadge extends Badge {

	public PodiumBadge(User user) {
		super(user);
	}

}
