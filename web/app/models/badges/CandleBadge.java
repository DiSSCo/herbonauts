package models.badges;

import models.User;
import models.tags.TagType;
import play.db.jpa.JPA;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("CANDLE")
public class CandleBadge extends Badge {

	public CandleBadge(User user) {
		super(user);
	}

}
