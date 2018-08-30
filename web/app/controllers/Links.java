package controllers;

import java.io.File;
import java.util.List;

import models.Content;
import models.Link;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.i18n.Lang;
import play.mvc.Before;

/**
 * Gestion des liens en en-tête
 */
public class Links extends Application {
	
	@Before
	static void checkAdmin() {
		Security.forbiddenIfNotAdmin();
	}
	
	public static void list() {
		List<Link> links = Link.findAll();
		render(links);
	}
	
	public static void blank() {
		render();
	}
	
	public static void edit(Long id) {
		Link link = Link.findById(id);
		render(link);
	}
	
	public static void create(Link link) {
		link.setLang(Lang.get());
		link.save();
		link.refresh();
		
		//edit(link.id);
		Cache.delete("all_top_links");
		Cache.delete("all_header_links");

		list();
	}
	
	public static void save(Long id, Link link) {
		Link savedLink = Link.findById(id);
		notFoundIfNull(id);
		
		savedLink.setLang(link.getLang());
		savedLink.setTitle(link.getTitle());
		savedLink.setUrl(link.getUrl());
		savedLink.setHeaderlink(link.getHeaderlink());

		savedLink.save();

		Cache.delete("all_top_links");
		Cache.delete("all_header_links");

		list();
	}
	
	public static void delete(Long id) {
		Link link = Link.findById(id);
		notFoundIfNull(id);
		
		link.delete();

		Cache.delete("all_top_links");
		Cache.delete("all_header_links");

		list();
	}

}
