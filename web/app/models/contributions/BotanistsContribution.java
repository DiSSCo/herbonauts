package models.contributions;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import play.Logger;

import models.Botanist;

@Entity
@DiscriminatorValue("BOTANISTS")
public class BotanistsContribution extends Contribution<BotanistsContribution> {

	@ManyToOne// Création si inconnu
	private Botanist determiner;
	
	@ManyToOne// Création si inconn
	private Botanist collector;
		
	@ManyToMany
	@JoinTable(name="contribution_collectors", 
			joinColumns = { @JoinColumn(name = "contribution_id", nullable = false) },
			inverseJoinColumns = { @JoinColumn(name ="botanist_id", nullable = false) })
	private List<Botanist> otherCollectors;

	private boolean determinerNotPresent;
	private boolean determinerNotSure;
	
	private boolean collectorNotPresent;
	private boolean collectorNotSure;
	
	
	@Override
	public boolean isInConflict(BotanistsContribution other) {
		if (isDeterminerNotPresent() != other.isDeterminerNotPresent()) {
			return true;
		}
		if (isCollectorNotPresent() != other.isCollectorNotPresent()) {
			return true;
		}
		if (isCollectorNotPresent() && other.isCollectorNotPresent() && isDeterminerNotPresent() && other.isDeterminerNotPresent()) {
			return false;
		}
		
		if (isCollectorNotPresent() && isDeterminerNotPresent()) {
			return false;
		} else if (isCollectorNotPresent()) {
			return !getDeterminer().getId().equals(other.getDeterminer().getId());
		} else if (isDeterminerNotPresent()) {
			return !getCollector().getId().equals(other.getCollector().getId());
		} else {
			return 
				!getDeterminer().getId().equals(other.getDeterminer().getId()) ||
				!getCollector().getId().equals(other.getCollector().getId());
		}
		
	}


	@Override
	public void sanitize() {
	
		if (isCollectorNotPresent()) {
			setCollector(null);
			setCollectorNotSure(false);
			setOtherCollectors(null);
		} else {
			// On marque les nouveaux botanistes 
			//getCollector().setCreatedByUser((getCollector().id == null));
			
			/*List<Botanist> persistentOtherCollectors = new ArrayList();
			
			if (getOtherCollectors() != null) {
				for (Botanist botanist : getOtherCollectors()) {
					if (botanist != null) {
						botanist.setCreatedByUser((botanist.id == null));
					}
					if (botanist.id != null) {
						botanist = Botanist.findById(botanist.id);
					} else if (botanist.getName() == null || botanist.getName().trim().length() == 0) {
						botanist = null;
					}
					
					if (botanist != null) {
						persistentOtherCollectors.add(botanist);
					}
				}
			}
			setOtherCollectors(persistentOtherCollectors);*/
		
		}
		
		if (isDeterminerNotPresent()) {
			setDeterminer(null);
			setDeterminerNotSure(false);
		} else {
			// On marque les nouveaux botanistes 
			//getDeterminer().setCreatedByUser((getDeterminer().id == null));
		}
		
	}


	@Override
	public void validate(BotanistsContribution other) {
		setDeterminerNotPresent(other.isDeterminerNotPresent());
		setCollectorNotPresent(other.isCollectorNotPresent());
		
		if (!isDeterminerNotPresent()) {
			this.setDeterminer(other.getDeterminer());
		}
		if (!isCollectorNotPresent()) {
			this.setCollector(other.getCollector());
		}
	}


	public void setDeterminer(Botanist determiner) {
		this.determiner = determiner;
	}


	public Botanist getDeterminer() {
		return determiner;
	}


	public void setCollector(Botanist collector) {
		this.collector = collector;
	}


	public Botanist getCollector() {
		return collector;
	}


	public void setOtherCollectors(List<Botanist> otherCollectors) {
		this.otherCollectors = otherCollectors;
	}


	public List<Botanist> getOtherCollectors() {
		return otherCollectors;
	}


	public void setDeterminerNotPresent(boolean determinerNotPresent) {
		this.determinerNotPresent = determinerNotPresent;
	}


	public boolean isDeterminerNotPresent() {
		return determinerNotPresent;
	}


	public void setDeterminerNotSure(boolean determinerNotSure) {
		this.determinerNotSure = determinerNotSure;
	}


	public boolean isDeterminerNotSure() {
		return determinerNotSure;
	}


	public void setCollectorNotPresent(boolean collectorNotPresent) {
		this.collectorNotPresent = collectorNotPresent;
	}


	public boolean isCollectorNotPresent() {
		return collectorNotPresent;
	}


	public void setCollectorNotSure(boolean collectorNotSure) {
		this.collectorNotSure = collectorNotSure;
	}


	public boolean isCollectorNotSure() {
		return collectorNotSure;
	}
	
}
