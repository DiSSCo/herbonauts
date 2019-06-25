package fr.mnhn.herbonautes.cleantilesbatch.specimen;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "H_SPECIMEN")
public class Specimen {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "INSTITUTE")
    private String institute;

    @Column(name = "COLLECTION")
    private String collection;

    @Column(name = "CODE")
    private String code;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getInstitute() {
        return institute;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
