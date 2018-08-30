package models.contributions;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import models.Country;

@Entity
@DiscriminatorValue("COUNTRY")
public class CountryContribution extends Contribution<CountryContribution> {

	@ManyToOne
	private Country country;
	
	@Override
	public boolean isInConflict(CountryContribution other) {
		CountryContribution otherCountry = (CountryContribution) other;
		
		// Si les 2 sont "non presente" = pas de conflit
		if (this.isNotPresent() && otherCountry.isNotPresent()) { // 1. identiques -> pas de conflits
			return false;
		} else  if (this.isNotPresent() || otherCountry.isNotPresent()) { // 2. differents (cf 1.)
			return true;
		} else {
			return !(this.getCountry().id.equals(otherCountry.getCountry().id)); // 3. comparons les pays
		}
	}
	
	@Override
	public String toString() {
		return this.getCountry().getName();
	}

	@Override
	public void sanitize() {
		if (isNotPresent()) {
			setCountry(null);
			setDeducted(false);
			setNotSure(false);
		}
	}

	@Override
	public void validate(CountryContribution other) {
		this.setNotPresent(other.isNotPresent());
		this.setCountry(other.getCountry());
		this.setNotSure(false);
		this.setDeducted(false);
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public Country getCountry() {
		return country;
	}
	
}
