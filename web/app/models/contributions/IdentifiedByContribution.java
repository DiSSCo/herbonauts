package models.contributions;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import models.Botanist;

@Entity
@DiscriminatorValue("IDENTIFIEDBY")
public class IdentifiedByContribution extends Contribution<IdentifiedByContribution>{
	@ManyToOne// Création si inconnu
	private Botanist determiner;
	private boolean determinerNotPresent;
	private boolean determinerNotSure;
	
	
	
	@Override
	public boolean isInConflict(IdentifiedByContribution other) {
		if (isDeterminerNotPresent() != other.isDeterminerNotPresent()) {
			return true;
		}
		
		if (isDeterminerNotPresent() && other.isDeterminerNotPresent()) {
			return false;
		}
		
		if (isDeterminerNotPresent()) {
			return false;
		} else {
			return !getDeterminer().getId().equals(other.getDeterminer().getId()) ;
		}
		
	}


	@Override
	public void sanitize() {
		
		if (isDeterminerNotPresent()) {
			setDeterminer(null);
			setDeterminerNotSure(false);
		} 
	}


	@Override
	public void validate(IdentifiedByContribution other) {
		setDeterminerNotPresent(other.isDeterminerNotPresent());
		
		if (!isDeterminerNotPresent()) {
			this.setDeterminer(other.getDeterminer());
		}
	}


	public void setDeterminer(Botanist determiner) {
		this.determiner = determiner;
	}


	public Botanist getDeterminer() {
		return determiner;
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
	
}
