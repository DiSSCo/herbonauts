package models.contributions;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity 
@DiscriminatorValue("LOCALITY")
public class LocalityContribution extends Contribution<LocalityContribution> {

	private String locality;
	
	@Override
	public boolean isInConflict(LocalityContribution other) {
		// Pour l'instant on ne fait rien sur le contenu.
		// Par contre la présence de l'information peut être un critère
		if (this.isNotPresent() != other.isNotPresent()) {
			return true;
		}

		return false;
	}
	
	@Override
	public void sanitize() {
		if (this.isNotPresent()) {
			this.setLocality(null);
			this.setNotSure(false);
		}
	}

	@Override
	public void validate(LocalityContribution other) {
		this.setNotPresent(other.isNotPresent());
		this.setLocality(other.getLocality());
		this.setNotSure(other.isNotSure());
		sanitize();
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public String getLocality() {
		return locality;
	}
	
}
