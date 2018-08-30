package controllers;

import static controllers.Security.connectedId;
import static controllers.Security.connectedLogin;

import com.thoughtworks.xstream.XStream;
import helpers.GsonUtils;
import helpers.XstreamRssConverters.VersionConverter;
import inspectors.Event;
import inspectors.InspectorChain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.Botanist;
import models.Mission;
import models.Specimen;
import models.contributions.BotanistsContribution;
import models.contributions.CollectorContribution;
import models.contributions.Contribution;
import models.contributions.ContributionFeedback;
import models.contributions.CountryContribution;
import models.contributions.DateContribution;
import models.contributions.GeolocalisationContribution;
import models.contributions.IdentifiedByContribution;
import models.contributions.LocalityContribution;
import models.contributions.RegionLevel1Contribution;
import models.contributions.RegionLevel2Contribution;
import models.contributions.UnusableContribution;
import models.contributions.reports.ContributionReport;
import models.serializer.SpecimenSimpleJsonSerializer;
import models.serializer.WedigbioJsonSerializer;
import models.serializer.contributions.BotanistsContributionJsonSerializer;
import models.serializer.contributions.CollectorContributionJsonSerializer;
import models.serializer.contributions.ContributionFeedbackJsonSerializer;
import models.serializer.contributions.ContributionJsonSerializerFactory;
import models.serializer.contributions.CountryContributionJsonSerializer;
import models.serializer.contributions.DateContributionJsonSerializer;
import models.serializer.contributions.GeolocalisationContributionJsonSerializer;
import models.serializer.contributions.IdentifiedByContributionJsonSerializer;
import models.serializer.contributions.LocalityContributionJsonSerializer;
import models.serializer.contributions.RegionLevel1ContributionJsonSerializer;
import models.serializer.contributions.RegionLevel2ContributionJsonSerializer;
import models.serializer.contributions.reports.ContributionReportJsonSerializer;
import models.wedigbio.Channel;
import models.wedigbio.ContribSet;
import models.wedigbio.ItemRss;
import models.wedigbio.Rss;
import play.Logger;
import play.mvc.Before;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;

/**
 * Controler d'ajout/suppression/validation des contributions (
 */
public class Contributions extends Application {

	@Before(unless = { "contributionReportsForSpecimen","lastWeekRss" ,"lastDaysRss","intervalJson"})
	static void ensureConnected() {
		Logger.info("Check if user is connected");
		if (connectedId() == null) {
			forbidden("not.connected");
		}
	}

	private static void ensureMember(Mission mission) {
		if (mission != null && !mission.isMember(connectedLogin())) {
			Logger.info("User not member of " + mission.getTitle());
			forbidden("not.member");
		}
	}

	/**
	 * Keep choice
	 */
	public static void keep(Long id ) {
		Contribution keepedContribution = Contribution.findById(id);

		if (keepedContribution == null) {
			badRequest(); 
		}
		
		ContributionFeedback feedback = 
			Contribution.feedback(keepedContribution, false);
		
		InspectorChain.get().inspect(Event.CONTRIBUTION_ADDED, keepedContribution, feedback);
		
		renderContribution(keepedContribution,
				ContributionJsonSerializerFactory.getInstance(Contribution.Type
						.valueOf(keepedContribution.getType())), feedback);
	}
	
	/**
	 * Validation de la contribution d'un autre
	 */
	public static void validate(Long id) {
		// Récupération contribution
		Contribution validContribution = Contribution.findById(id);

		if (validContribution == null) {
			badRequest(); 
		}

		Contribution modifiedContribution = Contribution
				.find("type = ? and specimen.id = ? and user.id = ? and canceled = false",
						validContribution.getType(), validContribution.getSpecimen().id,
						connectedId()).first();

		modifiedContribution.validate(validContribution);

		modifiedContribution.save();
		modifiedContribution.refresh();

		// feedback(..., false) pour ne pas remonter le flag 'conflicts' dans
		// le rapport de contribution et evite de boucler sur l'alerte des
		// conflits
		ContributionFeedback feedback = Contribution.feedback(
				modifiedContribution, false);

		// On n'envoie l'alerte que si vraiment des conflits
		InspectorChain.get().inspect(Event.CONTRIBUTION_ADDED, modifiedContribution, feedback);
		
		// JSON
		renderContribution(modifiedContribution,
				ContributionJsonSerializerFactory.getInstance(Contribution.Type
						.valueOf(validContribution.getType())), feedback);
	}

