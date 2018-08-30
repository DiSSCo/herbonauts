package models.badges;

import models.User;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("INVITATION")
public class InvitationBadge extends Badge {

	public InvitationBadge(User user) {
		super(user);
	}

}
