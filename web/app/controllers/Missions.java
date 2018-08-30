package controllers;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.fasterxml.jackson.databind.JsonNode; 
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import conf.Herbonautes;
import helpers.GsonUtils;
import inspectors.Event;
import inspectors.InspectorChain;
import jobs.CartJob;
import jobs.RecolnatTransferJob;
import jobs.SpecimensLoaderJob;
import libs.Images;
import libs.Json;
import models.*;
import models.User.UserContributionReport;
import models.activities.Activity;
import models.activities.ContributionV2AddActivity;
import models.alerts.Alert;
import models.alerts.ConflictV2Alert;
import models.comments.MissionComment;
import models.comments.SpecimenComment;
import models.contributions.DateContribution;
import models.contributions.UnusableContribution;
import models.contributions.UnusableContribution.Cause;
import models.questions.*;
import models.questions.special.GeolocalisationStaticAnswer;
import models.questions.special.StaticAnswer;
import models.recolnat.RecolnatTransferReport;
import models.serializer.*;
import models.serializer.activities.TimestampSinceJsonSerializer;
import models.serializer.contributions.*;
import models.tags.Tag;
import models.tags.TagLink;
import models.tags.TagLinkType;
import models.tags.TagType;
import models.stats.StatContributionMission;
import models.stats.StatContributionQuestion;
import models.stats.StatContributionValue;
import models.stats.StatDailyMission;
import notifiers.Mails;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.i18n.Messages;

import static controllers.Security.connectedId;
import static controllers.Security.connectedLogin;
import static controllers.Security.isLeader;

/**
 * Controleur gérant les actions liées aux missions
 */
public class Missions extends Application {

    public static final String CURRENT_MISSION_KEY = "m";
    public static final String CURRENT_SPECIMEN_KEY = "s";
    private static final ObjectMapper mapper = new ObjectMapper();
    public static int compteur = 0;

    /**
     * Ajoute une annonce
     */
    public static void addAnnouncement(Long id, @Valid Announcement announcement) {
        Security.forbiddenIfNotLeader();

        Mission mission = Mission.findById(id);
        notFoundIfNull(mission);
        if (!mission.isLeader(connectedLogin())) {
            forbidden();
        }

        if (validation.hasErrors()) {
            flash.put("tab", "announcements");
            validation.keep();
            params.flash();
            settings(id);
        }

        announcement.setMission(mission);
        announcement.save();

        if (announcement.isPublished()) {
            InspectorChain.get().inspect(Event.MISSION_ANNOUNCE, mission);
            Mails.missionAnnouncement(mission, announcement);
        }

        flash.clear();
        flash.put("tab", "announcements");
        announcement.setText("TEST");
        announcement.setTitle(null);
        settings(id);
    }

    /**
     * Retourne la liste des utilisateurs
     * (onglet membres)
     *
     * @param id
     */
    public static void allMembers(Long id) {
        Security.forbiddenIfNotLeader();

        Mission mission = Mission.findById(id);
        notFoundIfNull(mission);

        List<User> members = User.find("select m.users from Mission m where m.id = ? " + "order by login", id).fetch();


        GsonBuilder gson = GsonUtils.getGsonBuilder();

        gson.registerTypeAdapter(User.class, UserForAdminJsonSerializer.get());

        renderJSON(gson.create().toJson(members));
    }

    /**
     * Bannit un utilisateur (il ne pourra plus s'inscrire)
     */
    public static void banMember(Long id, String login) {
        Security.forbiddenIfNotLeader();

        Mission mission = Mission.findById(id);
        mission.ban(login);
        flash.success(Messages.get("mission.banned.user", login));
        settings(id);
    }