	/**
	 * Ajout générique d'une contribution. Les méthodes routées sont de la forme
	 * <code>Contributions.add{TypeContribution}
	 * 
	 * @param contribution
	 * @param type
	 */
	private static ContributionFeedback addContribution(
			Contribution contribution, Contribution.Type type) {

		Specimen specimen = Specimen.findById(contribution.getSpecimen().id);
		Mission mission = Mission.findById(contribution.getMission().id);

		
		if (connectedId() == null) {
			Logger.info("User not connected");
			forbidden("not.connected");
		}

		contribution.setUser(Security.connected());
		ensureMember(mission);

		cancelContributionIfExists(specimen, type);

		specimen.add(contribution);
		specimen.setLastModified(new Date());
		specimen.save();

		ContributionFeedback feedback = Contribution.feedback(contribution);

		// Emission d'un évenement (si pas de conflit. Sinon il faut garder la contrib)
		if (!feedback.isConflicts()) {
			InspectorChain.get().inspect(Event.CONTRIBUTION_ADDED, contribution, feedback);
		}

		Logger.info("Added contribution %s (id=%s) to %s", contribution.getType(),
				contribution.id, contribution.getSpecimen().getCode());

		return feedback;
	}

	/**
	 * Contribution pays
	 */
	public static void addCountry(CountryContribution contribution) {
		if (!contribution.isNotPresent()) {
			validation
					.required("contribution.country.id", contribution.getCountry());
		}

		if (validation.hasErrors()) {
			response.status = 400; // Bad request
			renderJSON(validation.errorsMap());
		}

		ContributionFeedback feedback = addContribution(contribution,
				Contribution.Type.COUNTRY);
		renderContribution(contribution,
				CountryContributionJsonSerializer.get(), feedback);
	}

	/**
	 * Contribution région
	 */
	public static void addRegionLevel1(RegionLevel1Contribution contribution) {
		if (!contribution.isNotPresent()) {
			validation.required("contribution.regionLevel1.id",
					contribution.getRegionLevel1());
		}

		if (validation.hasErrors()) {
			response.status = 400; // Bad request
			renderJSON(validation.errorsMap());
		}

		ContributionFeedback feedback = addContribution(contribution,
				Contribution.Type.REGION_1);
		renderContribution(contribution,
				RegionLevel1ContributionJsonSerializer.get(), feedback);
	}

	/**
	 * Contribution sous-r��gion
	 */
	public static void addRegionLevel2(RegionLevel2Contribution contribution) {

		if (!contribution.isNotPresent()) {
			validation.required("contribution.regionLevel2.id",
					contribution.getRegionLevel2());
		}

		if (validation.hasErrors()) {
			response.status = 400; // Bad request
			renderJSON(validation.errorsMap());
		}

		ContributionFeedback feedback = addContribution(contribution,
				Contribution.Type.REGION_2);
		renderContribution(contribution,
				RegionLevel2ContributionJsonSerializer.get(), feedback);
	}

	/**
	 * Contribution dates
	 */
	public static void addDate(DateContribution contribution) {
		if (!contribution.isNotPresent()) {
			if (contribution.isPeriod()) {
				validation.required("contribution.collectStartDate",
						contribution.getCollectStartDate());
				validation.required("contribution.collectEndDate",
						contribution.getCollectEndDate());
				if (!validation.hasErrors() &&
						contribution.getCollectEndDate().before(contribution.getCollectStartDate())) {
					validation.addError("contribution.collectEndDate", "error.contribution.period.invalid");
				}
			} else {
				validation.required("contribution.collectDate",
						contribution.getCollectDate());
			}
		}

		if (!contribution.isNotPresent()) {
			if ((contribution.isPeriod() && (validation
					.hasError("contribution.collectStartDate") || validation
					.hasError("contribution.collectEndDate")))
					|| (!contribution.isPeriod() && validation
							.hasError("contribution.collectDate"))) {
				response.status = 400; // Bad request
				renderJSON(validation.errorsMap());
			}
		}

		ContributionFeedback feedback = addContribution(contribution,
				Contribution.Type.DATE);
		renderContribution(contribution, DateContributionJsonSerializer.get(),
				feedback);
	}

	/**
	 * Contribution botanistes
	 */
//	public static void addBotanists(BotanistsContribution contribution) {
//		if (!contribution.isCollectorNotPresent()) {
//			validation.required("contribution.collector.name",
//					contribution.getCollector().getName());
//		}
//		if (!contribution.isDeterminerNotPresent()) {
//			validation.required("contribution.determiner.name",
//					contribution.getDeterminer().getName());
//		}
//		if (validation.hasErrors()) {
//			response.status = 400; // Bad request
//			renderJSON(validation.errorsMap());
//		}
//
//		// Create needed Botanists (pour eviter les doublons)
//		contribution.setDeterminer(Botanist
//				.createIfNeeded(contribution.getDeterminer()));
//		contribution.setCollector(Botanist
//				.createIfNeeded(contribution.getCollector()));
//		if (contribution.getOtherCollectors() != null) {
//			List<Botanist> collectorsClean = new ArrayList<Botanist>();
//			for (Botanist collector : contribution.getOtherCollectors()) {
//				Botanist bot = Botanist.createIfNeeded(collector);
//				if (bot != null) {
//					collectorsClean.add(bot);
//				}
//			}
//			contribution.setOtherCollectors(collectorsClean);
//		}
//
//		ContributionFeedback feedback = addContribution(contribution,
//				Contribution.Type.BOTANISTS);
//		renderContribution(contribution,
//				BotanistsContributionJsonSerializer.get(), feedback);
//	}

