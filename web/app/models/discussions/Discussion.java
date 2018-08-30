package models.discussions;

import models.DatedModificationsModel;
import models.User;
import models.tags.TagLinkType;
import models.tags.TagType;
import play.db.jpa.JPA;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Jonathan on 17/06/2015.
 */
@Entity
@Table(name = "H_DISCUSSION")
public class Discussion extends DatedModificationsModel<Discussion> {

    @Column(name = "title")
    private String title;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    private Date creationDate;

    @Column(name = "is_resolved")
    private Boolean resolved;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="author_id", referencedColumnName = "id", nullable = false)
    private User author;

    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(
            name="H_DISCUSSION_CATEGORIES",
            joinColumns={@JoinColumn(name="discussion_id", referencedColumnName="id")},
            inverseJoinColumns={@JoinColumn(name="category_id", referencedColumnName="id")})
    private List<DiscussionCategory> categories;

    @OneToMany(mappedBy="discussion", cascade = CascadeType.ALL)
    @OrderBy("id asc")
    private List<Message> messages;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean isResolved() {
        return resolved;
    }

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }

    public List<DiscussionCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<DiscussionCategory> categories) {
        this.categories = categories;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public static List<Discussion> getDiscussionsFilteredByCategories(String tagLabel, TagType tagType, String categories, String loadedDiscussions, int maxResults) {
        StringBuilder request = new StringBuilder();
        request.append("SELECT distinct D.* FROM H_DISCUSSION D ");
        if(categories !=  null && !categories.trim().isEmpty())
            request.append("INNER JOIN H_DISCUSSION_CATEGORIES DC ON DC.discussion_id = D.id ");
        request.append("INNER JOIN H_TAGS_LINKS TL ON TL.target_id = D.id ");
        request.append("INNER JOIN H_TAGS T ON t.id = tl.tag_id ");
        request.append("WHERE TL.link_type = '").append(TagLinkType.DISCUSSION).append("' ");
        if(tagType != null) {
            request.append("AND T.tag_type = '").append(tagType).append("' ");
        }
        if(tagLabel != null) {
            request.append("AND T.label = '").append(tagLabel).append("' ");
        }
        if(categories !=  null && !categories.trim().isEmpty()) {
            request.append("AND DC.category_id IN (").append(categories).append(") ");
        }
        if(loadedDiscussions !=  null && !loadedDiscussions.trim().isEmpty())
            request.append("AND D.id NOT IN (").append(loadedDiscussions).append(") ");
        request.append("ORDER BY D.last_update_date desc");
        return JPA.em().createNativeQuery(request.toString(), Discussion.class).setMaxResults(maxResults)
                .getResultList();
    }

    public static Integer getDiscussionsCount(String tagLabel, TagType tagType) {
        StringBuilder request = new StringBuilder();
        request.append("SELECT count(distinct D.id) FROM H_DISCUSSION D ");
        request.append("INNER JOIN H_TAGS_LINKS TL ON TL.target_id = D.id ");
        request.append("INNER JOIN H_TAGS T ON t.id = tl.tag_id ");
        request.append("WHERE TL.link_type = '").append(TagLinkType.DISCUSSION).append("' ");
        if(tagType != null)
            request.append("AND T.tag_type = '").append(tagType).append("' ");
        if(tagLabel != null)
            request.append("AND T.label = '").append(tagLabel).append("' ");
        return ((Number) JPA.em().createNativeQuery(request.toString()).getSingleResult()).intValue();
    }

    public static Integer countDiscussionsWithTagsByUser(User user) {
        StringBuilder request = new StringBuilder();
        request.append("SELECT count(*) from ( ");
        request.append("SELECT d.id, count(tl.tag_id) FROM H_TAGS T ");
        request.append("INNER JOIN H_TAGS_LINKS TL ON TL.tag_id = T.id ");
        request.append("INNER JOIN H_DISCUSSION D ON D.id = TL.target_id ");
        request.append("WHERE TL.link_type = '").append(TagLinkType.DISCUSSION).append("' ");
        request.append("AND D.author_id = ").append(user.getId());
        request.append("GROUP by d.id ");
        request.append("HAVING count(tl.tag_id) > 1) tmp");
        return ((Number) JPA.em().createNativeQuery(request.toString()).getSingleResult()).intValue();
    }

    public static Long countDiscussionsByCreator(User user) {
        try {
            CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
            CriteriaQuery<Long> query = cb.createQuery(Long.class);
            Root<Discussion> from = query.from(Discussion.class);
            Join<Discussion, User> userJoin = from.join("author");
            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(userJoin.get("id"), user.getId()));
            query.select(cb.count(from));
            query.where(predicates.toArray(new Predicate[]{}));
            TypedQuery<Long> createQuery = JPA.em().createQuery(query);
            return createQuery.getSingleResult();
        } catch (RuntimeException re) {
            throw re;
        }
    }

    public static String getDiscussionPrincipalTagType(Long discussionId) {
        StringBuilder request = new StringBuilder();
        request.append("SELECT T.tag_type FROM H_DISCUSSION d ");
        request.append("INNER JOIN H_TAGS_LINKS TL ON TL.target_id = d.id ");
        request.append("AND TL.link_type = '").append(TagLinkType.DISCUSSION).append("' ");
        request.append("AND TL.principal = 1 ");
        request.append("INNER JOIN H_TAGS T ON T.id = TL.tag_id ");
        request.append("WHERE d.id = ").append(discussionId).append(" ");
        return (String) JPA.em().createNativeQuery(request.toString()).getSingleResult();
    }

    public static List<Discussion> getDiscussionsBySpecimenCode(String code) {
        StringBuilder request = new StringBuilder();
        request.append("SELECT distinct D.* FROM H_DISCUSSION D ");
        request.append("INNER JOIN H_TAGS_LINKS TL ON TL.target_id = D.id ");
        request.append("INNER JOIN H_TAGS T ON t.id = tl.tag_id ");
        request.append("WHERE TL.link_type = '").append(TagLinkType.DISCUSSION).append("' ");
        request.append("AND T.tag_type = '").append(TagType.SPECIMEN).append("' ");
        request.append("AND T.label = '").append(code).append("' ");
        request.append("ORDER BY D.last_update_date desc");
        return JPA.em().createNativeQuery(request.toString(), Discussion.class).getResultList();
    }

    public static List<Discussion> getDiscussionsByMissionId(Long missionId) {
        StringBuilder request = new StringBuilder();
        request.append("SELECT distinct D.* FROM H_DISCUSSION D ");
        request.append("INNER JOIN H_TAGS_LINKS TL ON TL.target_id = D.id ");
        request.append("INNER JOIN H_TAGS T ON t.id = tl.tag_id ");
        request.append("INNER JOIN H_MISSION M ON M.principal_tag = T.id ");
        request.append("WHERE TL.link_type = '").append(TagLinkType.DISCUSSION).append("' ");
        request.append("AND T.tag_type = '").append(TagType.MISSION).append("' ");
        request.append("AND M.id = ").append(missionId).append(" ");
        request.append("ORDER BY D.last_update_date desc");
        return JPA.em().createNativeQuery(request.toString(), Discussion.class).getResultList();
    }



}
