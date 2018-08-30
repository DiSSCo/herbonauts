package models;

import play.data.validation.MaxSize;
import play.data.validation.Required;

import javax.persistence.*;
import java.util.Date;

/**
 * Annonce d'une mission
 */
@Entity
@Table(name="H_ANNOUNCEMENT")
public class Announcement extends DatedModificationsModel<Announcement> {

	@Required
	private String title;
	
	@MaxSize(255)
	@Required
	private String text;
	
	@Column(name="PUBLICATION_DATE")
	private Date date;
	
	@ManyToOne
	private Mission mission;

	private boolean published;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_UPDATE_DATE")
	public Date lastUpdateDate;

	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	@Override
	@PrePersist
	@PreUpdate
	public void setLastUpdateDate() {
		this.lastUpdateDate = new Date();
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	public void setMission(Mission mission) {
		this.mission = mission;
	}

	public Mission getMission() {
		return mission;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public boolean isPublished() {
		return published;
	}

}
