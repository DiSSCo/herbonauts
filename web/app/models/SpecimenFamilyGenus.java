package models;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import models.contributions.Contribution;
import models.contributions.reports.ContributionReport;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.db.jpa.Model;

/**
 * Utilisé dans les résultats de recherche
 */
public class SpecimenFamilyGenus {

	private String family;
	
	private String genus;

	public SpecimenFamilyGenus(String family, String genus) {
		super();
		this.setFamily(family);
		this.setGenus(genus);
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public String getFamily() {
		return family;
	}

	public void setGenus(String genus) {
		this.genus = genus;
	}

	public String getGenus() {
		return genus;
	}
	
}
