package inspectors;

import java.util.Calendar;
import java.util.Date;

import conf.Herbonautes;

import models.Specimen;
import models.activities.*;
import models.discussions.Message;
import models.questions.ContributionAnswer;
import models.questions.ContributionQuestion;
import play.Logger;
import play.templates.JavaExtensions;
import models.Mission;
import models.User;
import models.badges.Badge;
import models.comments.MissionComment;
import models.comments.SpecimenComment;
import models.contributions.Contribution;

/**
 * Inspector pour les activités
 */
public class ActivityInspector extends BaseInspector implements Inspector {

	@Override
	public void inspect(Event event, Object... objects) {
		
		switch (event) {
			
			case CONTRIBUTION_ADDED:	
				createContributionAdd(objects);
				break;

            case CONTRIBUTION_V2_ADDED:
                createContributionV2Add(objects);
                break;
				
			case BADGE_WINNED:			
				createBadgeWin(objects);
				break;
				
			case MISSION_OPENED:
				createMissionOpened(objects);
				break;

			case MISSION_PUBLISHED:
				createMissionPublished(objects);
				break;
				
			case MISSION_JOINED:
				createMissionJoined(objects);
				break;
				
			case COMMENT_MISSION_ADDED:
				createCommentMissionAdded(objects);
				break;
				
			case COMMENT_SPECIMEN_ADDED:
				createCommentSpecimenAdded(objects);
				break;
				
			case USER_SIGNUP:
				createSignUp(objects);
				break;
				
		}
		
	}
	
	private void createSignUp(Object... objects) {
		User user = extract(User.class, objects);
		if (user == null) { 
			return;
		}
		UserSignUpActivity activity = new UserSignUpActivity(user);
		activity.save();
		activity.refresh();
	}
	
	private void createCommentMissionAdded(Object... objects) {
		Message message = extract(Message.class, objects);
		Mission mission = extract(Mission.class, objects);
		if (message == null || mission == null) {
			return;
		}

		CommentMissionActivity activity = new CommentMissionActivity(message.getAuthor(), mission);
		activity.save();
		activity.refresh();
	}
	
	private void createCommentSpecimenAdded(Object... objects) {
		Message message = extract(Message.class, objects);
		Specimen specimen = extract(Specimen.class, objects);
		if (message == null || specimen == null) {
			return;
		}
		CommentSpecimenActivity activity = new CommentSpecimenActivity(message.getAuthor(), specimen);
		activity.save();
		activity.refresh();
	}
	
	
	private void createMissionPublished(Object... objects) {
		Mission mission = extract(Mission.class, objects);
		if (mission == null) { 
			return;
		}
		MissionPublishActivity activity = new MissionPublishActivity(mission);
		activity.save();
		activity.refresh();
	}
	
	private void createMissionJoined(Object... objects) {
		Mission mission = extract(Mission.class, objects);
		User user = extract(User.class, objects);
		if (mission == null) { 
			return;
		}
		MissionJoinActivity activity = new MissionJoinActivity(user, mission);
		activity.save();
		activity.refresh();
	}
	
	
	private void createMissionOpened(Object... objects) {
		Mission mission = extract(Mission.class, objects);
		if (mission == null) { 
			return;
		}
		MissionOpenActivity activity = new MissionOpenActivity(mission);
		activity.save();
		activity.refresh();
	}
	
	private void createBadgeWin(Object... objects) {

		Badge badge = extract(Badge.class, objects);
		if (badge == null) {
			return;
		}
		
		BadgeWinActivity activity = new BadgeWinActivity(badge);
		activity.save();
		activity.refresh();
	}

    private void createContributionV2Add(Object... objects) {

        User user = extract(User.class, objects);
        Mission mission = extract(Mission.class, objects);
        Specimen specimen = extract(Specimen.class, objects);
        ContributionQuestion question = extract(ContributionQuestion.class, objects);
        ContributionAnswer answer = extract(ContributionAnswer.class, objects);



		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, -1 * Herbonautes.get().activityTimeBuffer);
		Date recentDate = cal.getTime();

		ContributionV2AddActivity recent =
				ContributionV2AddActivity.find("user = ? and date > ?",
						user,
						recentDate).first();

		if (recent == null) {
			ContributionV2AddActivity activity = new ContributionV2AddActivity(user, mission, specimen, question, answer);
			activity.save();
			activity.refresh();
		} else {
			Logger.info("Activité récente pour %s, ignorée", user.getLogin());
		}
    }

	private void createContributionAdd(Object... objects) {
		
		Contribution contribution = extract(Contribution.class, objects);
		if (contribution == null) {
			return;
		}
		
		// Vérifie qu'une contribution n'a pas été faite récemment
		
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, -1 * Herbonautes.get().activityTimeBuffer);
		Date recentDate = cal.getTime();
		
		ContributionAddActivity recent = 
			ContributionAddActivity.find("user = ? and date > ?", 
					contribution.getUser(),
					recentDate).first();
		
		if (recent == null) {
			ContributionAddActivity activity = 
				new ContributionAddActivity(contribution);
			activity.save();
			activity.refresh();
		} else {
			Logger.info("Activité récente pour %s, ignorée", contribution.getUser().getLogin());
		}
	}
	

	
	
}
