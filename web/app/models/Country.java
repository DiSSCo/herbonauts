package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name="H_COUNTRY")
public class Country extends Model {

	private String iso;
	
	private String name;

	public void setIso(String iso) {
		this.iso = iso;
	}

	public String getIso() {
		return iso;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public static List<Country> findAllOrdered() {
		return Country.find("order by name").fetch();
	}
	
}
