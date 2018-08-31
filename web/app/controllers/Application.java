package controllers;

import helpers.Facebook;
import helpers.UserUtils;
import inspectors.Event;
import inspectors.InspectorChain;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.*;
import models.alerts.Alert;
import models.questions.ContributionAnswer;
import models.questions.ContributionSpecimenStat;
import models.serializer.MissionJsonSerializer;
import models.serializer.UserContributionJsonSerializer;
import models.serializer.UserJsonSerializer;
import notifiers.Mails;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.IO;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;
import play.vfs.VirtualFile;

import com.google.gson.JsonElement;

import conf.Herbonautes;

import static controllers.Security.*;

/**
 * Controleur principal dont les autres contrôleur héritent.
 */
public class Application extends Controller {

	/* POUR DEBUG
	@Before
	static void logRoute() {
		Logger.debug("Current action : %s", request.action);
	}
	*/
	
	/**
	 * Ajout des variables utilisées souvent dans le scope des templates
	 */
	@Before
    static void addDefaultArgs() {


		if (request.isAjax() || request.url.endsWith(".jpg")) {
			Logger.trace("Ignore args for" + request.url);
			return;
		}

        User u = connectedUser();
        if (u == null) {
        	disconnect();
        }
        
        // Configuration principales

        renderArgs.put("herbonautes", Herbonautes.get());
		renderArgs.put("isConnected", isConnected());
		renderArgs.put("headerLinks", Link.getAllHeaderLinksByLang());
		renderArgs.put("topLinks", Link.getAllTopLinksByLang());
        renderArgs.put("ctxPath", Play.ctxPath);


		if (isConnected()) {
	        renderArgs.put("connectedLogin", connectedLogin());
	        renderArgs.put("connectedImageId", connectedUser().getImageId());
	        renderArgs.put("connectedId", connectedId());
	        renderArgs.put("connectedLevel", connectedUser().getLevel());
	        renderArgs.put("connectedPendingLevel", connectedUser().getPendingLevel());
	        renderArgs.put("isLeader", isLeader());
	        renderArgs.put("isAdmin", isAdmin());
            renderArgs.put("isTeam", isTeam());
	        renderArgs.put("connectedUserMissions", connectedUserMissions());
	        renderArgs.put("connectedLeaderMissions", connectedLeaderMissions());
	        renderArgs.put("isFB", isFB());
			if(isTeam()) {
				//renderArgs.put("validatedMissions", Mission.getValidatedMissions(connectedUser()));
				//renderArgs.put("nbPropositions", Mission.countMissionsPropositions());
				renderArgs.put("nbPropositions", Mission.countPendingPropositions());
			}
		}

    }

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Actions transverses ----
	
	/**
	 * Connexion d'un utilisateur
	 */
    public static void login(String login, String password) {
    	Logger.info("Login attempt for %s", login);
		User user = Security.authenticate(login, password);
		
    	if (user != null) {
    		Logger.info("User %s logged in", login);
    		connect(user);
    		if (request.isAjax()) {
    			// Renvoie la liste des missions
    			ok();
    			//renderJSON(user, new UserJsonSerializer(), new MissionJsonSerializer());
    		} else {
    			Application.index();
    		}
    	} else {
    		Logger.info("User %s forbidden");
    		forbidden();
    	}
    }
    
    /**
     * Envoi du mot de passe à un utilisateur
     */
    public static void sendPassword(String login) {
    	Logger.info("Password asked for %s", login);
    	User user = Security.findUser(login);
    	if (user != null) {
    		Mails .sendPassword(user);
    		ok();
    	} else {
    		notFound();
    	}
    }
    
    /**
     * Connexion / inscription facebook
     */
    public static void fbauth(String uid, String accessToken) {
    	
    	JsonElement userJSON = fbAuthenticate(uid, accessToken);
    	
    	if (userJSON != null) {
    		
    		Logger.info("%s", userJSON.toString());

    		String fbId = userJSON.getAsJsonObject().get("id").getAsString();
    		
    		User user = User.find("facebookId = ?", fbId).first();
    		
    		
    		
    		if (user == null) {
    			// Création de l'utilisateur FB
    			user = createUserFromFB(userJSON);
    			
    			Mails.welcome(user);
    			InspectorChain.get().inspect(Event.USER_SIGNUP, user);
    		}
    		
    		
    		connect(user);
    		
    		ok();
    		Logger.info("FB User logged in (%s - %s)", uid, accessToken);
    	} else {
    		forbidden();
    	}
    	
    	
    }

	private static User createUserFromFB(JsonElement userJSON) {
		User user = new User();
		String fbUsername = userJSON.getAsJsonObject().get("username").getAsString();
		
		// Attribution d'un login unique si le login FB
		// est déjà utilisé
		user.setLogin(UserUtils.getUnusedLogin(fbUsername));
		user.setFacebookUsername(fbUsername);
		user.setFacebookId(userJSON.getAsJsonObject().get("id").getAsString());
		
		// Get USER image
		try {
			Logger.info("DL FB image for %s", fbUsername);
			
			String imageURL = Facebook.profileLargePictureURL(fbUsername);
			HttpResponse res = WS.url(imageURL).get();
			File tmpFile = new File(Herbonautes.get().uploadDir, "pic_" + fbUsername + ".jpg");
			IO.copy(res.getStream(), new FileOutputStream(tmpFile));
			Image image = Image.createAvatar(tmpFile);
			tmpFile.delete();
			
			user.setImageId(image.id);
			user.setHasImage(true);
		} catch (Exception e) {
			Logger.error(e, "Impossible de récuperer l'image FB pour %s", fbUsername);
		}
		
		user.setEmail(userJSON.getAsJsonObject().get("email").getAsString());
		user.save();
		user.refresh();
		
		
		
		
		Logger.info("User %s créé depuis FB", user.getLogin());
		return user;
	}
	
