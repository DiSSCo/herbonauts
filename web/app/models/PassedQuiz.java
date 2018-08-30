package models;

import play.db.jpa.GenericModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "H_USER_PASSED_QUIZ")
public class PassedQuiz extends GenericModel implements Serializable {

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @Id
    @Column(name = "QUIZ_ID")
    private Long quizId;

    @Column(name = "PASSED_AT")
    private Date passedAt;

    public PassedQuiz() {
    }

    public PassedQuiz(Long userId, Long quizId, Date passedAt) {
        this.userId = userId;
        this.quizId = quizId;
        this.passedAt = passedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public Date getPassedAt() {
        return passedAt;
    }

    public void setPassedAt(Date passedAt) {
        this.passedAt = passedAt;
    }
}
