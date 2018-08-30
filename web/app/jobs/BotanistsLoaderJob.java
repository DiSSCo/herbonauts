package jobs;

import helpers.BotanistsCVSLoader;
import helpers.SpecimensCVSLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import models.Mission;
import play.Logger;
import play.db.jpa.Transactional;
import play.jobs.Job;
import play.libs.IO;

public class BotanistsLoaderJob extends Job  {

	private File file;

	public BotanistsLoaderJob(File file) {
		super();
		this.file = file;
	}

	@Override
	public void doJob() throws Exception {
		
		Reader r = null;
		try {
			r = new InputStreamReader(new FileInputStream(file), "UTF-8"); //"ISO-8859-1");
			BotanistsCVSLoader.get().load(r);
		} catch (Exception e) {
			Logger.error("Erreur lors du chargement des botanistes", e);
		} finally {
			IOUtils.closeQuietly(r);
		}
	}
	
}
