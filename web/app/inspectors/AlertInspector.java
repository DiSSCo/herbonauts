package inspectors;

import java.util.List;

import models.Specimen;
import models.alerts.*;
import models.questions.ContributionAnswer;
import models.questions.ContributionQuestion;
import play.Logger;

import models.Mission;
import models.User;
import models.comments.MissionComment;
import models.comments.SpecimenComment;
import models.contributions.Contribution;
import models.contributions.ContributionFeedback;
import services.ContributionConflictService;

/**
 * Création des alertes
 */
public class AlertInspector extends BaseInspector implements Inspector {

	/**
	 * Dispatch de événements
	 */
	@Override
	public void inspect(Event event, Object... objects) {

		switch (event) {
		
			case CONTRIBUTION_ADDED:	
				createConflictAlert(objects);
				break;

            case CONTRIBUTION_V2_ADDED:
                createConflictV2Alert(objects);
                break;

			case MISSION_ANNOUNCE:
				createAnnounceAlert(objects);
				break;
				
			/*case MISSION_PUBLISHED:
				createMissionPublishedAlert(objects);
				break;*/
				
			/*case COMMENT_MISSION_ADDED:
				createMissionCommentAlert(objects);
				break;
				
			/*case COMMENT_SPECIMEN_ADDED:
				createSpecimenCommentAlert(objects);
				break;*/
			
		}
		
	}
	
	private void createMissionCommentAlert(Object... objects) {
		MissionComment comment = extract(MissionComment.class, objects);
		
		List<User> users = comment.getIncludedUsers();
		String posterLogin = comment.getUser().getLogin();
		for (User user : users) {
			if (!posterLogin.equals(user.getLogin())) {
				MissionCommentAlert alert = new MissionCommentAlert(comment);
				alert.setUser(user);
				alert.save();
			}
		}
	}
	
	/*private void createSpecimenCommentAlert(Object... objects) {
		SpecimenComment comment = extract(SpecimenComment.class, objects);

		List<User> users = comment.getIncludedUsers();
		String posterLogin = comment.getUser().getLogin();
		for (User user : users) {
			if (!posterLogin.equals(user.getLogin())) {
				SpecimenCommentAlert alert = new SpecimenCommentAlert(comment);
				alert.setUser(user);
				alert.save();
			}
		}
	}*/
	
	private void createMissionPublishedAlert(Object... objects) {
		Mission mission = extract(Mission.class, objects);
		
		for (User user : User.<User>findAll()) {
			MissionPublishedAlert alert = new MissionPublishedAlert(mission);
			alert.setUser(user);
			alert.setMission(mission);
			alert.save();
		}
	}
	
	
	private void createAnnounceAlert(Object... objects) {
		Mission mission = extract(Mission.class, objects);
		
		for (User user : mission.getUsers()) {
		  if(!user.getLogin().equals(mission.getLeader().getLogin())){
  			AnnouncementAlert alert = new AnnouncementAlert(mission);
  			alert.setUser(user);
  			alert.setMission(mission);
  			alert.save();
		  }
		}
	}
	
	private void createConflictAlert(Object... objects) {
		
		ContributionFeedback fb = extract(ContributionFeedback.class, objects);
		
		if (fb.isConflicts()) {	
			Contribution contribution = extract(Contribution.class, objects);
			for (Contribution other : fb.getByOthers()) {
				if (contribution.isInConflict(other)) {
					ConflictAlert alert = new ConflictAlert(contribution);				
					alert.setUser(other.getUser());
					alert.save();
				}
			}
		}
		
	}

    private void createConflictV2Alert(Object... objects) {

        ContributionConflictService.ConflictReport report = extract(ContributionConflictService.ConflictReport.class, objects);
        User connected = extract(User.class, objects);

		if (!report.sendAlert) {
			Logger.info("Alertes ignorées");
			return;
		}

		if (!Boolean.TRUE.equals(report.stat.getConflicts())) {
			// On ne garde que les "vrais" conflits, si on doit départager
			return;
		}
        if (report.conflictAnswers != null && report.conflictAnswers.size() > 0) {

            Specimen specimen = extract(Specimen.class, objects);
            ContributionQuestion question = extract(ContributionQuestion.class, objects);

            for (ContributionAnswer answer : report.conflictAnswers) {

                ConflictV2Alert alert = new ConflictV2Alert(connected, specimen, question, answer);
                User user = new User();
                user.id = answer.getUserId();
                alert.setUser(user);
                alert.save();
				alert.refresh();
            }

        }

    }




}
