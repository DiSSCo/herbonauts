package inspectors;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonElement;
import conf.Herbonautes;

import libs.Json;
import models.Mission;
import models.UserInvitation;
import models.discussions.Discussion;
import models.discussions.Message;
import models.questions.ContributionAnswer;
import models.questions.ContributionQuestion;
import play.Logger;
import models.User;
import models.badges.Badge;
import models.contributions.Contribution;
import models.contributions.ContributionFeedback;

/**
 * Création des badges
 */
public class BadgeInspector extends BaseInspector implements Inspector{

	
	@Override
	public void inspect(Event event, Object... objects) {
		switch(event) {
			
			case PROFILE_EDITED: 
				createCoolBadge(objects);
				break;
				
			case CONTRIBUTION_V2_ADDED:
				createSherlockBadge(objects);
				createPodiumBadge(objects);
				createEnderBadge(objects);
				createNightBadge(objects);
				createExplorerBadge(objects);
				break;

			case MISSION_PROPOSITION_ACCEPTED:
				createPioneerBadge(objects);
				break;

			case DISCUSSION_CREATED:
				createClassifierBadge(objects);
				createAnimatorBadge(objects);
				createWriterBadge(objects);
				break;

			case DISCUSSION_RESOLVED:
				createPedagogueBadge(objects);
				createSolidaryBadge(objects);
				break;

			case MESSAGE_POSTED:
				createWriterBadge(objects);
				createAnimatorBadge(objects);
				break;

			case USER_CONNECT:
				createCandleBadge(objects);
				break;

			case USER_INVITATION:
				createInvitationBadge(objects);
				break;
		}
	}
	
	private void createEnderBadge(Object... objects) {
		ContributionAnswer contribution = extract(ContributionAnswer.class, objects);
		if (contribution == null) {
			return;
		}

		User user = extract(User.class, objects);
		Logger.info("Create ender badge for %s ?", user.getLogin());
		
		if (user.hasBadge(Badge.Type.ENDER)) {
			Logger.info("Badge ender already winned by %s", user.getLogin());
			return;
		}

		Long nContributions =
			ContributionAnswer.countUserAnswers(contribution.getUserId(), contribution.getMissionId(), contribution.getSpecimenId());

		Long nQuestion = ContributionQuestion.countQuestionsForMission(contribution.getMissionId());

		Logger.info("%d answers out of %d questions", nContributions, nQuestion);

		boolean ender = nContributions >= nQuestion;

		if (ender) {
			Badge badge = Badge.add(user, Badge.Type.ENDER);
			chain(badge, objects);
		}
			
		
	}
	
	private void createNightBadge(Object... objects) {
		ContributionAnswer contribution = extract(ContributionAnswer.class, objects);
		if (contribution == null) {
			return;
		}

		User user = extract(User.class, objects);

		Logger.info("Create night badge for %s ?", user.getLogin());
		
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(contribution.getCreatedAt());
		
		if (calendar.get(Calendar.HOUR_OF_DAY) >= Herbonautes.get().nightBadgeHourStart && 
			calendar.get(Calendar.HOUR_OF_DAY) < Herbonautes.get().nightBadgeHourEnd) {
			Badge badge = Badge.add(user, Badge.Type.NIGHT);
			chain(badge, objects);
		}
		
	}
	
	private void createExplorerBadge(Object... objects) {
		ContributionAnswer contribution = extract(ContributionAnswer.class, objects);
		if (contribution == null) {
			return;
		}

		User user = extract(User.class, objects);

		Logger.info("Create explorer badge for %s ?", user.getLogin());
		
		if (user.hasBadge(Badge.Type.EXPLORER)) {
			Logger.info("Badge explorer already winned by %s", user.getLogin());
			return;
		}

		Long contribCount = Mission.getContributionsCount(contribution.getMissionId());
		if (contribCount <= Herbonautes.get().explorerBadgeThreshold) {
			Badge badge = Badge.add(user, Badge.Type.EXPLORER);
			chain(badge, objects);
		}
		
	}
	
