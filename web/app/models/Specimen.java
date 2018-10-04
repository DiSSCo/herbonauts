package models;

import models.contributions.Contribution;
import models.contributions.reports.ContributionReport;
import models.questions.ContributionAnswer;
import models.questions.ContributionQuestion;
import models.questions.ContributionSpecimenStat;
import models.tags.TagType;
import play.Logger;
import play.db.jpa.JPA;

import javax.persistence.*;
import java.util.*;

/**
 * Spécimen
 */
@Entity
@Table(name="H_SPECIMEN")
public class Specimen extends DatedModificationsModel<Specimen> {

    private String institute;
    private String collection;
    private String code;

    private String family;
    private String genus;
    private String specificEpithet;

    private String sonneratURL;
    private String tilesBaseURL;

    // list de liens
    private String tropicosURL;

    private Integer tileWidth = 200;
    private Integer tileHeight = 300;
    private boolean tiled;
    private boolean tilingError; // Pb rencontré par le batch

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MASTER_ID")
    private SpecimenMaster master;

    private boolean displayed;
    private Date firstDiplayed;

    // complet
    private boolean complete;

    // inutilisable
    private boolean unusable;


    private Date lastModified;

    private String alea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MISSION_ID")
    private Mission mission;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_UPDATE_DATE")
    public Date lastUpdateDate;

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    @Override
    @PrePersist
    @PreUpdate
    public void setLastUpdateDate() {
        this.lastUpdateDate = new Date();
    }

    //@ManyToMany(fetch=FetchType.LAZY)
    //@JoinTable(name="mission_specimen",
    //		joinColumns = { @JoinColumn(name = "specimen_id", nullable = false) },
    //		inverseJoinColumns = { @JoinColumn(name ="mission_id", nullable = false) })
    //private List<Mission> missions;

    public Specimen() {
        super();
    }

    public Specimen(Long id) {
        super();
        this.id = id;
    }

    public String getGenusSpecies() {
        if (this.specificEpithet == null) {
            // Rattrapage v1
            StringBuilder sb = new StringBuilder();
            if (this.family != null && !"null".equals(this.family)) {
                sb.append(this.family).append(" ");
            }
            if (this.genus != null && !"null".equals(this.genus)) {
                sb.append(this.genus);
            }
            if (sb.length() == 0) {
                sb.append("indet.");
            }
            return sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            if (this.genus != null && !"null".equals(this.genus)) {
                sb.append(this.genus).append(" ");
            }
            if (this.specificEpithet != null && !"null".equals(this.specificEpithet)) {
                sb.append(this.specificEpithet);
            }
            if (sb.length() == 0) {
                sb.append("indet.");
            }
            return sb.toString();
        }
    }

    public Map<String, ContributionAnswer> allValidAnswers() {
        List<ContributionAnswer> contributions =
                ContributionAnswer.find("specimenId = ? and deleted = false and userId is null", this.id).fetch();

        HashMap<String, ContributionAnswer> ret = new HashMap<String, ContributionAnswer>();

        for(ContributionAnswer answer : contributions) {
            ContributionQuestion q = ContributionQuestion.findById(answer.getQuestionId());
            ret.put(q.getName(), answer);
        }

        return ret;
    }

    public List<Contribution> getContributionsByLogin(String login) {
        return Contribution.find("user.login = ? and specimen = ? and canceled = false order by date desc", login, this).fetch();
    }

    public List<ContributionReport> getContributionReports() {

        List<ContributionReport> ctr = ContributionReport.find("specimen.id = ?", this.id).fetch();
        Logger.info("get contribution reports for %s (%s)", this.id, ctr.size());
        return ctr;
    }

    public ContributionReport getContributionReport(String type) {
        return ContributionReport.find("specimen.id = ? and type = ?", this.id, type).first();
    }

