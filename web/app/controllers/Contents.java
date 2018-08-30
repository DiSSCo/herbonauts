package controllers;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import libs.Images;
import models.Content;
import models.Image;
import play.db.jpa.Transactional;
import play.i18n.Lang;
import play.mvc.Router;

/**
 * Gestion des contenus
 * <ul>
 * <li>Pages complètes (about...)
 * <li>Images des présentations et rapports de missions 
 */
public class Contents extends Application {
	
	private static final String[] RESERVED_NAMES = { "presentation", "new" };

	/**
	 * 
	 */
	private static boolean isReserved(String url) {
		for (String reserved : RESERVED_NAMES) {
			if (url.equals(reserved)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Affiche un contenu
	 */
	public static void show(String name) {
		Content content = Content.findByURLAndLang(name, Lang.get());
		render(content);
	}

	/**
	 * Liste des contenues
	 */
	public static void list() {
		Security.forbiddenIfNotAdmin();
		List<Content> contents = Content.findAll();
		render(contents);
	}
	
	/**
	 * Nouveau contenu
	 */
	public static void blank() {
		Security.forbiddenIfNotAdmin();
		render();
	}
	
	/**
	 * Edition d'un contenu
	 */
	public static void edit(String name) {
		Security.forbiddenIfNotAdmin();
		Content content = Content.findByURLAndLang(name, Lang.get());
		render(content);
	}
	
	/**
	 * Modification d'un contenu
	 */
	public static void save(String name, Content content) {
		Security.forbiddenIfNotAdmin();
		
		Content savedContent = Content.findByURLAndLang(name, Lang.get());
		
		savedContent.setText(content.getText());
		savedContent.setTitle(content.getTitle());
		
		savedContent.save();
		
		edit(name);
	}

	/**
	 * Création
	 */
	public static void create(Content content) {
		Security.forbiddenIfNotAdmin();
		
		validation.required("content.url", content.getUrl());
		validation.required("content.title", content.getTitle());
		validation.required("content.text", content.getText());
		
		if (isReserved(content.getUrl())) {
			validation.addError("content.url", "validation.reserved.name");
		}
		
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			blank();
		}
		
		// Vérifier nom (list, new)
		content.setLang(Lang.get());
		content.save();
		content.refresh();
		
		show(content.getUrl());
	}
	
	/**
	 * Upload des images (avec CKEDITOR)
	 */
	public static void imagesUpload(File upload, String CKEditor, Long CKEditorFuncNum) {
		Security.forbiddenIfNotAdminOrLeader();

		Image image = 
			Image.createImageFromBytes(Images.compress(upload));
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("id", image.id);
		String imageURL = Router.reverse("Contents.image", map).url;  

		render(imageURL, CKEditor, CKEditorFuncNum);
	}
	
	/**
	 * Affichage d'une image
	 */
	@Transactional(readOnly=true)
	public static void image(Long id) {
		renderImage(id);
	}
	
	/*
	public static void imagesBrowse() {
		render();
	}
	*/
}
