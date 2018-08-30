package helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import models.contributions.Contribution;
import models.contributions.CountryContribution;

/**
 * Groupe les contributions par collections sans conflits.
 * Ainsi on détermine si la contribution est complète ou si il existe des conflits
 * 
 * - Si plus d'un groupe de contributions -> conflicts
 * - Si un groupe avec plus de N contributions -> complete
 * 
 */
public class ContributionGrouper {



	/**
	 * Groupe les contributions
	 */
	public static List<List<Contribution>> group(List<Contribution> contributions) {
		List<List<Contribution>> groups = new ArrayList<List<Contribution>>();
		
		if (contributions == null) {
			return groups;
		}
		
		LinkedList<Contribution> ungrouped = new LinkedList<Contribution>(contributions);
		
		while(ungrouped.size() > 0) {
			List<Contribution> newGroup = new ArrayList<Contribution>();
			
			// On prend la premiere pour rechercher sa classe d'équivalence
			// (dédicase à Evariste Galois)
			Contribution c = ungrouped.pop();
			newGroup.add(c);
			
			// On ajoute dans le groupe toutes celles qui corresepondent
			for (Contribution ctrb : ungrouped) {
				if (!c.isInConflict(ctrb)) {
					newGroup.add(ctrb);
				}
			}
			// On enleve les contribution fraichement groupées
			for (Contribution moved : newGroup) {
				ungrouped.remove(moved);
			}
			
			// On ajoute la classe à la liste
			groups.add(newGroup);
		}
		
		return groups;
	}
	
}
