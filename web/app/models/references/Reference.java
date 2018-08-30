package models.references;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="H_REFERENCE")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Reference extends Model {

    @ManyToOne
    @JoinColumn(name="PARENT_ID")
    private Reference parent;

    @Column(name = "NAME")
    private String name;

    @Column(name = "LABEL")
    private String label;

    @Column(name = "ALLOW_USER_CREATION")
    private Boolean allowUserCreation;

    public Reference getParent() {
        return parent;
    }

    public void setParent(Reference parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getAllowUserCreation() {
        return allowUserCreation;
    }

    public void setAllowUserCreation(Boolean allowUserCreation) {
        this.allowUserCreation = allowUserCreation;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public static List<ReferenceRecord> sampleRecords(Long referenceId) {
        List<ReferenceRecord> records = JPA.em()
                .createQuery("select r " +
                                "from ReferenceRecord r " +
                                "where r.reference.id = :refid order by label",
                        ReferenceRecord.class)
                .setParameter("refid", referenceId)
                .setMaxResults(10)
                .getResultList();
        return records;
    }

    public static List<ReferenceRecord> findAllRecords(Long referenceId) {
        List<ReferenceRecord> records = JPA.em()
                .createQuery("select r " +
                        "from ReferenceRecord r " +
                        "where r.reference.id = :refid order by label",
                        ReferenceRecord.class)
                .setParameter("refid", referenceId)
                .getResultList();
        return records;
    }

    public static List<ReferenceRecord> findAllRecords(Long referenceId, Long parentId) {
        List<ReferenceRecord> records = JPA.em()
                .createQuery("select r " +
                        "from ReferenceRecord r " +
                        "where r.reference.id = :refid and " +
                        "      r.parent.id = :parentId order by label",
                        ReferenceRecord.class)
                .setParameter("refid", referenceId)
                .setParameter("parentId", parentId)
                .getResultList();
        return records;
    }




}
