import controllers.References;
import models.Content;
import models.User;
import models.questions.ContributionQuestion;
import models.references.Reference;
import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Création des données par défault au démarrage de l'application
 */
@OnApplicationStart
public class Bootstrap extends Job {
 
    private static final String UNUSABLE_CONFIGURATION = "[{\"name\":\"cause\",\"label\":\"\",\"type\":\"list\",\"options\":{\"multiple\":false,\"choice\":[{\"value\":\"Pas de spécimen\"},{\"value\":\"Trop de spécimen\"},{\"value\":\"Pas d'étiquette\"}],\"display\":\"radio\"}}]";
    private static final String COUNTRY_CONFIGURATION = "[{\"name\":\"country\",\"label\":\"\",\"type\":\"reference\",\"options\":{\"reference\":\"%s\",\"display\":\"autocomplete\"},\"mandatory\":true,\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":false}]},{\"name\":\"guessed\",\"label\":\"Je l'ai déduit\",\"type\":\"checkbox\",\"options\":{},\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":false}],\"noConflict\":true},{\"name\":\"not_sure\",\"label\":\"J'hésite\",\"type\":\"checkbox\",\"options\":{},\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":false}],\"noConflict\":true},{\"name\":\"no_info\",\"label\":\"Pas d'information\",\"type\":\"checkbox\",\"visible\":\"always\"}]";
    private static final String REGION1_CONFIGURATION = "[{\"name\":\"region\",\"label\":\"\",\"type\":\"reference\",\"options\":{\"reference\":\"%s\",\"display\":\"autocomplete\",\"placeholder\":\"Région\"},\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":false}],\"mandatory\":true},{\"name\":\"guessed\",\"label\":\"Je l'ai déduit\",\"type\":\"checkbox\",\"noConflict\":true,\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":false}]},{\"name\":\"not_sure\",\"label\":\"J'hésite\",\"type\":\"checkbox\",\"noConflict\":true,\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":false}],\"options\":{}},{\"name\":\"no_info\",\"label\":\"Pas d'information\",\"type\":\"checkbox\",\"visible\":\"always\"}]";
    private static final String REGION2_CONFIGURATION = "[{\"name\":\"region\",\"label\":\"\",\"type\":\"reference\",\"options\":{\"reference\":\"%s\",\"display\":\"autocomplete\",\"placeholder\":\"Région\"},\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":false}],\"mandatory\":true},{\"name\":\"guessed\",\"label\":\"Je l'ai déduit\",\"type\":\"checkbox\",\"noConflict\":true,\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":false}]},{\"name\":\"not_sure\",\"label\":\"J'hésite\",\"type\":\"checkbox\",\"noConflict\":true,\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":false}],\"options\":{}},{\"name\":\"no_info\",\"label\":\"Pas d'information\",\"type\":\"checkbox\",\"visible\":\"always\"}]";
    private static final String COLLECT_DATE_CONFIGURATION = "[{\"name\":\"collect_date\",\"label\":\"\",\"type\":\"period\",\"options\":{},\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":false}],\"mandatory\":true},{\"name\":\"not_sure\",\"label\":\"J'hésite\",\"type\":\"checkbox\",\"options\":{\"defaultValue\":false},\"visible\":\"condition\",\"noConflict\":false,\"visibleOptions\":[{\"field\":\"no_info\",\"value\":false}]},{\"name\":\"no_info\",\"label\":\"Pas d'information\",\"type\":\"checkbox\",\"options\":{},\"visible\":\"always\"}]";
    private static final String COLLECTOR_CONFIGURATION = "[{\"name\":\"collector\",\"label\":\"<h5><i class=\\\"icon-leaf\\\"></i> Récolté par :</h5>\",\"type\":\"reference\",\"mandatory\":true,\"options\":{\"reference\":\"%s\",\"display\":\"autocomplete\",\"allowUserCreation\":true,\"placeholder\":\"Tapez un nom\"},\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":\"\"}]},{\"name\":\"other_collectors\",\"label\":\"\",\"type\":\"reference\",\"options\":{\"reference\":\"%s\",\"display\":\"autocomplete\",\"multiple\":true,\"buttonLabel\":\"Ajouter un récolteur\"},\"noConflict\":true,\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":false}]},{\"name\":\"not_sure\",\"label\":\"J'hésite\",\"type\":\"checkbox\",\"options\":{},\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":false}],\"noConflict\":true},{\"name\":\"no_info\",\"label\":\"Pas d'information\",\"type\":\"checkbox\",\"options\":{},\"visible\":\"always\"}]";
    private static final String IDENTIFIER_CONFIGURATION = "[{\"name\":\"identifier\",\"label\":\"<h5><i class=\\\"icon-pencil\\\"></i> Déterminié (<b>{{ specimen.family }} {{ specimen.genus }}</b>) par :</h5>\",\"type\":\"reference\",\"options\":{\"reference\":\"%s\",\"allowUserCreation\":true,\"display\":\"autocomplete\",\"placeholder\":\"Tapez un nom\"},\"mandatory\":true,\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":false}]},{\"name\":\"not_sure\",\"label\":\"J'hésite\",\"type\":\"checkbox\",\"options\":{},\"noConflict\":true,\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":false}]},{\"name\":\"no_info\",\"label\":\"Pas d'information\",\"type\":\"checkbox\",\"options\":{}}]";
    private static final String LOCALITY_CONFIGURATION = "[{\"name\":\"localisation\",\"label\":\"\",\"type\":\"text\",\"mandatory\":true,\"options\":{\"placeholder\":\"Localisation\"},\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":\"\"}]},{\"name\":\"not_sure\",\"label\":\"J'hésite\",\"type\":\"checkbox\",\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":false}],\"options\":{}},{\"name\":\"no_info\",\"label\":\"Pas d'information\",\"type\":\"checkbox\",\"visible\":\"always\",\"visibleOptions\":{\"field\":\"\",\"value\":false,\"choice\":[]}}]";
    private static final String GEOLOCALISATION_CONFIGURATION = "[{\"name\":\"position\",\"label\":\"\",\"type\":\"geo\",\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":false}],\"mandatory\":true},{\"name\":\"description\",\"label\":\"\",\"type\":\"text\",\"options\":{\"placeholder\":\"Précision\"},\"noConflict\":true,\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":\"\"}]},{\"name\":\"not_sure\",\"label\":\"J'hésite\",\"type\":\"checkbox\",\"options\":{},\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":\"\"}]},{\"name\":\"no_info\",\"label\":\"Photo non géolocalisable\",\"type\":\"checkbox\"}]";
    private static final String SCIENTIFIC_NAME_CONFIGURATION = "[{\"name\":\"same_as_label\",\"label\":\"Le nom est celui de l'étiquette\",\"type\":\"checkbox\",\"visible\":\"always\",\"visibleOptions\":[{\"field\":\"same_as_label\",\"value\":true}],\"options\":{\"defaultValue\":true}},{\"name\":\"scientific_name\",\"label\":\"Nom scientifique\",\"type\":\"reference\",\"options\":{\"display\":\"autocomplete\",\"placeholder\":\"Tapez un nom\"},\"mandatory\":true,\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"same_as_label\",\"value\":false},{\"field\":\"not_in_list\",\"value\":false}]},{\"name\":\"not_in_list\",\"label\":\"Le nom n'est pas dans la liste\",\"type\":\"checkbox\",\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"same_as_label\",\"value\":\"\"}]},{\"name\":\"family\",\"label\":\"\",\"type\":\"text\",\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"same_as_label\",\"value\":\"\"},{\"field\":\"not_in_list\",\"value\":true}],\"options\":{\"placeholder\":\"Famille\"}},{\"name\":\"genus\",\"label\":\"\",\"type\":\"text\",\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"same_as_label\",\"value\":\"\"},{\"field\":\"not_in_list\",\"value\":true}],\"options\":{\"placeholder\":\"Genre\"}},{\"name\":\"epiteth\",\"label\":\"\",\"type\":\"text\",\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"same_as_label\",\"value\":\"\"},{\"field\":\"not_in_list\",\"value\":true}],\"options\":{\"placeholder\":\"Espèce\"}}]";
    private static final String COLLECT_NUMBER_CONFIGURATION = "[{\"name\":\"\",\"label\":\"\",\"type\":\"text\",\"options\":{\"placeholder\":\"AAAA00000BBBBB\"},\"mandatory\":true,\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":false}]},{\"name\":\"no_info\",\"label\":\"Pas d'information\",\"type\":\"checkbox\",\"options\":{}}]";
    private static final String OWNER_CONFIGURATION = "[{\"name\":\"question_1\",\"label\":\"\",\"type\":\"reference\",\"options\":{\"reference\":\"%s\",\"multiple\":true,\"display\":\"autocomplete\",\"placeholder\":\"Tapez un nom d'herbier\",\"buttonLabel\":\"Ajouter un herbier\",\"sortable\":true},\"visible\":\"condition\",\"visibleOptions\":[{\"field\":\"no_info\",\"value\":false}]},{\"name\":\"no_info\",\"label\":\"Pas d'information\",\"type\":\"checkbox\",\"options\":{}}]";

    public void doJob() {

        // Création presentation vide si elle n'existe pas
        if (Content.findByURLAndLang("presentation", "fr") == null) {
        	Logger.warn("Création du contenu 'présentation'");
        	Content content = new Content();
        	content.setLang("fr");
        	content.setUrl("presentation");
        	content.save();
        }

        if (User.findByLogin("admin") == null) {
        	Logger.warn("Création de l'utilisateur admin, modifiez le mot de passe");
        	Fixtures.loadModels("data_admin.yml");
        }

        // Création reference institutions
        if (Reference.find("name = 'institution'").fetch().size() == 0) {
            Reference ref = new Reference();
            ref.setLabel("Institutions");
            ref.setName("institution");
            ref.setAllowUserCreation(false);
            ref.save();
        }

        // Création reference synonymes
        if (Reference.find("name = 'synonym'").fetch().size() == 0) {
            Reference ref = new Reference();
            ref.setLabel("Synonymes");
            ref.setName("synonym");
            ref.setAllowUserCreation(false);
            ref.save();
        }

        JPA.em().createNativeQuery("update H_LINK set HEADER_LINK = 1 where HEADER_LINK IS NULL").executeUpdate();

        // fix level 0
        JPA.em().createNativeQuery("update H_USER set CURRENT_LEVEL = 1 where CURRENT_LEVEL < 1").executeUpdate();

        updgradeV2();
    }

    public void updgradeV2() {

        Logger.info("");
        Logger.info("---------------------");
        Logger.info("Check V2 minimum data");
        Logger.info("---------------------");
        Logger.info("");

        Logger.info("Create references");
        Logger.info("---------------------");
        Logger.info("");
        Map<String, String> nameLabels = new LinkedHashMap<String, String>();
        nameLabels.put("country", "Pays");
        nameLabels.put("region1", "Régions");
        nameLabels.put("region2", "Sous-Régions");
        nameLabels.put("botanist", "Botanistes");
        nameLabels.put("herb", "Herbiers");
        nameLabels.put("scientific_name", "Noms scientifiques");

        Map<String, String> parents = new HashMap<String, String>() {{
            put("region2", "region1");
            put("region1", "country");
        }};
        Map<String, Reference> references = new HashMap<String, Reference>();

        for (Map.Entry<String, String> nameLabel : nameLabels.entrySet()) {
            String name = nameLabel.getKey();
            String label = nameLabel.getValue();

            Reference parent = references.get(parents.get(name));
            Reference ref = createReference(name, label, parent);
            Logger.info("Init ref %s with parent %s", name, parent != null ? parent.getName() : "[empty]");
            references.put(name, ref);
        }

        Logger.info("");
        Logger.info("Create questions");
        Logger.info("---------------------");
        Logger.info("");
        createQuestion(1l,  true , "unusable",          "Photo inutilisable",           1l, 2l, UNUSABLE_CONFIGURATION);
        createQuestion(2l,  false, "country",           "Pays",                         1l, 2l, String.format(COUNTRY_CONFIGURATION,    references.get("country").getId().toString()));
        createQuestion(3l,  false, "region1",           "Région niv 1",                 2l, 2l, String.format(REGION1_CONFIGURATION,    references.get("region1").getId().toString()));
        createQuestion(4l,  false, "region2",           "Région niv 2",                 2l, 2l, String.format(REGION2_CONFIGURATION,    references.get("region2").getId().toString()));
        createQuestion(7l,  false, "collect_date",      "Date",                         3l, 2l, COLLECT_DATE_CONFIGURATION);
        createQuestion(5l,  false, "collector",         "Récolteur",                    4l, 2l, String.format(COLLECTOR_CONFIGURATION,  references.get("botanist").getId().toString(), references.get("botanist").getId().toString()));
        createQuestion(6l,  false, "identifier",        "Déterminateur",                5l, 2l, String.format(IDENTIFIER_CONFIGURATION, references.get("botanist").getId().toString()));
        createQuestion(8l,  false, "locality",          "Localité",                     6l, 2l, LOCALITY_CONFIGURATION);
        createQuestion(8l,  false, "geo",               "Géolocalisation",              6l, 2l, GEOLOCALISATION_CONFIGURATION);
        createQuestion(9l,  false, "scientific_name",   "Nom scientifique",             7l, 2l, String.format(SCIENTIFIC_NAME_CONFIGURATION,  references.get("scientific_name").getId().toString()));
        createQuestion(10l, false, "collect_number",    "Numéro de récolte",            8l, 2l, COLLECT_NUMBER_CONFIGURATION);
        createQuestion(11l, false, "owner",             "Herbier propriétaire",         8l, 2l, String.format(OWNER_CONFIGURATION,              references.get("herb").getId().toString()));
        Logger.info("");
        Logger.info("---------------------");
        Logger.info("V2 data initialized");
        Logger.info("---------------------");
        Logger.info("");


    }

    private Reference createReference(String name, String label, Reference parent) {
        Reference ref = Reference.find("name = ?", name).first();
        if (ref == null) {
            ref = new Reference();
        }
        ref.setName(name);
        ref.setLabel(label);
        ref.setParent(parent);
        ref.save();
        return ref;
    }

    private ContributionQuestion createQuestion(Long index, Boolean man, String name, String label, Long minLevel, Long validation, String configuration) {
        ContributionQuestion existing = ContributionQuestion.find("name = ? and missionId is null", name).first();
        if (existing != null) {
            Logger.info("Existing question [" + label + "]");
            return existing;
        } else {
            Logger.info("Create question [" + label + "]");
        }

        ContributionQuestion question = new ContributionQuestion();
        question.setName(name);
        question.setLabel(label);
        question.setMinLevel(minLevel);
        question.setValidationLevel(validation);
        question.setConfiguration(configuration);
        question.setDefaultForMission(true);
        question.setMandatoryForMission(man);
        question.setEditable(false);
        question.setSortIndex(index);
        question.setDeleted(false);
        question.save();
        return question;
    }


 
}