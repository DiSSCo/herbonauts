package models.references;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import models.DatedModificationsModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="H_REFERENCE_RECORD")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReferenceRecord extends DatedModificationsModel<ReferenceRecord> {

    @Column(name = "ID_V1")
    private Long idV1;

    @ManyToOne
    @JoinColumn(name="REFERENCE_ID")
    private Reference reference;

    @ManyToOne
    @JoinColumn(name="PARENT_ID")
    private ReferenceRecord parent;

    @Column(name = "VALUE")
    private String value;

    @Column(name = "LABEL")
    private String label;

    //@Lob
    //@Column(name = "INFO_JSON")
    //private String infoJson;

    //@ElementCollection
    //@MapKeyColumn(name="FIELD_NAME")
    //@Column(name="FIELD_VALUE")
    //@CollectionTable(name="H_REFERENCE_RECORD_INFO", joinColumns=@JoinColumn(name="RECORD_ID"))
    // private Map<String, String> info = new HashMap<String, String>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECORD_ID")
    private List<ReferenceRecordInfo> info;

    @Column(name = "IMAGE_ID")
    private Long imageId;

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

    public ReferenceRecord(Long id) {
        super();
        this.id = id;
    }

    public ReferenceRecord() {
        super();
    }

    public ReferenceRecord getParent() {
        return parent;
    }

    public void setParent(ReferenceRecord parent) {
        this.parent = parent;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Reference getReference() {

        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public List<ReferenceRecordInfo> getInfo() {
        return info;
    }

    public void setInfo(List<ReferenceRecordInfo> info) {
        this.info = info;
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public static final ReferenceRecord findByLabelAndRef(String label, Long refId) {
        List<ReferenceRecord> records =
                ReferenceRecord.find("label = ? and reference.id = ?", label, refId).fetch();

        if (records.size() > 0) {
            return records.get(0);
        }
        return null;
    }

    public Long getIdV1() {
        return idV1;
    }

    public void setIdV1(Long idV1) {
        this.idV1 = idV1;
    }

    public static final List<ReferenceRecord> search(Long referenceId, String term, Long parentId, Integer limit) {
        if (term == null) {
            return new ArrayList<ReferenceRecord>();
        }

        if ("*".equals(term)) {
            if (parentId == null) {
                List<ReferenceRecord> records =
                        ReferenceRecord.find("reference.id = ? order by label", referenceId)
                                .fetch(limit);
                return records;
            } else {
                List<ReferenceRecord> records =
                        ReferenceRecord.find("reference.id = ? and parent.id = ? order by label", referenceId, parentId)
                                .fetch(limit);
                return records;
            }
        }

        String likeValue = term.toLowerCase().replace(' ', '%');//replace('*', '%');

        if (parentId == null) {
            List<ReferenceRecord> records =
                    ReferenceRecord.find("reference.id = ? and lower(label) like ? order by label", referenceId, "%" + likeValue + "%")
                                    .fetch(limit);

            return records;
        } else {
            List<ReferenceRecord> records =
                    ReferenceRecord.find("reference.id = ? and parent.id = ? and lower(label) like ? order by label", referenceId, parentId, "%" + likeValue + "%").fetch(limit);

            return records;
        }
    }

}
