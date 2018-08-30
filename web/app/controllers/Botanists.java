package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import conf.Herbonautes;
import helpers.GsonUtils;
import jobs.BotanistsLoaderJob;
import libs.Images;
import models.Botanist;
import models.Image;
import models.Specimen;
import models.contributions.DateContribution;
import models.contributions.GeolocalisationContribution;
import models.questions.special.CollectorStaticAnswer;
import models.questions.special.GeolocalisationStaticAnswer;
import models.questions.special.IdentifierStaticAnswer;
import models.questions.special.StaticAnswer;
import models.references.ReferenceRecord;
import models.references.ReferenceRecordInfo;
import models.serializer.SpecimenSimpleJsonSerializer;
import models.serializer.SpecimenWithBotanistsReportsJsonSerializer;
import models.serializer.contributions.DateContributionForTimelineJsonSerializer;
import models.serializer.contributions.GeolocalisationContributionJsonSerializer;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Router;
import play.vfs.VirtualFile;

import java.io.File;
import java.util.*;

/**
 * Controleur Botaniste
 */
public class Botanists extends Application {

	/**
	 * Fiche botanistes
	 */
	public static void show(Long id) {
		/*Botanist botanist = Botanist.findById(id);
		render(botanist);  */
        ReferenceRecord botanist = ReferenceRecord.findById(id);

		if (botanist == null || !"botanist".equals(botanist.getReference().getName())) {
			Logger.info("Botanist not found, try V1");
			botanist = ReferenceRecord.find("idV1 = ?", id).first();
		}

        // stats
        Long missionCount = StaticAnswer.count("select count(distinct missionId) from StaticAnswer where type in ('COLLECTOR', 'IDENTIFIER') and referenceRecord.id = ?", id);
        Long collectedSpecimensCount = CollectorStaticAnswer.count("referenceRecord.id = ?", botanist.id);
        Long determinedSpecimensCount = IdentifierStaticAnswer.count("referenceRecord.id = ?", botanist.id);

        Map<String, String> info = new HashMap<String, String>();
        if (botanist.getInfo() != null) {
            for (ReferenceRecordInfo i : botanist.getInfo()) {
                info.put(i.getName(), i.getValue());
            }
        }

        render(botanist, info, missionCount, collectedSpecimensCount, determinedSpecimensCount);
	}
	
	/*
	pour tests*/
	public static void admin() {
		render();
	}
	
	public static void load(File file) {
		File newFile = Play.getFile("/public/uploads/" + file.getName());
		file.renameTo(newFile);
		file.delete();

		new BotanistsLoaderJob(newFile).now();
		admin();
	}
	
	
	/**
	 * Affichage d'une image botaniste
	 */
	@Transactional(readOnly=true)
	public static void image(Long id) {
        ReferenceRecord botanist = ReferenceRecord.findById(id);
		notFoundIfNull(botanist);
		
		if (botanist.getImageId() == null) {
			// Redirection vers l'avatar anonyme
			String url = Router.reverse(VirtualFile.fromRelativePath("/public/img/anonymous-avatar.jpg"));
			redirect(url);
		}

		renderImage(botanist.getImageId());
	}
	
	public static void saveImage(Long id, File image) {
		Security.forbiddenIfNotLeader();

        ReferenceRecord botanist = ReferenceRecord.findById(id);
		notFoundIfNull(botanist);
		
		libs.Images.squarify(image, image);
		libs.Images.resize(image, image, 200, 200);

		Image imageDB = Image.createImageFromBytes(Images.compress(image));
		botanist.setImageId(imageDB.id);
		botanist.save();
		show(botanist.id);
	}

