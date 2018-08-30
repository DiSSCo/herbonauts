package models.contributions;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.mvc.Scope.Flash;

import models.contributions.reports.ContributionReport;

/**
 * Informations renvoyées à l'utilisateur après qu'il a 
 * soumis une contribution
 */
public class ContributionFeedback {

	private Contribution contribution;
	
	private List<Contribution> byOthers;
	
	// 
	private boolean complete;
	
	private boolean conflicts;
	
	private boolean showConflicts;
	
	public boolean isShowConflicts() {
		return showConflicts;
	}

	public void setShowConflicts(boolean showConflicts) {
		this.showConflicts = showConflicts;
	}

	private boolean levelUp;
	
	private Integer userLevel;
	
	private Integer userPendingLevel;
	
	private ContributionReport report;
	
	// Badges gagnés ... autres trucs
	private Map<String, String> stringAttributes = new HashMap<String, String>();
	
	// TODO gerer listes
	public void addAttribute(String key, String value) {
		stringAttributes.put(key, value);
	}
	
	public Map<String, String> getStringAttributes() {
		return stringAttributes;
	}

	public void setContribution(Contribution contribution) {
		this.contribution = contribution;
	}

	public Contribution getContribution() {
		return contribution;
	}

	public void setByOthers(List<Contribution> byOthers) {
		this.byOthers = byOthers;
	}

	public List<Contribution> getByOthers() {
		return byOthers;
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

	public void setLevelUp(boolean levelUp) {
		this.levelUp = levelUp;
	}

	public boolean isLevelUp() {
		return levelUp;
	}

	public void setUserLevel(Integer userLevel) {
		this.userLevel = userLevel;
	}

	public Integer getUserLevel() {
		return userLevel;
	}

	public void setUserPendingLevel(Integer userPendingLevel) {
		this.userPendingLevel = userPendingLevel;
	}

	public Integer getUserPendingLevel() {
		return userPendingLevel;
	}

	public void setReport(ContributionReport report) {
		this.report = report;
	}

	public ContributionReport getReport() {
		return report;
	}

	
}
