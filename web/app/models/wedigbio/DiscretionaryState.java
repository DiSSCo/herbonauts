/**
 * 
 */
package models.wedigbio;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import models.Mission;
import models.questions.ContributionQuestion;
import play.Logger;

/**
 * Une classe fourre-tout pour les attributs particuliers à un centre de transcription
 * Encapsule le workUnit qui est la ponderation d'une contribution
 * la somme des ponderations des questions d'une mission égale 1
 * @author schagnoux
 *
 */
public class DiscretionaryState {
	String workUnit;
	public DiscretionaryState(Long missionId) {
		DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
		df.applyPattern("##.##");
		workUnit = df.format(1.00/(ContributionQuestion.countQuestionsForMission(missionId)));	
	}
	public String getWorkUnit() {
		return workUnit;
	}
	
}
