package models.stats;

import models.tags.Tag;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "H_TAGS_USAGE_STAT")
public class TagUsage extends GenericModel {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TAG_ID")
    private Tag tag;

    @Column(name = "COUNT_USAGE")
    private Long countUsage;

    @Column(name = "LAST_USAGE")
    private Date lastUsage;

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public Long getCountUsage() {
        return countUsage;
    }

    public void setCountUsage(Long countUsage) {
        this.countUsage = countUsage;
    }

    public Date getLastUsage() {
        return lastUsage;
    }

    public void setLastUsage(Date lastUsage) {
        this.lastUsage = lastUsage;
    }
}
