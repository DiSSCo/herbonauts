package controllers;

import helpers.Facebook;
import helpers.GsonUtils;
import helpers.RecolnatAPIClient;
import helpers.RecolnatUser;
import inspectors.Event;
import inspectors.InspectorChain;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import libs.Images;
import models.*;
import models.alerts.Alert;
import models.contributions.DateContribution;
import models.questions.ContributionAnswer;
import models.questions.ContributionQuestion;
import models.questions.ContributionSpecimenUserStat;
import models.serializer.*;
import models.serializer.contributions.ContributionAnswerSimpleJsonSerializer;
import models.serializer.contributions.DateContributionForTimelineJsonSerializer;
import notifiers.Mails;
import org.apache.commons.codec.binary.Base64;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Router;
import play.vfs.VirtualFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import conf.Herbonautes;
import services.JPAUtils;
import services.Page;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Classe utilisateurs
 */
public class Users extends Application {

	/**
	 * Affiche la page de profil
	 */
	@Transactional(readOnly=true)
	public static void show(String login) {

		Logger.info("Show user profile %s", login);

		User user = User.findByLogin(login, false);
		notFoundIfNull(user);

		//Logger.info("Got missions in %d", last);


		/*Contribution.Type[] allContributionTypes = Contribution.Type.values();
		List<String> contributionTypes = new ArrayList<String>();
		for (Contribution.Type type : allContributionTypes) {
			contributionTypes.add(type.toString());
		}*/

		if (user.getDeleted()) {
			notFound();
		}

		render(user);
	}


	@Transactional(readOnly=true)
	public static void showMissions(String login) {
		//Logger.info("Show user profile %s", login);

		User user = User.findByLogin(login, false);
		notFoundIfNull(user);

		long start = System.currentTimeMillis();
		Logger.info("Get user mission for %s", login);
		List<MissionSimple> missions = user.getAllMissions();

		long last = System.currentTimeMillis() - start;
		Logger.info("Got missions in %d", last);

		renderJSON(missions);
	}
	
	/**
	 * Contributions de l'utilisateurs
	 * affichées dans l'onblet "Contributions" du profil
	 */
	@Transactional(readOnly=true)
	public static void specimenWithContributions(final Long userId, final Long missionId, int page) {
		
		//
		//Map<Specimen, Map<String, Contribution>> lastContributions = user
		//		.getLastContributionsBySpecimenAndType(page,
		//				Herbonautes.get().pageLength);
		//
		//renderJSON(lastContributions.values(), UserSimpleJsonSerializer.get(),
		//		MissionSimpleJsonSerializer.get(),
		//		SpecimenSimpleJsonSerializer.get(),
		//		TimestampSinceJsonSerializer.get());

		int pageSize = Herbonautes.get().pageLengthUserContributions;

		final Page<ContributionSpecimenUserStat> resultRaw = JPAUtils.getPage(ContributionSpecimenUserStat.class, page, pageSize, new JPAUtils.PredicateBuilder() {

			@Override
			public Predicate buildPredicate(CriteriaBuilder cb, Root root) {
				return cb.and(
						cb.equal(root.get("userId"), userId),
						cb.equal(root.get("missionId"), missionId)
				);
			}
		}, "lastModifiedAt", "desc");


		Page<SpecimenWithContribution> resultPage = JPAUtils.convertPage(SpecimenWithContribution.class, resultRaw, new JPAUtils.Converter<ContributionSpecimenUserStat, SpecimenWithContribution>() {

			public SpecimenWithContribution convert(ContributionSpecimenUserStat stat) {
				SpecimenWithContribution specimenWithContribution = new SpecimenWithContribution();
				specimenWithContribution.stat = stat;
				specimenWithContribution.answers = ContributionAnswer.find(
						"userId = ? and specimenId = ?",
						userId,
						stat.getSpecimen().id
				).fetch();


				return specimenWithContribution;
			}
		});


		GsonBuilder gson = GsonUtils.getGsonBuilder();
		gson.registerTypeAdapter(Specimen.class, SpecimenSimpleJsonSerializer.get());
		renderJSON(gson.create().toJson(resultPage));

	}

	public static class SpecimenWithContribution {
		public ContributionSpecimenUserStat stat;
		public List<ContributionAnswer> answers;
	}




