package controllers;

import models.Mission;
import models.serializer.*;
import models.wedigbio.Channel;
import models.wedigbio.ContribSet;
import models.wedigbio.ItemRss;
import models.wedigbio.Rss;
import play.Logger;

import java.util.List;

import com.thoughtworks.xstream.XStream;

import helpers.XstreamRssConverters.VersionConverter;

/**
 * Ce controleur gère les flux destiné à WeDigBio
 * @see https://github.com/iDigBio/wedigbio-dashboard
 */
public class WedigbioController extends Application {


   
    public static void getAllMissions () {
        List<Mission> missions = Mission.findAll();
        renderJSON(missions, MissionWedigbioJsonSerializer.get());
    }


   
    public static void getMissionById (Long missionId) {
        Mission mission = Mission.findById(missionId);
        if(!mission.isProposition() || mission.isPropositionValidated())
            renderJSON(mission, MissionWedigbioJsonSerializer.get());
        else
            error();
    }
	/**
	 * RSS
	 */
	public static void  lastDaysRss(int nb) {

		// instanciation et parametrage de la classe generant le RSS
		XStream xs = new XStream();
		xs.alias("rss", Rss.class);
		xs.alias("item", ItemRss.class);
		xs.addImplicitCollection(Channel.class, "items");
		xs.useAttributeFor(Rss.class,"version");
		xs.registerConverter(new VersionConverter());

		// generation du flux
		Rss feed = new Rss(nb);
		renderXml(feed, xs);

	}
	
	public static void  intervalJson(String timestampStart, String timestampEnd, int rowStart ) {

		//valeur par défaut
		if (timestampStart ==null)
			timestampStart = "2000-01-01T12:00:00Z";
		if (timestampEnd ==null)
			timestampEnd = "2050-01-01T12:00:00Z";
				
		// generation du flux
		Logger.info("generation flux wedigbio " + timestampStart );
		ContribSet cs = new ContribSet(timestampStart,timestampEnd,rowStart );
		renderJSON(cs, WedigbioJsonSerializer.get());

	}

	public static void  lastWeekRss() {
		lastDaysRss(7);
	}


}
