package controllers;

import models.Botanist;
import models.Mission;
import models.Specimen;
import models.User;

import org.apache.commons.lang.StringUtils;

import play.Logger;

import conf.Herbonautes;

import transfert.SearchResult;
import transfert.SearchResultJsonSerializer;

/**
 * Controller de recherche
 */
public class Search extends Application {

	/**
	 * Point d'entrée unique de la recherche.
	 * Retourne les résultats au format JSON ou HTML selon
	 * si la requête provient d'un appel AJAX ou non
	 */
	public static void search(String term, Integer page) {
		/*if (request.isAjax()) {
			jsonQuickAccess(term);
		} else {
			classicSearch(term, page);
		}*/
		classicSearch(term, page);
	}
	
	/**
	 * Recherche "classique", avec numero de page et retour HTML
	 */
	private static void classicSearch(String term, Integer page) {
		
		Logger.info("SEARCH %s %d", term, page);

		// SearchResult result = new SearchResult();

		Integer limit = Herbonautes.get().searchResultLength;

		/*result.missions = Mission.search(term, limit, page);
		result.botanists = Botanist.search(term, limit, page);
		result.users = User.search(term, limit, page);
		result.familiesGenuses = Specimen.searchFamilyGenus(term, limit, page);
		result.specimens = Specimen.search(term, limit, page);

		Integer currentPage = (page == null ? 1 : page);

		renderTemplate("/Search/results.html", term, result, currentPage);*/
		renderTemplate("/Search/results.html", term);

	}
	

	/**
	 * Format "accès rapide", retourne les n premiers résultats pour 
	 * chaque catégorie
	 */
	/*private static void jsonQuickAccess(String term) {
		if (StringUtils.isEmpty(term)) {
			badRequest();
		}
		
		SearchResult result = new SearchResult();
		
		result.missions = Mission.search(term, Herbonautes.get().quickSearchLengthByCategory(Herbonautes.Category.MISSIONS));
		result.botanists = Botanist.search(term, Herbonautes.get().quickSearchLengthByCategory(Herbonautes.Category.BOTANISTS));
		result.users = User.search(term, Herbonautes.get().quickSearchLengthByCategory(Herbonautes.Category.HERBONAUTES));
		result.familiesGenuses = Specimen.searchFamilyGenus(term, Herbonautes.get().quickSearchLengthByCategory(Herbonautes.Category.SPECIMENS));
		result.specimens = Specimen.search(term, Herbonautes.get().quickSearchLengthByCategory(Herbonautes.Category.SPECIMENS));
		
		renderJSON(result, SearchResultJsonSerializer.get());
	}*/
	
}
