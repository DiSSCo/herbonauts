package models;


import conf.Herbonautes;
import helpers.NumberUtils;
import models.User.UserContributionReport;
import models.comments.MissionComment;
import models.contributions.Contribution;
import models.contributions.DateContribution;
import models.contributions.GeolocalisationContribution;
import models.questions.*;
import models.stats.StatContributionMission;
import models.tags.Tag;
import models.tags.TagLink;
import models.tags.TagLinkType;
import models.tags.TagType;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.NoTransaction;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Exchanger;

/**
 * Mission
 */
@Entity
@Table(name = "H_MISSION")
public class Mission extends DatedModificationsModel<Mission> {

    public boolean published;
    public boolean proposition;
    @Column(name="proposition_validated")
    public boolean propositionValidated;
    @Column(name="proposition_submitted")
    public boolean propositionSubmitted;
    public boolean closed;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_UPDATE_DATE")
    public Date lastUpdateDate;
    @Column(name = "RECOLNAT_TRANSFER_PROGRESS")
    public boolean recolnatTransferInProgress;
    @Column(name = "RECOLNAT_TRANSFER_DONE")
    public boolean recolnatTransferDone;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "RECOLNAT_TRANSFER_DATE")
    public Date recolnatTransferDate;
    public boolean reportPublished;
    @Required
    @Column(columnDefinition = "VARCHAR(1000)")
    private String title;
    @Required
    @Column(columnDefinition = "VARCHAR(1000 char)")
    private String shortDescription;
    @Required
    private String lang;

    private Integer goal = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    private Country country;
    @ManyToOne(fetch = FetchType.LAZY)
    private User leader;
    @ManyToOne(fetch = FetchType.LAZY)
    private User validator;


    // RECOLNAT
    @Required
    private Date openingDate;
    private boolean loading;
    @Column(name = "LOADING_CART")
    private boolean loadingCart;

    // CART
    // @Column(name = "LOADING_CART")
    // private boolean loadingCart;
    // @Lob
    // @Column(name="CART_JSON", columnDefinition = "CLOB")
    // private String cartJson;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "MISSION_ID")
    private List<MissionCartQuery> cartQueries;
    // LOB
    @Lob
    @Column(columnDefinition = "CLOB")
    private String presentation;
    // LOB
    @Lob
    @Column(columnDefinition = "CLOB")
    private String report;
    private Long imageId;
    private Long bigImageId;
    private boolean hasImage;
    private boolean hasBigImage;
    private Integer priority = 0;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "mission_user", joinColumns = {@JoinColumn(name = "mission_id", nullable = false)},
            inverseJoinColumns = {@JoinColumn(name = "user_id", nullable = false)})
    private List<User> users;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "mission_banned_user", joinColumns = {@JoinColumn(name = "mission_id", nullable = false)},
            inverseJoinColumns = {@JoinColumn(name = "user_id", nullable = false)})
    private List<User> bannedUsers;
    /*@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "mission_specimen", joinColumns = {@JoinColumn(name = "mission_id", nullable = false)},
      inverseJoinColumns = {@JoinColumn(name = "specimen_id", nullable = false)})  */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "mission")
    private List<Specimen> specimens;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "missionId", cascade = CascadeType.MERGE)
    private List<ContributionQuestion> questions;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="principal_tag")
    private Tag principalTag;
    @Transient
    @Required
    private String principalTagString;

    public static void clearCache(Long missionId) {
        Cache.delete("mission_contributions_count" + missionId);
    }

    // Actualités (Annonces pour les missions d'un utilisateur)
    // TODO mission ouverte
    public static List<Announcement> getAnnouncementsForUser(Long userId) {
        return JPA
                .em()
                .createNativeQuery(
                        "SELECT A.* from H_ANNOUNCEMENT A " + "where A.PUBLISHED = 1 AND A.MISSION_ID in "
                                + "(SELECT MISSION_ID from mission_user WHERE USER_ID = ?) " + "ORDER BY A.ID DESC", Announcement.class)
                .setParameter(1, userId).getResultList();
    }

    public static List<Mission> getClosedMissions(boolean withUnpublished) {
        if (withUnpublished) {
            return Mission.find("closed = true order by priority").fetch();
        } else {
            return Mission.find("closed = true and published = true order by priority").fetch();
        }
    }

    public static long getCurrentMissionsCount() {
        return Mission.count("openingDate <= CURRENT_DATE and closed = false and published = true");
    }

    public static List<Mission> getFirstMissions(Integer limit) {
        return Mission.find("openingDate <= CURRENT_DATE and published = true " + "and closed = false order by priority")
                .fetch(limit);
    }

    public static List<Mission> getMissions(boolean withUnpublished) {
        if (withUnpublished) {
            return Mission.find("order by priority").fetch();
        } else {
            return Mission.find("published = true order by priority").fetch();
        }

    }

    //
    public static List<Mission> getNextMissions(boolean withUnpublished) {
        if (withUnpublished) {
            return Mission.find("openingDate > CURRENT_DATE and closed = false order by priority").fetch();
        } else {
            return Mission.find("openingDate > CURRENT_DATE and published = true and closed = false order by priority")
                    .fetch();
        }

    }

    public static List<Mission> getPresentMissions(boolean withUnpublished) {
        ;
        if (withUnpublished) {
            return Mission.find("openingDate <= CURRENT_DATE and closed = false order by priority").fetch();
        } else {
            return Mission.find("openingDate <= CURRENT_DATE and published = true and closed = false order by priority")
                    .fetch();
        }
    }

    public static Mission getRandomMission() {
        Long count = getCurrentMissionsCount();

        if (count.intValue() > 0) {
            int start = new Random().nextInt(count.intValue());

            return JPA
                    .em()
                    .createQuery(
                            "select m from Mission m " + "where openingDate <= CURRENT_DATE and closed = false and published = true",
                            Mission.class).setFirstResult(start).setMaxResults(1).getSingleResult();
        } else {
            return null;
        }
    }

    public static List<DateContribution> getValidatedDates(Long missionId) {

        List<DateContribution> contributions =
                JPA
                        .em()
                        .createNativeQuery(
                                "select c.* from H_CONTRIBUTION c "
                                        + "inner join H_CONTRIBUTION_REPORT cr on cr.validatedcontribution_id = c.id "
                                        + "inner join mission_specimen ms on ms.specimen_id = c.specimen_id "
                                        + "where cr.type = 'DATE' and ms.mission_id = ?", DateContribution.class).setParameter(1, missionId)
                        .getResultList();

        List<DateContribution> contributionsWithDate = new ArrayList<DateContribution>();
        for (DateContribution dateContribution : contributions) {
            if (!dateContribution.isNotPresent()) {
                contributionsWithDate.add(dateContribution);
            }
        }

        return contributionsWithDate;
    }

    public static List<GeolocalisationContribution> getValidatedGeolocalisations(Long missionId) {

        List<GeolocalisationContribution> contributions =
                JPA
                        .em()
                        .createNativeQuery(
                                "select c.* from H_CONTRIBUTION c "
                                        + "inner join H_CONTRIBUTION_REPORT cr on cr.validatedcontribution_id = c.id "
                                        + "inner join mission_specimen ms on ms.specimen_id = c.specimen_id "
                                        + "where cr.type = 'GEOLOCALISATION' and ms.mission_id = ?", GeolocalisationContribution.class)
                        .setParameter(1, missionId).getResultList();

        return contributions;
    }

    public static boolean isLoading(Long missionId) {
        return Cache.get("mission_loading_" + missionId) != null;
    }

    public static Mission lastContributedMission(String login) {
        return Mission.find(
                "select c.mission from Contribution c where " + "c.user.login = ? and c.canceled = false order by c.id desc",
                login).first();

    }

    public static MissionSimple lastContributedSimpleMission(Long userId) {

        List<MissionSimple> missions = JPA.em().createNativeQuery(

                "SELECT M.* FROM HV_MISSION_SIMPLE M INNER JOIN H_CONTRIBUTION_ANSWER A ON A.MISSION_ID = M.ID " +
                        "WHERE A.USER_ID = ? ORDER BY CREATED_AT DESC", MissionSimple.class)
                .setParameter(1, userId)
                .setMaxResults(1)
                .getResultList();

        if (missions.size() > 0) {
            return missions.get(0);
        }
        return null;

        //return Mission.find(
        //        "select c.mission from Contribution c where " + "c.user.login = ? and c.canceled = false order by c.id desc",
        //        login).first();

    }

    // Pour le chargement des specimens
    public static void markLoading(Long missionId) {
        Cache.set("mission_loading_" + missionId, Boolean.TRUE);
    }

    public static List<Mission> search(String q, int max) {
        return search(q, max, null);
    }

    public static List<Mission> search(String q, int max, Integer page) {
        if (q == null) {
            return new ArrayList<Mission>();
        }

        String likeValue = q.toLowerCase().replace(' ', '%');

        Integer realPage = (page == null ? 1 : page);

        List<Mission> missions = Mission.find("lower(title) like ?", "%" + likeValue + "%").fetch(realPage, max);

        return missions;
    }

    public static void unmarkLoading(Long missionId) {
        Cache.delete("mission_loading_" + missionId);
    }

    public static boolean exist(Long missionId) {

        Long exist = count("select count(*) from Mission m where m.id = ?", missionId);

        if (exist == 1) return true;
        return false;
    }

    public static Long getContributionsCount(Long missionId) {
        Long count = Cache.get("mission_contributions_count" + missionId, Long.class);

        //Long count = null;
        if (count == null) {
            if (ContributionSpecimenStat.count("select count(c) from ContributionSpecimenStat c where c.answerCount is not null and c.missionId = ?", missionId) > 0) {
                count = ContributionSpecimenStat.count("select sum(c.answerCount) " +
                        "from ContributionSpecimenStat c " +
                        "where c.missionId = ?", missionId);
            }  else {
                count = 0l;
            }
            //if (count =)
            Cache.set("mission_contributions_count" + missionId, count);
        }
        return count;
    }

    public static boolean isUserInTop(Long missionId, Long userId) {
        List<StatContributionMission> stats =
                StatContributionMission.find("missionId = ? and user is not null order by answerCount desc, user.id", missionId)
                        .fetch(1, 3);

        for (StatContributionMission stat : stats) {
            if(stat.getUser().id.equals(userId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Random draw - step 1 : choose the first specimen S from the mission M that doesn't have a record on the
     * SEEN_SPECIMEN table.
     *
     * @param Long mission_id
     * @return Specimen specimen
     * @author Marie-Elise Lecoq
     */

    public static Specimen getRandomSpecimenStep1(Long mission_id) {
        List<Specimen> specimens =
                JPA
                        .em()
                        .createNativeQuery(
                                "SELECT *  " +
                                        "FROM ( " +
                                        "     SELECT SP.* FROM H_SPECIMEN SP " +
                                        "     WHERE SP.MISSION_ID = ? AND  " +
                                        "     SP.ID NOT IN (SELECT SSP.SPECIMEN_ID FROM SEEN_SPECIMEN SSP) AND  " +
                                        "     SP.COMPLETE=0  " +
                                        "     ORDER BY ALEA  " +
                                        "     )  " +
                                        "WHERE ROWNUM=1; ",
                                Specimen.class).setParameter(1, mission_id).getResultList();

        if (specimens.size() == 0) return null;
        return specimens.get(0);
    }

    /**
     * Random draw - step 2 : choose the first specimen S from the mission M that doesn't have a record on the
     * SEEN_SPECIMEN table for the specific user U.
     *
     * @param Long mission_id
     * @param Long user_id
     * @return Specimen specimen
     * @author Marie-Elise Lecoq
     */

    public static Specimen getRandomSpecimenStep2(Long mission_id, Long user_id) {
        List<Specimen> specimens =
                JPA
                        .em()
                        .createNativeQuery(
                                "SELECT * FROM " +
                                        "(   " +
                                        "    SELECT  SP.* " +
                                        "    FROM    H_SPECIMEN SP " +
                                        "    WHERE   SP.MISSION_ID = 1  " +
                                        "            AND SP.ID NOT IN (SELECT SSP.SPECIMEN_ID " +
                                        "                              FROM SEEN_SPECIMEN SSP " +
                                        "                              WHERE SSP.USER_ID = ?) " +
                                        "            AND SP.COMPLETE = ? " +
                                        "    ORDER BY SP.ALEA " +
                                        ") " +
                                        "WHERE ROWNUM=1;",
                                Specimen.class)
                        .setParameter(1, user_id)
                        .setParameter(2, mission_id)
                        .getResultList();
        if (specimens.size() == 0) return null;
        return specimens.get(0);
    }

    /**
     * Random draw - step 0 : choose the first specimen S from the mission M for a user not connected
     *
     * @param Long mission_id
     * @return Specimen specimen
     * @author Marie-Elise Lecoq
     */
    public static Specimen getRandomSpecimenNotConnected(Long missionId) {

        String query = "SELECT SP.* FROM H_SPECIMEN SP  WHERE " +
                       "SP.complete = 0 and  SP.MISSION_ID = ? " +
                       "order by SP.ALEA";
        //" order by dbms_random.value ";

        List<Specimen> specimens =
                JPA
                        .em()
                        .createNativeQuery(query,
                                Specimen.class)
                        .setParameter(1, missionId)
                        .setMaxResults(1)
                        .getResultList();

        if (specimens.size() == 0) return null;
        return specimens.get(0);
    }

    public static void markSeenV2(Long missionId, Long specimenId, Long userId) {
        ContributionSpecimenUserStat stat =
                ContributionSpecimenUserStat.findByUserAndSpecimen(userId, specimenId);
        if (stat == null) {
            stat = new ContributionSpecimenUserStat();
            stat.setUserId(userId);
            stat.setMinUnansweredLevel(1l);
            stat.setMissionId(missionId);
            stat.setSpecimen(new Specimen(specimenId));
        }
        stat.setMarkedSeen(true);
        stat.save();

        ContributionSpecimenStat specimenStat = ContributionSpecimenStat.findById(specimenId);
        if (specimenStat == null) {
            Logger.info("Stat null pour " + specimenId + " : create blank stat");
            specimenStat = new ContributionSpecimenStat();
            specimenStat.setSpecimen(new Specimen(specimenId));
            specimenStat.setMissionId(missionId);
            specimenStat.setMinUsefulLevel(1l);
            specimenStat.save();
        }
    }


    /*public static Long getSpecimenCount(Long missionId) {

    }*/

    public static void resetSeenSpecimenV2ForUser(Long missionId, User user) {
        String update = "update H_CONTRIBUTION_SP_USER_STAT set marked_seen = 0 " +
        "where mission_id = :missionId and user_id = :userId";
        JPA.em().createNativeQuery(update)
                .setParameter("missionId", missionId)
                .setParameter("userId", user.id)
                .executeUpdate();

    }

    public static Specimen getRandomSpecimenV2ForAnonymous(Long missionId) {
        String query = " select " +
                "         * " +
                "         from h_specimen s " +
                " where " +
                " s.TILED = 1 and s.TILINGERROR != 1 and " +
                " s.mission_id = :missionId and " +
                " id not in (  " +
                "              " +
                "         select  "  +
                "         ss.specimen_id  " +
                "         from " +
                "         h_contribution_specimen_stat ss  " +
                "         where " +
                "         ss.mission_id = :missionId and " +
                "         (   " +                                   // -- utilisateur n'est pas passé sur la planche
                "  " +                                        // -- utilisateur ne l'a pas marqué inutilisable
                " ss.unusable_validated = 1 " +                                      // -- pas validéé comme inutilisable
                " or ss.min_useful_level is null " +                                    // -- identique specimen complet
                "  " +                                        // -- niveau trop haut
                " ) " + // -- utilisateur a répondu à tout
                " ) order by alea";

        List<Specimen> specimens = JPA.em()
                .createNativeQuery(query, Specimen.class)
                .setParameter("missionId", missionId)
                .setMaxResults(1)
                .getResultList();

        if (specimens.size() == 0) return null;
        return specimens.get(0);
    }

    public static Specimen getRandomSpecimenV2ForUser(Long missionId, User user) {

        String query = " select " +
                       "         * " +
                       "         from h_specimen s " +
                       " where " +
                       " s.TILED = 1 and s.TILINGERROR != 1 and " +
                       " s.mission_id = :missionId and " +
                       " id not in (  " +
                       "              " +
                       "         select  "  +
                       "         ss.specimen_id  " +
                       "         from " +
                       "         h_contribution_specimen_stat ss  " +
                       "         left outer join H_CONTRIBUTION_SP_USER_STAT sus " +
                       "         on ss.specimen_id = sus.specimen_id and user_id = :userId " +
                       "         where " +
                       "         ss.mission_id = :missionId and " +
                       "         (   sus.marked_seen = 1 " +                                   // -- utilisateur n'est pas passé sur la planche
                       " or sus.marked_unusable = 1 " +                                        // -- utilisateur ne l'a pas marqué inutilisable
                       " or ss.validated = 1 " +                                               // -- specimen complet
                       " or ss.unusable_validated = 1 " +                                      // -- pas validéé comme inutilisable
                       " or ss.min_useful_level is null " +                                    // -- identique specimen complet
                       " or ss.min_useful_level > :userLevel " +                                        // -- niveau trop haut
                       " or (sus.user_id is not null and sus.min_unanswered_level is null) " +      // -- utilisateur a répondu à tout
                       " or (sus.user_id is not null and sus.min_unanswered_level > :userLevel) " + // -- utilisateur n'a pas le niveau pour les questions qu'il reste
                       " ) " +
                       "  " +
                       " )  order by alea";

        List<Specimen> specimens = JPA.em()
                                      .createNativeQuery(query, Specimen.class)
                                      .setParameter("missionId", missionId)
                                      .setParameter("userId", user.id)
                                      .setParameter("userLevel", user.getLevel())
                                      .setMaxResults(1)
                                      .getResultList();

        if (specimens.size() == 0) return null;
        return specimens.get(0);
    }

    /**
     * Fonction qui ajoute dans SeenSpecimen les spécimens dans la mission pour l'utilisateur x
     *
     * @param missionId
     * @param userId
     * @return specimen choisi
     */
    public static Specimen addSeenSpecimen(Long missionId, Long userId) {
        Specimen specimenDrawedStep1 = Mission.getRandomSpecimenStep1(missionId);
        if (specimenDrawedStep1 == null) {
            Specimen specimenDrawedStep2 = Mission.getRandomSpecimenStep2(missionId, userId);
            if (specimenDrawedStep2 == null) {
                Specimen specimenDrawedStep3 = getRandomSpecimenNotConnected(missionId);
                return specimenDrawedStep3;
            } else {
                SeenSpecimen seenSpecimenStep2 = new SeenSpecimen();
                seenSpecimenStep2.setMission_id(missionId);
                seenSpecimenStep2.setSpecimen_id(specimenDrawedStep2.id);
                seenSpecimenStep2.setUser_id(userId);
                seenSpecimenStep2.save();
                return specimenDrawedStep2;
            }
        } else {
            SeenSpecimen seenSpecimenStep1 = new SeenSpecimen();
            seenSpecimenStep1.setMission_id(missionId);
            seenSpecimenStep1.setSpecimen_id(specimenDrawedStep1.id);
            seenSpecimenStep1.setUser_id(userId);
            seenSpecimenStep1.save();
            return specimenDrawedStep1;
        }
    }

    public static boolean cartLoading(Long missionId) {
        return JPA.em().createQuery("select m.loadingCart from Mission m where m.id = ?", Boolean.class)
                .setParameter(1, missionId)
                .getSingleResult();
    }

// STATS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public static Long imageId(Long missionId) {
        return (Long)
                JPA.em().createNativeQuery("SELECT IMAGEID FROM H_MISSION WHERE ID = :missionId")
                .setParameter("missionId", missionId)
                .getSingleResult();
    }

// COUNTS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /*
    public Long getContributionsCount() {
        Long count = Cache.get("mission_contributions_count" + this.id, Long.class);
        if (count == null) {
            count =
                    count("select count(c) from Contribution c join c.mission m where c.canceled = false and m.id = ?", this.id);
            Cache.set("mission_contributions_count" + this.id, count);
        }
        return count;
    }
    */

    public static Long bigImageId(Long missionId) {
        Number n = (Number)
                JPA.em().createNativeQuery("SELECT BIGIMAGEID FROM H_MISSION WHERE ID = :missionId")
                        .setParameter("missionId", missionId)
                        .getSingleResult();
        if (n != null) {
            return n.longValue();
        }
        return null;
    }

    public static List<Mission> getPublishedMissionsByTag(String tagLabel) {
        StringBuilder request = new StringBuilder();
        request.append("SELECT M.id, M.title, M.shortDescription, M.lang, M.goal, M.country_id, M.leader_id, M.validator_id, M.openingDate, M.published, M.proposition, M.proposition_validated, M.proposition_submitted, M.closed, M.loading, M.last_update_date, M.recolnat_transfer_progress, M.recolnat_transfer_done, M.recolnat_transfer_date,  M.loading_cart, null as presentation, null as report, M.reportPublished, M.imageId, M.bigImageId, M.hasImage, M.hasBigImage, M.priority, m.principal_tag, m.principal_tag FROM H_MISSION M ");
        request.append("INNER JOIN H_TAGS_LINKS TL ON TL.target_id = M.id ");
        request.append("INNER JOIN H_TAGS T ON t.id = tl.tag_id ");
        request.append("WHERE TL.link_type = '").append(TagType.MISSION).append("' ");
        request.append("AND T.label = '").append(tagLabel).append("' ");
        request.append("AND M.published = 1 ");
        request.append("ORDER BY M.last_update_date desc");
        return JPA.em().createNativeQuery(request.toString(), Mission.class)
                .getResultList();
    }

    public static Mission findByPrincipalTag(String tagLabel) {
        return Mission.find("principalTag.tagLabel = ?1 and principalTag.tagType = ?2", tagLabel, TagType.MISSION).first();
    }

    public static List<Mission> getMissionsPropositions() {
        return Mission.find("proposition = true").fetch();
    }

    public static List<Mission> getPendingPropositions() {
        return Mission.find("proposition = true and propositionSubmitted = true").fetch();
    }

    public static long countMissionsPropositions() {
        return Mission.count("proposition = true");
    }

    public static long countPendingPropositions() {
        return Mission.count("proposition = true and propositionSubmitted = true");
    }

    public static void rejectMissionProposition(Mission mission) {

        //mission.getPrincipalTag().delete();

        if(mission.getSpecimens() != null && !mission.getSpecimens().isEmpty()) {
            Tag.deletePropositionSpecimensTags(mission.getId());
            TagLink.deleteByLinkTypeAndTargetId(TagLinkType.MISSION, mission.getId());
        }
        Specimen.deleteByMissionId(mission.getId());
        //mission.propositionValidated = false;
        mission.delete();
    }

    public static List<Mission> getLostMissionsPropositions() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return Mission.find("proposition = true and propositionSubmitted = false and lastUpdateDate < ?1" , calendar.getTime()).fetch();
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    @Override
    @PrePersist
    @PreUpdate
    public void setLastUpdateDate() {
        this.lastUpdateDate = new Date();
    }

    public String getPrincipalTagString() {
        return principalTag != null && principalTag.getTagLabel() != null ? principalTag.getTagLabel() : principalTagString;
    }

    public void setPrincipalTagString(String principalTagString) {
        this.principalTagString = principalTagString;
    }

    // TODO vérifier si membre
    public void addComment(User user, String text) {
        MissionComment comment = new MissionComment();
        comment.setUser(user);
        comment.setMission(this);
        comment.setText(text);
        comment.save();
    }

    public void ban(String login) {
        User user = User.findByLogin(login, false);
        getUsers().remove(user);
        getBannedUsers().add(user);
        save();
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // JPA Hooks
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    @PostPersist
    public void clearCaches() {
    }

    public Long getAllSpecimensCount() {
        /*Long count = Cache.get("all_mission_specimens_count" + this.id, Long.class);
        if (count == null) {
            count = count("select count(s) from Mission m join m.specimens s where m.id = ?", this.id);
            Cache.set("all_mission_specimens_count" + this.id, count);
        }
        return count;    */
        return count("select count(s) from Specimen s where s.mission.id = ?", this.id);
    }

    public List<Announcement> getAnnouncements() {
        return Announcement.find("mission.id = ?", this.id).fetch();
    }

    public List<User> getBannedUsers() {
        return bannedUsers;
    }

    public void setBannedUsers(List<User> bannedUsers) {
        this.bannedUsers = bannedUsers;
    }

    public Long getBigImageId() {
        return bigImageId;
    }

    public void setBigImageId(Long bigImageId) {
        this.bigImageId = bigImageId;
    }

    @NoTransaction
    public Long getCompletedSpecimensCount() {
        return count("select count(c) " +
                "from ContributionSpecimenStat c " +
                "where (c.validated = true or unusableValidated = true) and c.missionId = ?", this.id);
        //return count("select count(s) from Mission m join m.specimens s where s.complete = true and m.id = ?", this.id);
    }

    // V2
    public Long getContributionsCount() {
        Long count = Cache.get("mission_contributions_count" + this.id, Long.class);

        //Long count = null;
        if (count == null) {
            if (ContributionSpecimenStat.count("select count(c) from ContributionSpecimenStat c where c.answerCount is not null and c.missionId = ?", this.id) > 0) {
                count = ContributionSpecimenStat.count("select sum(c.answerCount) " +
                        "from ContributionSpecimenStat c " +
                        "where c.missionId = ?", this.id);
            }  else {
                count = 0l;
            }
            //if (count =)
            Cache.set("mission_contributions_count" + this.id, count, "5mn");
        }
        Logger.info("Count %s : %d", this, count);
        return count;
    }

    public Long getConflictsCount() {
        return ContributionQuestionStat.count("missionId = ? and conflicts = true", this.id);
    }
  
  /*public static Specimen getRandomSpecimenStep2(Long mission_id, Long user_id){
      List<Specimen> specimens =
		      JPA
		        .em()
		        .createNativeQuery(
		          "select * from (SELECT SP.* FROM H_SPECIMEN SP, " 
		        	+ "MISSION_SPECIMEN MS "
		        	+ "WHERE SP.id = MS.specimen_id and "
		            + "SP.id not in (SELECT SSP.SPECIMEN_ID from SEEN_SPECIMEN SSP "
		            + "WHERE SSP.MISSION_ID = MS.MISSION_ID and SSP.user_id = ?) and " 
		            + "SP.complete = 0 and "
		            + "MS.mission_id = ?" 
		            + "order by SP.alea) where rownum=1 ",
		          Specimen.class)
		        .setParameter(1, user_id)
		        .setParameter(2, mission_id)
		        .getResultList();
	  if (specimens.size() == 0) return null;
	  return specimens.get(0);
  }*/

    @NoTransaction
    public Long getContributionsCountForUser(String login) {
        Long count =
                count("select count(c) " + "from Contribution c " + "where c.canceled = false " + "and c.user.login = ? "
                        + "and c.mission.id = ?", login, this.id);
        return count;
    }

    @NoTransaction
    public Long getContributionsCountForUser(Long id) {

        Long count =
                count("select count(c) " + "from ContributionAnswer c " + "where c.deleted != true " +
                        "and c.userId = ? " +
                        "and c.missionId = ? "
                        + "", id, this.id);
        return count;
    }

    public List<UserContributionReport> getContributorRatings(int from, int to) {
    /*
     * List<User> users = JPA.em()
     * .createNativeQuery(
     * "SELECT HU.* FROM H_USER HU, (" +
     * "SELECT U.ID U_ID, count(*) CNT " +
     * "FROM H_CONTRIBUTION C " +
     * "INNER JOIN H_USER U ON U.ID = C.USER_ID " +
     * "WHERE C.CANCELED = 0 and C.MISSION_ID = ? " +
     * "GROUP BY U.ID " +
     * "ORDER BY CNT DESC) T WHERE T.U_ID = HU.ID", User.class)
     * .setParameter(1, this.id)
     * .setFirstResult(from)
     * .setMaxResults(to - from)
     * .getResultList();
     */

        List<Object[]> userIds =
                JPA
                        .em()
                        .createNativeQuery(
                                "SELECT HU.ID, CNT FROM H_USER HU, ( " + "SELECT U.ID U_ID, count(*) CNT " + "FROM H_CONTRIBUTION C "
                                        + "INNER JOIN H_USER U ON U.ID = C.USER_ID " + "WHERE C.CANCELED = 0 and C.MISSION_ID = ? "
                                        + "GROUP BY U.ID) T WHERE T.U_ID = HU.ID ORDER BY CNT DESC").setParameter(1, this.id).setFirstResult(from)
                        .setMaxResults(to - from).getResultList();

        List<UserContributionReport> reports = new ArrayList<User.UserContributionReport>();

        if (users != null) {
            for (Object[] userId : userIds) {
                BigInteger bigIntId = NumberUtils.toBigInteger(userId[0]);
                User user = User.findById(bigIntId.longValue());
                reports.add(user.getContributionReport(this.id));
            }
        }

        return reports;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public List<Announcement> getCurrentPublishedAnnouncements() {
        return Announcement.find(
                "date < CURRENT_DATE " + "and published = true " + "and mission.id = ? " + "order by date desc", this.id).fetch();
    }

    @NoTransaction
    public Long getDisplayedSpecimensCount() {

        /*Long count = Cache.get("mission_displayed_specimens_count" + this.id, Long.class);
        count = count("select count(s) from Mission m join m.specimens s where s.displayed = true and m.id = ?", this.id);

        if (count == null) {
            count = count("select count(s) from Mission m join m.specimens s where s.displayed = true and m.id = ?", this.id);
            Cache.set("mission_displayed_specimens_count" + this.id, count);
        }     */

        Long count = count("select count(c) from ContributionSpecimenStat c where c.missionId = ?", this.id);

        return count;
    }

    public Integer getGoal() {
        return goal;
    }

    public void setGoal(Integer goal) {
        this.goal = goal;
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    //
    public List<MissionComment> getLastComments() {
        List comments = MissionComment.find("mission = ? order by id desc", this).fetch(4);
        Collections.reverse(comments);
        return comments;
    }

    public List<Specimen> getLastContributedSpecimens(int page, int length) {
        int start = (page - 1) * length;
        return JPA
                .em()
                .createNativeQuery(
                        "SELECT DISTINCT S.* " + "FROM H_SPECIMEN S " + "INNER JOIN "
                                + "    (SELECT S.ID S_ID, C.ID, C.CREATION_DATE CREATION_DATE" + "     FROM H_CONTRIBUTION C "
                                + "     INNER JOIN H_SPECIMEN S ON C.SPECIMEN_ID = S.ID " + "     WHERE C.MISSION_ID = ? AND C.CANCELED = 0 "
                                + "     ) T " + "ON S.ID = T.S_ID ORDER BY S.LASTMODIFIED DESC", Specimen.class).setParameter(1, this.id)
                .setFirstResult(start).setMaxResults(length).getResultList();
    }

    public User getLeader() {
        return leader;
    }

    public void setLeader(User leader) {
        this.leader = leader;
    }

    public Long getMembersCount() {
        return count("select count(u) from Mission m join m.users u where m.id = ?", this.id);
    }

    public List<User> getMembersOrderByLogin(Integer page) {
        Integer pageLength = Herbonautes.get().pageLength;
        return User.find("").fetch(page, pageLength);
    }

    public Date getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(Date openingDate) {
        this.openingDate = openingDate;
    }

    public String getPresentation() {
        return presentation;
    }

    public void setPresentation(String presentation) {
        this.presentation = presentation;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Specimen getRandomSpecimen() {

        Long count = getShowableSpecimensCount();
        if (count == 0) {
            return null;
        }

        int start = new Random().nextInt(count.intValue());


        return JPA
                .em()
                .createQuery(
                        "select s from Specimen s " + "where ? member of s.missions " + "and s.tiled = true "
                                + "and s.tilingError = false", Specimen.class).setParameter(1, this).setFirstResult(start).setMaxResults(1)
                .getSingleResult();
    }

    public Specimen getRandomSpecimenForUser(String login) {
        User user = User.findByLogin(login);

        List<String> types = new ArrayList<String>();

        for (Contribution.Type type : Contribution.Type.values()) {
            if (this.isContributionRequired(type.toString()) && this.requiredLevel(type.toString()) <= user.getLevel()) {
                types.add(type.toString());
            }
        }

        // Déja contribué
        String queryContributed =
                "select c.specimen.id " + "from Contribution c " + "where c.specimen = s and c.mission = :mission "
                        + " and c.canceled = false " + " and c.user.id = :userId " + " and c.type in (:types) "
                        + "GROUP BY c.specimen.id " + "HAVING count(c) >= :typesCount ";

        String queryUnusable =
                "select c.specimen.id " + "from Contribution c " + "where c.specimen = s and c.mission = :mission  "
                        + " and c.canceled = false " + " and c.user.id = :userId " + " and c.type = 'UNUSABLE' ";

        // D��ja rapport
        String queryReported =
                "select c.specimen.id " + "from ContributionReport c " + "where :mission member of c.specimen.missions "
                        + " and c.specimen = s and c.complete = true and c.type in (:types) " + "GROUP BY c.specimen.id "
                        + "HAVING count(c) >= (:typesCount) ";

        String queryUnusableReported =
                "select c.specimen.id " + "from ContributionReport c " + "where :mission member of c.specimen.missions "
                        + " and c.specimen = s " + " and c.complete = true " + " and c.type = 'UNUSABLE' ";

        String query =
                "select s from Specimen s " + "where :mission member of s.missions " + "and s.tiled = true "
                        + "and s.tilingError = false " + "and not exists (" + queryContributed + ") " + "and not exists ("
                        + queryUnusable + ") " + "and not exists (" + queryReported + ") " + "and not exists (" + queryUnusableReported
                        + ") ";

        String countQuery =
                "select count(s) from Specimen s " + "where :mission member of s.missions " + "and s.unusable = false "
                        + "and s.tiled = true " + "and s.tilingError = false " + "and not exists (" + queryContributed + ") "
                        + "and not exists (" + queryUnusable + ")" + "and not exists (" + queryReported + ") " + "and not exists ("
                        + queryUnusableReported + ") ";

        int count =
                JPA.em().createQuery(countQuery, Long.class).setParameter("mission", this).setParameter("userId", user.getId())
                        .setParameter("types", types).setParameter("typesCount", Long.valueOf(types.size())).getSingleResult()
                        .intValue();

        if (count > 0) {

            int start = new Random().nextInt(count);

            return JPA.em().createQuery(query, Specimen.class).setParameter("mission", this)
                    .setParameter("userId", user.getId()).setParameter("types", types)
                    .setParameter("typesCount", Long.valueOf(types.size())).setFirstResult(start).setMaxResults(1)
                    .getSingleResult();

        } else {
            return getRandomSpecimen();
        }

    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public List<String> getRequiredContributionTypes() {
        List<String> contributionTypes = new ArrayList<String>();
        for (Contribution.Type type : Contribution.Type.values()) {
            if (this.isContributionRequired(type.toString())) {
                contributionTypes.add(type.toString());
            }
        }
        return contributionTypes;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * (utilisé pour le tirage aleatoire de specimens)
     */
    private Long getShowableSpecimensCount() {
        Long count =
                count("select count(s) " + "from Mission m " + "join m.specimens s " + "where s.tiled = true "
                        + "and s.tilingError = false " + "and m.id = ?", this.id);
        return count;
    }

    public List<Specimen> getSpecimens() {
        return specimens;
    }

    public void setSpecimens(List<Specimen> specimens) {
        this.specimens = specimens;
    }

    public Long getSpecimensCount() {
        Long count = Cache.get("mission_specimens_count" + this.id, Long.class);
        if (count == null) {
            count =
                    count(
                            //"select count(s) from Mission m join m.specimens s where m.id = ? and tiled = true and tilingError = false",
                            "select count(s) from Specimen s where s.mission.id = ? and tiled = true and tilingError = false",
                            this.id);
            Cache.set("mission_specimens_count" + this.id, count);
        }
        return count;
    }

    @NoTransaction
    public Long getTiledSpecimensCount() {
        Long count = count("select count(s) from Mission m join m.specimens s where s.tiled = true and m.id = ?", this.id);
        return count;
    }

    public Long getTotalSpecimensCount() {
        Long count = count("select count(s) from Specimen s where s.mission.id = ?", this.id);
        return count;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<User> getTopContributors() {
        return JPA
                .em()
                .createNativeQuery(
                        "SELECT HU.* FROM H_USER HU, (" + "SELECT U.ID U_ID, count(*) CNT " + "FROM H_CONTRIBUTION C "
                                + "INNER JOIN H_USER U ON U.ID = C.USER_ID " + "WHERE C.CANCELED = 0 and C.MISSION_ID = ? "
                                + "GROUP BY U.ID " + "ORDER BY CNT DESC) T WHERE T.U_ID = HU.ID", User.class).setParameter(1, this.id)
                .setMaxResults(3).getResultList();
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public Boolean isBanned(String login) {
        User user = User.findByLogin(login, false);
        return (getBannedUsers().contains(user));
    }

    //public static List<Mission>

    /*public static List<Mission> getValidatedMissions(User user) {
        return Mission.find("validator.id = ?1", user.getId()).fetch();
    }*/

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public boolean isContributionAllowed(String type, Integer level) {
        return (level != null && level >= requiredLevel(type));
    }

    /**
     * Determine si la contribution est requise pour cette mission.
     * Pour l'instant ce sont seulement 'COUNTRY' et 'REGION_2' qui peuvent changer.
     * Utilisé dans contributionBoard.html pour afficher les contributions utiles
     * et dans Specimens/show.html pour griser les contributions impossible selon les missions
     */
    //
    public boolean isContributionRequired(String type) {
        if (type == null) {
            return false;
        }

        switch (Contribution.Type.valueOf(type)) {
            case COUNTRY:
                return (this.getCountry() == null); // Pas de pays si le pays est prédéfini
            case REGION_2:
                return (this.getCountry() != null); // Pas de region 2 si le pays est à déterminer
            case UNUSABLE:
                return false; // On en a pas besoin de lui
            default:
                return true; // Toutes les autres configurations sont requises
        }

    }

    public boolean isHasBigImage() {
        return hasBigImage;
    }

    public void setHasBigImage(boolean hasBigImage) {
        this.hasBigImage = hasBigImage;
    }

    public boolean isHasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

    // TODO: leader null impossible
    public Boolean isLeader(String login) {
        if (getLeader() == null) {
            return false;
        }
        if (login == null) {
            return false;
        }
        if (login.equals(getLeader().getLogin())) {
            return true;
        }

        List<MissionLeader> leaders = MissionLeader.find("missionId = ?", this.id).fetch();
        for (MissionLeader leader : leaders) {
            if (login.equals(leader.getUserLogin())) {
                return true;
            }
        }

        return false;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public Boolean isMember(String userLogin) {
        Long c =
                User.count("select count(u) from User u" + " where u.login = ? and ? member of u.missions", userLogin, this);
        return c > 0;
    }

    public boolean isOpened() {
        return new Date().after(openingDate);
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public boolean isProposition() {
        return proposition;
    }

    public void setProposition(boolean proposition) {
        this.proposition = proposition;
    }

    public boolean isPropositionValidated() {
        return propositionValidated;
    }

    public void setPropositionValidated(boolean propositionValidated) {
        this.propositionValidated = propositionValidated;
    }

    public boolean isPropositionSubmitted() {
        return propositionSubmitted;
    }

    public void setPropositionSubmitted(boolean propositionSubmitted) {
        this.propositionSubmitted = propositionSubmitted;
    }

    public Integer requiredLevel(String type) {
        switch (Contribution.Type.valueOf(type)) {
            case COUNTRY:
                return 0; //

            case REGION_1:
                return (this.getCountry() == null) ? 2 : 1;

            case REGION_2:
                return (this.getCountry() == null) ? 3 : 2;

            case DATE:
                return 3;

//      case BOTANISTS:
//        return 4;

            case COLLECTOR:
                return 4;

            case IDENTIFIEDBY:
                return 4;

            case LOCALITY:
                return 5;

            case GEOLOCALISATION:
                return 6;

            default:
                return 0;
        }
    }

    public boolean isReportPublished() {
        return reportPublished;
    }

    public void setReportPublished(boolean reportPublished) {
        this.reportPublished = reportPublished;
    }

    public List<ContributionQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<ContributionQuestion> questions) {
        this.questions = questions;
    }

    public Boolean isLoadingCart() {
        return loadingCart;
    }

    public void setLoadingCart(boolean loadingCart) {
        this.loadingCart = loadingCart;
    }

    /*public String getCartJson() {
        return cartJson;
    }

    public void setCartJson(String cartJson) {
        this.cartJson = cartJson;
    }*/

    public User getValidator() {
        return validator;
    }

    public void setValidator(User validator) {
        this.validator = validator;
    }

    public Tag getPrincipalTag() {
        return principalTag;
    }

    public void setPrincipalTag(Tag principalTag) {
        this.principalTag = principalTag;
    }

}