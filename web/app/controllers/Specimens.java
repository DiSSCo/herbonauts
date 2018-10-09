package controllers;

import static controllers.Security.connectedId;
import static controllers.Security.connectedLogin;

import java.util.*;

import conf.Herbonautes;
import models.*;
import models.questions.ContributionAnswer;
import models.questions.ContributionQuestion;
import models.questions.special.GeolocalisationStaticAnswer;
import models.quiz.Question;
import models.serializer.SpecimenSimpleJsonSerializer;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.NoTransaction;
import play.db.jpa.Transactional;
import models.comments.SpecimenComment;
import models.contributions.Contribution;
import models.contributions.GeolocalisationContribution;
import models.contributions.reports.ContributionReport;
import models.contributions.reports.GeolocalisationContributionReport;
import models.serializer.contributions.GeolocalisationContributionJsonSerializer;
import services.JPAUtils;

/**
 * Gestion des specimen
 */
public class Specimens extends Application {

	/**
	 * Affichage de la page de specimen
	 */
	public static void show(String institute, String collection, String code) {
		//Specimen specimen = Specimen.find(
		//		"institute = ? and collection = ? and code = ?", institute,
		//		collection, code).first();


		// MASTER

		//SpecimenMaster master =  SpecimenMaster.find(
		//				"institute = ? and collection = ? and code = ?", institute,
		//				collection, code).first();
//
		//notFoundIfNull(master);


		List<Specimen> specimens = Specimen.find(
			"institute = ? and collection = ? and code = ? and mission is not null", institute,
			collection, code).fetch();

		if (specimens.isEmpty()) {
			notFound();
		}

		Specimen specimen = specimens.get(0);

		// master
		SpecimenMaster master = specimen.getMaster();

				//Map<User, Map<String, Contribution>> contributions = specimen
		//		.getAllContributionsByUserAndType();

		Map<Long, Map<String, Map<String, ContributionAnswer>>> missionsAnswers = new HashMap<Long, Map<String, Map<String, ContributionAnswer>>>();

		for (Specimen s : specimens) {
			Map<String, Map<String, ContributionAnswer>> answersByLogin =
					ContributionAnswer.findAllUserAnswersForSpecimenByLoginAndName(s.id);
			missionsAnswers.put(s.getMission().getId(), answersByLogin);
		}

		// TODO trier les types de contribution utiles ici
		// plutot que dans la vue

		//List<String> contributionTypes = usefulContributionTypes(specimen);

		Map<Long, List<ContributionQuestion>> missionsQuestions = new HashMap<Long, List<ContributionQuestion>>();

		for (Specimen s : specimens) {
			List<ContributionQuestion> questions = ContributionQuestion.findAllActiveForMission(s.getMission().id);
			missionsQuestions.put(s.getMission().id, questions);
		}

		String tab = params.get("tab");
		if (tab == null) {
			tab = "contributions";
		}

		Long forceDiscussion = params.get("discussion") != null ? Long.valueOf(params.get("discussion")) : null;
		if (forceDiscussion != null) {
			tab = "comments";
		}

		List<Mission> missions = new ArrayList<Mission>();

		for (Specimen s : specimens) {
			missions.add(s.getMission());
		}


		List<Specimen.SpecimenAttribute> attributes = new ArrayList<Specimen.SpecimenAttribute>();
		for (Specimen s : specimens) {
			attributes.addAll(s.getAttributesForUser(connectedId()));
		}

		//List<Specimen.SpecimenAttribute> attributes = specimen.getAttributesForUser(connectedId());




		int multipleActiveSpecimenCount = 0;
		int hasActiveMissionCount = 0;
		//if (specimens.size() > 0) {
		Specimen defaultSpecimen = null;
		for (Specimen s : specimens) {
			Logger.info("Specimen in mission %s :", s.getMission().getTitle());
			if (s.hasActiveMission() /*&& !s.isCompleted()*/) {
				Logger.info(" active not complete");
				multipleActiveSpecimenCount++;
				if (defaultSpecimen == null) {
					defaultSpecimen = s;
				}
			} else {
				Logger.info(" disabled");
			}

			if (s.hasActiveMission()) {
				hasActiveMissionCount++;
			}
		}

		if (defaultSpecimen == null) {
			defaultSpecimen = specimens.get(0);
		}

		//}
		boolean multipleActiveSpecimen = multipleActiveSpecimenCount > 1;
		boolean hasActiveMission = hasActiveMissionCount > 0;

		List<ContributionQuestion> allQuestions = new ArrayList<ContributionQuestion>();

		for (List<ContributionQuestion> missionQuestion : missionsQuestions.values()) {
			allQuestions.addAll(missionQuestion);
		}



		render(specimen, defaultSpecimen, multipleActiveSpecimen, hasActiveMission, master, specimens, tab, missions, attributes, missionsAnswers, allQuestions, missionsQuestions, forceDiscussion);
	}

	/**
	 * Flag "spécimen vu" déclenché depuis la page de contribution
	 */
	// TODO : Securiser avec cache de quelques secondes (eviter un spam sur tous
	// les specimens)
	public static void markDisplayed(Long id) {

		Specimen specimen = Specimen.findById(id);

		specimen.setDisplayed(true);
		specimen.save();

		ok();
	}

