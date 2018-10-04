package fr.mnhn.herbonautes.tiles;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SpecimenRepository {

	private DB db = new DB();
	
	// ~~ SINGLETON ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private static final SpecimenRepository INSTANCE = new SpecimenRepository();
	private SpecimenRepository() {}
	public static final SpecimenRepository get() { return INSTANCE; }
	
	public Specimen getNextUntiled() {
		try {
			ResultSet rs = db.select("SELECT * FROM H_SPECIMEN WHERE TILED = 0 AND TILINGERROR = 0");
			Specimen specimen = Specimen.create(rs);
			return specimen;
		} finally {
			db.closeConnection();
		}
	}

	public List<SpecimenMedia> getMediaList(Long specimenId) throws SQLException {
		ResultSet rs = db.selectMultiple("SELECT * FROM H_SPECIMEN_MEDIA WHERE SPECIMEN_ID = ? ORDER BY MEDIA_NUMBER", specimenId);
		List<SpecimenMedia> mediaList = new ArrayList<SpecimenMedia>();
		while (rs.next()) {
			SpecimenMedia media = SpecimenMedia.create(rs);
			mediaList.add(media);
		}
		return mediaList;
	}

	public void markAsTiled(Specimen specimen) {
		db.update("UPDATE H_SPECIMEN SET TILED=1 WHERE ID=?", specimen.getId());
	}
	
	public void markAsError(Specimen specimen) {
		db.update("UPDATE H_SPECIMEN SET TILINGERROR=1 WHERE ID=?", specimen.getId());
	}
	
	public void saveDims(Specimen specimen, Long width, Long height) {
		db.update("UPDATE H_SPECIMEN SET TILEWIDTH = ? ,TILEHEIGHT = ? WHERE ID=?", width, height, specimen.getId());
	}

	public void saveMediaDims(SpecimenMedia media, long width, long height) {
		db.update("UPDATE H_SPECIMEN_MEDIA SET TILEWIDTH = ? ,TILEHEIGHT = ? WHERE ID=?", width, height, media.getId());
	}

	public void markMediaAsTiled(SpecimenMedia media) {
		db.update("UPDATE H_SPECIMEN_MEDIA SET TILED=1 WHERE ID=?", media.getId());
	}

	public void markMediaAsError(SpecimenMedia media) {
		db.update("UPDATE H_SPECIMEN_MEDIA SET TILINGERROR=1 WHERE ID=?", media.getId());
	}
}
