package models.contributions;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import models.Botanist;

@Entity
@DiscriminatorValue("COLLECTOR")
public class CollectorContribution extends Contribution<CollectorContribution>{
	
	
	@ManyToOne// Création si inconn
	private Botanist collector;
		
	@ManyToMany
	@JoinTable(name="contribution_collectors", 
			joinColumns = { @JoinColumn(name = "contribution_id", nullable = false) },
			inverseJoinColumns = { @JoinColumn(name ="botanist_id", nullable = false) })
	private List<Botanist> otherCollectors;

	
	private boolean collectorNotPresent;
	private boolean collectorNotSure;
	
	
	@Override
	public boolean isInConflict(CollectorContribution other) {
		
		if (isCollectorNotPresent() != other.isCollectorNotPresent()) {
			return true;
		} 
		if (isCollectorNotPresent() && other.isCollectorNotPresent()) {
			return false;
		}
		
		if(getOtherCollectors().isEmpty()){
			if(!other.getOtherCollectors().isEmpty()){
				return true;
			}
			if (isCollectorNotPresent()) {
				return false;
			} else {
				return !getCollector().getId().equals(other.getCollector().getId());
			}
		}else{
			if(other.getOtherCollectors().isEmpty()){
				return true;
			}
			if(getOtherCollectors().size()!=other.getOtherCollectors().size()){
				return true;
			}
			getOtherCollectors().add(getCollector());
			other.getOtherCollectors().add(other.getCollector());
			if(!getOtherCollectors().containsAll(other.getOtherCollectors())){
				getOtherCollectors().remove(getCollector());
				other.getOtherCollectors().remove(other.getCollector());
				return true;
			}else{
				getOtherCollectors().remove(getCollector());
				other.getOtherCollectors().remove(other.getCollector());
				return false;
			}
				
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
		
	}


	@Override
	public void validate(CollectorContribution other) {
		setCollectorNotPresent(other.isCollectorNotPresent());
		if (!isCollectorNotPresent()) {
			this.setCollector(other.getCollector());
			if(other.getOtherCollectors().isEmpty()) this.setOtherCollectors(null);
			else {
				List<Botanist> otherBotanist = new ArrayList();
				for(Botanist botanist : other.getOtherCollectors()){
					botanist = Botanist.findById(botanist.id);
					if(botanist != null) otherBotanist.add(botanist);
				}
				this.setOtherCollectors(otherBotanist);
			}
		}
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