	/**
	 * Contribution récolteur
	 */
	public static void addCollector(CollectorContribution contribution) {
		if (!contribution.isCollectorNotPresent()) {
			validation.required("contribution.collector.name",
					contribution.getCollector().getName());
		}
		if (validation.hasErrors()) {
			response.status = 400; // Bad request
			renderJSON(validation.errorsMap());
		}

		// Create needed Collector (pour eviter les doublons)
		contribution.setCollector(Botanist
				.createIfNeeded(contribution.getCollector()));
		if (contribution.getOtherCollectors() != null) {
			List<Botanist> collectorsClean = new ArrayList<Botanist>();
			for (Botanist collector : contribution.getOtherCollectors()) {
				Botanist bot = Botanist.createIfNeeded(collector);
				if (bot != null) {
					collectorsClean.add(bot);
				}
			}
			contribution.setOtherCollectors(collectorsClean);
		}

		ContributionFeedback feedback = addContribution(contribution,
				Contribution.Type.COLLECTOR);
		renderContribution(contribution,
				CollectorContributionJsonSerializer.get(), feedback);
	}
	
	/**
	 * Contribution déterminateur
	 */
	public static void addIdentifiedBy(IdentifiedByContribution contribution) {

		if (!contribution.isDeterminerNotPresent()) {
			validation.required("contribution.determiner.name",
					contribution.getDeterminer().getName());
		}
		if (validation.hasErrors()) {
			response.status = 400; // Bad request
			renderJSON(validation.errorsMap());
		}

		// Create needed Botanists (pour eviter les doublons)
		contribution.setDeterminer(Botanist
				.createIfNeeded(contribution.getDeterminer()));

		ContributionFeedback feedback = addContribution(contribution,
				Contribution.Type.IDENTIFIEDBY);
		renderContribution(contribution,
				IdentifiedByContributionJsonSerializer.get(), feedback);
	}
	
	/**
	 * Contribution localités
	 */
	public static void addLocality(LocalityContribution contribution) {
		if (!contribution.isNotPresent()) {
			validation.required("contribution.locality", contribution.getLocality());
		}
		if (validation.hasErrors()) {
			response.status = 400; // Bad request
			renderJSON(validation.errorsMap());
		}

		ContributionFeedback feedback = addContribution(contribution,
				Contribution.Type.LOCALITY);
		renderContribution(contribution,
				LocalityContributionJsonSerializer.get(), feedback);
	}

	/**
	 * Contribution géolocalisation
	 */
	public static void addGeolocalisation(
			GeolocalisationContribution contribution) {
		if (!contribution.isNotPresent()) {
			validation.required("contribution.latitude", contribution.getLatitude());
			validation.required("contribution.latitude", contribution.getLongitude());
			
			if (!validation.hasErrors()) {
				if (contribution.getLatitude() < -90) validation.addError("contribution.latitude", "validation.min.lat");
				if (contribution.getLatitude() > 90) validation.addError("contribution.latitude", "validation.max.lat");
				if (contribution.getLongitude() < -180) validation.addError("contribution.latitude", "validation.min.lng");
				if (contribution.getLongitude() > 180) validation.addError("contribution.latitude", "validation.max.lng");
			}
		}
		
		
		if (validation.hasErrors()) {
			response.status = 400; // Bad request
			renderJSON(validation.errorsMap());
		}

		ContributionFeedback feedback = addContribution(contribution,
				Contribution.Type.GEOLOCALISATION);
		renderContribution(contribution,
				GeolocalisationContributionJsonSerializer.get(), feedback);
	}

	/**
	 * Contribution photo inutilisable
	 */
	public static void addUnusable(UnusableContribution contribution) {
		addContribution(contribution, Contribution.Type.UNUSABLE);
		renderJSON(contribution.id);
	}

