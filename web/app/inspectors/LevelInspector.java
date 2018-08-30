package inspectors;

import models.PassedQuiz;
import models.User;
import models.contributions.Contribution;
import models.contributions.ContributionFeedback;
import models.questions.ContributionAnswer;
import models.quiz.Quiz;
import conf.Herbonautes;
import controllers.Security;
import play.Logger;
import services.ContributionConflictService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Inspector qui modifie les niveaux de l'utilisateur
 */
public class LevelInspector extends BaseInspector implements Inspector {

	@Override
	public void inspect(Event event, Object... objects) {
		
		switch(event) {
			case CONTRIBUTION_V2_ADDED:
                changeLevel(objects);
				break;
				
			case QUIZ_PASSED:
                passedQuiz(objects);
				break;
		}

	}

    public void changeLevel(Object... objects) {
        User user = extract(User.class, objects);
        Long contributionCount = ContributionAnswer.count("userId = ? and deleted != true", user.id);

        Integer nextThreshold = Herbonautes.get().unlockContributionCount(user.getLevel() + 1);
        if (nextThreshold < 0) {
            // Max niveau atteint
            return;
        }
        //Logger.info("Current level %d, unlock for level %d : %d", user.getLevel(), user.getLevel() + 1, nextThreshold);
        if (contributionCount >= nextThreshold) {
            User huser = User.findById(user.id);
            huser.setLevel(huser.getLevel() + 1);
            huser.save();
            huser.refresh();
            Security.refreshConnected();
            Logger.info("%d contributions for user -> change level to %d", contributionCount, huser.getLevel());
            ContributionConflictService.ConflictReport report = extract(ContributionConflictService.ConflictReport.class, objects);
            report.attributes.put("newLevel", huser.getLevel().toString());
        }
    }

	/**
	 * On ajoute le quiz à la liste des quiz de l'utilisateur
	 * 
	 * @param objects 
	 */
	public void passedQuiz(Object... objects) {
		/*Quiz quiz = extract(Quiz.class, objects);
		User user = extract(User.class, objects);
		
		//
		user = User.findByLogin(user.getLogin(), false);
		
		if (user.getPendingLevel() >= quiz.getUnlockingLevel()) {
			user.setLevel(quiz.getUnlockingLevel());
		}
		user.save();               */

        User user = extract(User.class, objects);
        Quiz quiz = extract(Quiz.class, objects);

        if (PassedQuiz.count("userId = ? and quizId = ?", user.id, quiz.id) > 0) {
            Logger.info("Utilisateur %s a déjà passé le quiz %s", user.getLogin(), quiz.getName());
        } else {
            PassedQuiz passedQuiz = new PassedQuiz(user.id, quiz.id, new Date());
            passedQuiz.save();
        }

		Security.refreshConnected();
	}
	
	/**
	 * Modifie le niveau "en attente"
	 */
	public void changePendingLevelIfNeeded(Object... objects) {
		
		Contribution contribution = extract(Contribution.class, objects);
		
		if (contribution == null) {
			return;
		}
		
		Integer lvl = contribution.getUser().getLevel();
		Long cnt = User.getContributionsCount(contribution.getUser().getLogin());
		
		Herbonautes h = Herbonautes.get();
		
		boolean levelUp = false;
		
		//
		Integer pending = 0;
		for (int i = 1 ; i <= 5 ; i++) {
			if (lvl <= i && cnt > h.unlockContributionCount(i+1)) {
				pending = i+1;
				levelUp = true;
			}
		}
		/*levelUp =	(lvl == 1 && cnt > h.unlockContributionCount(2))  ||	
			(lvl == 2 && cnt > h.unlockContributionCount(3))  ||
			(lvl == 3 && cnt > h.unlockContributionCount(4))  ||  
			(lvl == 4 && cnt > h.unlockContributionCount(5))  ||	
			(lvl == 5 && cnt > h.unlockContributionCount(6)); */
		
		if (levelUp) {
			contribution.getUser().pendingLevelUp(pending);
			Security.refreshConnected();
		}
		
		ContributionFeedback feedBack = extract(ContributionFeedback.class, objects);
		if (feedBack != null) {
			feedBack.setLevelUp(levelUp);
			feedBack.setUserLevel(contribution.getUser().getLevel());
			feedBack.setUserPendingLevel(contribution.getUser().getPendingLevel());
		}
		
	}
	

	
	
}
