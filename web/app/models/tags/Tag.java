package models.tags;

import models.DatedModificationsModel;
import models.User;
import play.db.jpa.JPA;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Jonathan on 17/06/2015.
 */
@Entity
@Table(name = "H_TAGS")
public class Tag extends DatedModificationsModel<Tag> {

    @Column(name = "label")
    private String tagLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag_type")
    private TagType tagType;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "H_TAGS_SUBSCRIPTION", joinColumns = {@JoinColumn(name = "tag_id", nullable = false)},
            inverseJoinColumns = {@JoinColumn(name = "user_id", nullable = false)})
    private List<User> users;

    @Column(name = "published")
    public boolean published;

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

    public String getTagLabel() {
        return tagLabel;
    }

    public void setTagLabel(String tagLabel) {
        this.tagLabel = tagLabel;
    }

    public TagType getTagType() {
        return tagType;
    }

    public void setTagType(TagType tagType) {
        this.tagType = tagType;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public static List<Tag> findAllOrdered() {
        return Tag.find("order by tagLabel").fetch();
    }

    public static List<Tag> findByLinkTypeAndTargetId(TagLinkType linkType, Long targetId) {
        StringBuilder request = new StringBuilder();
        request.append("SELECT distinct T.*, TL.last_update_date FROM H_TAGS T ");
        request.append("INNER JOIN H_TAGS_LINKS TL ON TL.tag_id = T.id ");
        request.append("WHERE TL.link_type = '").append(linkType).append("' ");
        request.append("AND TL.target_id = '").append(targetId).append("' ");
        request.append("ORDER BY TL.last_update_date");
        return JPA.em().createNativeQuery(request.toString(), Tag.class)
                .getResultList();
    }

    public static List<Tag> findByDiscussionId(Long discussionId) {
        StringBuilder request = new StringBuilder();
        request.append("SELECT distinct T.* FROM H_TAGS T ");
        request.append("INNER JOIN H_TAGS_LINKS TL ON TL.tag_id = T.id ");
        request.append("WHERE TL.link_type = '").append(TagLinkType.DISCUSSION).append("' ");
        request.append("AND TL.target_id = '").append(discussionId).append("' ");
        return JPA.em().createNativeQuery(request.toString(), Tag.class)
                .getResultList();
    }

    public static final void deletePropositionSpecimensTags(Long missionId) {
        JPA.em().createNativeQuery("DELETE FROM H_TAGS_LINKS WHERE tag_id IN " +
                "(SELECT t.id FROM H_SPECIMEN s, H_TAGS t WHERE s.mission_id = ?1 AND t.published = 0 AND t.label = s.code)")
                .setParameter(1, missionId)
                .executeUpdate();
        JPA.em().createNativeQuery("DELETE FROM H_TAGS WHERE label IN (SELECT code FROM H_SPECIMEN WHERE mission_id = ?1) AND published = 0")
                .setParameter(1, missionId)
                .executeUpdate();
    }

    public static final void publishPropositionSpecimensTags(Long missionId) {
        JPA.em().createNativeQuery("UPDATE H_TAGS SET published = 1, last_update_date = current_timestamp WHERE label IN (SELECT code FROM H_SPECIMEN WHERE mission_id = ?1)")
                .setParameter(1, missionId)
                .executeUpdate();
    }

    public static Tag findByLabel(String label) {
        return Tag.find("label = ?1", label).first();
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public static List<Tag> getTagsBySpecimenCode(String code) {
        StringBuilder request = new StringBuilder();
        request.append("SELECT distinct T.* FROM H_TAGS T ");
        request.append("INNER JOIN H_TAGS_LINKS TL ON T.id = TL.tag_id ");
        request.append("INNER JOIN H_SPECIMEN S ON TL.target_id = S.id ");
        request.append("WHERE TL.link_type = '").append(TagLinkType.SPECIMEN).append("' ");
        request.append("AND S.code = '").append(code).append("' ");
        request.append("AND T.published = 1");
        return JPA.em().createNativeQuery(request.toString(), Tag.class).getResultList();
    }

    public static List<Tag> getTagsByMissionId(Long missionId) {
        StringBuilder request = new StringBuilder();
        request.append("SELECT distinct T.* FROM H_TAGS T ");
        request.append("INNER JOIN H_TAGS_LINKS TL ON T.id = TL.tag_id ");
        request.append("INNER JOIN H_MISSION M ON TL.target_id = M.id ");
        request.append("WHERE TL.link_type = '").append(TagLinkType.MISSION).append("' ");
        request.append("AND M.id = ").append(missionId).append(" ");
        request.append("AND T.published = 1");
        return JPA.em().createNativeQuery(request.toString(), Tag.class).getResultList();
    }

    public static int countTagsByElement(String tagLabel, TagLinkType tagLinkType) {
        StringBuilder request = new StringBuilder();
        request.append("SELECT count(distinct t.id) FROM H_TAGS t ");
        request.append("INNER JOIN H_TAGS_LINKS tl ON tl.tag_id = t.id ");
        request.append("WHERE tl.link_type = '").append(tagLinkType).append("' ");
        request.append("AND t.label = '").append(tagLabel).append("' ");
        return ((Number) JPA.em().createNativeQuery(request.toString()).getSingleResult()).intValue();
    }

    public Long getTargetId() {
        if (this.tagType != TagType.MISSION && this.tagType != TagType.SPECIMEN) {
            return null;
        }

        List<Number> targetIds = (List<Number>) JPA.em().createNativeQuery("select TARGET_ID from H_TAGS_LINKS where TAG_ID = :tagId and link_type = :linkType")
                .setParameter("tagId", this.id)
                .setParameter("linkType", this.tagType.toString())
                .getResultList();

        if (targetIds.size() > 0) {
            return targetIds.get(0).longValue();
        }

        return null;
    }

    public Long getSpecimenId() {
        if (this.tagType != TagType.SPECIMEN) {
            return null;
        }
        return null;
    }

}
