package models.stats;


import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "H_MISSION_DAILY_STAT")
public class StatDailyMission extends GenericModel {

    public static enum Type {
        MEMBERS,
        CONFLICTS,
        COMPLETE_SPECIMENS,
        CONTRIBUTIONS,
        TOTAL_MEMBERS,
        TOTAL_COMPLETE_SPECIMENS,
        TOTAL_CONTRIBUTIONS
    }

    @Id
    @Column(name = "MISSION_ID")
    private Long missionId;

    @Id
    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    private Type type;

    @Id
    @Column(name = "STAT_DATE")
    private Date date;

    @Column(name = "STAT_VALUE")
    private Long value;

    public Long getMissionId() {
        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
