package models.badges;

import models.User;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("CLASSIFIER")
public class ClassifierBadge extends Badge {

	public ClassifierBadge(User user) {
		super(user);
	}

}
