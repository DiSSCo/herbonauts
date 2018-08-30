package models.activities;

import models.Mission;
import models.Specimen;
import models.User;
import models.contributions.Contribution;
import models.questions.ContributionAnswer;
import models.questions.ContributionQuestion;

import javax.persistence.*;

@Entity
@DiscriminatorValue("CONTRIBUTION_V2_ADD")
public class ContributionV2AddActivity extends Activity {

	@ManyToOne
	private User user;

	@ManyToOne
	private Mission mission;

    @JoinColumn(name = "CONTRIBUTION_QUESTION_ID")
    @ManyToOne
    private ContributionQuestion question;

    @JoinColumn(name = "CONTRIBUTION_ANSWER_ID")
	@ManyToOne
	private ContributionAnswer answer;

	@ManyToOne
	private Specimen specimen;

	public ContributionV2AddActivity(User user, Mission mission, Specimen specimen, ContributionQuestion question, ContributionAnswer answer) {
		super();
		this.setUser(user);
		this.setMission(mission);
		this.setSpecimen(specimen);
		this.setAnswer(answer);
        this.setQuestion(question);
	}

    public ContributionQuestion getQuestion() {
        return question;
    }

    public void setQuestion(ContributionQuestion question) {
        this.question = question;
    }

    public ContributionAnswer getAnswer() {
        return answer;
    }

    public void setAnswer(ContributionAnswer answer) {
        this.answer = answer;
    }

    public void setSpecimen(Specimen specimen) {
		this.specimen = specimen;
	}

	public Specimen getSpecimen() {
		return specimen;
	}

	public void setMission(Mission mission) {
		this.mission = mission;
	}

	public Mission getMission() {
		return mission;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}
	
	
}
