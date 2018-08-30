package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.i18n.Lang;

/**
 * Liens (dans le header)
 */
@Entity
@Table(name="H_LINK")
public class Link extends Model {

	private String url;
	
	private String title;
	
	private String lang;

	@Column(name = "HEADER_LINK")
	private Boolean headerlink = true;

	public static Map<String, List<Link>> getAllHeaderLinksByLang() {

		HashMap<String, List<Link>> linksMap = (HashMap<String, List<Link>>) Cache.get("all_header_links");

		if (linksMap == null) {

			linksMap = new HashMap<String, List<Link>>();

			List<Link> linkList = JPA.em().createQuery("select l from Link l where l.headerlink = true", Link.class).getResultList();

			for (Link link : linkList) {
				if (linksMap.get(link.getLang()) == null) {
					linksMap.put(link.getLang(), new ArrayList<Link>());
				}
				linksMap.get(link.getLang()).add(link);
			}

			Cache.add("all_header_links", linksMap, "1min");

		}
		return linksMap;
	}


	public static Map<String, List<Link>> getAllTopLinksByLang() {

		HashMap<String, List<Link>> linksMap = (HashMap<String, List<Link>>) Cache.get("all_top_links");

		if (linksMap == null) {

			linksMap = new HashMap<String, List<Link>>();

			List<Link> linkList = JPA.em().createQuery("select l from Link l where l.headerlink != true", Link.class).getResultList();

			Logger.info("Top links : " + linkList.size());

			for (Link link : linkList) {
				if (linksMap.get(link.getLang()) == null) {
					linksMap.put(link.getLang(), new ArrayList<Link>());
				}
				linksMap.get(link.getLang()).add(link);
			}

			Cache.add("all_top_links", linksMap, "1min");

		}
		return linksMap;
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

	public Boolean getHeaderlink() {
		return headerlink;
	}

	public void setHeaderlink(Boolean headerlink) {
		this.headerlink = headerlink;
	}
}
