package models.wedigbio;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import conf.Herbonautes;
import models.Mission;
import models.MissionSimple;
import models.Specimen;
import models.contributions.Contribution;
import models.contributions.GeolocalisationContribution;
import models.questions.ContributionAnswer;
import models.questions.ContributionQuestion;
import play.Logger;


public class ItemRss {
	private static ObjectMapper mapper = new ObjectMapper();
	String title,				// ex. P0001234 country by Simon
		   project, 			// le nom de la mission
		   link,        		// ex. lesherbonautes.mnhn.fr/specimens/MNHN/P/P04518317
		   pubDate,				// date de la contribution
		   thumbnailUri,		// ex. http://lesherbonautes.mnhn.fr/tiles/MNHN/P/P04518317/tile_0_0_0.jpg
		   eventDate,			// date ou periode de collecte
		   decimalLatitude,		// cf. http://rs.tdwg.org/dwc/terms/#decimalLatitude
		   decimalLongitude,	// cf. http://rs.tdwg.org/dwc/terms/#decimalLongitude
		   decimalLatitudeTranscribing,
		   decimalLongitudeTranscribing,

		   description; // ???
	   								//  de labelCompeted mais n'est pas rendue dans le flux RSS
	Boolean labelCompleted, 		//  indique que cette contribution termine la transcription
	   		specimenIsCompleted;    //  indique que le specimen est complet, cette valeur est utile pour le calcul

	/**
	 * Generation d'un item RSS à partir d'une contribution 
	 * @param c
	 */
	/**
	 * @param c
	 */
	public ItemRss(Contribution c) {
		super();
		SimpleDateFormat sdf= new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",Locale.ENGLISH);
    	Specimen s=c.getSpecimen();
    	
    	//title
		this.title= s.getFamily() + " " + s.getGenus() + " " + s.getCode() 	+ " " ;
		if (c.getType().equalsIgnoreCase("geolocalisation")) {
			this.title += " geolocated by " + c.getUser().getLogin() ;
		} else {
			this.title += c.getType().toLowerCase() + " transcribed by " + c.getUser().getLogin() ;
		}
		
		//project
		if (c.getMission() != null) {
			this.project=c.getMission().getTitle();
		} else {
			this.project="no link to mission";
		}
		
		//link,pubDate,thumbnailUri
		this.link=Herbonautes.get().baseUrl + "specimens/"+s.getInstitute()
				+ "/" + s.getCollection() + "/" + s.getCode();
		this.pubDate = sdf.format(c.getDate());
		this.thumbnailUri=Herbonautes.get().tilesRootURL + s.getInstitute()
				+ "/" + s.getCollection() + "/" + s.getCode()
				+ "/tile_0_0_0.jpg";
		
		//coordinates
		this.decimalLatitudeTranscribing="48.841461";
		this.decimalLongitudeTranscribing="2.356335";
		if (c.getType().equalsIgnoreCase("geolocalisation")) {
			GeolocalisationContribution g = (GeolocalisationContribution) (Contribution <GeolocalisationContribution>) c;
			if (g.getLatitude()!=null) {
				this.decimalLatitude=g.getLatitude().toString();
			}
			if (g.getLongitude()!=null) {
				this.decimalLongitude=g.getLongitude().toString();
			}	
		}
		
		//labelCompleted
		this.specimenIsCompleted =	s.isComplete();
		this.labelCompleted = this.specimenIsCompleted; // il y aura des multiples "true"
														// que removeMultipleLabelCompleted nettoiera
	}

	public ItemRss(ContributionAnswer c) {
		super();
		SimpleDateFormat sdf= new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",Locale.ENGLISH);
		Specimen s = Specimen.findById(c.getSpecimenId());



		//title
		ContributionQuestion q =  ContributionQuestion.findById(c.getQuestionId());
		Logger.info("Item answer " + q.getName());

		this.title= s.getFamily() + " " + s.getGenus() + " " + s.getCode() 	+ " " ;
		if (q.getName().equalsIgnoreCase("geo")) {
			this.title += " geolocated by " + c.getUserLogin() ;
		} else {
			this.title += q.getName() + " transcribed by " + c.getUserLogin() ;
		}

		//project
		MissionSimple mission = MissionSimple.findById(c.getMissionId());
		if (mission != null) {
			this.project=mission.getTitle();
		} else {
			this.project="no link to mission";
		}

		//link,pubDate,thumbnailUri
		this.link=Herbonautes.get().baseUrl + "specimens/"+s.getInstitute()
				+ "/" + s.getCollection() + "/" + s.getCode();
		this.pubDate = sdf.format(c.getCreatedAt());
		this.thumbnailUri=Herbonautes.get().tilesRootURL + s.getInstitute()
				+ "/" + s.getCollection() + "/" + s.getCode()
				+ "/tile_0_0_0.jpg";

		//coordinates
		this.decimalLatitudeTranscribing="48.841461";
		this.decimalLongitudeTranscribing="2.356335";
		if (q.getName().equalsIgnoreCase("geo")) {
			//GeolocalisationContribution g = (GeolocalisationContribution) (Contribution <GeolocalisationContribution>) c;

			try {
				JsonNode answer = mapper.readTree(c.getJsonValue());

				if (answer.hasNonNull("position")) {

					if (answer.get("position").hasNonNull("lat")) {
						this.decimalLatitude= String.valueOf(answer.get("position").get("lat").asDouble());
					}
					if (answer.get("position").hasNonNull("lng")) {
						this.decimalLongitude= String.valueOf(answer.get("position").get("lng").asDouble());
					}
				}

			} catch (IOException e) {
				Logger.error(e, "");
			}
		}

		//labelCompleted
		this.specimenIsCompleted =	s.isComplete();
		this.labelCompleted = this.specimenIsCompleted; // il y aura des multiples "true"
		// que removeMultipleLabelCompleted nettoiera
	}
	
	
	/**
	 * Efface les autres labelCompleted à true portant sur le même spécimen dans le Channel RSS
	 * @param ch Channel
	 */
	public void removeMultipleLabelCompleted(Channel ch)	{
		
		for (ItemRss it:ch.getItems()) {
			if (it.equals(this)) {
				return; // comme ch est classé chronologiquement, 
						// quand l'item se rencontre dans ch, il est forcément le dernier
			}
			if (it.link.equals(this.link)) {
				it.labelCompleted=false;
			}
		}
	}
	
	

}
