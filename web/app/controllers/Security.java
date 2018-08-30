package controllers;

import static controllers.Security.connectedLogin;
import static controllers.Security.isConnected;

import java.util.List;

import com.google.gson.JsonElement;

import conf.Herbonautes;

import helpers.RecolnatAPIClient;
import helpers.RecolnatUser;
import models.Mission;
import models.User;
import play.Logger;
import play.cache.Cache;
import play.libs.OAuth2;
import play.libs.WS;
import play.modules.cas.models.CASUser;
import play.mvc.Controller;
import play.mvc.Scope;

public class Security extends Controller {

	public static final String USER_LOGIN_KEY = "_ulo";
	public static final String USER_ID_KEY = "_uid";
	public static final String USER_IS_LEADER = "_uil";
	public static final String USER_IS_ADMIN = "_uia";
    public static final String USER_IS_TEAM = "_uit";
	public static final String USER_IS_FB = "_fb";
	
	
	public static OAuth2 FACEBOOK = new OAuth2(
            "https://graph.facebook.com/oauth/authorize",
            "https://graph.facebook.com/oauth/access_token",
            Herbonautes.get().fbAppID,
            Herbonautes.get().fbSecretKey
    );
    
	
	static boolean isLeader() {
		return isConnected() &&  Boolean.valueOf(session.get(USER_IS_LEADER));
	}

    static boolean isTeam() {
        return isConnected() &&  Boolean.valueOf(session.get(USER_IS_TEAM));
    }
	
	static boolean isAdmin() {
		return isConnected() &&  Boolean.valueOf(session.get(USER_IS_ADMIN));
	}
	
	static boolean isConnected() {
		return connectedId() != null;
	}
	
	static boolean isFB() {
		return isConnected() && (session.get(USER_IS_FB) != null);
	}
	
	static String connectedLogin() {
        return session.get(USER_LOGIN_KEY);
    }
    
    static Long connectedId() {
    	String id = session.get(USER_ID_KEY) ;
    	return id != null ? Long.valueOf(id) : null;
    }
    
	static void connect(User user) {

        session.put(USER_LOGIN_KEY, user.getLogin());
		session.put(USER_ID_KEY, user.id.toString());
		session.put(USER_IS_LEADER, user.isLeader());
		session.put(USER_IS_ADMIN, user.isAdmin());
        session.put(USER_IS_TEAM, user.isTeam());


		//Logger.info("Connect user %s (%d,%d)", user.getLogin(), user.getLongitude(), user.getLatitude());

		if (user.getLongitude() != null && user.getLatitude() != null) {
			session.put("locationSaved", true);
		}

		if (user.getFacebookId() != null) {
			session.put(USER_IS_FB, true);
		}
		User.findByLogin(user.getLogin(), false);
	}  
    
	static User findUser(String loginOrEmail) {
		User user = 
			User.find("lower(login) = ? or email = ?", 
					loginOrEmail.trim().toLowerCase(), 
					loginOrEmail).first();
		return user;
	}
	
	static User authenticate(String loginOrEmail, String password) {
		User user = findUser(loginOrEmail);
		
		if (user != null && password.equals(user.getPassword())) {
			return user;
		} else {
			return null;
		}
	}
	
	
	static JsonElement fbAuthenticate(String uid, String accessToken) {
		
		JsonElement userJSON = WS.url(
			    "https://graph.facebook.com/me?access_token=%s", accessToken
			).get().getJson();
		
		return userJSON;
	}
	
	static void disconnect() {
		session.clear();
	}
	
	static User connectedUser() {
		return connected();
	}
	
	public static User refreshConnected() {
		if (!isConnected()) return null;
		User user = User.findByLogin(connectedLogin(), false);
		return user;
	}
	
    static User connected() {
    	if (!isConnected()) return null;
    	User user = User.findByLogin(connectedLogin());
    	return user;
    }
    // TODO uniquement les missions courantes
    static List<Mission> connectedUserMissions() {
    	if (!isConnected()) return null;
    	User user = connected();

		List<Mission> missions = (List<Mission>) Cache.get("connected_missions_" + user.id);
		if (missions == null) {
			missions = user.getCurrentMissions();
			Cache.add("connected_missions_" + user.id, missions, "10min");
		} else {
			Logger.info("user missions from cache (" + request.url + ")");
		}

    	return missions;
    }
    
    static List<Mission> connectedLeaderMissions() {
    	if (!isConnected()) return null;
    	User user = connected();
    	//return user.getLeadMissions();

		List<Mission> missions = (List<Mission>) Cache.get("connected_leader_missions_" + user.id);
		if (missions == null) {
			missions = user.getLeadMissions();
			Cache.add("connected_leader_missions_" + user.id, missions, "10min");
		} else {
			Logger.info("leader missions from cache");
		}

		return missions;
    }
    
    static boolean isCurrentUser(String login) {
    	return connectedLogin() != null && connectedLogin().equals(login);
    }
    
    static void forbiddenIfNotCurrentUser(String login) {
    	if (!isCurrentUser(login)) {
    		forbidden();
    	}
    }
    
    static void forbiddenIfNotAdmin() {
    	if (!isAdmin()) {
    		forbidden();
    	}
    }

    static void forbiddenIfNotTeam() {
        if (!isTeam()) {
            forbidden();
        }
    }

	static void forbiddenIfNotSpecial() {
		if (!isAdmin() && !isLeader() && !isTeam()) {
			forbidden();
		}
	}

    static void forbiddenIfNotAdminOrLeader() {
    	if (!isAdmin() && !isLeader() && !isTeam()) {
    		forbidden();
    	}
    }
    
    static void forbiddenIfNotLeader() {
    	if (!isLeader() && !isTeam()) {
    		forbidden();
    	}
    }
    
    static boolean passwordMatch(User user, String password) {
    	return (user != null && password != null && password.equals(user.getPassword()));
    }
	
}