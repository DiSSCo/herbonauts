package models.contributions.reports;

import helpers.ContributionGrouper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import models.Mission;
import models.Specimen;
import models.contributions.Contribution;
import models.contributions.ContributionFeedback;
import play.Logger;
import play.db.jpa.Model;
import conf.Herbonautes;

/**
 * Rapport de contributions
 * 
 * La contribution validée n'est lié ni à une mission, ni 
 * `` un utilisateur et ne fait pas mention des indications
 * "déduit", ou "je ne suis pas sûr"
 */
@Entity
@Table(name="H_CONTRIBUTION_REPORT")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE) 
@DiscriminatorColumn(name="type")
public abstract class ContributionReport<T extends Contribution<T>> extends Model {

	@ManyToOne
	private Specimen specimen;
	
	private Integer count;
	
	private boolean complete;
	
	private boolean conflicts;
	
	private Date lastModified;
	
	@Column(insertable = false, updatable = false)
	private String type;
	
	public abstract T getValidatedContribution();
	
	public abstract void takeForExample(Collection<T> contribution);
	
	// Contribution la contribution valide à partir d'une contribution coherente
	public static ContributionReport compile(ContributionFeedback feedback) {
	
		Logger.info("Compiling contribution %s on specimen %s to report", 
				feedback.getContribution().getType(), 
				feedback.getContribution().getSpecimen().getCode());
		
		ContributionReport report = 
			find("type = ? and specimen.id = ?", 
					feedback.getContribution().getType(),
					feedback.getContribution().getSpecimen().id).first();
		
		if (report == null) {
			report = create(feedback.getContribution().getType());
			if (report == null) {
				// Pas de compilation pour ce type de contributions
				Logger.info(" - %s contributions are not compiled", feedback.getContribution().getType());
				return null;
			}
			Logger.info(" - no report, creating one");
			report.setSpecimen(feedback.getContribution().getSpecimen());
		}
		
		Logger.info(" - %s other contributions", feedback.getByOthers() != null ? feedback.getByOthers().size() : 0);
		Logger.info(" - contribution is %s", feedback.getContribution().isCanceled() ? "canceled" : "not canceled");
		report.setCount((feedback.getByOthers() != null ? feedback.getByOthers().size() : 0) + 
		(feedback.getContribution().isCanceled() ? 0 : 1));	// Pour la compilation de la suppression				
		
		List<Contribution> all = new ArrayList<Contribution>(feedback.getByOthers());
		if (!feedback.getContribution().isCanceled()) { // Pas incluse pour une suppression
			all.add(feedback.getContribution());
		}
		
		List<List<Contribution>> groups = ContributionGrouper.group(all);

		// Si plus d'un groupe => Conflicts
		report.setConflicts((groups.size() > 1));
		
		// Si un groupe avec size >= contributionValidationMin => Complete
		int maxCoherence = 0;
		List<Contribution> coherentGroup = null;
		for (List<Contribution> group : groups) {
			if (group.size() > maxCoherence) {
				maxCoherence = group.size();
				coherentGroup = group;
			}
		}
		
		//report.complete =  (maxCoherence >= Herbonautes.get().contributionValidationMin);
		
		report.setComplete((maxCoherence >= Herbonautes.get().contributionValidationMinByType(feedback.getContribution().getType())));	
		
		if (report.isComplete()) {
			report.takeForExample(coherentGroup);
		}
		
		Logger.info(" - count : %s", report.getCount());
		Logger.info(" - conflicts : %s", report.isConflicts());
		Logger.info(" - complete : %s", report.isComplete());
		
		report.setLastModified(new Date());

		report.save();
		
		
		if(report.isComplete()){
			Specimen specimen = report.getSpecimen();
			if(feedback.getContribution().getType().equals("UNUSABLE")){
				specimen.setComplete(true);
				specimen.save();
			}else{
				List<ContributionReport> contributionReports = specimen.getContributionReports();
				List<String> requiredType = feedback.getContribution().getMission().getRequiredContributionTypes();
				if(contributionReports.size() == requiredType.size()){
					specimen.setComplete(true);
					for(ContributionReport contributionReport : contributionReports){
						if(!contributionReport.isComplete()){
							specimen.setComplete(false);
							break;
						}
					}
					if(specimen.isComplete()) {
						Logger.info(" Le spécimen %s est complet.", specimen.getCode());
						specimen.save();
					}
				}
				
			}
		}
		
		return report;
	}
	
	private static ContributionReport create(String type) {
		switch (Contribution.Type.valueOf(type)) {
			case COUNTRY: 	return new CountryContributionReport();
			case REGION_1: 	return new RegionLevel1ContributionReport();
			case REGION_2: 	return new RegionLevel2ContributionReport();
			case DATE: 		return new DateContributionReport();
//			case BOTANISTS: return new BotanistsContributionReport();
			case COLLECTOR: return new CollectorContributionReport();
			case IDENTIFIEDBY:	return new IdentifiedByContributionReport(); 
			case LOCALITY: return new LocalityContributionReport();
			case GEOLOCALISATION: return new GeolocalisationContributionReport();
			case UNUSABLE: return new UnusableContributionReport();
			default: return null;
		}
	}

	public void setSpecimen(Specimen specimen) {
		this.specimen = specimen;
	}

	public Specimen getSpecimen() {
		return specimen;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Integer getCount() {
		return count;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setConflicts(boolean conflicts) {
		this.conflicts = conflicts;
	}

	public boolean isConflicts() {
		return conflicts;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
	
}
