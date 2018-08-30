package models.references;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import play.db.jpa.GenericModel;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name="H_REFERENCE_RECORD_INFO")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReferenceRecordInfo extends GenericModel {

    @Id
    @Column(name="RECORD_ID")
    private Long recordId;

    @Id
    @Column(name = "FIELD_NAME")
    private String name;

    @Column(name = "FIELD_VALUE")
    private String value;

    public ReferenceRecordInfo() {
    }

    public ReferenceRecordInfo(Long recordId, String name, String value) {
        this.recordId = recordId;
        this.name = name;
        this.value = value;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
