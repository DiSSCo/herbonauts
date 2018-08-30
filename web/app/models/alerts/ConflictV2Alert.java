package models.alerts;

import models.Specimen;
import models.User;
import models.contributions.Contribution;
import models.questions.ContributionAnswer;
import models.questions.ContributionQuestion;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.List;

@Entity
@DiscriminatorValue("CONFLICT_V2")
public class ConflictV2Alert extends Alert {

    @ManyToOne
    @JoinColumn(name = "SPECIMEN_ID")
    private Specimen specimen;

    @ManyToOne
    @JoinColumn(name = "CONTRIBUTION_QUESTION_ID")
    private ContributionQuestion question;

	@ManyToOne
    @JoinColumn(name = "CONTRIBUTION_ANSWER_ID")
	private ContributionAnswer answer;

    @ManyToOne
    @JoinColumn(name = "OTHER_USER_ID")
    private User otherUser;

    public ConflictV2Alert(User otherUser, Specimen specimen, ContributionQuestion question, ContributionAnswer answer) {
        this.specimen = specimen;
        this.question = question;
        this.answer = answer;
        this.otherUser = otherUser;
    }

    public ContributionAnswer getOtherAnswer() {
        ContributionAnswer userAnswer =
                ContributionAnswer.find("userId = ? and questionId = ? and specimenId = ? and missionId = ?"
                ,this.getOtherUser().id, this.answer.getQuestionId(), this.answer.getSpecimenId(), this.answer.getMissionId())
                .first();

        return userAnswer;
    }

    public Specimen getSpecimen() {
        return specimen;
    }

    public void setSpecimen(Specimen specimen) {
        this.specimen = specimen;
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

    public User getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(User otherUser) {
        this.otherUser = otherUser;
    }
}


