package models.questions;

import models.Specimen;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import services.JPAUtils;
import services.Page;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Permet de garder des stats au niveau specimen
 * - permet de garder le niveau minimum requis pour un specimen
 *   (eviter de presenter un specimen sans contribution utile pour l'herbonaute)
 * - Mis à jour lors de l'ajout ou suppression d'une contribution
 *
 */
@Entity
@Table(name="H_CONTRIBUTION_SPECIMEN_STAT")
public class ContributionSpecimenStat extends GenericModel {



    /*@Id
    @Column(name = "SPECIMEN_ID")
    private Long specimenId;
    public Long getSpecimenId() {
        return specimenId;
    }

    public void setSpecimenId(Long specimenId) {
        this.specimenId = specimenId;
    }
    */

    @Id
    @ManyToOne
    @JoinColumn(name = "SPECIMEN_ID")
    private Specimen specimen;

    public Specimen getSpecimen() {
        return specimen;
    }

    public void setSpecimen(Specimen specimen) {
        this.specimen = specimen;
    }

    @Column(name = "MISSION_ID")
    private Long missionId;

    @Column(name = "ANSWER_COUNT")
    private Long answerCount;

    @Column(name = "VALIDATED")
    private Boolean validated = false;

    @Column(name = "CONFLICTS")
    private Boolean conflicts = false;

    @Column(name = "MIN_USEFUL_LEVEL")
    private Long minUsefulLevel;

    @Column(name = "UNUSABLE_VALIDATED")
    private Boolean unusableValidated = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_MODIFIED_AT")
    private Date lastModifiedAt;

    @OneToMany
    @JoinColumn(name = "SPECIMEN_ID")
    private List<ContributionQuestionStat> questionStats;


    public Long getMissionId() {
        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public Long getAnswerCount() {
        return answerCount;
    }

    public void setAnswerCount(Long answerCount) {
        this.answerCount = answerCount;
    }

    public Boolean getValidated() {
        return validated;
    }

    public void setValidated(Boolean validated) {
        this.validated = validated;
    }

    public Boolean getConflicts() {
        return conflicts;
    }

    public void setConflicts(Boolean conflicts) {
        this.conflicts = conflicts;
    }

    public Date getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(Date lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public List<ContributionQuestionStat> getQuestionStats() {
        return questionStats;
    }

    public void setQuestionStats(List<ContributionQuestionStat> questionStats) {
        this.questionStats = questionStats;
    }

    public Long getMinUsefulLevel() {
        return minUsefulLevel;
    }

    public void setMinUsefulLevel(Long minUsefulLevel) {
        this.minUsefulLevel = minUsefulLevel;
    }

    public Boolean getUnusableValidated() {
        return unusableValidated;
    }

    public void setUnusableValidated(Boolean unusableValidated) {
        this.unusableValidated = unusableValidated;
    }


}