	/**
	 * Dernières contributions, affichée sur la page d'accueil
	 * contributeur (/Application/indexConnected.html)
	 */
	@Transactional(readOnly=true)
	public static void lastContributions(String login) {

		/*List<Contribution> contributions =
				Contribution.find(
						"user.login = ? and canceled = false order by id desc", login)
						.fetch(Herbonautes.get().pageLength);*/

		User user = User.findByLogin(login);

		List<ContributionAnswer> answersRaw = ContributionAnswer.find("userId = ? and deleted = false order by createdAt desc", user.id)
				.fetch(Herbonautes.get().pageLength);

		List<AnswerWithQuestionAndMission> answers = new ArrayList<AnswerWithQuestionAndMission>();
		for (ContributionAnswer a : answersRaw) {
			answers.add(new AnswerWithQuestionAndMission(a));
		}

		renderJSON(answers,
				AnswerContributionSimpleJsonSerializer.get(),
				SpecimenSimpleJsonSerializer.get(),
				MissionSimpleJsonSerializer.get(),
				QuestionSimpleJsonSerializer.get());

		//renderJSON(contributions,
		//		MissionSimpleJsonSerializer.get(),
		//		SpecimenSimpleJsonSerializer.get(),
		//		UserSimpleJsonSerializer.get(),
		//		TimestampSinceJsonSerializer.get());

	}




	public static class AnswerWithQuestionAndMission {
		public MissionSimple mission;
		public ContributionQuestion question;
		public Specimen specimen;
		public Long createdAt;

		public AnswerWithQuestionAndMission(ContributionAnswer answer) {
			this.specimen = Specimen.findById(answer.getSpecimenId());
			this.mission = MissionSimple.findById(answer.getMissionId());
			this.question = ContributionQuestion.findById(answer.getQuestionId());
			this.createdAt = answer.getCreatedAt().getTime();
		}


	}
	
	/**
	 * Géolocalisations pour l'utilisateur
	 */
	@Transactional(readOnly=true)
	public static void geolocalisationContributions(String login) {
		
		/*List<GeolocalisationContribution> list =
			User.getGeolocalisations(login);
		
		renderJSON(list, GeolocalisationContributionJsonSerializer.get());  */

		User user = User.findByLogin(login);

		List<ContributionAnswer> list =
				JPA.em().createNativeQuery(
					"select *\n" +
					"from H_CONTRIBUTION_ANSWER a " +
					"inner join H_CONTRIBUTION_QUESTION q on q.id = a.question_id " +
					"where a.user_id = :userId and q.name = 'geo'", ContributionAnswer.class)
					.setParameter("userId", user.id)
					.getResultList();

		//List<ContributionAnswer> list = new ArrayList<ContributionAnswer>();

		renderJSON(list, ContributionAnswerSimpleJsonSerializer.get());
	}
	
	@Transactional(readOnly=true)
	public static void missions(String login) {
		User user = Security.connected();
		
		List<Mission> missions = user.getMissions();
		render(missions);
	}
	
	/**
	 * Contributions dates pour l'utilisateur
	 */
	@Transactional(readOnly=true)
	public static void dateContributions(String login) {
		
		List<DateContribution> list = 
			User.getDateContributions(login);
		
		Gson gson =
				GsonUtils.getGsonBuilder()
				.registerTypeAdapter(DateContribution.class, DateContributionForTimelineJsonSerializer.get())
				.create();
		
		JsonObject tl = new JsonObject();
		tl.addProperty("dateTimeFormat", "iso8601");
		tl.add("events", gson.toJsonTree(list).getAsJsonArray());
		
		renderJSON(gson.toJson(tl));
	}

	@Transactional(readOnly=true)
	public static void noimage(String login) {
		response.cacheFor("24h");
		renderBinary(VirtualFile.fromRelativePath("/public/img/anonymous-avatar.jpg").getRealFile());
	}
	
	/**
	 * Affichage de l'image de l'utilisateur
	 */
	@Transactional(readOnly=true)
	public static void image(String login, String imageId) {
		final User user = User.findByLogin(login);
		notFoundIfNull(user);
		
		if (!user.isHasImage()) {
			if (user.getFacebookId() != null) {
				// Si utilisateur facebook, affichage de son avatar
				redirect(Facebook.profilePictureURL(user.getFacebookUsername()));
			} else {
				// Redirection vers l'avatar anonyme
				String url = Router.reverse(VirtualFile.fromRelativePath("/public/img/anonymous-avatar.jpg"));
				redirect(url);
			}
		}
		
		renderImage(user.getImageId());
		
		//response.setContentTypeIfNotSet(user.image.type());
		//renderBinary(user.image.get());
		//Contents.image(user.imageId);
	}

