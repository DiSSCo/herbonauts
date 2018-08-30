
package jobs;

import models.Mission;
import models.User;
import models.alerts.Alert;
import models.stats.StatDailyMission;
import notifiers.Mails;
import org.apache.commons.lang.time.DateUtils;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Mise à jour des stats
 */
//@Every("30s")
@On("0 0 0 * * ?")
@Transactional
public class StatUpdateJob extends Job {

	@Override
	public void doJob() throws Exception {

        Date today = DateUtils.truncate(new Date(), Calendar.MINUTE);

		Logger.info("Mise à jour des stats %s", today);
        Logger.info("| ");

        List<Mission> currentMissions = Mission.getPresentMissions(true);

        for (Mission mission : currentMissions) {

            Logger.info("| Mission " + mission.getTitle());

            try {

                updateConflicts(today, mission);
                updateMembers(today, mission);
                updateContributions(today, mission);
                updateComplete(today, mission);

            } catch (Exception e) {
                Logger.error(e, "");
            }

        }

        // Stats globales
        try {
            //updateTotalConflicts(today);
            updateTotalMembers(today);
            //updateTotalContributions(today);
            //updateTotalComplete(today);
        } catch (Exception e) {
            Logger.error(e, "");
        }

	}

    private void updateTotalMembers(Date date) {
        Logger.info("| -- update total members");
        StatDailyMission stat = new StatDailyMission();
        stat.setDate(date);
        stat.setType(StatDailyMission.Type.TOTAL_MEMBERS);
        stat.setMissionId(0l);
        stat.setValue(User.count());
        saveOrUpdateStat(stat);
        Logger.info("| ");
    }

    private void updateComplete(Date date, Mission mission) {
        Logger.info("| -- update complete");
        StatDailyMission stat = new StatDailyMission();
        stat.setMissionId(mission.id);
        stat.setDate(date);
        stat.setType(StatDailyMission.Type.COMPLETE_SPECIMENS);

        stat.setValue(mission.getCompletedSpecimensCount());

        saveOrUpdateStat(stat);
        Logger.info("| ");
    }

    private void updateConflicts(Date date, Mission mission) {
        Logger.info("| -- update conflicts");
        StatDailyMission stat = new StatDailyMission();
        stat.setMissionId(mission.id);
        stat.setDate(date);
        stat.setType(StatDailyMission.Type.CONFLICTS);

        stat.setValue(mission.getConflictsCount());

        saveOrUpdateStat(stat);
        Logger.info("| ");
    }

    private void updateContributions(Date date, Mission mission) {
        Logger.info("| -- update contributions");
        StatDailyMission stat = new StatDailyMission();
        stat.setMissionId(mission.id);
        stat.setDate(date);
        stat.setType(StatDailyMission.Type.CONTRIBUTIONS);

        stat.setValue(mission.getContributionsCount());

        saveOrUpdateStat(stat);
        Logger.info("| ");
    }

    private void updateMembers(Date date, Mission mission) {
        Logger.info("| -- update members");
        StatDailyMission stat = new StatDailyMission();
        stat.setMissionId(mission.id);
        stat.setDate(date);
        stat.setType(StatDailyMission.Type.MEMBERS);

        stat.setValue(mission.getMembersCount());


        saveOrUpdateStat(stat);
        Logger.info("| ");
    }


    private void saveOrUpdateStat(StatDailyMission stat) {
        StatDailyMission existing = StatDailyMission.findById(stat);
        if (existing == null) {
            stat.save();
        } else {
            existing.setValue(stat.getValue());
            existing.save();
        }
    }
}
