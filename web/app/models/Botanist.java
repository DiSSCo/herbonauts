package models;

import models.contributions.DateContribution;
import models.contributions.GeolocalisationContribution;
import play.Logger;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="H_BOTANIST")
public class Botanist extends DatedModificationsModel<Botanist> {

	private Long harvardId;
	
	private String name;
	
	private String nameInv;
	
	private String period;
	
	private String herborariumIndex;

	@Lob
	@Column(columnDefinition="CLOB")
	private String countries;
	
	@Lob
	@Column(columnDefinition="CLOB")
	private String speciality;
	
	private boolean createdByUser;

	private Long imageId;
	private boolean hasImage;

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

	public static List<Botanist> search(String q, Integer max) {
		return search(q, max, null);
	}
	
	public static List<Botanist> search(String q, Integer max, Integer page) {
		if (q == null) {
			return new ArrayList<Botanist>();
		}
		Integer realPage = (page == null ? 1 : page);
		
		String likeValue = q.toLowerCase().replace(' ', '%');
		
		List<Botanist> botanits = Botanist
			.find("lower(name) like ?", "%" + likeValue + "%")
			.fetch(realPage, max);
		
		return botanits;
	}
	
	public static List<Specimen> getSpecimens(Long id, int limit, int page) {
		 return Specimen.find(
					"select distinct c.specimen " +
					"from BotanistsContribution c " +
					"where report = true and (c.collector.id = ? or c.determiner.id = ?)", id, id)
				.fetch(page, limit);
	}
	
	public static List<GeolocalisationContribution> getGeolocalisations(Long id) {
		return GeolocalisationContribution.find(
				"report = true and " +
				"notPresent = false and specimen in " +
				"(select c.specimen " +
					"from BotanistsContribution c " +
					"where report = true and (c.collector.id = ? or c.determiner.id = ?))", id, id).fetch();
	}
	
	public static List<DateContribution> getDateContributions(Long id) {
		return DateContribution.find(
				"report = true and " +
				"notPresent = false and specimen in " +
				"(select c.specimen " +
					"from BotanistsContribution c " +
					"where report = true and (c.collector.id = ? or c.determiner.id = ?))", id, id).fetch();
	}
	
	public Long getMissionsCount() {
		return Mission.count(
				"select count(distinct c.mission) " +
				"from BotanistsContribution c " +
				"where (collector.id = ? or determiner.id = ?)", this.id, this.id);
	}
	
	
	public Long getCollectedSpecimensCount() {
		return Specimen.count(
				"select count(distinct c.specimen) " +
				"from BotanistsContribution c " +
				"where report = true and collector.id = ?", this.id);
	}
	
	public Long getDeterminedSpecimensCount() {
		return Specimen.count(
				"select count(distinct c.specimen) " +
				"from BotanistsContribution c " +
				"where report = true and determiner.id = ?", this.id);
	}
	
	public static Botanist createIfNeeded(Botanist botanist) {
		if (botanist == null) {
			return null;
		}
		if (botanist.id == null && botanist.getName() != null) {
			if (botanist.getName().trim().length() == 0) {
				return null;
			}
			Botanist existingBotanist = Botanist.find("name = ?", botanist.getName().trim()).first();
			Logger.info("botanist %s exists ? %s", botanist.getName(), (existingBotanist != null));
			if (existingBotanist != null) {
				return existingBotanist;
			} else {
				Botanist newBotanist = new Botanist();
				newBotanist.setName(botanist.getName());
				newBotanist.setCreatedByUser(true);
				newBotanist.save();
				newBotanist.refresh();
				return newBotanist;
			}
		}
		return botanist;
	}

	public void setHarvardId(Long harvardId) {
		this.harvardId = harvardId;
	}

	public Long getHarvardId() {
		return harvardId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setNameInv(String nameInv) {
		this.nameInv = nameInv;
	}

	public String getNameInv() {
		return nameInv;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String getPeriod() {
		return period;
	}

	public void setHerborariumIndex(String herborariumIndex) {
		this.herborariumIndex = herborariumIndex;
	}

	public String getHerborariumIndex() {
		return herborariumIndex;
	}

	public void setCountries(String countries) {
		this.countries = countries;
	}

	public String getCountries() {
		return countries;
	}

	public void setSpeciality(String speciality) {
		this.speciality = speciality;
	}

	public String getSpeciality() {
		return speciality;
	}

	public void setCreatedByUser(boolean createdByUser) {
		this.createdByUser = createdByUser;
	}

	public boolean isCreatedByUser() {
		return createdByUser;
	}

	public Long getImageId() {
		return imageId;
	}

	public void setImageId(Long imageId) {
		this.imageId = imageId;
	}

	public void setHasImage(boolean hasImage) {
		this.hasImage = hasImage;
	}

	public boolean isHasImage() {
		return hasImage;
	}

}
