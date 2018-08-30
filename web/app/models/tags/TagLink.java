package models.tags;

import models.GenericDatedModificationsModel;
import play.db.jpa.JPA;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Jonathan on 17/06/2015.
 */
@Entity
@Table(name = "H_TAGS_LINKS")
public class TagLink extends GenericDatedModificationsModel<TagLink> {

    @Id
    @Column(name = "tag_id")
    private Long tagId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "link_type")
    private TagLinkType linkType;

    @Id
    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "principal")
    public boolean principal;

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

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    public TagLinkType getLinkType() {
        return linkType;
    }

    public void setLinkType(TagLinkType linkType) {
        this.linkType = linkType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public void setPrincipal(boolean principal) {
        this.principal = principal;
    }

    public static final void deleteByLinkTypeAndTargetId(TagLinkType linkType, Long targetId) {
        JPA.em().createQuery("delete from TagLink WHERE linkType = ?1 and targetId = ?2")
                .setParameter(1, linkType)
                .setParameter(2, targetId)
                .executeUpdate();
    }

    public static final void deleteByTagIdLinkTypeAndTargetId(Long tagId, TagLinkType linkType, Long targetId) {
        JPA.em().createQuery("delete from TagLink WHERE linkType = ?1 and targetId = ?2 and tagId = ?3")
                .setParameter(1, linkType)
                .setParameter(2, targetId)
                .setParameter(3, tagId)
                .executeUpdate();
    }

    @Override
    public Object _key() {
        return getTagId();
    }

}
