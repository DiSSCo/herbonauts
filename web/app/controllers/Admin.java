package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import models.Mission;
import models.User;
import models.serializer.PageJsonSerializer;
import models.serializer.UserForAdminJsonSerializer;
import play.Logger;
import play.Play;
import play.libs.WS;
import play.mvc.Before;
import play.utils.HTTP;
import services.Page;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller d'admin. Ces actions ne sont autorisées que pour l'administrateurs
 */
public class Admin extends Application {

    private static final int PAGE_SIZE = 3;

	@Before
	static void checkAdmin() {
		Security.forbiddenIfNotAdmin();
	}

	
	/**
	 * Affiche la liste des missions (pour gestion des priorités)
	 */
	public static void missionsList() {
		List<Mission> missions = Mission.find("order by priority").fetch();
		render(missions);
	}
	
	/**
	 * Sauvegarde le tri des priorités
	 */
	public static void saveMissionsSort(Long[] sort) {
		
		// Dans /views/Admin/missionList.html, on retourne l'intégralité
		// de la liste des missions avec le nouvel ordre
		
		int index = 1;
		for (Long id : sort) {
			Mission mission = Mission.findById(id);
			mission.setPriority(index++);
			mission.save();
		}
		
		ok();
	}
	
	/**
	 * Affiche la liste des chefs de missions
	 */
	public static void leaders() {
		List<User> leaders = User.find("leader = true").fetch();
		render(leaders);
	}
	
	/** 
	 * Ajoute un chef de mission
	 */
	public static void addLeader(String login) {
		User user = User.findByLogin(login, false);
		
		if (user == null) {
			flash.error(play.i18n.Messages.get("user.doesnt.exists", login));
			leaders();
		}
		
		user.setLeader(true);
		user.save();
		
		leaders();
	}
	
	/**
	 * Supprime un chef de mission
	 */
	public static void removeLeader(String login) {
		User user = User.findByLogin(login, false);
		
		if (user == null) {
			flash.error(play.i18n.Messages.get("user.doesnt.exists", login));
			leaders();
		}
		
		user.setLeader(false);
		user.save();
		
		leaders();
	}

    /**
     * Gestion des droits (chef de mission, équipe, admin)
     */
    public static void adminUsers() {
        render();
    }

    public static void saveUser(Long id) throws IOException {
        User user = User.findById(id);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(request.body);
        Boolean leader = json.get("leader").asBoolean();
        Boolean admin = json.get("admin").asBoolean();
        Boolean team = json.get("team").asBoolean();

        // Ne pas supprimer le dernier admin
        if (!Security.connectedId().equals(id)) {
            user.setAdmin(admin);
        }
        user.setLeader(leader);
        user.setTeam(team);
        user.save();


        renderJSON(user, UserForAdminJsonSerializer.get());
    }


    public static void findUsers(Integer page, String filter, String sortBy, String order) {
        Logger.debug("users {}, {}", sortBy, order);
        Page<User> users = User.findUsers(filter, page, PAGE_SIZE, sortBy, order);
        renderJSON(users, PageJsonSerializer.get(), UserForAdminJsonSerializer.get());
    }




}
