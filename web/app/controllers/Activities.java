package controllers;

import static controllers.Security.connectedLogin;

import helpers.GsonUtils;
import inspectors.ActivityInspector;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import models.Mission;
import models.Specimen;
import models.User;
import models.activities.Activity;
import models.questions.ContributionAnswer;
import models.questions.ContributionQuestion;
import models.serializer.MissionSimpleJsonSerializer;
import models.serializer.SpecimenSimpleJsonSerializer;
import models.serializer.UserSimpleJsonSerializer;
import models.serializer.activities.DateSinceJsonSerializer;
import models.serializer.activities.TimestampSinceJsonSerializer;
import models.serializer.contributions.ContributionAnswerJsonSerializer;
import models.serializer.contributions.ContributionQuestionJsonSerializer;
import models.serializer.contributions.ContributionQuestionSimpleJsonSerializer;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
//import sun.launcher.resources.launcher;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

/**
 * Controller retournant les activités
 * <br>
 * Les activités sont créées dans le {@link ActivityInspector} 
 */
public class Activities extends Controller {

	/** 
	 * Retourne les activités (tout le site, mes missions ou mes actions) 
	 * au format JSON
	 */
	public static void list(String view, Long before, String login, Long id) {
		
		List<Activity> activities = null; 
		if (view == null || view.equals("all")) { 
			activities = Activity.lastGlobalActivities(before);
		} else if (view.equals("missions")) {
			activities = Activity.lastMissionActivitiesForUser(Security.connectedId(), before);
		} else if (view.equals("mission")) {
			activities = Activity.lastMissionActivities(id, before);
		} else if (view.equals("user")) {
			activities = Activity.lastActivitiesForUser(login, before);
		} else if (view.equals("specimen")) {
			activities = Activity.lastActivitiesForSpecimen(id, before);
		}else {
			notFound("View \"" + view + "\" unknow. Usage all|missions|user");
		}
		
		GsonBuilder gson = GsonUtils.getGsonBuilder();
		
        gson.registerTypeAdapter(Mission.class, MissionSimpleJsonSerializer.get());
        gson.registerTypeAdapter(Specimen.class, SpecimenSimpleJsonSerializer.get());
        gson.registerTypeAdapter(User.class, UserSimpleJsonSerializer.get());
        gson.registerTypeAdapter(Timestamp.class, TimestampSinceJsonSerializer.get());

        gson.registerTypeAdapter(ContributionQuestion.class, ContributionQuestionSimpleJsonSerializer.get());
        gson.registerTypeAdapter(ContributionAnswer.class, ContributionAnswerJsonSerializer.get());

        renderJSON(gson.create().toJson(activities));
	}
	
}
