package models.badges;

import models.User;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("PEDAGOGUE")
public class PedagogueBadge extends Badge {

	public PedagogueBadge(User user) {
		super(user);
	}

}
