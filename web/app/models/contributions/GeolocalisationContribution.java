package models.contributions;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import conf.Herbonautes;

import libs.GPS;

@Entity 
@DiscriminatorValue("GEOLOCALISATION")
public class GeolocalisationContribution extends Contribution<GeolocalisationContribution> {

	private Float latitude;
	private Float longitude;
	
	@Column(name="H_PRECISION")
	public String precision;
	
	// Pas de conflit pour les contributions
	@Override
	public boolean isInConflict(GeolocalisationContribution other) {
		// Idem localité
		
		if (this.isNotPresent() != other.isNotPresent()) {
			return true;
		}
		
		if (this.isNotPresent()) {
			return false;
		}
		
		return 
			GPS.distance(latitude, longitude, other.latitude, other.longitude) > Herbonautes.get().geolocalisationConflictDistance;
		
	}
	
	public String getPrecision() {
		return precision;
	}

	public void setPrecision(String precision) {
		this.precision = precision;
	}

	@Override
	public void sanitize() {
		if (this.isNotPresent()) {
			this.setNotSure(false);
			this.setLongitude(null);
			this.setLatitude(null);
			this.setPrecision(null);
		}
	}
	@Override
	public void validate(GeolocalisationContribution other) {
		this.setNotPresent(other.isNotPresent());
		this.setLongitude(other.getLongitude());
		this.setLatitude(other.getLatitude());
		this.setPrecision(other.getPrecision());
	}

	public void setLatitude(Float latitude) {
		this.latitude = latitude;
	}

	public Float getLatitude() {
		return latitude;
	}

	public void setLongitude(Float longitude) {
		this.longitude = longitude;
	}

	public Float getLongitude() {
		return longitude;
	}
	
}
