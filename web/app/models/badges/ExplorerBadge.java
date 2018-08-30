package models.badges;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import models.User;

@Entity
@DiscriminatorValue("EXPLORER")
public class ExplorerBadge extends Badge {

	public ExplorerBadge(User user) {
		super(user);
	}

}