    /**
     * Inscription "classique"
     */
    public static void signup(String login, String email, String password) {
    	
    	validation.match(login, "[A-Za-z0-9_]{1,12}");
    	validation.required(login);
    	validation.required(email);
    	validation.email(email);
    	validation.required(password);
    	
		User userLogin = 
    		User.find("lower(login) = ?", login.toLowerCase().trim()).first();
		if (userLogin != null) {
			validation.addError("login", "validation.exists");
		}

		User userEmail = 
    		User.find("email = ?", email).first();
		if (userEmail != null) {
			validation.addError("email", "validation.exists");
		}
    	
    	if (validation.hasErrors()) {
    		response.status = 400;
    		renderJSON(validation.errorsMap());
    	}
    	
		User newUser = new User();
		newUser.setLogin(login.trim());
		newUser.setPassword(password);
		newUser.setEmail(email.trim());
		newUser.save();
		newUser.refresh();
		
		connect(newUser);
    	
		Mails.welcome(newUser);
		
		InspectorChain.get().inspect(Event.USER_SIGNUP, newUser);
		
		renderJSON(newUser, UserJsonSerializer.get(), MissionJsonSerializer.get());
    	
    }
    
    /**
     * Déconnexion
     */
    public static void logout() {
    	disconnect();

		flash.keep();

    	Application.index();
    }
    
    /**
     * Accueil visiteurs
     */
    @Transactional(readOnly=true)
    public static void index() {
    	
    	if (isConnected()) {
    		indexConnected();
    	}
    	
    	List<Mission> missions = Mission.getFirstMissions(Herbonautes.get().missionsCountOnIndex);
    	
    	//Mission randomMission = Mission.getRandomMission();
    	//Specimen randomSpecimen = randomMission == null ? null : randomMission.getRandomSpecimenNotConnected(randomMission.getId());

    	
    	Content presentation = Content.findByURLAndLang("presentation", "fr");

		long missionsCount = Mission.count();
		long herbonautesCount = User.count();
		long specimensCount = Specimen.count();

    	render(missions, presentation, missionsCount, herbonautesCount, specimensCount);
    }


	@Transactional(readOnly=true)
	public static void stats() {

		long missionsCount = Mission.count();
		long herbonautesCount = User.count();
		long specimensCount = Specimen.count();
		long contributionsCount =
				(JPA.em().createQuery("select sum(s.answerCount) from ContributionSpecimenStat s", Number.class).getSingleResult()).longValue();
		// long contributionsCount = ContributionAnswer.count("userId is not null");

		render(missionsCount, herbonautesCount, specimensCount, contributionsCount);
	}

    @Transactional(readOnly=true)
    public static void profileMenu() {
    	render("menus/profile.html");
    }
    
    @Transactional(readOnly=true)
    public static void missionsMenu() {
    	render("menus/missions.html");
    }

    @Transactional(readOnly=true)
    public static void currentUser() {
        renderJSON(Security.connected(), UserContributionJsonSerializer.get());
    }
    @Transactional(readOnly=true)
    public static void currentUserPassedQuiz() {
        List<PassedQuiz> passedQuizList = PassedQuiz.find("userId = ?", Security.connectedId()).fetch();
        renderJSON(passedQuizList);
    }
    
    /**
     * Accueil herbonaute (utilisateur connecté)
     */
    private static void indexConnected() {
    	User user = User.findById(connectedId());
    	//List<Mission> missions = user.getMissions();

		if (response.cookies.get("invitation") != null) {
			flash.success("invitation", "message.invitation.welcome");
			response.removeCookie("invitation");
		}

		List<MissionSimple> missions = user.getSimpleMissions();

    	List<Mission> allMissions = new ArrayList();

		if (missions == null || missions.size() == 0) {
    		allMissions = Mission.getFirstMissions(Herbonautes.get().missionsCountOnIndex);
    	}

    	//Mission mainMission = Mission.lastContributedMission(connectedLogin());
    	MissionSimple mainMission = Mission.lastContributedSimpleMission(user.id);


    	if (mainMission == null && missions.size() > 0) {
    		mainMission =  missions.get(0);
    	}
    	
    	List<Announcement> announcements = Mission.getAnnouncementsForUser(connectedId());
    	List<Alert> alerts =
				Alert.find("user.id =  ? and userRead = false order by date desc", connectedId())
					.fetch(Herbonautes.get().pageLength + 1);
    	
    	renderTemplate("Application/indexConnected.html", missions, allMissions, announcements, alerts, mainMission);
    }
    
    // ---- Methode utilitaire
    
    /**
     * Retourne l'image
     */
    protected static void renderImage(Long id) {
    	Image image = Image.findById(id);
		notFoundIfNull(image);
    	response.cacheFor(image.id.toString(), "24h", 0);
		renderBinary(image.getInputStream(), "img_" + image.id, image.getMimeType(), true);
    }
    
    @Transactional(readOnly=true)
    public static void getLogo() {

                // Redirection vers le logo
    	        String logoUrl = Herbonautes.get().logoUrl;
                String url = Router.reverse(VirtualFile.fromRelativePath(logoUrl));
                redirect(url);

    }


}