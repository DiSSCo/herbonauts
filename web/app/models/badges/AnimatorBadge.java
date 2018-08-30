package models.badges;

import models.User;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ANIMATOR")
public class AnimatorBadge extends Badge {

	public AnimatorBadge(User user) {
		super(user);
	}

}
