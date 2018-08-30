package conf;

import models.contributions.Contribution;
import play.Logger;
import play.Play;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton contenant la configuration de l'application
 * 
 * Les clés sont préfixées de "herbonautes." et sont présentes dans
 * le fichier de configuration conf/application.conf
 */
public class Herbonautes {

	public static final Herbonautes INSTANCE = new Herbonautes();

	public Integer timezoneOffset;
	public Integer specimenFileLimit;

	public String tilesRootURL;
	public Integer tilesDefaultZoom;
	public boolean forceTiled; 
	public Integer specimenMarkedSeenDelay; 
	public Integer pageLength; //
	public Integer quickSearchLength;
	public Integer searchResultLength;
	public Integer contributionValidationMin;
	public String title;
	public String titleSep;
	public Integer explorerBadgeThreshold;
	public Integer podiumBadgeThreshold;
	public Integer nightBadgeHourStart;
	public Integer nightBadgeHourEnd;
	public Integer activityTimeBuffer;
	public Integer maxLevel = 6;
	public Integer avatarSize = 239;
	public String imageMimeType = "image/jpeg";

	public Integer geolocalisationConflictDistance;
	
	public Integer missionsCountOnIndex;
	
	public Map<Integer, Integer> unlockLevelAt;
	public Map<String, Integer> quickSearchLengthByCategory;
	public Map<String, Integer> contributionValidationMinByType;

	public String mailsFrom;
	
	public String fbAppID;
	public String fbSecretKey;
	public String fbChannelURL;


	public String uploadDir;

	public String recolnatApiEndpoint;
	public String recolnatApiKey;
	public boolean recolnatEnabled;

	public boolean recolnatMenuShow;
	public String recolnatMenuUrl;

	public Integer pageLengthUserContributions;
	public Integer pageLengthSpecimenList;

	public List<String> contributionTypes;

	public Integer specimenPerMissionLimit;
	public Integer classifierMinDiscussions;
	public Integer pedagogueMinMessages;
	public Integer solidaryMinMessages;
	public Integer writerMinMessageLength;
	public Integer animatorMinMessages;
	public Integer animatorMinDiscussions;
	public Integer missionProposalMinLevel;
	public Integer discussionTagsMinLevel;
	public Integer saveTagsElementMinLevel;
	public Integer lastMessagesMaxResults;
	public Integer nbDiscussionsToLoadPerCall;

	public Integer elasticReadTimeout;

	public String baseUrl;
	public String logoUrl;

