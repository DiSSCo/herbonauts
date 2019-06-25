package models;


import play.db.jpa.GenericModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Mission
 */
@Entity
@Table(name = "H_MISSION_LEADER")
public class MissionLeader extends GenericModel {

    @Id
    @Column(name = "MISSION_ID")
    private Long missionId;

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "USER_LOGIN")
    private String userLogin;

    @Column(name = "VISIBLE")
    private Boolean visible;

    public Long getMissionId() {
        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }
}