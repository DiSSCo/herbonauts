package models;


import conf.Herbonautes;
import helpers.NumberUtils;
import models.User.UserContributionReport;
import models.comments.MissionComment;
import models.contributions.Contribution;
import models.contributions.DateContribution;
import models.contributions.GeolocalisationContribution;
import models.questions.ContributionQuestion;
import models.questions.ContributionQuestionStat;
import models.questions.ContributionSpecimenStat;
import models.questions.ContributionSpecimenUserStat;
import models.tags.Tag;
import models.tags.TagLink;
import models.tags.TagLinkType;
import models.tags.TagType;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.db.jpa.NoTransaction;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.*;

/**
 * Mission
 */
@Entity
@Table(name = "HV_MISSION_SIMPLE")
public class MissionSimple extends Model {

    @Required
    @Column(columnDefinition = "VARCHAR(1000)")
    private String title;

    private String shortDescription;

    private Long imageId;
    private Long bigImageId;

    private Integer goal;

    private boolean hasImage;

    private boolean closed;
    private Date openingDate;
    private boolean priority;
    private boolean published;

    @ManyToOne(fetch = FetchType.LAZY)
    private User leader;

    public User getLeader() {
        return leader;
    }

    public void setLeader(User leader) {
        this.leader = leader;
    }

    public Integer getGoal() {
        return goal;
    }

    public void setGoal(Integer goal) {
        this.goal = goal;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public boolean isHasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public Date getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(Date openingDate) {
        this.openingDate = openingDate;
    }

    public boolean isPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public boolean isOpened() {
        return new Date().after(openingDate);
    }

    public Boolean isLeader(String login) {
        if (getLeader() == null) {
            return false;
        }
        if (login == null) {
            return false;
        }
        return login.equals(getLeader().getLogin());
    }

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
            Cache.set("mission_contributions_count" + this.id, count);
        }
        Logger.info("Count %s : %d", this, count);
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


    @NoTransaction
    public static List<MissionSimple> getClosedMissions(boolean withUnpublished) {
        if (withUnpublished) {
            return MissionSimple.find("closed = true order by priority").fetch();
        } else {
            return MissionSimple.find("closed = true and published = true order by priority").fetch();
        }
    }

    @NoTransaction
    public static List<MissionSimple> getNextMissions(boolean withUnpublished) {
        if (withUnpublished) {
            return MissionSimple.find("openingDate > CURRENT_DATE and closed = false order by priority").fetch();
        } else {
            return MissionSimple.find("openingDate > CURRENT_DATE and published = true and closed = false order by priority")
                    .fetch();
        }
    }

    @NoTransaction
    public static List<MissionSimple> getPresentMissions(boolean withUnpublished) {
        ;
        if (withUnpublished) {
            return Mission.find("openingDate <= CURRENT_DATE and closed = false order by priority").fetch();
        } else {
            return Mission.find("openingDate <= CURRENT_DATE and published = true and closed = false order by priority")
                    .fetch();
        }
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
    public Long getCompletedSpecimensCount() {
        return count("select count(c) " +
                "from ContributionSpecimenStat c " +
                "where (c.validated = true or unusableValidated = true) and c.missionId = ?", this.id);
        //return count("select count(s) from Mission m join m.specimens s where s.complete = true and m.id = ?", this.id);
    }

    @NoTransaction
    public List<MissionLeader> getLeaderList() {
        List<MissionLeader> leaders = MissionLeader.find("missionId = ?", this.id).fetch();
        return leaders;
    }
}