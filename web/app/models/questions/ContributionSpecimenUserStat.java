package models.questions;

import models.Specimen;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.io.Serializable;
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
@Table(name="H_CONTRIBUTION_SP_USER_STAT")
public class ContributionSpecimenUserStat extends GenericModel implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(name = "SPECIMEN_ID")
    private Specimen specimen;

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "MISSION_ID")
    private Long missionId;

    @Column(name = "MARKED_UNUSABLE")
    private Boolean markedUnusable = false;

    @Column(name = "MIN_UNANSWERED_LEVEL")
    private Long minUnansweredLevel;

    @Column(name = "MARKED_SEEN")
    private Boolean markedSeen;

    @Column(name = "LAST_MODIFIED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedAt = new Date();

    public Date getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(Date lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public Specimen getSpecimen() {
        return specimen;
    }

    public void setSpecimen(Specimen specimen) {
        this.specimen = specimen;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getMissionId() {
        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public Boolean getMarkedUnusable() {
        return markedUnusable;
    }

    public void setMarkedUnusable(Boolean markedUnusable) {
        this.markedUnusable = markedUnusable;
    }

    public Long getMinUnansweredLevel() {
        return minUnansweredLevel;
    }

    public void setMinUnansweredLevel(Long minUnansweredLevel) {
        this.minUnansweredLevel = minUnansweredLevel;
    }

    public Boolean getMarkedSeen() {
        return markedSeen;
    }

    public void setMarkedSeen(Boolean markedSeen) {
        this.markedSeen = markedSeen;
    }

    public static ContributionSpecimenUserStat findByUserAndSpecimen(Long userId, Long specimenId) {
        List<ContributionSpecimenUserStat> candidates =
                ContributionSpecimenUserStat.find("userId = ? and specimen.id = ?", userId, specimenId).fetch();
        if (candidates.size() > 0) {
            return candidates.get(0);
        }
        return null;
    }
}
