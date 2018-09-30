package models;

import models.questions.ContributionAnswer;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name="H_MISSION_IMPORT_EXCEPTION")
public class MissionImportException extends Model {

	@Required
	@Column(name = "MISSION_ID")
	private Long missionId;

	@Required
	@Column(name = "IGNORE_MISSION_ID")
	private Long ignoreMissionId;

	public Long getMissionId() {
		return missionId;
	}

	public void setMissionId(Long missionId) {
		this.missionId = missionId;
	}

	public Long getIgnoreMissionId() {
		return ignoreMissionId;
	}

	public void setIgnoreMissionId(Long ignoreMissionId) {
		this.ignoreMissionId = ignoreMissionId;
	}

	public static List<MissionImportException> findImportExceptionByMissionId(Long missionId) {
		List<MissionImportException> exceptions = MissionImportException
				.find("missionId = :missionId")
				.setParameter("missionId", missionId)
				.fetch();

		return exceptions;
	}

	public static void importMission(Long missionId, Long ignoreMissionId) {

		List<MissionImportException> exceptions = MissionImportException
				.find("missionId = :missionId and ignoreMissionId = :ignoreMissionId")
				.setParameter("missionId", missionId)
				.setParameter("ignoreMissionId", ignoreMissionId)
				.fetch();

		// Remove existing exceptions
		for (MissionImportException exception : exceptions) {
			exception.delete();
		}

	}

	public static void ignoreMission(Long missionId, Long ignoreMissionId) {

		List<MissionImportException> exceptions = MissionImportException
				.find("missionId = :missionId and ignoreMissionId = :ignoreMissionId")
				.setParameter("missionId", missionId)
				.setParameter("ignoreMissionId", ignoreMissionId)
				.fetch();


		// Create if not exists
		if (exceptions.size() == 0) {
			MissionImportException exception = new MissionImportException();
			exception.setMissionId(missionId);
			exception.setIgnoreMissionId(ignoreMissionId);
			exception.save();
		}

	}

}
