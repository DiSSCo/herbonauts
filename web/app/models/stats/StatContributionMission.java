package models.stats;

import models.User;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "VMH_MISSION_CONTRIBUTION_STAT")
public class StatContributionMission extends GenericModel {

    @Id
    @Column(name = "MISSION_ID")
    private Long missionId;

    @Id
    @ManyToOne(optional = true)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "ANSWER_COUNT")
    private Long answerCount;

    @OneToMany
    @JoinColumns({
            @JoinColumn(name = "USER_ID"),
            @JoinColumn(name = "MISSION_ID")
    })
    private List<StatContributionByUserMission> statsByQuestions;

    public List<StatContributionByUserMission> getStatsByQuestions() {
        return statsByQuestions;
    }

    public void setStatsByQuestions(List<StatContributionByUserMission> statsByQuestions) {
        this.statsByQuestions = statsByQuestions;
    }

    public Long getMissionId() {
        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getAnswerCount() {
        return answerCount;
    }

    public void setAnswerCount(Long answerCount) {
        this.answerCount = answerCount;
    }
}
