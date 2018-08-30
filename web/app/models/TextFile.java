package models;

import conf.Herbonautes;
import libs.Images;
import org.apache.commons.io.output.ByteArrayOutputStream;
import play.Logger;
import play.db.jpa.Model;
import play.libs.IO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.io.*;

/**
 * Entité permettant de sauvegarder les images en 
 * base (BLOB)
 */
@Entity
@Table(name="H_FILE")
public class TextFile extends Model {

	@Lob
	@Column(length=100000)
	private String data;

	private String name;

	private String fileType;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}


	public static TextFile createFile(String name, String data) {
		TextFile file = new TextFile();

		file.name = name;
		file.data = data;

		file.save();
		return file;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
}
