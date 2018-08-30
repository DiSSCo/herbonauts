package models.discussions;

import models.DatedModificationsModel;
import models.Mission;
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
@Table(name = "H_MESSAGE")
public class Message extends DatedModificationsModel<Message> {

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_UPDATE_DATE")
    public Date lastUpdateDate;
    @Column(name = "text")
    private String text;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    private Date creationDate;
    @Column(name = "is_resolution")
    private Boolean resolution;
    @Column(name = "is_first")
    private Boolean first;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="author_id", referencedColumnName = "id", nullable = false)
    private User author;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="discussion_id", referencedColumnName = "id", nullable = false)
    private Discussion discussion;
    @Column(name = "moderator_login")
    private String moderatorLogin;

    public static List<Message> getNewMessages(String tagLabel, TagType tagType, Long lastMessageId) {
        /*return JPA.em().createNativeQuery(
                "SELECT M.* FROM H_MESSAGE M " +
                        "INNER JOIN H_DISCUSSION D on D.id = M.discussion_id " +
                        "INNER JOIN H_TAGS_LINKS TL ON TL.target_id = D.id " +
                        "INNER JOIN H_TAGS T ON t.id = tl.tag_id " +
                        "WHERE TL.link_type = '" + TagLinkType.DISCUSSION + "' " +
                        "AND T.tag_type = '" + tagType + "' " +
                        "AND T.label = '" + tagLabel + "' " +
                        "AND M.id > " + lastMessageId, Message.class).getResultList();*/


        StringBuilder request = new StringBuilder();
        request.append("SELECT distinct M.* FROM H_MESSAGE M ");
        request.append("INNER JOIN H_DISCUSSION D on D.id = M.discussion_id ");
        request.append("INNER JOIN H_TAGS_LINKS TL ON TL.target_id = D.id ");
        request.append("INNER JOIN H_TAGS T ON t.id = tl.tag_id ");
        request.append("WHERE TL.link_type = '").append(TagLinkType.DISCUSSION).append("' ");
        if(tagType != null)
            request.append("AND T.tag_type = '").append(tagType).append("' ");
        if(tagLabel != null)
            request.append("AND T.label = '").append(tagLabel).append("' ");
        request.append("AND M.id > ").append(lastMessageId);
        Query query = JPA.em().createNativeQuery(request.toString(), Message.class);
        return query.getResultList();



    }

    public static List<Message> getNewMessages(Long discussionId, Long lastMessageId) {
        try {
            CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
            CriteriaQuery<Message> query = cb.createQuery(Message.class);
            Root<Message> from = query.from(Message.class);
            Join<Message, Discussion> messageJoin = from.join("discussion");
            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(cb.gt(from.<Long>get("id"), lastMessageId));
            predicates.add(cb.equal(messageJoin.get("id"), discussionId));
            query.select(from);
            query.where(predicates.toArray(new Predicate[]{}));
            TypedQuery<Message> createQuery = JPA.em().createQuery(query);
            return createQuery.getResultList();
        } catch (RuntimeException re) {
            throw re;
        }
    }

    public static Long countMessagesResponsesByUser(User user) {
        try {
            CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
            CriteriaQuery<Long> query = cb.createQuery(Long.class);
            Root<Message> from = query.from(Message.class);
            Join<Message, User> userJoin = from.join("author");
            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(userJoin.get("id"), user.getId()));
            predicates.add(cb.equal(from.get("first"), true));
            query.select(cb.count(from));
            query.where(predicates.toArray(new Predicate[]{}));
            TypedQuery<Long> createQuery = JPA.em().createQuery(query);
            return createQuery.getSingleResult();
        } catch (RuntimeException re) {
            throw re;
        }
    }

    public static Long countResolutionMessagesByUser(User user) {
        try {
            CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
            CriteriaQuery<Long> query = cb.createQuery(Long.class);
            Root<Message> from = query.from(Message.class);
            Join<Message, User> userJoin = from.join("author");
            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(userJoin.get("id"), user.getId()));
            predicates.add(cb.equal(from.get("resolution"), true));
            query.select(cb.count(from));
            query.where(predicates.toArray(new Predicate[]{}));
            TypedQuery<Long> createQuery = JPA.em().createQuery(query);
            return createQuery.getSingleResult();
        } catch (RuntimeException re) {
            throw re;
        }
    }

    public static Long countSosResolvedDiscussionsByUser(User user) {
        try {

            CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
            CriteriaQuery<Long> query = cb.createQuery(Long.class);
            Root<Message> from = query.from(Message.class);
            Join<Message, User> userJoin = from.join("author");
            Join<Message, Discussion> discussionJoin = from.join("discussion");
            Join<Discussion, DiscussionCategory> discussionCategoriesJoin = discussionJoin.join("categories");
            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(userJoin.get("id"), user.getId()));
            predicates.add(cb.equal(from.get("resolution"), true));
            predicates.add(cb.equal(discussionCategoriesJoin.get("label"), "SOS"));
            query.select(cb.count(from));
            query.where(predicates.toArray(new Predicate[]{}));
            TypedQuery<Long> createQuery = JPA.em().createQuery(query);

            return createQuery.getSingleResult();
        } catch (RuntimeException re) {
            throw re;
        }
    }

    public static Long countMessageByMinLengthAndUser(User user, Integer minLength) {
        try {
            CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
            CriteriaQuery<Long> query = cb.createQuery(Long.class);
            Root<Message> from = query.from(Message.class);
            Join<Message, User> userJoin = from.join("author");
            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(userJoin.get("id"), user.getId()));
            Expression<String> path = from.get("text");
            Expression<Integer> length = cb.length(path);
            predicates.add(cb.ge(length, minLength));
            query.select(cb.count(from));
            query.where(predicates.toArray(new Predicate[]{}));
            TypedQuery<Long> createQuery = JPA.em().createQuery(query);
            return createQuery.getSingleResult();
        } catch (RuntimeException re) {
            throw re;
        }
    }

    public static List<Message> getLastMessages(String tagLabel, TagType tagType, Integer maxResults) {
        StringBuilder request = new StringBuilder();
        request.append("SELECT M.* FROM H_MESSAGE M ");
        request.append("INNER JOIN H_DISCUSSION D on D.id = M.discussion_id ");
        request.append("INNER JOIN H_TAGS_LINKS TL ON TL.target_id = D.id ");
        request.append("INNER JOIN H_TAGS T ON t.id = tl.tag_id ");
        request.append("WHERE TL.link_type = '").append(TagLinkType.DISCUSSION).append("' ");
        request.append("AND T.tag_type = '").append(tagType).append("' ");
        request.append("AND T.label = '").append(tagLabel).append("' ");
        request.append("ORDER BY M.last_update_date desc");
        Query query = JPA.em().createNativeQuery(request.toString(), Message.class);
        query.setMaxResults(maxResults);
        return query.getResultList();
    }

    public static List<Message> getMessagesBySpecimenCode(String code) {
        StringBuilder request = new StringBuilder();
        request.append("SELECT distinct M.* FROM H_MESSAGE M ");
        request.append("INNER JOIN H_DISCUSSION D ON d.id = M.discussion_id ");
        request.append("INNER JOIN H_TAGS_LINKS TL ON TL.target_id = D.id ");
        request.append("INNER JOIN H_TAGS T ON t.id = tl.tag_id ");
        request.append("WHERE TL.link_type = '").append(TagLinkType.DISCUSSION).append("' ");
        request.append("AND T.tag_type = '").append(TagType.SPECIMEN).append("' ");
        request.append("AND T.label = '").append(code).append("' ");
        request.append("ORDER BY M.last_update_date desc");
        return JPA.em().createNativeQuery(request.toString(), Message.class).getResultList();
    }

    public static List<Message> getMessagesByMissionId(Long missionId) {
        StringBuilder request = new StringBuilder();
        Mission mission = Mission.findById(missionId);
        request.append("SELECT distinct M.* FROM H_MESSAGE M ");
        request.append("INNER JOIN H_DISCUSSION D ON d.id = M.discussion_id ");
        request.append("INNER JOIN H_TAGS_LINKS TL ON TL.target_id = D.id ");
        request.append("INNER JOIN H_TAGS T ON t.id = tl.tag_id ");
        request.append("WHERE TL.link_type = '").append(TagLinkType.DISCUSSION).append("' ");
        request.append("AND T.tag_type = '").append(TagType.MISSION).append("' ");
        request.append("AND T.label = '").append(mission.getPrincipalTagString()).append("' ");
        request.append("ORDER BY M.last_update_date desc");
        return JPA.em().createNativeQuery(request.toString(), Message.class).getResultList();
    }

    public static List<Message> getMessagesByDiscussionId(Long discussId) {
        StringBuilder request = new StringBuilder();
        request.append("SELECT distinct M.* FROM H_MESSAGE M ");
        request.append("INNER JOIN H_DISCUSSION D ON d.id = M.discussion_id ");
        request.append("WHERE D.ID = '").append(discussId).append("' ");
        request.append("ORDER BY M.last_update_date asc");
        return JPA.em().createNativeQuery(request.toString(), Message.class).getResultList();
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Discussion getDiscussion() {
        return discussion;
    }

    public void setDiscussion(Discussion discussion) {
        this.discussion = discussion;
    }

    public Boolean isResolution() {
        return resolution;
    }

    public void setResolution(Boolean resolution) {
        this.resolution = resolution;
    }

    public Boolean isFirst() {
        return first;
    }

    public void setFirst(Boolean first) {
        this.first = first;
    }

    public String getModeratorLogin() {
        return moderatorLogin;
    }

    public void setModeratorLogin(String moderatorLogin) {
        this.moderatorLogin = moderatorLogin;
    }

}