    public Map<String, ContributionReport> getContributionReportsByType() {
        List<ContributionReport> validatedContributions = getContributionReports();

        HashMap<String, ContributionReport> map =
                new HashMap<String, ContributionReport>();

        if (validatedContributions == null) {
            return map;
        }

        for (ContributionReport contribution : validatedContributions) {
            map.put(contribution.getType().toString(), contribution);
        }

        return map;
    }

    public Map<String, Contribution> getContributionsMapByLogin(String login) {
        List<Contribution> contributions = getContributionsByLogin(login);
        HashMap<String, Contribution> map = new HashMap<String, Contribution>();

        if (contributions == null) {
            return map;
        }

        for (Contribution contribution : contributions) {
            map.put(contribution.getType(), contribution);
        }

        return map;
    }

    public List<SpecimenAttribute> getAttributesForUser(Long userId) {

        List<ContributionQuestion> questions = ContributionQuestion.findAllActiveForMission(this.mission.id);

        List<ContributionAnswer> answers = ContributionAnswer.find("specimenId = :specimenId and userId is null")
                .setParameter("specimenId", this.id).fetch();

        ArrayList<SpecimenAttribute> attributes = new ArrayList();

        HashSet<Long> doneQuestionsIds = new HashSet<Long>();

        boolean isUtil = true;
        for (ContributionAnswer a : answers) {

            SpecimenAttribute attribute = new SpecimenAttribute();
            attribute.label = ContributionQuestion.getQuestionLabel(a.getQuestionId());
            attribute.sortIndex = ContributionQuestion.getQuestionSortIndex(a.getQuestionId());
            attribute.validatedValue = a.toHumanValue();
            attributes.add(attribute);
            doneQuestionsIds.add(a.getQuestionId());
            System.out.println("aa");
            if("unusable".equals(ContributionQuestion.getQuestionName(a.getQuestionId()))){
                isUtil=false;
            }

        }

        for (ContributionQuestion q : questions) {
            if (!"unusable".equals(q.getName()) && !doneQuestionsIds.contains(q.id) && isUtil == true) {
                SpecimenAttribute attribute = new SpecimenAttribute();
                attribute.label = q.getLabel();
                attribute.sortIndex = q.getSortIndex();
                attribute.todo = true;
                attributes.add(attribute);

            }
//            else{
//            	attributes.clear();
//            }
        }

        Collections.sort(attributes, new Comparator<SpecimenAttribute>() {
            @Override
            public int compare(SpecimenAttribute o1, SpecimenAttribute o2) {
                return o1.sortIndex.compareTo(o2.sortIndex);
            }
        });

        return attributes;

    }

    public static class SpecimenAttribute {
        public Long sortIndex;
        public String label;
        public List<ContributionAnswer.QuestionLineHumanValue> validatedValue;
        public List<ContributionAnswer.QuestionLineHumanValue> userValue;
        public boolean todo = false;

    }




    public List<User> getContributors() {
        return JPA.em()
                .createQuery(
                        "select distinct c.user " +
                                "from Contribution c " +
                                "where c.canceled = false and c.specimen.id = ?", User.class)
                .setParameter(1, this.id)
                .getResultList();
    }

    public Map<User, Map<String, Contribution>> getAllContributionsByUserAndType() {
        List<User> contributors = getContributors();
        HashMap<User, Map<String, Contribution>> map = new HashMap<User, Map<String,Contribution>>();
        if (contributors != null) {
            for (User contributor : contributors) {
                map.put(contributor, getContributionsMapByLogin(contributor.getLogin()));
            }
        }
        return map;
    }

    public List<Contribution> getContributionsByTypeAndLogin(String type, String login) {
        return Contribution.find("type = ? and user.login = ? and specimen = ? and canceled = false", type, login, this).fetch();
    }

    public boolean hasActiveMission() {
        Mission mission = getMission();
        if (mission != null &&mission.published && !mission.closed) {
            return true;
        }
        return false;
    }

    //public boolean isContributionRequired(String type) {
    //	if (getMissions() != null) {
    //		for (Mission mission : getMissions()) {
    //			if (mission.isContributionRequired(type)) {
    //				return true;
    //			}
    //		}
    //	}
    //	return false;
    //}