	/**
	 * Suppression d'une contribution
	 */
	public static void delete(Long id) {

		Contribution contribution = Contribution.findById(id);

		Security.forbiddenIfNotCurrentUser(contribution.getUser().getLogin());
		notFoundIfNull(contribution);

		Contribution.cancel(contribution);

		// On recompile les rapport
		ContributionFeedback feedback = Contribution.feedback(contribution);

		// InspectorChain.get().inspect(Event.CONTRIBUTION_ADDED, contribution,
		// feedback);

		if (request.isAjax()) {
			renderJSON(feedback, ContributionFeedbackJsonSerializer.get(),
					ContributionJsonSerializerFactory
							.getInstance(contribution.getType()),
					ContributionReportJsonSerializer.get());
		} else {
			Long missionId = contribution.getMission().id;
			Long specimenId = contribution.getSpecimen().id;
			Missions.contributionBoard(missionId, specimenId);
		}
	}

	/**
	 * Retourne le specimen pour une contribution donnée
	 */
	public static void specimenForContribution(Long id) {
		Specimen specimen = Specimen.find(
				"select c.specimen from Contribution c where c.id = ?", id)
				.first();

		renderJSON(specimen, SpecimenSimpleJsonSerializer.get());
	}

	/**
	 * Renvoie toutes les contributions d'un utilisateur par type Utilisé pour
	 * afficher les contributions au départ
	 */
	public static void contributionsForSpecimen(Long id) {
		Specimen specimen = Specimen.findById(id);
		notFoundIfNull(specimen);

		List<Contribution> myContributions = specimen
				.getContributionsByLogin(connectedLogin());

		// Les serializers dont on aura besoin
		ArrayList<JsonSerializer> serializers = new ArrayList<JsonSerializer>();
		for (Contribution contribution : myContributions) {
			serializers.add(ContributionJsonSerializerFactory
					.getInstance(contribution.getType()));
		}
		JsonSerializer[] ar = new JsonSerializer[serializers.size()];
		renderJSON(myContributions, serializers.toArray(ar));
	}
	
	/**
   * Renvoie toutes les contributions du type pour tous les utilisateurs
   */
  public static void contributionsTypeForSpecimen(Long id, String type) {
    Specimen specimen = Specimen.findById(id);

    notFoundIfNull(specimen);

    List<Contribution> myContributions = specimen
        .getContributionsByLogin(connectedLogin());

    // Les serializers dont on aura besoin
    ContributionFeedback feedback = null;
    Contribution contribution = null;
    for (Contribution c : myContributions) {
      if (c.getType().equals(type)) {
        contribution = c;
        feedback = Contribution.feedback(c, true);
      }
    }
    notFoundIfNull(feedback);
    
    renderContribution(contribution,
      ContributionJsonSerializerFactory.getInstance(Contribution.Type
          .valueOf(contribution.getType())), feedback);
  }

	/** 
	 * Renvoie toutes les rapports de contribution (complète, conflits) sur un
	 * spécimen Utilisé pour afficher les contributions
	 */
	public static void contributionReportsForSpecimen(Long id) {
		Specimen specimen = Specimen.findById(id);

		Map<String, ContributionReport> reports = specimen
				.getContributionReportsByType();
//    reports.remove("BOTANISTS");
//    reports.remove("REGION_1");
//    reports.remove("COUNTRY");
//    reports.remove("DATE");
//    reports.remove("LOCALITY");

		Gson gson = GsonUtils.getGsonBuilder().addSerializationExclusionStrategy(
				new ExclusionStrategy() {

					@Override
					public boolean shouldSkipField(FieldAttributes arg0) {
						if (arg0.getName() == "mission")
							return true;
						if (arg0.getName() == "specimen")
							return true;
						return false;
					}

					@Override
					public boolean shouldSkipClass(Class<?> klass) {
						return false;
					}
				}).create();

		renderJSON(gson.toJson(reports));
	}

	// PRIVATE ~~~~~~

	/**
	 * Annule la contribution (marquée canceled)
	 */
	private static void cancelContributionIfExists(Specimen specimen,
			Contribution.Type type) {
		List<Contribution> contributions = specimen.getContributionsByTypeAndLogin(
				type.toString(), connectedLogin());
		if (contributions != null) {
			for (Contribution contribution : contributions) {
				Contribution.cancel(contribution);
			}
		}
	}

	/**
	 * Retour JSON Générique d'une contribution
	 */
	private static void renderContribution(Contribution contribution,
			JsonSerializer<? extends Contribution> serializer,
			ContributionFeedback feedback) {

		if (request.isAjax()) {
			renderJSON(feedback, ContributionFeedbackJsonSerializer.get(),
					ContributionReportJsonSerializer.get(), serializer);
		} else {
			flash("contribution_feedback", feedback);
			Missions.contributionBoard(contribution.getMission().id,
					contribution.getSpecimen().id);
		}

	}

}