	private void createCoolBadge(Object... objects) {
		User user = extract(User.class, objects);
		if (user == null) {
			return;
		}
		
		Logger.info("Create cool badge for %s ?", user.getLogin());
		
		if (user.hasBadge(Badge.Type.COOL)) {
			Logger.info("Badge cool already winned by %s", user.getLogin());
			return;
		}
		
		if (user.isHasImage()) {
			Badge badge = Badge.add(user, Badge.Type.COOL);
			chain(badge, objects);
		}
		
	}

	private void createInvitationBadge(Object... objects) {
		UserInvitation invitation = extract(UserInvitation.class, objects);
		if (invitation.getFromUser().hasBadge(Badge.Type.INVITATION)) {
			Logger.info("Badge invitation already winned by %s", invitation.getFromUser().getLogin());
			return;
		}
		Logger.info("Create badge invitation for %s", invitation.getFromUser().getLogin());
		Badge badge = Badge.add(invitation.getFromUser(), Badge.Type.INVITATION);
		chain(badge, objects);
	}
	
	private void createSherlockBadge(Object... objects) {
		//Contribution contribution = extract(Contribution.class, objects);
		ContributionAnswer contribution = extract(ContributionAnswer.class, objects);

		if (contribution == null) {
			return;
		}

		User user = extract(User.class, objects);

		Logger.info("Create sherlock badge for %s ?", user.getLogin());
		
		if (user.hasBadge(Badge.Type.SHERLOCK)) {
			Logger.info("Badge sherlock already winned by %s", user.getLogin());
			return;
		}

		String json = contribution.getJsonValue();
		if (json != null) {
			JsonNode jsonResponse = Json.parse(json);
			if (jsonResponse.hasNonNull("guessed") && jsonResponse.get("guessed").asBoolean()) {
				Badge badge = Badge.add(user, Badge.Type.SHERLOCK);
				chain(badge, objects);
			}
		}
		
	}
	
	private void createPodiumBadge(Object... objects) {
		ContributionAnswer contribution = extract(ContributionAnswer.class, objects);
		if (contribution == null) {
			return;
		}

		User user = extract(User.class, objects);
		Logger.info("Create podium badge for %s ?", user.getLogin());
		
		if (user.hasBadge(Badge.Type.PODIUM)) {
			Logger.info("Badge podium already winned by %s", user.getLogin());
			return;
		}
		
		boolean podium = false;
		/*for (User user : contribution.getMission().getTopContributors()) {
			Logger.debug("%s %s", user.id, user.getLogin());
			if (contribution.getUser().id.equals(user.id)) {
				podium = true;
				break;
			}
		}*/

		if (Mission.isUserInTop(contribution.getMissionId(), contribution.getUserId())) {
			podium = true;
		}


		
		if (podium) {
			Badge badge = Badge.add(user, Badge.Type.PODIUM);
			chain(badge, objects);
		}
		
	}

	private void chain(Badge badge, Object... objects) {
		if (badge != null) {
			InspectorChain.get().inspect(Event.BADGE_WINNED, badge);
			ContributionFeedback fb = extract(ContributionFeedback.class, objects);
			if (fb != null) {
				fb.addAttribute("badge.win", badge.getType());
			}
		}
	}


	private void createPioneerBadge(Object... objects) {
		Mission mission = extract(Mission.class, objects);
		User user = extract(User.class, objects);
		user = User.findById(user.getId());
		if (user == null || mission == null) {
			return;
		}

		Logger.info("Create pioneer badge for %s ?", user.getLogin());

		if (user.hasBadge(Badge.Type.PIONEER)) {
			Logger.info("Badge pioneer already winned by %s", user.getLogin());
			return;
		}

		if (!mission.isProposition() && user.isLeader()) {
			Badge badge = Badge.add(user, Badge.Type.PIONEER);
			chain(badge, objects);
		}
	}

