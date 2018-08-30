package helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import conf.Herbonautes;

import models.Link;
import models.Mission;
import models.Specimen;
import play.Logger;
import play.cache.Cache;

public class SpecimensCVSLoader {

	private static final SpecimensCVSLoader INSTANCE = new SpecimensCVSLoader();
	
	private SpecimensCVSLoader() {
	}
	
	public static SpecimensCVSLoader get() {
		return INSTANCE;
	}
	
	public void load(Reader reader, Mission mission) {
		Long initialSpecimentCount = mission.getSpecimensCount();
		Cache.set("mission_specimens_count" + mission.id, initialSpecimentCount);
		
		BufferedReader br = null;
		
		   try
           {
			  br = new BufferedReader(reader);
                   String line = null;
                   int lineCount = 0;
                  
                   if (mission.getSpecimens() == null) {
                	   mission.setSpecimens(new ArrayList<Specimen>());
                   }
                   
                   //read comma separated file line by line
                   while((line = br.readLine()) != null) {
                           lineCount++;
                          
                           Specimen specimen = null;
                           try {
                        	   specimen = buildSpecimen(line);
                           } catch (Exception e) {
                        	   Logger.error("Line %d ignorée : %s", lineCount, line);
                        	   continue;
                           }
                           Specimen existing = Specimen.find("code = ?", specimen.getCode()).first();
                           if (existing != null) {
                        	   Logger.info("Specimen %s already exists", specimen.getCode(), mission.getTitle());
                        	   specimen = existing;
               				} else {
	                           specimen.save();
	                           Logger.info("Specimen %s created", specimen.getCode(), mission.getTitle());
               				}
                           mission.getSpecimens().add(specimen);
                           Logger.info(" Added specimen %s to mission %s", specimen.getCode(), mission.getTitle());
                           Cache.incr("mission_specimens_count" + mission.id);
                   }
                   
           } catch(Exception e) {
        	   Logger.error(e, "Erreur lors du chargement des specimens");
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
	
	private Specimen buildSpecimen(String csvLine) {
		StringTokenizer st = new StringTokenizer(csvLine, ",");
		
		Specimen specimen = new Specimen();
		specimen.setInstitute(sanitizeQuotes(st.nextToken()));
		specimen.setCollection(sanitizeQuotes(st.nextToken()));
		specimen.setCode(sanitizeQuotes(st.nextToken()));
		specimen.setFamily(sanitizeQuotes(st.nextToken()));
		specimen.setGenus(sanitizeQuotes(st.nextToken()));
		specimen.setSonneratURL(sanitizeQuotes(st.nextToken()));
		specimen.setTropicosURL(sanitizeQuotes(st.nextToken()));
		
		specimen.setTiled(Herbonautes.get().forceTiled);
       
        return specimen;
	}
	
	private String sanitizeQuotes(String str) {
		if (str == null) return null;
		if (str.trim().startsWith("\"")) {
			return str.trim().substring(1, str.length() - 1);
		}
		return str;
	}
	
}
