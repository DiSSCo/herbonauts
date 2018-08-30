package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import play.data.validation.Required;
import play.db.jpa.Model;

/**
 * Class SeenSpecimen links to the SeenSpecimen table.
 * @author marie-eliselecoq
 *
 */

@Entity
@Table(name="SEEN_SPECIMEN")
public class SeenSpecimen extends Model {
	

	@Required
	private Long mission_id;
	
	
	@Required
	private Long specimen_id;
	
	
	@Required
	private Long user_id;
	
	public Long getMission_id() {
		return mission_id;
	}
	public void setMission_id(Long mission_id) {
		this.mission_id = mission_id;
	}
	public Long getSpecimen_id() {
		return specimen_id;
	}
	public void setSpecimen_id(Long specimen_id) {
		this.specimen_id = specimen_id;
	}
	public Long getUser_id() {
		return user_id;
	}
	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}

}
