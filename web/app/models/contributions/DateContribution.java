package models.contributions;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

import play.Logger;

import models.Country;

@Entity
@DiscriminatorValue("DATE")
public class DateContribution extends Contribution<DateContribution> {

	private boolean period;
	
	private Date collectDate;
	
	private Date collectStartDate;
	
	private Date collectEndDate;

	@Override
	public void sanitize() {
		if (isNotPresent()) {
			setCollectDate(null);
			setCollectStartDate(null);
			setCollectEndDate(null);
			setNotSure(false);
		}
		if (isPeriod()) {
			setCollectDate(null);
		} else {
			setCollectStartDate(null);
			setCollectEndDate(null);
		}
	}

	@Override
	public boolean isInConflict(DateContribution other) {
		if (other == null) { return true; }
		
		if (this.isNotPresent() != other.isNotPresent()) {
			return true;
		}
		
		if (this.isNotPresent() && other.isNotPresent()) {
			return false;
		}
		
		if (this.isPeriod() != other.isPeriod()) {
			return true;
		}
		
		if (this.isPeriod()) {
			if (!this.getCollectStartDate().equals(other.getCollectStartDate()) ||
					!this.getCollectEndDate().equals(other.getCollectEndDate())) {
				return true;
			}
		} else {
			return !this.getCollectDate().equals(other.getCollectDate()) ;
		}
		
		return false;
	}

	@Override
	public void validate(DateContribution other) {
		
		Logger.info("Validate date %s", other.getCollectDate());
		
		this.setNotPresent(other.isNotPresent());
		this.setPeriod(other.isPeriod());
		this.setCollectDate(other.getCollectDate());
		this.setCollectStartDate(other.getCollectStartDate());
		this.setCollectEndDate(other.getCollectEndDate());
		this.setNotSure(false);
		
		sanitize();
	}

	public void setPeriod(boolean period) {
		this.period = period;
	}

	public boolean isPeriod() {
		return period;
	}

	public void setCollectDate(Date collectDate) {
		this.collectDate = collectDate;
	}

	public Date getCollectDate() {
		return collectDate;
	}

	public void setCollectStartDate(Date collectStartDate) {
		this.collectStartDate = collectStartDate;
	}

	public Date getCollectStartDate() {
		return collectStartDate;
	}

	public void setCollectEndDate(Date collectEndDate) {
		this.collectEndDate = collectEndDate;
	}

	public Date getCollectEndDate() {
		return collectEndDate;
	}
	
}