    public void add(Contribution contribution) {
        contribution.sanitize();
        contribution.save();
        contribution.refresh();

        Mission.clearCache(contribution.getMission().id);
    }

    public String toCompleteString() {
        return String.format("%s %s (%s/%s/%s)", this.getFamily(), this.getGenus(), this.getInstitute(), this.getCollection(), this.getCode());
    }

    public Long getContributionsCount() {
        Long count = count("select count(c) from ContributionAnswer c where c.deleted != true and c.userId is not null and c.specimenId = ?", this.id);
        return count;
    }

    public Long getContributorsCount() {
        Long count = count("select count(distinct c.userId) from ContributionAnswer c where c.deleted != true and c.userId is not null and c.specimenId = ?", this.id);

        return count;
    }

    public static List<Specimen> search(String q, int limit) {
        return search(q, limit, 1);
    }

    public static List<Specimen> search(String q, int max, Integer page) {
        if (q == null) {
            return null;
        }

        Integer realPage = (page == null ? 1 : page);

        String likeValue = q.toLowerCase().replace(' ', '%');

        return Specimen
                .find("tilingError = false and tiled = true and lower(code) like ?", "%" + likeValue + "%")
                .fetch(realPage, max);
    }

    public static Specimen findByCode(String code) {
        return Specimen.find("code = ?1", code).first();
    }

    public static List<SpecimenFamilyGenus> searchFamilyGenus(String q, int limit) {
        return searchFamilyGenus(q, limit, 1);
    }

    public static List<SpecimenFamilyGenus> searchFamilyGenus(String q, int limit, Integer page) {
        List<SpecimenFamilyGenus> familiesGenuses = new ArrayList<SpecimenFamilyGenus>();
        if (q == null) {
            return familiesGenuses;
        }

        int cleanPage = (page == null ? 1 : page);

        String likeValue = '%' + q.toLowerCase().replace(' ', '%') + '%';

        List<Object[]> results = JPA.em().createQuery(
                "select distinct s.family, s.genus " +
                        "from Specimen s " +
                        "where tilingError = false and tiled = true and " +
                        "(lower(family) like ? or lower(genus) like ?)")
                .setParameter(1, likeValue)
                .setParameter(2, likeValue)
                .setFirstResult((cleanPage - 1) * limit)
                .setMaxResults(limit)
                .getResultList();

        if (results == null) {
            return familiesGenuses;
        }

        for (Object[] obj : results) {
            familiesGenuses.add(new SpecimenFamilyGenus((String) obj[0], (String) obj[1]));
        }

        return familiesGenuses;
    }


    public static int countSpecimensByTag(String tagLabel) {
        StringBuilder request = new StringBuilder();
        request.append("SELECT count(distinct s.id) FROM H_SPECIMEN s ");
        request.append("INNER JOIN H_TAGS_LINKS TL ON TL.target_id = s.id ");
        request.append("INNER JOIN H_TAGS T ON t.id = tl.tag_id ");
        request.append("WHERE TL.link_type = '").append(TagType.SPECIMEN).append("' ");
        request.append("AND T.label = '").append(tagLabel).append("' ");
        request.append("ORDER BY s.last_update_date desc");
        return ((Number) JPA.em().createNativeQuery(request.toString()).getSingleResult()).intValue();
    }

    public static List<Specimen> getSpecimensByTag(String tagLabel) {
        StringBuilder request = new StringBuilder();
        request.append("SELECT s.* FROM H_SPECIMEN s ");
        request.append("INNER JOIN H_TAGS_LINKS TL ON TL.target_id = s.id ");
        request.append("INNER JOIN H_TAGS T ON t.id = tl.tag_id ");
        request.append("WHERE TL.link_type = '").append(TagType.SPECIMEN).append("' ");
        request.append("AND T.label = '").append(tagLabel).append("' ");
        request.append("ORDER BY s.last_update_date desc");
        return JPA.em().createNativeQuery(request.toString(), Specimen.class)
                .getResultList();
    }

