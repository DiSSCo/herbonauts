package models;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import libs.Images;

import org.apache.commons.io.output.ByteArrayOutputStream;

import play.Logger;
import play.db.jpa.Model;
import play.libs.IO;
import conf.Herbonautes;

/**
 * Entité permettant de sauvegarder les images en 
 * base (BLOB)
 */
@Entity
@Table(name="H_IMAGE")
public class Image extends Model {

	@Lob
	@Column(length=100000)
	private byte[] data;
	
	private String mimeType;
	
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public InputStream getInputStream() {
		return new ByteArrayInputStream(data);
	}
	
	public static Image createAvatar(File file) {
		libs.Images.squarify(file, file);
		libs.Images.resize(file, file, Herbonautes.get().avatarSize, Herbonautes.get().avatarSize);
		Image image = Image.createImageFromBytes(Images.compress(file));
		return image;
	}
	
	public static Image createImageFromBytes(byte[] data) {
		Image image = new Image();
		image.data = data;
		image.setMimeType(Herbonautes.get().imageMimeType);
		image.save();
		return image;
	}
	
	public static Image createImageFromFile(File file) {
		Image image = new Image();
		image.copy(Herbonautes.get().imageMimeType, file);
		image.save();
		return image;
	}
	
	public static Image createImageFromStream(InputStream is) {
		Image image = new Image();
		image.copy(Herbonautes.get().imageMimeType, is);
		image.save();
		return image;
	}
	
	public void copy(String mimeType, File file) {
		
		try {
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			IO.copy(fis, baos);
			
			setMimeType(mimeType);
			data = baos.toByteArray();
		} catch (FileNotFoundException e) {
			Logger.error(e, "Impossible de copier le fichier");
		}
		
	}
	
	public void copy(String mimeType, InputStream is) {
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			IO.copy(is, baos);
			
			setMimeType(mimeType);
			data = baos.toByteArray();
		} catch (Exception e) {
			Logger.error(e, "Impossible de copier le flux");
		}
		
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getMimeType() {
		return mimeType;
	}
	
}
