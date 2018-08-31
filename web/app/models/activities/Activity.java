package models.activities;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import play.db.jpa.JPA;
import play.db.jpa.Model;
import conf.Herbonautes;

@Entity
@Table(name="H_ACTIVITY")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="type")
public abstract class Activity extends Model {

	@Column(insertable = false, updatable = false)
	private String type;

	@Column(name="CREATION_DATE", updatable = false)
	private Date date = new Date();


	public static List<Activity> lastGlobalActivities() {
		return lastGlobalActivities(Herbonautes.get().pageLength, null);
	}

	public static List<Activity> lastGlobalActivities(Long before) {
		return lastGlobalActivities(Herbonautes.get().pageLength, before);
	}

	public static List<Activity> lastGlobalActivities(Integer limit, Long before) {
		if (before != null) {
			return Activity.find("id < ? order by id desc", before).fetch(limit);
		} else {
			return Activity.find("order by id desc").fetch(limit);
		}
	}

	public static List<Activity> lastMissionActivitiesForUser(Long userId, Long before) {

		if (before == null) {
			return JPA.em().createNativeQuery(
					"SELECT A.* FROM H_ACTIVITY A " +
							"WHERE A.MISSION_ID in " +
							"(SELECT MISSION_ID from mission_user WHERE USER_ID = ?) " +
							"ORDER BY A.ID DESC", Activity.class)
					.setParameter(1, userId)
					.setMaxResults(Herbonautes.get().pageLength)
					.getResultList();
		} else {
			return JPA.em().createNativeQuery("SELECT A.* from H_ACTIVITY A " +
					"where A.MISSION_ID in " +
					"(SELECT MISSION_ID from mission_user WHERE USER_ID = ?) " +
					"AND A.ID < ? ORDER BY A.ID DESC", Activity.class)
					.setParameter(1, userId)
					.setParameter(2, before)
					.setMaxResults(Herbonautes.get().pageLength)
					.getResultList();
		}
	}

	public static List<Activity> lastMissionActivities(Long missionId, Long before) {
		//User user = User.find("login", login).first();

		if (before != null) {
			return Activity
					.find("mission.id  = ? and id < ? order by id desc", missionId, before)
					.fetch(Herbonautes.get().pageLength);
		} else {
			return Activity
					.find("mission.id  = ? order by id desc", missionId)
					.fetch(Herbonautes.get().pageLength);
		}

		//return JPA.em().createQuery("select Activity a where a.contribution.mission = ?", Activity.class)
		//	.setParameter(1, user.missions.get(0))
		//	.getResultList();
	}

	public static List<Activity> lastActivitiesForUser(String login, Long before) {
		return lastActivitiesForUser(login, Herbonautes.get().pageLength, before);
	}

	public static List<Activity> lastActivitiesForUser(String login, Integer limit, Long before) {
		if (before != null) {
			return Activity.find("user.login = ? and id < ? order by id desc", login, before).fetch(limit);
		} else {
			return Activity.find("user.login = ? order by id desc", login).fetch(limit);
		}
	}
	
	/*
	public static List<Activity> lastActivitiesForMission(Long id) {
		return lastActivitiesForMission(id, null, null);
	}
	
	public static List<Activity> lastActivitiesForMission(Long id, Integer count, Long before) {
		Integer limit = count != null ? count : Herbonautes.get().pageLength;
		if (before != null) {
			return Activity.find("contribution.mission.id = ? and id < ? order by id desc", id, before).fetch(limit);
		} else {
			return Activity.find("contribution.mission.id = ? order by id desc", id).fetch(limit); 
		}
	}
	 */

	public static List<Activity> lastActivitiesForSpecimen(Long id, Long before) {
		return lastActivitiesForSpecimen(id, Herbonautes.get().pageLength, before);
	}

	public static List<Activity> lastActivitiesForSpecimen(Long id, Integer limit, Long before) {
		if (before != null) {
			return Activity.find("specimen.master.id = ? and id < ? order by id desc", id, before).fetch(limit);
		} else {
			return Activity.find("specimen.master.id = ? order by id desc", id).fetch(limit);
		}
	}

	public String getI18nKey() {
		return "activity." + this.getType().toLowerCase();
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

}