    public static final void updatePropositionSpecimensDates(Long missionId) {
        JPA.em().createNativeQuery("UPDATE H_SPECIMEN SET last_update_date = current_timestamp WHERE mission_id = ?1")
                .setParameter(1, missionId)
                .executeUpdate();
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public String getInstitute() {
        return institute;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getCollection() {
        return collection;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    public String getGenus() {
        return genus;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getFamily() {
        return family;
    }

    public void setSonneratURL(String sonneratURL) {
        this.sonneratURL = sonneratURL;
    }

    public String getSonneratURL() {
        return sonneratURL;
    }

    public void setTilesBaseURL(String tilesBaseURL) {
        this.tilesBaseURL = tilesBaseURL;
    }

    public String getTilesBaseURL() {
        return tilesBaseURL;
    }


    public void setTropicosURL(String tropicosURL) {
        this.tropicosURL = tropicosURL;
    }


    public String getTropicosURL() {
        return tropicosURL;
    }


    public void setTileWidth(Integer tileWidth) {
        this.tileWidth = tileWidth;
    }


    public Integer getTileWidth() {
        return tileWidth;
    }


    public void setTileHeight(Integer tileHeight) {
        this.tileHeight = tileHeight;
    }


    public Integer getTileHeight() {
        return tileHeight;
    }


    public void setTiled(boolean tiled) {
        this.tiled = tiled;
    }


    public boolean isTiled() {
        return tiled;
    }


    public void setTilingError(boolean tilingError) {
        this.tilingError = tilingError;
    }


    public boolean isTilingError() {
        return tilingError;
    }


    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }


    public boolean isDisplayed() {
        return displayed;
    }


    public void setFirstDiplayed(Date firstDiplayed) {
        this.firstDiplayed = firstDiplayed;
    }


    public Date getFirstDiplayed() {
        return firstDiplayed;
    }


    public void setComplete(boolean complete) {
        this.complete = complete;
    }


    public boolean isComplete() {
        return complete;
    }


    public void setUnusable(boolean unusable) {
        this.unusable = unusable;
    }


    public boolean isUnusable() {
        return unusable;
    }


    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }


    public Date getLastModified() {
        return lastModified;
    }


    //public void setMissions(List<Mission> missions) {
    //	this.missions = missions;
    //}
    //
    //
    //public List<Mission> getMissions() {
    //	return missions;
    //}

    /*public String getAlea() {
        return alea;
    }

    public void setAlea(String alea) {
        this.alea = alea;
    }*/

    public Mission getMission() {
        return mission;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }

    public String getSpecificEpithet() {
        return specificEpithet;
    }

    public void setSpecificEpithet(String specificEpithet) {
        this.specificEpithet = specificEpithet;
    }

    /*
	public ContributionFeedback addWithFeedback(Contribution contribution) {
		add(contribution);
		return Contribution.feedback(contribution);
	}
	*/

    public static final void deleteByMissionId(Long missionId) {
        JPA.em().createQuery("delete from Specimen WHERE mission.id = ?1")
                .setParameter(1, missionId)
                .executeUpdate();
    }

    public String getAlea() {
        return alea;
    }

    public void setAlea(String alea) {
        this.alea = alea;
    }

    public static List<Specimen> getSpecimensByMissionId(Long missionId) {
        return Specimen.find("mission.id = ?1", missionId).fetch();
    }

    public boolean isCompleted() {

        List<ContributionSpecimenStat> stat = find("select c from ContributionSpecimenStat c " +
                "where c.specimen.id = ?", this.id).fetch();

        if (stat.size() == 0) {
            return false;
        }

        return stat.get(0).getValidated() || stat.get(0).getUnusableValidated();
    }

    public SpecimenMaster getMaster() {
        return master;
    }

    public void setMaster(SpecimenMaster master) {
        this.master = master;
    }
}