	private void createClassifierBadge(Object... objects) {
		User user = extract(User.class, objects);
		user = User.findById(user.getId());
		if (user == null) {
			return;
		}
		Logger.info("Create classifier badge for %s ?", user.getLogin());
		if (user.hasBadge(Badge.Type.CLASSIFIER)) {
			Logger.info("Badge classifier already possessed by %s", user.getLogin());
			return;
		}
		if(Discussion.countDiscussionsWithTagsByUser(user) >= Herbonautes.get().classifierMinDiscussions) {
			Badge badge = Badge.add(user, Badge.Type.CLASSIFIER);
			chain(badge, objects);
		}
	}


	private void createPedagogueBadge(Object... objects) {
		User user = extract(User.class, objects);
		user = User.findById(user.getId());
		if (user == null) {
			return;
		}

		Logger.info("Create pedagogue badge for %s ?", user.getLogin());

		if (user.hasBadge(Badge.Type.PEDAGOGUE)) {
			Logger.info("Badge pedagogue possessed by %s", user.getLogin());
			return;
		}

		if (Message.countResolutionMessagesByUser(user).intValue() >= Herbonautes.get().pedagogueMinMessages) {
			Badge badge = Badge.add(user, Badge.Type.PEDAGOGUE);
			chain(badge, objects);
		}
	}

	private void createSolidaryBadge(Object... objects) {
		Message message = extract(Message.class, objects);
		User user = User.findById(message.getAuthor().getId());
		if (user == null) {
			return;
		}

		Logger.info("Create solidary badge for %s ?", user.getLogin());

		if (user.hasBadge(Badge.Type.SOLIDARY)) {
			Logger.info("Badge solidary possessed by %s", user.getLogin());
			return;
		}

		if (Message.countSosResolvedDiscussionsByUser(user).intValue() >= Herbonautes.get().solidaryMinMessages) {
			Badge badge = Badge.add(user, Badge.Type.SOLIDARY);
			chain(badge, objects);
		}
	}

	private void createWriterBadge(Object... objects) {
		User user = extract(User.class, objects);
		user = User.findById(user.getId());
		if (user == null) {
			return;
		}

		Logger.info("Create writer badge for %s ?", user.getLogin());

		if (user.hasBadge(Badge.Type.WRITER)) {
			Logger.info("Badge writer already possessed by %s", user.getLogin());
			return;
		}

		Long count = Message.countMessageByMinLengthAndUser(user, Herbonautes.get().writerMinMessageLength);

		if (count != null && count > 0L) {
			Badge badge = Badge.add(user, Badge.Type.WRITER);
			chain(badge, objects);
		}
	}

	private void createAnimatorBadge(Object... objects) {
		User user = extract(User.class, objects);
		user = User.findById(user.getId());
		if (user == null) {
			return;
		}

		Logger.info("Create animator badge for %s ?", user.getLogin());

		if (user.hasBadge(Badge.Type.ANIMATOR)) {
			Logger.info("Badge animator already winned by %s", user.getLogin());
			return;
		}

		if (Message.countMessagesResponsesByUser(user).intValue() >= Herbonautes.get().animatorMinMessages
				|| Discussion.countDiscussionsByCreator(user).intValue() >= Herbonautes.get().animatorMinDiscussions) {
			Badge badge = Badge.add(user, Badge.Type.ANIMATOR);
			chain(badge, objects);
		}
	}

	private void createCandleBadge(Object... objects) {
		User user = extract(User.class, objects);
		user = User.findById(user.getId());
		if (user == null) {
			return;
		}

		Logger.info("Create candle badge for %s ?", user.getLogin());

		if (user.hasBadge(Badge.Type.CANDLE)) {
			Logger.info("Badge candle already winned by %s", user.getLogin());
			return;
		}

		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		if (User.isEligibleUsersForCandleBadge(calendar.getTime(), user)) {
			Badge badge = Badge.add(user, Badge.Type.CANDLE);
			chain(badge, objects);
		}
	}

}
