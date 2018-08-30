package controllers;

import inspectors.Event;
import inspectors.InspectorChain;

import java.util.List;

import play.Logger;
import play.data.validation.Valid;
import net.sf.ehcache.util.counter.sampled.TimeStampedCounterValue;
import conf.Herbonautes;
import models.comments.Comment;
import models.comments.MissionComment;
import models.comments.SpecimenComment;
import models.serializer.MissionSimpleJsonSerializer;
import models.serializer.UserSimpleJsonSerializer;
import models.serializer.activities.TimestampSinceJsonSerializer;

/**
 * Controler commentaires
 */
public class Comments extends Application {

	/**
	 * Ajoute un commentaire pour une mission 
	 */
	public static void addMissionComment(@Valid MissionComment comment) {
		if (comment.getUser() == null ||  
				!Security.connectedId().equals(comment.getUser().id)) {
			forbidden();
		}
		
		if (validation.hasErrors()) {
			response.status = 400; // Bad request
			renderJSON(validation.errorsMap());
		}
		
		comment.save();
		comment.refresh();
		
		InspectorChain.get().inspect(Event.COMMENT_MISSION_ADDED, comment);
		
		renderJSON(comment, 
				MissionSimpleJsonSerializer.get(), 
				UserSimpleJsonSerializer.get(),
				TimestampSinceJsonSerializer.get());
	}
	
	/**
	 * Derniers commentaires pour une mission
	 */
	public static void lastMissionComments(Long id, Long before) {
		
		List<MissionComment> comments = null;
		
		if (before == null) {
			comments = MissionComment.find("mission.id = ? order by date desc", id)
			.fetch(Herbonautes.get().pageLength);
		} else {
			comments = MissionComment.find("mission.id = ? and id<  ? order by date desc", id, before)
			.fetch();
		}
		
		renderJSON(comments, 
				MissionSimpleJsonSerializer.get(), 
				UserSimpleJsonSerializer.get(),
				TimestampSinceJsonSerializer.get());
	}
	
	/**
	 * Retourne le nombre de commentaires pour la mission d'id {id} et
	 * avant le commentaire d'id {before}
	 */
	public static void missionCommentsCount(Long id, Long before) {
		Long count = MissionComment.count("mission.id = ? and id < ?", id, before);
		renderJSON(count);
		
	}
	
	
	/**
	 * Ajoute un commentaire pour un spécimen
	 */
	public static void addSpecimenComment(@Valid SpecimenComment comment) {
		if (comment.getUser() == null ||  
				!Security.connectedId().equals(comment.getUser().id)) {
			forbidden();
		}
		
		if (validation.hasErrors()) {
			response.status = 400; // Bad request
			renderJSON(validation.errorsMap());
		}
		
		comment.save();
		comment.refresh();
		
		InspectorChain.get().inspect(Event.COMMENT_SPECIMEN_ADDED, comment);
		
		renderJSON(comment, 
				MissionSimpleJsonSerializer.get(), 
				UserSimpleJsonSerializer.get(),
				TimestampSinceJsonSerializer.get());
	}
	
	/**
	 * Derniers commentaires pour un sp��cimen 
	 */
	public static void lastSpecimenComments(Long id, Long before) {
		List<SpecimenComment> comments = null;

		if (before == null) {
			comments = SpecimenComment.find("specimen.id = ? order by date desc", id)
			.fetch(Herbonautes.get().pageLength);
		} else {
			comments = SpecimenComment.find("specimen.id = ? and id < ? order by date desc", id, before)
			.fetch();
		}
		
		renderJSON(comments, 
				MissionSimpleJsonSerializer.get(), 
				UserSimpleJsonSerializer.get(),
				TimestampSinceJsonSerializer.get());
	}
	
}