    @Transactional(readOnly=true)
    public static void imageByUserId(Long userId) {
        final User user = User.findById(userId);
        notFoundIfNull(user);

        if (!user.isHasImage()) {
            if (user.getFacebookId() != null) {
                // Si utilisateur facebook, affichage de son avatar
                redirect(Facebook.profilePictureURL(user.getFacebookUsername()));
            } else {
                // Redirection vers l'avatar anonyme
                String url = Router.reverse(VirtualFile.fromRelativePath("/public/img/anonymous-avatar.jpg"));
                redirect(url);
            }
        }

        renderImage(user.getImageId());

        //response.setContentTypeIfNotSet(user.image.type());
        //renderBinary(user.image.get());
        //Contents.image(user.imageId);
    }

	
	// ~~~~~~~~~~~~~~~~~~~~~~~~
	// Modifications du profil	
	// ~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Affichage de la page de paramétrage utilisateur
	 */
	@Transactional(readOnly=true)
	public static void settings(String login) {
		Security.forbiddenIfNotCurrentUser(login);
		
		User user = User.findByLogin(login, false);
		notFoundIfNull(user);

		render(user);
	}
	
	/**
	 * Affichage de la page de paramétrage utilisateur
	 */
	@Transactional(readOnly=true)
	public static void password(String login) {
		Security.forbiddenIfNotCurrentUser(login);
		
		User user = User.findByLogin(login, false);
		notFoundIfNull(user);

		render(user);
	}
	
	/**
	 * Modification du mot de passe
	 */
	public static void changePassword(String login, 
			String password, 
			String newPassword, 
			String newPasswordConfirm) {
		
		Security.forbiddenIfNotCurrentUser(login);
		User user = User.findByLogin(login, false);
		notFoundIfNull(user);
		

		validation.required(newPassword);
		if (!validation.hasErrors() && !newPassword.equals(newPasswordConfirm)) {
			validation.addError("newPasswordConfirm", "validation.different.passwords");
		}


		RecolnatUser recolnatUser = RecolnatAPIClient.getUserByLogin(login);
		Logger.info("Change password for user %s (uuid %s)", login, recolnatUser.user_uuid);
		//validation.addError("password", "validation.bad.password");

		boolean success =
				RecolnatAPIClient.changePassword(recolnatUser.user_uuid, password, newPassword);

		if (!success) {
			validation.addError("password", "validation.bad.password");
		}

		if (validation.hasErrors()) {
			params.flash(); 
			validation.keep(); 
			password(login);
		}

		
		settings(login);
	}
	
	public static void levelInfos() {
		if (!Security.isConnected()) {
			forbidden();
		}
		
		Gson gson = new Gson();
		JsonObject json = new JsonObject();
		json.addProperty("level", User.getLevel(Security.connectedLogin()));
		json.addProperty("pendingLevel", User.getPendingLevel(Security.connectedLogin()));
		
		renderJSON(gson.toJson(json));
	}


	/**
	 * Sauvegarder l'image
	 */
	public static void saveImage(String login, File image) {
		
		Security.forbiddenIfNotCurrentUser(login);
		
		User user = User.findByLogin(login);
		notFoundIfNull(user);

		Images.resize(image, image, 200, 200, true);
		Image imageDB = Image.createImageFromFile(image);
		
		//user.image = image;
		user.setImageId(imageDB.id);
		user.setHasImage(true);
		user.save();

		flash.success(Messages.get("message.user.image.modified"));

		show(login);
	}

	/**
	 * Besoin du mot de passe pour sauvegarder ? 
	 */
	// Défini le besoin du mot de passe pour l'enregistrement de certaines données
	private static boolean needPassword(User oldUser, User newUser) {
		/*boolean needPassword = false;
		needPassword = (oldUser.getFacebookId() == null) && (!oldUser.getEmail().equals(newUser.getEmail()));
		return needPassword;   */
        return false;
	}
	
