package models.contributions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.PostPersist;
import javax.persistence.Table;

import models.Mission;
import models.Specimen;
import models.User;
import models.contributions.reports.ContributionReport;
import play.db.jpa.JPA;
import play.db.jpa.Model;

@Entity
@Table(name="H_CONTRIBUTION")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE) 
@DiscriminatorColumn(name="type", discriminatorType=DiscriminatorType.STRING)
public abstract class Contribution<T extends Contribution> extends Model {

	// TODO enum
	public enum Type {
		COUNTRY,
		REGION_1,
		REGION_2,
		DATE,
//		BOTANISTS,
//		Récolteur
		COLLECTOR,
//		Déterminateur
		IDENTIFIEDBY,
		LOCALITY,
		GEOLOCALISATION,
		UNUSABLE
	};
	
	@ManyToOne
	private User user;
	
	@ManyToOne
	private Specimen specimen;
	
	@ManyToOne
	private Mission mission;
	
	@Column(name="CREATION_DATE", updatable = false)
	private Date date;
	
	private boolean canceled = false;
	
	public Contribution() {
		this.setDate(new Date());
	}
	
	@Column(insertable = false, updatable = false)
	private String type;
	
	private boolean notSure;
	private boolean notReadable;
	private boolean notPresent;
	
	private boolean deducted;
	
	private boolean validatedFromOther;
	
	private boolean report;
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// JPA Hooks
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	@PostPersist
	public void clearCaches() { }

	// TODO Eventuellement un tag dédié (#{activityMessage contribution /})
	public String getI18nKey() {
		return "contribution." + this.getType().toLowerCase();
	}
	
	
	public static List<Specimen> getLastContributedSpecimensForUser(String login) {
		/*JPA.em()
			.createQuery("select distinct sub.specimen from (select c Contribution c where c.user.login = :login order by c.id desc) sub", Specimen.class)
			.setParameter("login", login)
			.setMaxResults(3)
			.getResultList();*/
		
		// SANS LOGIN, AVEC ID ?
		
		return JPA.em()
			.createNativeQuery(
				"SELECT DISTINCT S.* " +
				"FROM H_SPECIMEN S " +
				"INNER JOIN " +
				"    (SELECT S.ID S_ID, C.ID " +
				"     FROM H_CONTRIBUTION C " +
				"     INNER JOIN SPECIMEN S ON C.SPECIMEN_ID = S.ID " +
				"     INNER JOIN USER U ON C.USER_ID = U.ID " +
				"     WHERE U.LOGIN = :login " +
				"     ORDER BY C.ID ASC) T" + 
				"ON S.ID = T.S_ID", Specimen.class)
			.setParameter("login", login)
			.setMaxResults(2)
			.getResultList();
	}
	
	/**
	 * Annule la contribution. Elle n'est pas effacée mais marquée
	 * <code>canceled = true</code>
	 */
	public final static void cancel(Contribution contribution) {
		contribution.setCanceled(true);
		contribution.save();
	}
	
	/**
	 * Determine si une contribution est en conflit avec une autre
	 * @param other l'autre contribution
	 * @return vrai si un conflit existe, faux sinon
	 */
	public abstract boolean isInConflict(T other);

	public abstract void validate(T other);
	
	/**
	 * Nettoie la contribution. Par exemple, si <code>notPresent</code>
	 * est vrai, alors on positionne les valeurs à <code>null</code>.
	 */
	// TODO Hook JPA
	public abstract void sanitize();
	
	public final List<Contribution> getByOtherUsers() {
		List<Contribution> contributions = Contribution.find(
				"canceled = false and type = ? and mission.id = ? and specimen.id = ? and user.id != ?",
				this.getType(),
				this.getMission().id,
				this.getSpecimen().id,
				this.getUser().id).fetch();
		return (contributions != null) ? contributions : new ArrayList<Contribution>();
	}
	
	
	public static ContributionFeedback feedback(Contribution contribution) {
		return feedback(contribution, true);
	}
	
	/**
	 * Construit un "rapport" de contribution contenant la 
	 * contribution elle-même ainsi qu'un résumé des conflits
	 * au besoin
	 */
	public static ContributionFeedback feedback(Contribution contribution, boolean showConflicts) {
		ContributionFeedback feedback = new ContributionFeedback();
		feedback.setContribution(contribution);
		feedback.setShowConflicts(showConflicts);
		feedback.setByOthers(contribution.getByOtherUsers());
	
		boolean conflicts = false;
		for (Contribution other : feedback.getByOthers()) {
			if (conflicts = contribution.isInConflict(other)) {
				break;
			}
		}
		
		feedback.setConflicts(conflicts);

		// Compilation de la contribution
		ContributionReport report = ContributionReport.compile(feedback);
		
		feedback.setReport(report);
		
		return feedback;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public void setSpecimen(Specimen specimen) {
		this.specimen = specimen;
	}

	public Specimen getSpecimen() {
		return specimen;
	}

	public void setMission(Mission mission) {
		this.mission = mission;
	}

	public Mission getMission() {
		return mission;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setNotSure(boolean notSure) {
		this.notSure = notSure;
	}

	public boolean isNotSure() {
		return notSure;
	}

	public void setNotReadable(boolean notReadable) {
		this.notReadable = notReadable;
	}

	public boolean isNotReadable() {
		return notReadable;
	}

	public void setNotPresent(boolean notPresent) {
		this.notPresent = notPresent;
	}

	public boolean isNotPresent() {
		return notPresent;
	}

	public void setDeducted(boolean deducted) {
		this.deducted = deducted;
	}

	public boolean isDeducted() {
		return deducted;
	}

	public void setValidatedFromOther(boolean validatedFromOther) {
		this.validatedFromOther = validatedFromOther;
	}

	public boolean isValidatedFromOther() {
		return validatedFromOther;
	}

	public void setReport(boolean report) {
		this.report = report;
	}

	public boolean isReport() {
		return report;
	}
	
}
