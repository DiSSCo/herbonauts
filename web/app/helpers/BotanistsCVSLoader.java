package helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import java.util.StringTokenizer;

import models.Botanist;
import models.Specimen;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.db.jpa.NoTransaction;
import conf.Herbonautes;

/**
 * Job de chargement des botanistes
 */
public class BotanistsCVSLoader {

	private static final BotanistsCVSLoader INSTANCE = new BotanistsCVSLoader();

	private BotanistsCVSLoader() {
	}

	public static BotanistsCVSLoader get() {
		return INSTANCE;
	}

	@NoTransaction
	public void load(Reader reader) {

		BufferedReader br = null;

		try {
			br = new BufferedReader(reader);
			String line = null;
			int lineCount = 0;

			// read comma separated file line by line
			while ((line = br.readLine()) != null) {

				try {
					Botanist botanist = buildBotanist(line);
					botanist.create();
					JPA.em().flush();
					Logger.info("line %s > Botaniste %s ajouté", lineCount,
							botanist.getName());
				} catch (Exception e) {
					Logger.error(e, "Botaniste ignoré (%s)", line);
				}
				lineCount++;

			}

		} catch (Exception e) {
			Logger.error("Erreur lors du chargement des botanistes", e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					//
				}
			}
		}
	}

	private Botanist buildBotanist(String csvLine) {
		// break comma separated line using ","
		String[] tokens = csvLine.split(";", -1);

		Botanist botanist = new Botanist();

		botanist.setHarvardId(Long.valueOf(tokens[0]));
		botanist.setName(tokens[1]);
		botanist.setPeriod(tokens[2]);
		botanist.setNameInv(tokens[3]);
		botanist.setHerborariumIndex(tokens[4]);
		botanist.setCountries(tokens[5]);
		botanist.setSpeciality(tokens[6]);
		return botanist;
	}

	private String sanitizeQuotes(String str) {
		if (str == null)
			return null;
		return str.substring(1, str.length() - 1);
	}

}