	/**
	 * Bulle d'informations affichées dans les cartes et timelines
	 */
	public static void bubble(Long id) {

		//SpecimenMaster specimen = SpecimenMaster.findById(id);

		Specimen specimen = Specimen.findById(id);
		List<Specimen.SpecimenAttribute> attributes = specimen.getAttributesForUser(connectedId());


		//
		//Map<String, ContributionReport> reports = specimen
		//		.getContributionReportsByType();
		//List<String> contributionTypes = usefulContributionTypes(specimen);
		//
		//Map<String, Contribution> userContributions = null;
		//if (connectedLogin() != null) {
		//	userContributions = specimen
		//		.getContributionsMapByLogin(connectedLogin());
		//}


		//List<Specimen.SpecimenAttribute> attributes = specimen.getAttributesForUser(connectedId());

		//Map<String, Contribution> contributions = User.

		render(specimen, attributes);
	}

	/**
	 * Bulles multiples
	 */
	public static void bubbles(List<Long> id) {

		List<Specimen> specimens =
				JPA.em().createQuery("select s from Specimen s where s.id in (:ids)", Specimen.class)
				.setParameter("ids", id)
				.getResultList();

		HashMap<Specimen, List<Specimen.SpecimenAttribute>> attributesMap = new HashMap<Specimen, List<Specimen.SpecimenAttribute>>();

		for (Specimen specimen : specimens) {
			attributesMap.put(specimen, specimen.getAttributesForUser(connectedId()));
		}

		//Map<String, Contribution> contributions = User.

		render(attributesMap);
	}



	/**
	 * Affichage de la liste des specimen pour une famille donnée (accés depuis
	 * quick search ou liens specimens)
	 */
	@Transactional(readOnly = true)
	public static void list(String family, String genus, String specificEpithet, String institute, String collection, Integer page) {

		if (page == null) {
			page = 1;
		}

		Integer pageSize = Herbonautes.get().pageLengthSpecimenList;

		// institute

		if (institute != null) {
			Long totalCount = Specimen.count("institute = ?", institute);
			List<Specimen> specimens = Specimen.find("institute = ?", institute)
					.fetch(page, pageSize);

			Long pageCount = Double.valueOf(Math.floor((totalCount- 1) / pageSize) + 1).longValue();

			render(specimens, institute, totalCount, page, pageCount);
		}

		// collection

		if (collection != null) {
			Long totalCount = Specimen.count("collection = ?", collection);
			List<Specimen> specimens = Specimen.find("collection = ?", collection)
					.fetch(page, pageSize);

			Long pageCount = Double.valueOf(Math.floor((totalCount- 1) / pageSize) + 1).longValue();

			render(specimens, collection, totalCount, page, pageCount);
		}

		// Specimens

		if (specificEpithet == null) {
			list(null, family, genus, null, null, page);
			return;
		}

		Long totalCount = Specimen.count("(family = ? and genus = ? and specificEpithet is null) or (genus = ? and specificEpithet = ?)",
				genus, specificEpithet, genus, specificEpithet);
		List<Specimen> specimens = Specimen.find("(family = ? and genus = ? and specificEpithet is null) or (genus = ? and specificEpithet = ?)",
				genus, specificEpithet, genus, specificEpithet)
				.fetch(page, pageSize);

		Long pageCount = Double.valueOf(Math.floor((totalCount- 1) / pageSize) + 1).longValue();

		render(specimens, family, genus, specificEpithet, totalCount, page, pageCount);
	}

	/**
	 * Récupération de la geolocalisation validée pour un spécimen. Le résultat
	 * est unique mais il est envoyé sous la forme d'une liste pour utiliser le
	 * même code javascript d'affichage des cartes (mapTab.tag)
	 */
	@Transactional(readOnly = true)
	public static void geolocalisationContributions(Long id) {

		// MAaster
		//List<Specimen> specimens = Specimen.find("master.id = ?", id).fetch();

		Specimen specimen = Specimen.findById(id);
		List<Specimen> specimens = Arrays.asList(specimen);

		//master.

		List<GeolocalisationStaticAnswer> geos = new ArrayList<GeolocalisationStaticAnswer>();
		for (Specimen s : specimens) {
			Logger.info("geos for %s", s.id);
			List<GeolocalisationStaticAnswer> ans = GeolocalisationStaticAnswer.find("specimenId = ?", s.id).fetch();
			for (GeolocalisationStaticAnswer a : ans) {
				a.setMasterId(id);
			}
			geos.addAll(ans);
		}

		renderJSON(geos, SpecimenSimpleJsonSerializer.get());
	}


	@Transactional(readOnly = true)
	public static void specimenMedia(Long id) {
		List<SpecimenMedia> mediaList = SpecimenMedia.find("specimenId = ? order by mediaNumber asc", id).fetch();
		renderJSON(mediaList);
	}
	/**
	 * Retourne la liste des types requis pour le specimens
	 * (union des type requis pour les missions du specimen)
	 */
	private static List<String> usefulContributionTypes(Specimen specimen) {
		Contribution.Type[] allContributionTypes = Contribution.Type.values();
		List<String> contributionTypes = new ArrayList<String>();
		for (Contribution.Type type : allContributionTypes) {
			//if (specimen.isContributionRequired(type.toString())) {
				contributionTypes.add(type.toString());
			//}
		}
		return contributionTypes;
	}

}
