/**
 * 
 */
package models.wedigbio;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.JsonNode;

import conf.Herbonautes;
import libs.Json;
import models.Mission;
import models.MissionSimple;
import models.Specimen;
import models.contributions.Contribution;
import models.contributions.GeolocalisationContribution;
import models.questions.ContributionAnswer;
import models.questions.ContributionQuestion;
import play.Logger;

/**
 * 
 * Une transcription transmise à WeDigBio dans un ContribSet
 * Dans le vocabulaire "herbonautes" ca serait une contribution mais les attributs sont différents
 * @author schagnoux
 */
public class Item {
	private String project; 		 // le titre de la mission
	private String description; 	 // de la nature de la transcription
	private String guid;			 // identifiant de l'évènement (= mission ?)
	private String timestamp;		 // horodatage
	private Subject subject;		 // le specimen
	private Contributor contributor; // l'herbonaute
    private TranscriptionContent 
    			transcriptionContent;// transcription
    private DiscretionaryState
    			discretionaryState;	 // champs supplémentaires;
	
	/**
	 * constructeur d'un item RSS Ã  partir d'une contribution 
	 * @param c
	 */
	public Item(ContributionAnswer c) {
		super();
		guid = c.getId().toString();
		SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.ENGLISH);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		Specimen s = Specimen.findById(c.getSpecimenId());
		ContributionQuestion q =  ContributionQuestion.findById(c.getQuestionId());
		this.timestamp = sdf.format(c.getCreatedAt());
    	
    	//description et transcriptionContent
		this.description= s.getFamily() + " " + s.getGenus() + " " + s.getCode() 	+ " " ;
		if (q.getName().equalsIgnoreCase("geolocalization")) {
			this.description += " geolocated by " + c.getUserLogin() ;
			//transcriptionContent
			Logger.debug("Geo :-)  Json="+c.getJsonValue());
			JsonNode node = Json.parse(c.getJsonValue());
			DecimalFormat df = new DecimalFormat("#.######");
			String lat = df.format(node.get("position").get("lat").asDouble());
            String lng = df.format(node.get("position").get("lng").asDouble());
            this.transcriptionContent =
            		new TranscriptionContent(lat,lng);

		
		} else {
			this.description += q.getName() + " transcribed by " + c.getUserLogin() ;
		}
		
		//project
		MissionSimple mission = MissionSimple.findById(c.getMissionId());
		if (mission != null) {
			this.project=mission.getTitle();
		} else {
			this.project="no link to mission";
		}

		//subject
		this.subject = new Subject(
				Herbonautes.get().baseUrl + "specimens/"+s.getInstitute()
				+ "/" + s.getCollection() + "/" + s.getCode(),
				Herbonautes.get().tilesRootURL + s.getInstitute()
				+ "/" + s.getCollection() + "/" + s.getCode()
				+ "/tile_0_0_0.jpg");
		//contributor
		this.contributor = new Contributor (
				"48.841461","2.356335",c.getUserLogin());
		//discretionaryState
		this.discretionaryState= new DiscretionaryState(c.getMissionId());
	}




	
}
