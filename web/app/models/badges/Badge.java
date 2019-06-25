package models.badges;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import models.User;
import play.db.jpa.Model;

@Entity
@Table(name="H_BADGE")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE) 
@DiscriminatorColumn(name="type")
public class Badge extends Model {

	// TODO enum
	public enum Type {
		COOL,
		SHERLOCK,
		INVITATION,
		PODIUM,
		NIGHT,
		EXPLORER,
		ENDER,
		PIONEER,
		CLASSIFIER,
		PEDAGOGUE,
		WRITER,
		ANIMATOR,
		CANDLE,
		SOLIDARY,

		HIGH_FIVE,
		LUCKY
	};
	
	@ManyToOne
	private User user;
	
	@Column(insertable=false, updatable=false)
	private String type;
	
	public Badge(User user) {
		super();
		this.setUser(user);
	}
	
	/**
	 * Fabrique statique
	 */
	private static Badge createBadge(User user, Type type) {
		switch(type) {
			case COOL: return new CoolBadge(user);
			case SHERLOCK: return new SherlockBadge(user);
			case PODIUM: return new PodiumBadge(user);
			case NIGHT: return new NightBadge(user);
			case EXPLORER: return new ExplorerBadge(user);
			case ENDER: return new EnderBadge(user);
			case ANIMATOR: return new AnimatorBadge(user);
			case CLASSIFIER: return new ClassifierBadge(user);
			case PIONEER: return new PioneerBadge(user);
			case PEDAGOGUE: return new PedagogueBadge(user);
			case WRITER: return new WriterBadge(user);
			case CANDLE: return new CandleBadge(user);
			case SOLIDARY: return new SolidaryBadge(user);
			case INVITATION: return new InvitationBadge(user);
			case LUCKY: return new LuckyBadge(user);
			case HIGH_FIVE: return new HighFiveBadge(user);
		}
		return null;
	}
	
	/**
	 * Ajoute un badge à un utilisateur
	 */
	public static Badge add(User user, Type type) {
		if (user.hasBadge(type)) {
			return null;
		}
		Badge badge = createBadge(user, type);
		if (badge != null) {
			badge.save();
			badge.refresh();
		}
		return badge;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
	
}