	/**
	 * Retourne la liste des spécimens récoltés ou déterminés par le botanistes
	 */
	@Transactional(readOnly=true)
	public static void specimensForBotanist(Long id, Integer page) {

		if (page == null) {
			page = 1;
		}

		String query = "select * from h_SPECIMEN where id in ( " +
				"select distinct specimen_id from H_CONTRIBUTION_STATIC_VALUE " +
				"where REFERENCE_RECORD_ID = :refId and type in ('COLLECTOR', 'IDENTIFIER')" +
				")";

		List<Specimen> specimens = JPA.em().createNativeQuery(query, Specimen.class)
				.setParameter("refId", id)
				.setFirstResult(Herbonautes.get().pageLength * (page - 1))
				.setMaxResults(Herbonautes.get().pageLength)
				.getResultList();


		List<SpecimenForBotanist> specimenForBotanists = new ArrayList<SpecimenForBotanist>();


		for (Specimen specimen : specimens) {

			SpecimenForBotanist sfb = new SpecimenForBotanist();

			List<StaticAnswer> list = JPA.em().createNativeQuery("select * from H_CONTRIBUTION_STATIC_VALUE " +
					"where SPECIMEN_ID = :specimenId and type in ('COLLECTOR', 'IDENTIFIER')", StaticAnswer.class)
					.setParameter("specimenId", specimen.id)
					.getResultList();

			List<CollectorStaticAnswer> collectorAns = CollectorStaticAnswer.find("specimenId = ?", specimen.id).fetch(1);
			if (collectorAns != null &&
					collectorAns.size() > 0 &&
					collectorAns.get(0).referenceRecord != null &&
					id.equals(collectorAns.get(0).referenceRecord.id)) {
				sfb.collector = true;
			}

			List<IdentifierStaticAnswer> identifierAns = IdentifierStaticAnswer.find("specimenId = ?", specimen.id).fetch(1);
			if (identifierAns != null &&
					identifierAns.size() > 0 &&
					identifierAns.get(0).referenceRecord != null &&
					id.equals(identifierAns.get(0).referenceRecord.id)) {
				sfb.identifier = true;
			}


			sfb.specimen = specimen;


			specimenForBotanists.add(sfb);

		}

		renderJSON(specimenForBotanists, SpecimenSimpleJsonSerializer.get());


		//List<Specimen> specimens = Botanist.getSpecimens(id, Herbonautes.get().pageLength, page);
//
		//renderJSON(specimens,
		//		SpecimenWithBotanistsReportsJsonSerializer.get());
	}

	@Transactional(readOnly = true)
	public static void specimensGeolocalisationForBotanist(Long id) {
		String query = "select * from H_CONTRIBUTION_STATIC_VALUE where specimen_id in ( " +
				"select distinct specimen_id from H_CONTRIBUTION_STATIC_VALUE " +
				"where type = 'COLLECTOR' and REFERENCE_RECORD_ID = :refId " +
				") and type = 'GEOLOCALISATION'";

		List<GeolocalisationStaticAnswer> geos = JPA.em().createNativeQuery(query, GeolocalisationStaticAnswer.class)
				.setParameter("refId", id)
				.getResultList();

		renderJSON(geos);
	}

	/**
	 * Retourne la liste des spécimens récoltés ou déterminés par le botanistes
	 */
	@Transactional(readOnly=true)
	public static void specimensWithBotanistsReports(Long id, int page) {

		List<Specimen> specimens = Botanist.getSpecimens(id, Herbonautes.get().pageLength, page);

		renderJSON(specimens,
				SpecimenWithBotanistsReportsJsonSerializer.get());
	}

	/**
	 * Retourne la géolocalisation des spécimens récoltés ou déterminés
	 */
	@Transactional(readOnly=true)
	public static void geolocalisationContributions(Long id) {

		List<GeolocalisationContribution> list =
			Botanist.getGeolocalisations(id);

		renderJSON(list, GeolocalisationContributionJsonSerializer.get());
	}
	
	/**
	 *  Retourne les datés de récolte des spécimens récoltés ou déterminés
	 */
	@Transactional(readOnly=true)
	public static void dateContributions(Long id) {

		List<DateContribution> list =
			Botanist.getDateContributions(id);

		Gson gson =
				GsonUtils.getGsonBuilder()
				.registerTypeAdapter(DateContribution.class, DateContributionForTimelineJsonSerializer.get())
				.create();

		JsonObject tl = new JsonObject();
		tl.addProperty("dateTimeFormat", "iso8601");
		tl.add("events", gson.toJsonTree(list).getAsJsonArray());

		renderJSON(gson.toJson(tl));
	}
	
	@Transactional(readOnly = true)
	public static void minDateContributions(Long id) {
		List<DateContribution> list =
			Botanist.getDateContributions(id);

		Date minDate = null;
		if (list != null) {
			Date currentDate = null;
			for (DateContribution contribution : list) {
				if (!contribution.isPeriod()) {
					currentDate = contribution.getCollectDate();
				} else {
					currentDate = contribution.getCollectStartDate();
				}
				if (currentDate != null) {
					if (minDate == null || minDate.after(currentDate)) {
						minDate = currentDate;
					}
				}
			}
		}
		if (minDate != null) {
			renderJSON(minDate);
		} else {
			notFound();
		}

	}
	
	public static class SpecimenForBotanist {
		public Specimen specimen;
		public boolean collector = false;
		public boolean identifier = false;
	}
	
}