	private Herbonautes() {

		baseUrl = getString("application.baseUrl","");
		logoUrl = getString("herbonautes.logoUrl","");
		specimenFileLimit = getInteger("specimen.file.limit", 1000);

		elasticReadTimeout = getInteger("elastic.proxy.timeout", 3000);

		specimenPerMissionLimit = getInteger("specimens.per.mission.limit", 100);

		recolnatApiEndpoint = getString("recolnat.api.endpoint", "");
		recolnatApiKey = getString("recolnat.api.key", "");
		recolnatMenuShow = getBoolean("recolnat.menu.show", false);
		recolnatMenuUrl = getString("recolnat.menu.url", "");

		recolnatEnabled = getBoolean("recolnat.enabled", false);

		tilesRootURL = getString("herbonautes.tiles.root.url", "");
		forceTiled = getBoolean("herbonautes.force.tiled", false);
		tilesDefaultZoom = getInteger("herbonautes.tiles.default.zoom", 0);

		specimenMarkedSeenDelay = getInteger("herbonautes.specimen.marked.seen.delay", 3000);

		pageLength = getInteger("herbonautes.page.length", 3);
		quickSearchLength = getInteger("herbonautes.quick.search.limit", 4);
		searchResultLength = getInteger("herbonautes.search.result.length", 10);
		contributionValidationMin = getInteger("herbonautes.contribution.validation.min", 3);
		title = getString("herbonautes.title", "Les herbonautes");
		titleSep = " " + getString("herbonautes.title.separator", "-") + " ";
		explorerBadgeThreshold = getInteger("herbonautes.explorer.badge.threshold", 10);
		podiumBadgeThreshold = getInteger("herbonautes.podium.badge.threshold", 20);
		activityTimeBuffer = getInteger("herbonautes.activity.time.buffer", 10);

		nightBadgeHourStart = getInteger("herbonautes.night.badge.hour.start", 0);
		nightBadgeHourEnd = getInteger("herbonautes.night.badge.hour.end", 6);

		mailsFrom = getString("herbonautes.mails.from", "Les herbonautes <herbonautes@mnhn.fr>");

		missionsCountOnIndex = getInteger("herbonautes.missions.on.index", 2);

		fbAppID = getString("herbonautes.fb.app.id", null);
		fbSecretKey = getString("herbonautes.fb.secret.key", null);
		fbChannelURL = getString("herbonautes.fb.channel.url", null);

		uploadDir = getString("herbonautes.upload.dir", Play.applicationPath + "/uploads");

		unlockLevelAt = new HashMap<Integer, Integer>();
		for (Integer level : new Integer[] { 2, 3, 4, 5, 6, 7, 8 }) {
			unlockLevelAt.put(level, getInteger("herbonautes.unlock.level." + level + ".at", 0));
		}

		specimenMarkedSeenDelay = getInteger("herbonautes.specimen.marked.seen.delay", 2000);

		geolocalisationConflictDistance = getInteger("herbonautes.geolocalisation.conflict.distance", 20000);

		Contribution.Type[] allContributionTypes = Contribution.Type.values();
		this.contributionTypes = new ArrayList<String>();
		for (Contribution.Type type : allContributionTypes) {
			this.contributionTypes.add(type.toString());
		}

		this.quickSearchLengthByCategory = new HashMap();
		for (Category category : Category.values()) {
			String cat = category.toString().toLowerCase();
			quickSearchLengthByCategory.put(cat,
					getInteger("herbonautes.quick.search.limit." + cat, 5));
		}

		this.contributionValidationMinByType = new HashMap<String, Integer>();
		for (Contribution.Type ctype : allContributionTypes) {
			String type = ctype.toString().toLowerCase();
			contributionValidationMinByType.put(type,
					getInteger("herbonautes.contribution.validation.limit." + type, contributionValidationMin));
		}

		classifierMinDiscussions = getInteger("herbonautes.badge.classifier.minimum.discussions", 20);
		pedagogueMinMessages = getInteger("herbonautes.badge.pedagogue.minimum.messages", 5);
		solidaryMinMessages = getInteger("herbonautes.badge.solidary.minimum.sos.resolutions", 5);
		writerMinMessageLength = getInteger("herbonautes.badge.writer.minimum.message.length", 1000);
		animatorMinMessages = getInteger("herbonautes.badge.animator.minimum.messages", 10);
		animatorMinDiscussions = getInteger("herbonautes.badge.animator.minimum.discussions", 30);
		missionProposalMinLevel = getInteger("herbonautes.mission.proposition.minimum.level", 6);
		discussionTagsMinLevel = getInteger("herbonautes.discussion.tags.minimum.level", 6);
		saveTagsElementMinLevel = getInteger("herbonautes.elements.tags.save.minimum.level", 6);
		lastMessagesMaxResults = getInteger("herbonautes.last.messages.max.results", 3);
		nbDiscussionsToLoadPerCall = getInteger("herbonautes.discussion.call.nb.loads", 3);


		pageLengthUserContributions = getInteger("herbonautes.page.length.user.contributions", 30);
		pageLengthSpecimenList = getInteger("herbonautes.page.length.specimens.list", 30);


		timezoneOffset = getInteger("herbonautes.timezone.offset", -60);


	}

	public static final Herbonautes get() {
		return INSTANCE;
	}

	/**
	 * Récupère la chaine ou retourne la valeur par défaut
	 */
	private static String getString(String key, String defaultValue) {
		String val = Play.configuration.getProperty(key);
		if (val != null) {
			Logger.info("%s : %s", key, val);
			return val;
		} else {
			Logger.warn("%s (defaut) : %s", key, defaultValue);
			return defaultValue;
		}
	}

	/**
	 * Récupère le booleen ou retourne la valeur par défaut
	 */
	private static boolean getBoolean(String key, boolean defaultValue) {
		String val = Play.configuration.getProperty(key);
		if (val != null) {
			Logger.info("%s : %s", key, val);
			return Boolean.valueOf(val);
		} else {
			Logger.warn("%s (defaut) : %s", key, defaultValue);
			return defaultValue;
		}
	}
	
	/**
	 * retourne l'entier ou la valeur par défaut
	 */
	private static Integer getInteger(String key, Integer defaultValue) {
		Integer val = null;
		try {
			val = Integer.valueOf(Play.configuration.getProperty(key));
		} catch(Exception e) {
			Logger.error("%s non numerique", key);
		}
		if (val != null) {
			Logger.info("%s : %s", key, val);
			return val;
		} else {
			Logger.warn("%s (defaut) : %s", key, defaultValue);
			return defaultValue;
		}
	}
	
	public Integer contributionValidationMinByType(String type) {
		return contributionValidationMinByType.get(type.toLowerCase());
	}
	
	public Integer quickSearchLengthByCategory(Category category) {
		return quickSearchLengthByCategory.get(category.toString().toLowerCase());
	}
	
	public Integer unlockContributionCount(Integer level) {
		Integer count = unlockLevelAt.get(level);
		if (count == null) {
			count = -1;
		}
		return count;
	}
	
	public enum Category {
		MISSIONS,
		BOTANISTS,
		SPECIMENS,
		HERBONAUTES,
		DISCUSSIONS,
		SCIENTIFICNAMES,
		TAGS
	}

	
}