	/**
	 * Sauvegarder les paramètres
	 */
	public static void saveSettings(String login, String password, File newImage, User user,
			boolean deleteImage) {
		
		Security.forbiddenIfNotCurrentUser(login);

		User savedUser = User.findByLogin(login, false);
		
		Logger.info("Update info user %s:", login);
		Logger.info(" - firstName:%s", user.getFirstName());
		Logger.info(" - lastName:%s", user.getLastName());
		Logger.info(" - city:%s", user.getCity());
		Logger.info(" - email:%s (%s)", user.getEmail(), savedUser.getEmail());
		Logger.info(" - alerts: %s %s %s", user.isReceiveMails(), user.isAlertMission(), user.isAlertSpecimen());
		
		if (needPassword(savedUser, user) && !Security.passwordMatch(savedUser, password)) {
			flash.error(Messages.get("bad.password"));
			settings(login);
		}
		

		if (validation.hasErrors()) {
			validation.keep();
    		settings(login);
    	}
		
		savedUser.setFirstName(user.getFirstName());
		savedUser.setLastName(user.getLastName());
		savedUser.setReceiveMails(user.isReceiveMails());
		savedUser.setAlertMission(user.isAlertMission());
		savedUser.setAlertSpecimen(user.isAlertSpecimen());
		savedUser.setDescription(user.getDescription());
		savedUser.setCity(user.getCity());
		savedUser.setLatitude(user.getLatitude());
		savedUser.setLongitude(user.getLongitude());

		boolean confirmEmail = !savedUser.getEmail().equals(user.getEmail());

		boolean deleteAvatar = false;
		Image imageAvatar = null;

		if (deleteImage) {
			
			savedUser.setImage(null);
			savedUser.setHasImage(false);

			deleteAvatar = true;

		} else if (newImage != null) {
			
			boolean readableImage = libs.Images.isImageReadable(newImage);
			
			if (readableImage) {
				try {
					Image imageDB = Image.createAvatar(newImage);
					savedUser.setImageId(imageDB.id);
					savedUser.setHasImage(true);
					imageAvatar = imageDB;

				} catch(Exception e) {
					readableImage = false;
				}
			}
			
			if (!readableImage) {
				flash.error(Messages.get("message.user.image.ignored"));
			}
			
		}

		Logger.info("Save user");

		savedUser.save();

		// Envoi de notification d'envoie de mail
		if (confirmEmail) {
			EmailConfirmation.createAndSend(Security.connectedUser(), user.getEmail());
			flash.put("warning", Messages.get("user.confirm.email", user.getEmail()));
		}

		Logger.info("Update user in Recolnat ? " + Herbonautes.get().recolnatEnabled);

		if (Herbonautes.get().recolnatEnabled) {

			try {
				RecolnatUser recolnatUser = RecolnatAPIClient.getUserByLogin(login);
				recolnatUser.firstname = savedUser.getFirstName();
				recolnatUser.lastname = savedUser.getLastName();
				RecolnatAPIClient.updateUser(recolnatUser);

				if (imageAvatar != null) {
					Logger.info("Change user avatar in recolnat");
					String data = Base64.encodeBase64String(imageAvatar.getData());
					RecolnatAPIClient.updateAvatar(recolnatUser, data);
				}

			} catch (Exception e) {
				Logger.error(e, "Impossible de mettre l'utilisateur à jour");
			}

		}

		User.findByLogin(login, false); // Clear cache

		// Pour le badge "Cool"
		InspectorChain.get().inspect(Event.PROFILE_EDITED, savedUser);

		flash.success(Messages.get("message.user.profile.saved"));
		
		Security.refreshConnected();
		
		show(login);
	}
	
	/**
	 * Marquer comme lu 
	 */
	public static void markAlertAsRead(Long id) {
		Alert alert = Alert.findById(id);
		notFoundIfNull(alert);
		Security.forbiddenIfNotCurrentUser(alert.getUser().getLogin());
		
		if (alert.getUser().id.equals(Security.connectedId())) {
			alert.setUserRead(true);
			alert.save();
			ok();
		} else {
			forbidden();
		}
	}

	/**
	 * Update location
	 */
	public static void saveLocation(String login, Double latitude, Double longitude) {
		Security.forbiddenIfNotCurrentUser(login);
		User currentUser = User.findByLogin(login, false);

		Logger.info("Save user location (%s): %f %f", currentUser.getLogin(), latitude, longitude);

		currentUser.setLatitude(latitude);
		currentUser.setLongitude(longitude);

		currentUser.save();

		// Save location 1 fois par session (dans main.html)
		session.put("locationSaved", true);

		ok();
	}

	/**
	 * Delete account
	 */
	public static void deleteAccount(String login) {
		Security.forbiddenIfNotCurrentUser(login);
		User user = User.findByLogin(login, false);

		render(user);
	}


	public static void confirmDeleteAccount(String login) {
		Security.forbiddenIfNotCurrentUser(login);
		User user = User.findByLogin(login, false);

		String deletedLogin = user.getLogin();
		String email = user.getEmail();

		Logger.info("Delete account %s (id=%d)", login, user.id);

		user.setLogin("_");
		user.setFirstName(null);
		user.setLastName(null);
		user.setBirthDate(null);
		user.setDescription(null);
		user.setCity(null);
		user.setAdmin(false);
		user.setLatitude(null);
		user.setLongitude(null);
		user.setEmail(null);
		user.setHasImage(false);
		user.setImage(null);
		user.setImageId(null);

		user.setDeleted(true);

		user.save();

		Mails.goodbye(deletedLogin, email);

		flash.success(Messages.get("message.user.deleted.account"));
		Application.logout();
	}

}
