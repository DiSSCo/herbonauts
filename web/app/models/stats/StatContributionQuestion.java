package models.stats;


import models.questions.ContributionQuestion;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "HV_STAT_CONTRIBUTION_QUESTION")
public class StatContributionQuestion extends GenericModel {

    @Id
    @Column(name = "MISSION_ID")
    private Long missionId;

    @Id
    @ManyToOne
    @JoinColumn(name = "QUESTION_ID")
    private ContributionQuestion question;

    @Column(name = "VALIDATED")
    private Long validated;

    @Column(name = "IN_PROGRESS")
    private Long inProgress;

    @Column(name = "CONFLICTS")
    private Long conflicts;

    public Long getMissionId() {
        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public ContributionQuestion getQuestion() {
        return question;
    }

    public void setQuestion(ContributionQuestion question) {
        this.question = question;
    }

    public Long getValidated() {
        return validated;
    }

    public void setValidated(Long validated) {
        this.validated = validated;
    }

    public Long getInProgress() {
        return inProgress;
    }

    public void setInProgress(Long inProgress) {
        this.inProgress = inProgress;
    }

    public Long getConflicts() {
        return conflicts;
    }

    public void setConflicts(Long conflicts) {
        this.conflicts = conflicts;
    }

}
