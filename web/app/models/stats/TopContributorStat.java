package models.stats;

import models.User;
import play.db.jpa.GenericModel;

import javax.persistence.*;

@Entity
@Table(name = "VH_TOP_CONTRIBUTIOR_STAT")
public class TopContributorStat extends GenericModel {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "ANSWER_COUNT")
    private Long answerCount;

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
