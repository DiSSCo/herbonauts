package jobs;

import helpers.SpecimensCVSLoader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import models.Mission;
import models.Specimen;
import play.db.jpa.Transactional;
import play.jobs.Job;

public class SpecimensLoaderJob extends Job  {

	private Long missionId;
	private File file;

	public SpecimensLoaderJob(Long missionId, File file) {
		super();
		this.missionId = missionId;
		this.file = file;
	}

	@Override
	public void doJob() throws Exception {
		Mission mission = Mission.findById(missionId);
		
		// Marque la mission en chargement pour l'affichage 
		// (et bloquer les contribution ?)
		Mission.markLoading(mission.id);
		
		Reader r = null;
		try {
			r = new FileReader(file);
			SpecimensCVSLoader.get().load(r, mission);
			mission.save();
            
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Mission.unmarkLoading(mission.id);
			if (r != null) {
				try {
					r.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
