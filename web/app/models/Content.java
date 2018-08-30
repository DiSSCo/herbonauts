package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import play.Play;
import play.db.jpa.JPA;
import play.db.jpa.Model;

@Entity
@Table(name="H_CONTENT")
public class Content extends Model {

	private String name;
	
	private String url;
	
	private String title;
	
	private String lang;
	
	@Lob
	@Column(columnDefinition="CLOB")
	private String text;
	
	public static Content findByURLAndLang(String url, String lang) {
		Content content = Content.find("url = ? and lang = ?", url, lang).first();
		
		if (content == null) {
			String defaultLang = Play.langs.get(0);
			Content.find("url = ? and lang = ?", url, defaultLang).first();
		}
		return content;
	}
	
	public static Content findSpecial(String name, String lang) {
		Content content = Content.find("name = ? and lang = ?", name, lang).first();
		
		if (content == null) {
			String defaultLang = Play.langs.get(0);
			Content.find("name = ? and lang = ?", name, defaultLang).first();
		}
		return content;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getLang() {
		return lang;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
}
