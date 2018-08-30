package jobs;

import models.Mission;
import play.db.jpa.Transactional;
import play.jobs.Job;
import play.jobs.On;

import java.util.List;

@On("0 0 0 * * ?")
public class DeleteLostPropositionsJob extends Job  {

	public DeleteLostPropositionsJob() {
		super();
	}

	@Override
	@Transactional
	public void doJob() throws Exception {
		try {
			List<Mission> missionsToDelete = Mission.getLostMissionsPropositions();
			for(Mission mission : missionsToDelete) {
				Mission.rejectMissionProposition(mission);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