    @Transactional(readOnly = true)
    public static void bigImage(Long id, Long imageId) {
        //final Mission mission = Mission.findById(id);
        //notFoundIfNull(mission);
        //if (!mission.isHasBigImage()) {
        //    notFound();
        //}

        Long bigImageId = Mission.bigImageId(id);
        renderImage(bigImageId);
        // Contents.image(mission.getBigImageId());
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Actions pour les chefs de mission
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    @Transactional(readOnly = true)
    public static void blank() {
        Security.forbiddenIfNotLeader();

        List<String> langs = Play.langs;
        // List<Country> countries = Country.findAllOrdered();
        // render(langs, countries);
        render(langs);
    }

    /**
     * Ferme la mission
     */
    public static void closeMission(Long id) {
        Security.forbiddenIfNotLeader();

        Mission mission = Mission.findById(id);
        if (!mission.isLeader(connectedLogin())) {
            forbidden();
        }

        mission.closed = true;
        mission.save();

        show(mission.id);
    }

    /**
     * Affiche la page de contribution pour un specimen
     */
    @Transactional(readOnly = true)
    public static void contributionBoard(Long missionId, Long specimenId) {

        Mission mission = Mission.findById(missionId);
        Specimen specimen = Specimen.findById(specimenId);

        // EVOLUTION 0001146 : compter le nombre total de commentaire pour le specimen d'id {id}
        Long comments = SpecimenComment.count("specimen.id = ?", specimenId);

        // Reference data
        List<Country> countries = null;
        if (mission.getCountry() == null) {
            countries = Country.findAllOrdered();
        }


        Cause[] causes = UnusableContribution.Cause.values();

        boolean isMember = mission.isMember(connectedLogin());

        saveCurrentInSession(missionId, specimenId);

        render(mission, specimen, isMember, countries, causes, comments);
    }


    /**
     * Retourne le classement des utilisateurs ainsi que le nombre
     * de contributions par type
     */
    @Transactional(readOnly = true)
    public static void contributionRatings(Long id, Integer page) {
        Mission mission = Mission.findById(id);
        notFoundIfNull(mission);

        int from = ((page == null ? 1 : page) - 1) * Herbonautes.get().pageLength;

        List<UserContributionReport> reports = mission.getContributorRatings(from, from + Herbonautes.get().pageLength);

        int threshold = 1;
        for (UserContributionReport report : reports) {
            report.rank = from + threshold++;
        }

        renderJSON(reports, UserSimpleJsonSerializer.get());
    }

    /**
     * Creer une mission
     */
    public static void create(@Valid Mission mission, File specimensFile, File image, File bigImage, boolean oneCountry,
                              Long countryId) {

        Security.forbiddenIfNotLeader();



        String label = mission.getPrincipalTagString();
        Tag tag = Tag.findByLabel(label);
        if (validation.hasErrors() || tag != null) {
            params.flash();
            validation.keep();
            if(tag != null)
                validation.current().addError("mission.principalTagString", play.i18n.Messages.get("tag.already.used"));
            blank();
        }

        if (image != null) {
            libs.Images.squarify(image, image);
            libs.Images.resize(image, image, 220, 220);
            // Image imageDB = Image.createImageFromFile(image);
            Image imageDB = Image.createImageFromBytes(Images.compress(image));
            mission.setImageId(imageDB.id);
            mission.setHasImage(true);
        }

        if (bigImage != null) {
            libs.Images.centeredCrop(bigImage, bigImage, 600, 200);
            // Image imageDB = Image.createImageFromFile(bigImage);
            Image imageDB = Image.createImageFromBytes(Images.compress(bigImage));
            mission.setBigImageId(imageDB.id);
            mission.setHasBigImage(true);
        }

        mission.setLeader(Security.connected());
        Cache.delete("connected_leader_missions_" + Security.connected().id);

        tag = new Tag();
        tag.setTagLabel(label);
        tag.setTagType(TagType.MISSION);
        tag.setPublished(true);
        tag.save();
        tag.refresh();
        mission.setPrincipalTag(tag);
        mission.save();
        mission.refresh(); // Pour retrouver l'id dans le Job de chargement

        if (mission.published) {
            InspectorChain.get().inspect(Event.MISSION_PUBLISHED, mission);
        }

        TagLink tagLink = new TagLink();
        tagLink.setLinkType(TagLinkType.MISSION);
        tagLink.setTagId(tag.getId());
        tagLink.setTargetId(mission.getId());
        tagLink.setPrincipal(true);
        tagLink.create();

        // Creation des questions par defaut
        Logger.info("Create default questions for mission [%d]", mission.id);
        List<ContributionQuestion> templates = ContributionQuestion.findAllDefaultTemplates();
        Logger.info("  from %d templates", templates.size());
        for (ContributionQuestion template : templates) {

            ContributionQuestion missionQuestion = ContributionQuestion.createFromTemplate(template, mission.id);
            missionQuestion.save();

        }


        flash.success(Messages.get("mission.created", mission.getTitle()));

        show(mission.id);
    }

    /**
     * Retourne les dates validées
     */
    @Transactional(readOnly = true)
    public static void dateContributions(Long id) {

        List<DateContribution> list = Mission.getValidatedDates(id);

        Gson gson =
                GsonUtils.getGsonBuilder().registerTypeAdapter(DateContribution.class, DateContributionForTimelineJsonSerializer.get())
                        .create();

        JsonObject tl = new JsonObject();
        tl.addProperty("dateTimeFormat", "iso8601");
        tl.add("events", gson.toJsonTree(list).getAsJsonArray());

        renderJSON(gson.toJson(tl));
    }

    /**
     * Supprime une annonce
     */
    public static void deleteAnnouncement(Long id) {
        Security.forbiddenIfNotLeader();

        Announcement announcement = Announcement.findById(id);
        notFoundIfNull(announcement);

        Mission mission = announcement.getMission();
        if (!mission.isLeader(connectedLogin())) {
            forbidden();
        }

        announcement.delete();

        flash.put("tab", "announcements");
        settings(mission.id);
    }

    /**
     * Retourne les géolocalisations validées
     */
    @Transactional(readOnly = true)
    public static void geolocalisationContributions(Long id) {
        List<GeolocalisationStaticAnswer> geos = GeolocalisationStaticAnswer.find("missionId = ?", id).fetch();
        renderJSON(geos, SpecimenSimpleJsonSerializer.get());

    }

    @Transactional(readOnly = true)
// public static void image(Long id, Long imageId) {
//
// final Mission mission = Mission.findById(id);
// notFoundIfNull(mission);
// if (!mission.isHasImage()) {
// notFound();
// }
//
// renderImage(mission.getImageId());
// //Contents.image(mission.getImageId());
// }
    public static void image(Long id, Long imageId) {

// final Mission mission = Mission.findById(id);
        notFoundIfNull(imageId);

        renderImage(imageId);
        // Contents.image(mission.getImageId());
    }


    /**
     * Rejoindre une mission
     */
    public static void joinMission(Long id, Boolean ajax) {

        if (!Security.isConnected()) {
            flash.error(Messages.get("alert.no.connected"));
            show(id);
        }

        String login = connectedLogin();

        Cache.delete("connected_missions_" + connectedId());

        Mission mission = Mission.findById(id);

        Logger.info("%s joining mission %s", login, mission.getTitle());

        if (mission.isMember(login)) {
            flash.error(Messages.get("alert.already.joined"));
            show(mission.id);
        }

        if (mission.isBanned(login)) {
            flash.error(Messages.get("mission.join.forbidden"));
            show(mission.id);
        }

        User user = User.findByLogin(login);

        mission.getUsers().add(user);
        mission.save();

        Mails.welcomeToMission(user, mission);

        InspectorChain.get().inspect(Event.MISSION_JOINED, user, mission);

        if (request.isAjax() || Boolean.TRUE.equals(ajax)) {
            ok();
        } else {
            flash.success(Messages.get("alert.join.mission.success", user.getLogin()));
            show(mission.id);
        }
    }

    /**
     * Affichage de la liste des missions
     * (publiées pour les herbonautes, toutes pour les chefs de missions)
     */
    public static void list() {

        boolean isLeader = isLeader();
        List<MissionSimple> nextMissions = MissionSimple.getNextMissions(isLeader);
        List<MissionSimple> presentMissions = MissionSimple.getPresentMissions(isLeader);
        List<MissionSimple> closedMissions = MissionSimple.getClosedMissions(isLeader);

        render(nextMissions, presentMissions, closedMissions);
    }

    /**
     * Charge le fichier de specimens
     */
    private static void loadSpecimens(Mission mission, File specimens) {
        // File newFile = Play.getFile("/uploads/ + specimens.getName());
        // specimens.renameTo(newFile);
        // specimens.delete();

        File uploadDir = new File(Herbonautes.get().uploadDir);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        File newFile = new File(uploadDir, specimens.getName());
        try {
            FileUtils.moveFile(specimens, newFile);
        } catch (IOException e) {

        }

        new SpecimensLoaderJob(mission.id, newFile).now();

    }

    public static void markCurrent(Long missionId, Long specimenId) {
        Logger.info("Mark %s %s", missionId, specimenId);
        session.put(CURRENT_MISSION_KEY, missionId);
        session.put(CURRENT_SPECIMEN_KEY, specimenId);
        ok();
    }

    @Transactional(readOnly = true)
    public static void minDateContributions(Long id) {
        List<DateContribution> list = Mission.getValidatedDates(id);

        Date minDate = null;
        if (list != null) {
            Date currentDate = null;
            for (DateContribution contribution : list) {
                if (!contribution.isPeriod()) {
                    currentDate = contribution.getCollectDate();
                } else {
                    currentDate = contribution.getCollectStartDate();
                }
                if (currentDate != null) {
                    if (minDate == null || minDate.after(currentDate)) {
                        minDate = currentDate;
                    }
                }
            }
        }
        if (minDate != null) {
            renderJSON(minDate);
        } else {
            // renderJSON("test");
            notFound();
        }

    }

    /**
     * EVOLUTION 0001146
     * Compter le nombre total de commentaire pour la mission d'id {id}
     *
     * @param Long id
     * @author MELECOQ
     */
    public static void missionCommentsTotal(Long id) {
        Long count = MissionComment.count("mission.id = ?", id);
        renderJSON(count);
    }

    /**
     * Renvoi vers une page de contribution au hasard:
     */
    public static void randomContributionBoard(Long missionId) {
        boolean exist = Mission.exist(missionId);
        if (!exist) notFound();

        if (connectedId() == null) {
            Specimen specimenDrawedNotConnected = Mission.getRandomSpecimenNotConnected(missionId);
            if (request.isAjax()) renderText(specimenDrawedNotConnected.id);
            else contributionBoard(missionId, specimenDrawedNotConnected.id);
        } else {
            Specimen specimenChosen = Mission.addSeenSpecimen(missionId, connectedId());

            if (specimenChosen == null) notFound("Il n'y a plus de spécimen à tirer au sort pour cette mission");

            if (request.isAjax()) renderText(specimenChosen.id);
            else contributionBoard(missionId, specimenChosen.id);
        }

    }

    /**
     * Quitter la mission
     */
    public static void removeMember(Long id, String login) {

        Security.forbiddenIfNotCurrentUser(login);

        Mission mission = Mission.findById(id);

        if (!mission.isMember(login)) {
            flash.error(Messages.get("error.not.member"));
            show(mission.id);
        }

        User user = User.findByLogin(login);

        mission.getUsers().remove(user);
        mission.save();

        flash.success(Messages.get("alert.quit.mission.success", user.getLogin()));

        // Show mission page
        show(mission.id);
    }

    /**
     * Si ferm��e par erreur
     */
    public static void reopenMission(Long id) {
        Security.forbiddenIfNotLeader();

        Mission mission = Mission.findById(id);
        if (!mission.isLeader(connectedLogin())) {
            forbidden();
        }

        mission.closed = false;
        mission.save();

        show(mission.id);
    }

    /**
     * Modifie une annonce
     */
    public static void saveAnnouncement(Long id, @Valid Announcement announcement, boolean published) {
        Security.forbiddenIfNotAdminOrLeader();
        flash.put("tab", "announcements");

        Mission mission = Mission.findById(id);
        notFoundIfNull(mission);
        if (!mission.isLeader(connectedLogin())) {
            forbidden();
        }

        if (validation.hasErrors()) {
            validation.keep();
            settings(id);
        }

        announcement.setPublished(published);
        announcement.setMission(mission);
        announcement.save();

        if (announcement.isPublished()) {
            InspectorChain.get().inspect(Event.MISSION_ANNOUNCE, mission);
            Mails.missionAnnouncement(mission, announcement);
        }

        // redirect("?tab=announcements");

        settings(id);
    }

    /**
     * Sauvegarde la derni��re contribution pour pouvoir y retourner
     * (apr��s le passage d'un quiz)
     */
    private static void saveCurrentInSession(Long missionId, Long specimenId) {
        session.put(CURRENT_MISSION_KEY, missionId);
        session.put(CURRENT_SPECIMEN_KEY, specimenId);
    }

    /**
     * Modifie la presentation
     */
    public static void savePresentation(Long id, String presentation) {
        Mission mission = Mission.findById(id);
        notFoundIfNull(mission);
        mission.setPresentation(presentation);
        mission.save();
        settings(id);
    }

    /**
     * Modifie le rapport
     */
    public static void saveReport(Long id, String report) {
        Mission mission = Mission.findById(id);
        notFoundIfNull(mission);
        mission.setReport(report);
        mission.save();
        settings(id);
    }

    /**
     * Modifie la configuration de la mission
     */
    public static void saveSettings(Long id, @Valid Mission mission, File specimensFile, File image, File bigImage,
                                    boolean oneCountry) {


        Security.forbiddenIfNotAdminOrLeader();

        // CHECK chef de mission
        Mission missionToUpdate = Mission.findById(id);
        notFoundIfNull(missionToUpdate);
        if (connectedLogin() == null || (!Security.isAdmin() && !Security.isTeam() && !connectedLogin().equals(missionToUpdate.getLeader().getLogin()))) {
            forbidden();
        }


        String label = mission.getPrincipalTagString();
        boolean changedTag = missionToUpdate.getPrincipalTag() == null || !missionToUpdate.getPrincipalTag().getTagLabel().equals(mission.getPrincipalTagString());
        Tag tag = null;
        if (changedTag) tag = Tag.findByLabel(label);
        if (validation.hasErrors() || (changedTag && tag != null)) {
            params.flash();
            validation.keep();
            if(changedTag && tag != null)
                validation.current().addError("mission.principalTagString", play.i18n.Messages.get("tag.already.used"));
            settings(id);
        }

        if (image != null) {

            boolean readableImage = libs.Images.isImageReadable(image);

            if (readableImage) {
                try {
                    Image imageDB = Image.createAvatar(image);
                    missionToUpdate.setImageId(imageDB.id);
                    missionToUpdate.setHasImage(true);

                } catch (Exception e) {
                    readableImage = false;
                }
            }

            if (!readableImage) {
                flash.error(Messages.get("message.user.image.ignored"));
            }
        }

        if (bigImage != null) {

            // Image imageDB = Image.createImageFromFile(bigImage);

            boolean readableImage = libs.Images.isImageReadable(bigImage);

            if (readableImage) {
                try {
                    libs.Images.centeredCrop(bigImage, bigImage, 770, 230);
                    Image imageDB = Image.createImageFromBytes(Images.compress(bigImage));
                    missionToUpdate.setBigImageId(imageDB.id);
                    missionToUpdate.setHasBigImage(true);

                } catch (Exception e) {
                    readableImage = false;
                }
            }

            if (!readableImage) {
                flash.error(Messages.get("message.user.image.ignored"));
            }


        }

        if (specimensFile != null) {
            // Lance le job d'ajout des sp��cimens
            loadSpecimens(missionToUpdate, specimensFile);
        }

        missionToUpdate.setTitle(mission.getTitle());
        missionToUpdate.setShortDescription(mission.getShortDescription());
        missionToUpdate.setOpeningDate(mission.getOpeningDate());
        //missionToUpdate.setGoal(mission.getGoal());
        missionToUpdate.reportPublished = mission.reportPublished;

        boolean openOrCloseEvent = (missionToUpdate.published != mission.published);

        missionToUpdate.published = mission.published;

        if (oneCountry) {
            Long countryId = params.get("countryId", Long.class);
            missionToUpdate.setCountry(Country.<Country>findById(countryId));
        } else {
            missionToUpdate.setCountry(null);
        }

        if (missionToUpdate.getPrincipalTag() != null) {

            if(!missionToUpdate.getPrincipalTag().getTagLabel().equals(label)) {
                missionToUpdate.getPrincipalTag().setTagLabel(label);
                missionToUpdate.getPrincipalTag().save();
            }
        }


        if(missionToUpdate.getPrincipalTag() != null && missionToUpdate.getPrincipalTag().getTagLabel().equals(mission.getPrincipalTagString())) {
            missionToUpdate.getPrincipalTag().setTagLabel(mission.getPrincipalTagString());
            missionToUpdate.getPrincipalTag().save();
        }

        missionToUpdate.save();

        if (openOrCloseEvent && missionToUpdate.published) {
            InspectorChain.get().inspect(Event.MISSION_PUBLISHED, missionToUpdate);
        }

        flash.success(Messages.get("mission.modified"));
        // "Modification effectuée");

        settings(missionToUpdate.id);
    }

    /**
     * Affiche la page de configuration de la mission
     */
    public static void settings(Long id) {
        Security.forbiddenIfNotLeader();

        Mission mission = Mission.findById(id);

        List<Announcement> announcements = mission.getAnnouncements();

        List<String> langs = Play.langs;
        List<Country> countries = Country.findAllOrdered();

        List<String> contributionTypes = mission.getRequiredContributionTypes();

        String tab = params.get("tab");
        if (tab == null) {
            tab = flash.get("tab");
        }
        if (tab == null) {
            tab = "parameters";
        }

        render(mission, langs, countries, announcements, contributionTypes, tab);
    }

    /**
     * Affiche la page d'une missions
     */
    @Transactional(readOnly = true)
    public static void show(Long id) {

        Mission mission = Mission.findById(id);
        notFoundIfNull(mission);

        if ((!mission.published && !isLeader()) || mission.proposition) {
            // On n'affiche les missions non publiées
            // qu'aux chefs de mission
            notFound();
        }

        boolean isMember = mission.isMember(connectedLogin());

        //List<String> contributionTypes = mission.getRequiredContributionTypes();

        Boolean isLoading = Mission.isLoading(id);
        // List<User> topContributors = mission.getTopContributors();
        List<Announcement> announcements = mission.getCurrentPublishedAnnouncements();

        // Positionnement sur l'onglet au besoin
        String tab = params.get("tab");
        if (tab == null) {
            if (mission.closed && mission.reportPublished) {
                tab = "report";
            } else {
                tab = isMember ? "contributions" : "presentation";
            }
        }

        Long forceDiscussion = params.get("discussion") != null ? Long.valueOf(params.get("discussion")) : null;
        if (forceDiscussion != null) {
            tab = "comments";
        }


        render(mission, announcements, isMember, tab, isLoading, forceDiscussion);
    }

    /**
     * Pour affichage compteur chef de mission
     */
    @Transactional(readOnly = true)
    public static void specimensCount(Long id) {
        Mission mission = Mission.findById(id);
        if(mission.isProposition()) {
            if(!Security.isConnected() || Security.connectedUser().getLevel() < Herbonautes.get().missionProposalMinLevel)
                forbidden();
        } else {
            Security.forbiddenIfNotLeader();
        }
        Long count = mission.getSpecimensCount();
        Long countTotal = mission.getTotalSpecimensCount();
        Long countTiled = mission.getTiledSpecimensCount();
        Boolean loading = Mission.isLoading(id) || mission.isLoadingCart();

        JsonObject state = new JsonObject();
        state.addProperty("count", count);
        state.addProperty("countTotal", countTotal);
        state.addProperty("countTiled", countTiled);
        state.addProperty("loading", loading);
        renderJSON(state);
    }

    /**
     * Retourne les rapports de contribution des specimens au format JSON
     * (onglet mission>contributions)
     */
    @Transactional(readOnly = true)
    public static void specimenWithContributionReports(Long id, Integer page, Boolean onlyConflicts) {
        Mission mission = Mission.findById(id);
        notFoundIfNull(mission);



        /*List<Specimen> lastSpecimens = mission.getLastContributedSpecimens(page, Herbonautes.get().pageLength);

        renderJSON(lastSpecimens, SpecimenWithContributionReportsJsonSerializer.get(),
                ContributionReportJsonSerializer.get());
        */

        //int from = ((page == null ? 1 : page) - 1) * Herbonautes.get().pageLength;

        Logger.info("Only conflicts : " + onlyConflicts);

        if (onlyConflicts == null) {
            onlyConflicts = false;
        }

        // V2 stats
        List<ContributionSpecimenStat> stats = null;
        if (onlyConflicts) {
            stats = ContributionSpecimenStat
                        .find("missionId = ? and conflicts = true and lastModifiedAt is not null order by lastModifiedAt desc", id)
                        .fetch((page == null ? 1 : page), Herbonautes.get().pageLength);
        } else {
            stats = ContributionSpecimenStat
                        .find("missionId = ? and lastModifiedAt is not null order by lastModifiedAt desc", id)
                        .fetch((page == null ? 1 : page), Herbonautes.get().pageLength);
        }

        //List<Specimen> lastSpecimens = Specimen.find("mission.id = ?", id).fetch(5);

        renderJSON(stats, ContributionQuestionStatSimpleJsonSerializer.get(), SpecimenSimpleJsonSerializer.get(), QuestionSimpleJsonSerializer.get());
    }

    /**
     * Pour affichage compteur chef de mission
     */
    @Transactional(readOnly = true)
    public static void tiledSpecimensCount(Long id) {
        Security.forbiddenIfNotLeader();

        Mission mission = Mission.findById(id);
        Long count = mission.getTiledSpecimensCount();
        Boolean loading = true;

        JsonObject state = new JsonObject();
        state.addProperty("count", count);
        state.addProperty("loading", loading);
        renderJSON(state);
    }

    /**
     * Retourne les questions de la mission
     */

    @Transactional(readOnly = true)
    public static void allQuestions(Long id) {
        List<ContributionQuestion> questions = ContributionQuestion.findAllActiveForMission(id);
        renderJSON(questions, ContributionQuestionJsonSerializer.get(), SimpleQuizJsonSerializer.get());
    }



    @Transactional
    public static void reloadQuestionsConfiguration(Long id, Long questionId) {
        Security.forbiddenIfNotLeader();

        if (questionId != null) {

            ContributionQuestion question = ContributionQuestion.findById(questionId);
            ContributionQuestion template = ContributionQuestion.findById(question.getTemplateId());
            question.setLabel(template.getLabel());
            question.setShortLabel(template.getShortLabel());
            question.setConfiguration(template.getConfiguration());
            question.save();

        } else {

            List<ContributionQuestion> allQuestions = ContributionQuestion.findAllForMission(id);

            for (ContributionQuestion question : allQuestions) {
                ContributionQuestion template = ContributionQuestion.findById(question.getTemplateId());
                question.setLabel(template.getLabel());
                question.setShortLabel(template.getShortLabel());
                question.setConfiguration(template.getConfiguration());
                question.save();
            }

        }

        List<ContributionQuestion>questions = ContributionQuestion.findAllForMission(id);



        renderJSON(questions, ContributionQuestionJsonSerializer.get(), SimpleQuizJsonSerializer.get());
    }

    public static void saveQuestions(Long id) throws IOException {

        JsonNode json = mapper.readTree(request.body);
        Logger.debug(json.toString());

        // Setting mission ID
        List<ContributionQuestion> questions = Json.parseContributionQuestions(json);

        long order = 1;
        HashSet<Long> saveIds = new HashSet<Long>();
        HashMap<Long, Long> deletedIdByTemplateId = new HashMap<Long, Long>();
        for (ContributionQuestion q : questions) {
            q.setSortIndex(order++);
            q.setMissionId(id);
            q.setDeleted(false);
            saveIds.add(q.id);
        }

        // Questions à supprimer (logiquement)
        Mission mission = Mission.findById(id);
        for (ContributionQuestion q : mission.getQuestions()) {
            if (!saveIds.contains(q.id)) {
//                q.delete();
                q.setDeleted(true);
                q.save();
            }
            if (q.getDeleted()) {
                Logger.info("DELETED " + q.getTemplateId() + " -> " + q.getId());
                deletedIdByTemplateId.put(q.getTemplateId(), q.getId());
            }
        }

        for (ContributionQuestion q : questions) {
            Logger.info("Save question " + q.getLabel() + " (" + q.id + ") " + q.getNeededQuiz());
            if (q.id != null) {
                q = JPA.em().merge(q);
            } else {
                // Recherche version supprimée
                if (deletedIdByTemplateId.get(q.getTemplateId()) != null) {
                    q.id = deletedIdByTemplateId.get(q.getTemplateId());
                    Logger.info("Already exists " + q.getLabel() + " -> " + q.id);
                    q = JPA.em().merge(q);
                }
            }
            q.save();
        }
        // Clear stats
        ContributionQuestionStat.deleteStatsWithoutQuestion(id);
        //mission.setQuestions(questions);
        //mission.save();

        renderJSON(questions, ContributionQuestionJsonSerializer.get(), SimpleQuizJsonSerializer.get());
    }




    // Tableaux de bord V2

    public static void lastSpecimenIdsForStats(Long id) {
        List<ContributionSpecimenStat> stats = JPA.em().createQuery("select s from ContributionSpecimenStat s where s.missionId = :missionId " +
                "order by lastModifiedAt desc", ContributionSpecimenStat.class)
                                 .setParameter("missionId", id)
                                 .setMaxResults(5)
                                 .getResultList();

        renderJSON(stats);
    }




    // Stats
    // /missions/:id/stats/contributions
    public static void statsContributionsByQuestion(Long id) {
         List<StatContributionQuestion> stats = StatContributionQuestion.find("missionId = ?", id).fetch();
         renderJSON(stats, QuestionSimpleJsonSerializer.get());
    }



    public static void statsContributionsByUser(Long id) {
        List<StatContributionMission> stats =  StatContributionMission.find("missionId = ? and user is not null order by answerCount desc", id).fetch(10);
        renderJSON(stats, UserSimpleJsonSerializer.get(), ContributionQuestionSimpleJsonSerializer.get());
    }


    public static void statsContributionsByUserDetails(Long id, Integer page) {

        List<StatContributionMission> stats =
                StatContributionMission.find("missionId = ? and user is not null order by answerCount desc, user.id", id)
                                       .fetch(page != null ? page : 1, 3);

        renderJSON(stats, UserSimpleJsonSerializer.get(), ContributionQuestionSimpleJsonSerializer.get());
    }


    public static void statsValuesByType(Long id, String type) {

        List<StatContributionValue> stats =
                StatContributionValue.find("missionId = ? and type = ? order by answerCount desc", id, type.toUpperCase())
                                     .fetch(10);

        renderJSON(stats);
    }


    public static void statsDaily(Long id) {

        List<StatDailyMission> stats =
                StatDailyMission.find("missionId = ? order by date desc", id)
                        .fetch();

        renderJSON(stats, StatDailyMissionJsonSerializer.get());
    }

    public static void exportValidAnswersJson(Long id, Boolean dl) {
        Security.forbiddenIfNotLeader();

        List<Specimen> specimens =
                Specimen.find("select distinct c.specimen from ContributionSpecimenStat c where c.validated = true and c.missionId = ?", id).fetch();

        response.setHeader("Content-Disposition", "attachment; filename=Herbonautes_Mission_" + id + "_Contributions.json");

        GsonBuilder gson = GsonUtils.getGsonBuilder();
        gson.registerTypeAdapter(Specimen.class, SpecimenWithValidAnswersJsonSerializer.get());
        gson.registerTypeAdapter(ContributionAnswer.class, ContributionAnswerSimpleJsonSerializer.get());
        gson.setPrettyPrinting();

        String json = gson.create().toJson(specimens);


        response.setContentTypeIfNotSet("text/csv; charset=utf-8");
        renderText(json);



        //renderJSON(specimens, SpecimenWithValidAnswersJsonSerializer.get(), ContributionAnswerSimpleJsonSerializer.get() );
    }

    public static void exportUsersAnswers(Long id, Boolean dl, Long limit) throws IOException {


        List<Specimen> specimens = Specimen.find("mission.id = ?", id).fetch();

        Logger.info("Export user answers ("  + specimens.size()  + " specimens) - limit : " + limit);

        if (dl) {
            response.setHeader("Content-Disposition", "attachment; filename=herbonautes_mission_" + id + "_contributions_utilisateurs.csv");
        }

        ///GsonBuilder gson = GsonUtils.getGsonBuilder();
        ///gson.registerTypeAdapter(Specimen.class, SpecimenWithValidAnswersJsonSerializer.get());
        ///gson.registerTypeAdapter(ContributionAnswer.class, ContributionAnswerSimpleJsonSerializer.get());
        ///gson.setPrettyPrinting();

        ///String json = gson.create().toJson(specimens);

        ///response.setContentTypeIfNotSet("application/json; charset=utf-8");
        ///renderText(json);

        Map<String, List<String>> questionLines = ContributionQuestion.getAllQuestionLines(id);

        List<String> flatLines = new ArrayList();
        for (Map.Entry<String, List<String>> lines : questionLines.entrySet()) {
            for (String line : lines.getValue()) {
                flatLines.add(lines.getKey() + " - " + line);
            }
        }

        List<String> headerList = new ArrayList(); //;
        headerList.addAll(Arrays.asList(
                "specimen - institute",
                "specimen - collection",
                "specimen - code",
                "specimen - family",
                "specimen - genus",
                "specimen - specific_epithet",
                "user - login"));
        headerList.addAll(flatLines);
        String[] headerArray = headerList.toArray(new String[headerList.size()]);


        StringWriter sw = new StringWriter();

        CSVWriter w = new CSVWriter(sw, ';');

        w.writeNext(headerArray);
        //response.writeChunk(sw.getBuffer().toString());
        //sw.flush();
        //sw.getBuffer().setLength(0);

        int countLines = 0;

        for (Specimen specimen : specimens) {

            Logger.info("Export specimen " + specimen.getCode());

            if (limit != null && countLines >= limit) {
                break;
            }

           Map<String, Map<String, ContributionAnswer>> answersByLoginQuestionName = ContributionAnswer.findAllUserAnswersForSpecimenByLoginAndName(specimen.id);

            for (Map.Entry<String, Map<String, ContributionAnswer>> userAns : answersByLoginQuestionName.entrySet()) {

                String login = userAns.getKey();

                countLines++;
                Logger.info("export line " + countLines);
                List<String> line = new ArrayList();
                line.add(specimen.getInstitute());
                line.add(specimen.getCollection());
                line.add(specimen.getCode());
                if (specimen.getSpecificEpithet() == null) {
                    line.add(null);
                    line.add(specimen.getFamily());
                    line.add(specimen.getGenus());
                } else {
                    line.add(specimen.getFamily());
                    line.add(specimen.getGenus());
                    line.add(specimen.getSpecificEpithet());
                }

                line.add(login);

                //
                // Valid answers
                List<String> answerLine = new ArrayList();

                for (Map.Entry<String, List<String>> lines : questionLines.entrySet()) {
                    String q = lines.getKey();

                    ContributionAnswer answer = userAns.getValue().get(q);

                    Map<String, String> exportAnswer = null;
                    if (answer != null) {

                        exportAnswer = answer.toExportableValue();
                        //Logger.info("%s: %s", q, exportAnswer);
                    } else {
                        //Logger.info("%s : <NULL>", q, exportAnswer);
                    }

                    for (String l : lines.getValue()) {

                        String val = null;

                        if (exportAnswer != null) {
                            val = exportAnswer.get(l);
                        }

                        line.add(val);
                    }
                }

                w.writeNext(line.toArray(new String[line.size()]));
                //w.flush();
                System.out.println(line);

                //response.writeChunk(sw.getBuffer().toString());
                //System.out.println(sw.getBuffer().toString());

                //sw.flush();
                //sw.getBuffer().setLength(0);

            }






            //w.writeNext(line.toArray(new String[line.size()]));
        }
        w.flush();
        renderText(sw.getBuffer().toString());
    }

    public static void exportValidAnswers(Long id, Boolean dl, Long limit) throws IOException {
        Security.forbiddenIfNotLeader();

        List<Specimen> specimens = Specimen.find("mission.id = ?", id).fetch();

        if (dl) {
            response.setHeader("Content-Disposition", "attachment; filename=herbonautes_mission_" + id + "_contributions_valides.csv");
        }
       ///GsonBuilder gson = GsonUtils.getGsonBuilder();
       ///gson.registerTypeAdapter(Specimen.class, SpecimenWithValidAnswersJsonSerializer.get());
       ///gson.registerTypeAdapter(ContributionAnswer.class, ContributionAnswerSimpleJsonSerializer.get());
       ///gson.setPrettyPrinting();

       ///String json = gson.create().toJson(specimens);


       ///response.setContentTypeIfNotSet("application/json; charset=utf-8");
       ///renderText(json);

        Map<String, List<String>> questionLines = ContributionQuestion.getAllQuestionLines(id);

        List<String> flatLines = new ArrayList();
        for (Map.Entry<String, List<String>> lines : questionLines.entrySet()) {
            for (String line : lines.getValue()) {
                flatLines.add(lines.getKey() + " - " + line);
            }
        }

        List<String> headerList = new ArrayList(); //;
        headerList.addAll(Arrays.asList(
                "specimen - institute",
                "specimen - collection",
                "specimen - code",
                "specimen - family",
                "specimen - genus",
                "specimen - specific_epithet"));
        headerList.addAll(flatLines);
        String[] headerArray = headerList.toArray(new String[headerList.size()]);


        StringWriter sw = new StringWriter();

        CSVWriter w = new CSVWriter(sw, ';');

        w.writeNext(headerArray);
        //response.writeChunk(sw.getBuffer().toString());
        //sw.flush();
        //sw.getBuffer().setLength(0);

        int countLines = 0;

        for (Specimen specimen : specimens) {

            if (limit != null && countLines >= limit) {
                break;
            }

            countLines++;
            Logger.info("export line " + countLines);
            List<String> line = new ArrayList();
            line.add(specimen.getInstitute());
            line.add(specimen.getCollection());
            line.add(specimen.getCode());
            if (specimen.getSpecificEpithet() == null) {
                line.add(null);
                line.add(specimen.getFamily());
                line.add(specimen.getGenus());
            } else {
                line.add(specimen.getFamily());
                line.add(specimen.getGenus());
                line.add(specimen.getSpecificEpithet());
            }

            //
            // Valid answers
            List<String> answerLine = new ArrayList();


            Map<String, ContributionAnswer> answersByQuestionName = ContributionAnswer.findAllValidAnswersForSpecimenByName(specimen.id);

            for (Map.Entry<String, List<String>> lines : questionLines.entrySet()) {
                String q = lines.getKey();

                ContributionAnswer answer  = answersByQuestionName.get(q);

                Map<String, String> exportAnswer = null;
                if (answer != null) {

                    exportAnswer = answer.toExportableValue();
                    //Logger.info("%s: %s", q, exportAnswer);
                } else {
                    //Logger.info("%s : <NULL>", q, exportAnswer);
                }

                for (String l : lines.getValue()) {

                    String val = null;

                    if (exportAnswer != null) {
                        val = exportAnswer.get(l);
                    }

                    line.add(val);
                }
            }


            w.writeNext(line.toArray(new String[line.size()]));


            //response.writeChunk(sw.getBuffer().toString());
            //System.out.println(sw.getBuffer().toString());

            //sw.flush();
            //sw.getBuffer().setLength(0);



            //w.writeNext(line.toArray(new String[line.size()]));
        }


        //w.close();
        w.flush();
        renderText(sw.getBuffer().toString());

        //renderJSON(specimens, SpecimenWithValidAnswersJsonSerializer.get(), ContributionAnswerSimpleJsonSerializer.get() );
    }


    @Transactional
         public static void startRecolnatTransfer(Long id) {
        if(!Security.isConnected() || !Security.isAdmin()) {
            forbidden();
        }
        Mission mission = Mission.findById(id);
        notFoundIfNull(mission);
        // TODO IS CLOSED

        mission.recolnatTransferInProgress = true;
        mission.save();

        new RecolnatTransferJob(id).now();

        ok();
    }

    @Transactional
    public static void recolnatTransferReport(Long id) {
        if(!Security.isConnected() || !Security.isAdmin()) {
            forbidden();
        }
        Mission mission = Mission.findById(id);
        notFoundIfNull(mission);
        // TODO IS CLOSED

        RecolnatTransferReport report = RecolnatTransferReport.getReport(id);

        renderJSON(report);
    }

    @Transactional
    public static void resetContributions(Long id) {

        if(!Security.isConnected() || !Security.isLeader()) {
            forbidden();
        }

        Mission mission = Mission.findById(id);
        notFoundIfNull(mission);

        if (!Security.connected().id.equals(mission.getLeader().id)) {
            forbidden("Vous n'êtes pas le chef de cette mission");
        }

        if (mission.isPublished() || mission.isClosed()) {
            forbidden();
        }

        JPA.em()
                .createNativeQuery("delete from H_ALERT d where d.id in " +
                        "(select a.id from H_alert a inner join h_specimen s on a.specimen_id = s.id where s.mission_id = :missionId)")
                .setParameter("missionId", id)
                .executeUpdate();
        JPA.em()
                .createNativeQuery("delete from H_ACTIVITY d where d.id in " +
                        "(select a.id from H_ACTIVITY a inner join h_specimen s on a.specimen_id = s.id where s.mission_id = :missionId)")
                .setParameter("missionId", id)
                .executeUpdate();

        ContributionAnswer.delete("missionId = ?", id);
        ContributionQuestionStat.delete("missionId = ?", id);
        ContributionSpecimenStat.delete("missionId = ?", id);
        ContributionSpecimenUserStat.delete("missionId = ?", id);
        StaticAnswer.delete("missionId = ?", id);
        StatDailyMission.delete("missionId = ?", id);



        //ConflictV2Alert.delete("specimen.mission.id = ?", id);


        show(id);

    }


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Formulaire de proposition Proposition de mission
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    @Transactional(readOnly = true)
    public static void missionPropositionForm(Long id, Mission mission) {
        if(!Security.isConnected() || Security.connectedUser().getLevel() < Herbonautes.get().missionProposalMinLevel) {
            forbidden();
        }
        if(id != null) {
            mission = Mission.findById(id);
        }

        if(mission == null) {
            mission = new Mission();
        }

        List<String> langs = Play.langs;
        render(langs, mission);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Création de la mission proposée
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    /**
     * Mission proposition creation
     * @param mission
     * @param specimensFile
     * @param image
     * @param bigImage
     * @param oneCountry
     * @param countryId
     */
    @Transactional()
    public static void propose(@Valid Mission mission, File specimensFile, File image, File bigImage, boolean oneCountry,
                              Long countryId) {

        if(!Security.isConnected() || Security.connectedUser().getLevel() < Herbonautes.get().missionProposalMinLevel) {
            forbidden();
        }
        String label = mission.getPrincipalTagString();
        Tag tag = Tag.findByLabel(label);
        boolean createTag = tag == null;
        if (validation.hasErrors() || (!createTag && mission.getId() == null)) {
            params.flash();
            validation.keep();
            if(tag != null)
                validation.current().addError("mission.principalTagString", play.i18n.Messages.get("tag.already.used"));
            missionPropositionForm(mission == null || mission.getId() == null ? null : mission.getId(), mission);
        }
        mission.setLeader(Security.connected());
        mission.setProposition(true);
        mission.setPropositionSubmitted(true);
        if(createTag) {
            tag = new Tag();
            tag.setTagLabel(label);
            tag.setTagType(TagType.MISSION);
            tag.setPublished(false);
            tag.save();
            tag.refresh();
        }
        mission.setPrincipalTag(tag);
        mission.save();
        mission.refresh();
        if(createTag){
            TagLink tagLink = new TagLink();
            tagLink.setLinkType(TagLinkType.MISSION);
            tagLink.setTagId(tag.getId());
            tagLink.setTargetId(mission.getId());
            tagLink.setPrincipal(true);
            tagLink.create();
        }
        //InspectorChain.get().inspect(Event.MISSION_PROPOSED, Security.connectedUser());
        flash.success(Messages.get("mission.proposition.submit", mission.getTitle()));
        Mails.mailAfterProposedMission(mission);
        //show(mission.id);
        //redirect("/home");
        index();
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Accès à la page de validation de proposition de mission
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    @Transactional(readOnly = true)
    public static void displayPropositionValidationForm(Long id) {
        if(Security.isConnected() && !Security.connectedUser().isTeam()) {
            forbidden();
        }
        List<String> langs = Play.langs;
        Mission mission = Mission.findById(id);
        if(mission == null) {
            flash.success(Messages.get("mission.proposition.already.rejected"));
            redirect("/home");
        } else  if(!mission.isProposition()) {
            flash.success(Messages.get("mission.proposition.already.validated", mission.getValidator().getLogin()));
            redirect("/home");
        } else {
            renderTemplate("Missions/validateProposition.html", mission, langs);
        }
    }

    @Transactional()
    public static void validateProposition(Long id) {
        Security.forbiddenIfNotTeam();
        Mission mission = Mission.findById(id);
        mission.setProposition(false);
        mission.setPropositionValidated(true);
        mission.setValidator(Security.connectedUser());
        User leader = mission.getLeader();
        leader.setLeader(true);
        leader.save();
        mission.getPrincipalTag().setPublished(true);
        mission.getPrincipalTag().save();
        mission.save();
        if(mission.getSpecimens() != null && !mission.getSpecimens().isEmpty()) {
            Specimen.updatePropositionSpecimensDates(id);
            Tag.publishPropositionSpecimensTags(id);
        }
        flash.success(Messages.get("mission.proposition.validated", mission.getTitle()));
        Mails.mailAfterProposedMissionValidation(leader, mission);
        InspectorChain.get().inspect(Event.MISSION_PROPOSITION_ACCEPTED, mission, leader);
        index();
    }

    @Transactional()
    public static void reject(Long id) {
        Security.forbiddenIfNotTeam();
        Mission mission = Mission.findById(id);
        Mission.rejectMissionProposition(mission);
        flash.success(Messages.get("mission.proposition.rejected", mission.getTitle()));
        index();
    }

    @Transactional(readOnly = true)
    public static void propositionsList() {
        Security.forbiddenIfNotTeam();
        //List<Mission> propositions = Mission.getMissionsPropositions();
        List<Mission> propositions = Mission.getPendingPropositions();
        render(propositions);
    }

    @Transactional(readOnly = true)
    public static void propositionCartValidation(Long id) {
        Security.forbiddenIfNotTeam();
        if(id == null) {
            error();
        }
        Mission mission = Mission.findById(id);
        renderTemplate("Missions/settingsCart.html", mission);
    }

    @Transactional()
    public static void propositionCartInit(@Valid Mission mission) {
        if(!Security.isConnected() || Security.connectedUser().getLevel() < Herbonautes.get().missionProposalMinLevel) {
            forbidden();
        }

        String label = mission.getPrincipalTagString();
        Tag tag = Tag.findByLabel(label);
        if (validation.hasErrors() || (mission.getId() == null && tag != null)) {
            params.flash();
            validation.keep();
            if(tag != null)
                validation.current().addError("mission.principalTagString", play.i18n.Messages.get("tag.already.used"));
            missionPropositionForm(null, mission);
        }

        if(mission.getId() == null) {
            mission.setLeader(Security.connected());
            mission.setProposition(true);
            mission.setPropositionValidated(false);
            tag = new Tag();
            tag.setTagLabel(label);
            tag.setTagType(TagType.MISSION);
            tag.setPublished(false);
            tag.save();
            tag.refresh();
            mission.setPrincipalTag(tag);
            mission.save();
            mission.refresh();
            TagLink tagLink = new TagLink();
            tagLink.setLinkType(TagLinkType.MISSION);
            tagLink.setTagId(tag.getId());
            tagLink.setTargetId(mission.getId());
            tagLink.setPrincipal(true);
            tagLink.create();
        } else {
            mission = Mission.findById(mission.getId());
        }
        renderTemplate("Missions/settingsCart.html", mission);
    }

    public static void settingsCart(Long id) {
        Security.forbiddenIfNotLeader();

        Mission mission = Mission.findById(id);
        render(mission);
    }

    public static void getCartItems(Long id) {
        List<MissionCartQuery> queries = MissionCartQuery.find("missionId = ?", id).fetch();
        renderJSON(queries);
    }

    public static void cartUploadFile(Long id, File codeFile) throws IOException {

        String codes = FileUtils.readFileToString(codeFile);

        if (codes == null || codes.split("\n").length > Herbonautes.get().specimenFileLimit) {
            forbidden();
        }

        TextFile textFile = TextFile.createFile(codeFile.getName(), codes);

        MissionCartQuery cartQuery = new MissionCartQuery();

        cartQuery.setMissionId(id);
        cartQuery.setTextFile(textFile);
        cartQuery.setHits((long) codes.split("\n").length);
        cartQuery.setSync(false);
        cartQuery.save();

        renderJSON(cartQuery);
    }

    public static void saveCartItem(Long id) throws IOException {
        Mission mission = Mission.findById(id);
        if(mission.isProposition()) {
            if(!Security.isConnected() || Security.connectedUser().getLevel() < Herbonautes.get().missionProposalMinLevel)
                forbidden();
        } else {
            Security.forbiddenIfNotLeader();
        }

        JsonNode json = mapper.readTree(request.body);
        MissionCartQuery query = Json.parseCartQuery(json);
        query.setMissionId(id);

        MissionCartQuery existing = MissionCartQuery.findExistingSimilar(query);

        if (existing == null) {
            existing = query;
            existing.setSync(false); // Sync false car nouvelle requete
        } else {
            boolean sync = true;
            if (existing.getAllSelected() != query.getAllSelectedDraft()) {
               sync = false;
            }
            if (!CollectionUtils.isEqualCollection(existing.getSelection(), query.getSelectionDraft())) {
               sync = false;
            }
            existing.setSync(sync);
        }

        existing.setHits(query.getHits());
        existing.setAllSelectedDraft(query.getAllSelectedDraft());
        existing.setSelectionDraft(query.getSelectionDraft());
        //existing.setSync(false);


        existing.save();

        renderJSON(existing);
    }

    @Transactional(readOnly = true)
    public static void isLoadingCart(Long id) {
        Mission mission = Mission.findById(id);
        if(mission.isProposition()) {
            if(!Security.isConnected() || Security.connectedUser().getLevel() < Herbonautes.get().missionProposalMinLevel)
                forbidden();
        } else {
            Security.forbiddenIfNotLeader();
        }
        Boolean loading = Mission.cartLoading(id);
        renderJSON("{\"loading\":" + loading.toString() + "}");
    }

    @Transactional(readOnly = true)
    public static void syncCart(Long id) {
        Mission mission = Mission.findById(id);
        if(mission.isProposition()) {
            if(!Security.isConnected() || Security.connectedUser().getLevel() < Herbonautes.get().missionProposalMinLevel)
                forbidden();
        } else {
            Security.forbiddenIfNotLeader();
        }
        Boolean loading = Mission.cartLoading(id);
        if (loading) {
            Logger.error("Cart already loading for mission %d", id);
            forbidden("Already loading");
        }
        new CartJob(id).now();
        ok();
    }

    @Transactional(readOnly = true)
    public static void existingSpecimens(Long id) throws IOException {
        Mission mission = Mission.findById(id);
        if(mission.isProposition()) {
            if(!Security.isConnected() || Security.connectedUser().getLevel() < Herbonautes.get().missionProposalMinLevel)
                forbidden();
        } else {
            Security.forbiddenIfNotLeader();
        }

        JsonNode json = mapper.readTree(request.body);
        List<String> catalogNumbers = new ArrayList<String>();
        for (JsonNode node : json) {
            catalogNumbers.add(node.asText());
        }

        List<Specimen> specimens = JPA.em().createQuery("select s from Specimen s where s.code in :catalogNumbers")
                                           .setParameter("catalogNumbers", catalogNumbers)
                                           .getResultList();


        GsonBuilder gson = GsonUtils.getGsonBuilder();
        gson.registerTypeAdapter(Specimen.class, SpecimenSimpleWithMissionJsonSerializer.get());
        gson.registerTypeAdapter(Mission.class, MissionSimpleJsonSerializer.get());
        renderJSON(gson.create().toJson(specimens));

        //renderJSON(specimens, SpecimenSimpleWithMissionJsonSerializer.get(), MissionSimpleJsonSerializer.get());
    }

    @Transactional
    public static void cancelModifications(Long id) throws IOException {
        Mission mission = Mission.findById(id);
        if(mission.isProposition()) {
            if(!Security.isConnected() || Security.connectedUser().getLevel() < Herbonautes.get().missionProposalMinLevel)
                forbidden();
        } else {
            Security.forbiddenIfNotLeader();
        }

        List<MissionCartQuery> queries = MissionCartQuery.find("missionId = ?", id).fetch();
        for (MissionCartQuery query : queries) {
            query.setAllSelectedDraft(query.getAllSelectedDraft());
            ArrayList<String> selection = new ArrayList();
            selection.addAll(query.getSelection());
            query.setSelectionDraft(selection);//query.getSelection());
            query.setSync(true);
            query.save();
        }

        renderJSON(queries); //specimens, SpecimenSimpleWithMissionJsonSerializer.get(), MissionSimpleJsonSerializer.get());
    }



    /*
    public static void settingsCart(Long id) {
        Security.forbiddenIfNotLeader();

        Mission mission = Mission.findById(id);
        render(mission);
    }

    @Transactional(readOnly = true)
    public static void isLoadingCart(Long id) {
        Security.forbiddenIfNotLeader();

        Boolean loading = Mission.cartLoading(id);

        renderJSON("{\"loading\":" + loading.toString() + "}");
    }


    public static void settingsCartGet(Long id) throws IOException {
        Security.forbiddenIfNotLeader();

        Mission mission = Mission.findById(id);

        ObjectMapper mapper = new ObjectMapper();
        String json = mission.getCartJson();
        Cart cart = json != null ? mapper.readValue(json, Cart.class) : null;

        renderJSON(cart);
    }




    public static void settingsCartSave(Long id) throws IOException {
        Security.forbiddenIfNotLeader();

        Mission mission = Mission.findById(id);

        ObjectMapper mapper = new ObjectMapper();
        Cart cart = mapper.readValue(request.body, Cart.class);

        mission.setCartJson(mapper.writeValueAsString(cart));

        mission.save();

        renderJSON(cart);
    }

    public static void settingsCartValidate(Long id) throws IOException {
        Security.forbiddenIfNotLeader();

        ObjectMapper mapper = new ObjectMapper();
        Cart cart = mapper.readValue(request.body, Cart.class);

        new CartJob(id, cart, CartJob.Action.ADD).now();

        renderJSON(cart);
    }


    public static void settingsCartRemove(Long id) throws IOException {
        Security.forbiddenIfNotLeader();

        ObjectMapper mapper = new ObjectMapper();
        Cart cart = mapper.readValue(request.body, Cart.class);
        new CartJob(id, cart, CartJob.Action.REMOVE).now();

        renderJSON(cart);
    }
    */
}
