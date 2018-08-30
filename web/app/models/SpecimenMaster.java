package models;


import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Spécimen
 */
//@Entity
//@Table(name="H_SPECIMEN_MASTER")
public class SpecimenMaster {

    private Long id;

    private String institute;
    private String collection;
    private String code;

    private String family;
    private String genus;
    private String specificEpithet;

    private String sonneratURL;
    private String tropicosURL;

    public String getTropicosURL() {
        return tropicosURL;
    }

    public void setTropicosURL(String tropicosURL) {
        this.tropicosURL = tropicosURL;
    }



    public String getInstitute() {

        return institute;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getGenus() {
        return genus;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    public String getSpecificEpithet() {
        return specificEpithet;
    }

    public void setSpecificEpithet(String specificEpithet) {
        this.specificEpithet = specificEpithet;
    }

    public String getSonneratURL() {
        return sonneratURL;
    }

    public void setSonneratURL(String sonneratURL) {
        this.sonneratURL = sonneratURL;
    }

    public static Integer getContributorsCount(List<Specimen> specimens) {
        HashSet<Long> distinctUsers = new HashSet<Long>();

        for (Specimen specimen : specimens) {
            List<Long> userIds = Specimen.find("select distinct c.userId " +
                    "from ContributionAnswer c " +
                    "where c.deleted != true and c.userId is not null and c.specimenId = ?", specimen.id)
                    .fetch();
            distinctUsers.addAll(userIds);
        }
        return distinctUsers.size();
    }

    public static Long getContributionsCount(List<Specimen> specimens) {
        Long count = 0l;
        for (Specimen specimen : specimens) {
            count += Specimen.count("select count(c) from ContributionAnswer c where c.deleted != true and c.userId is not null and c.specimenId = ?", specimen.id);
        }
        return count;
    }

    public List<Specimen.SpecimenAttribute>  getAttributesForUser(Long id) {
        List<Specimen> specimens = Specimen.find("master.id = ?", this.id).fetch();
        List<Specimen.SpecimenAttribute> attributes = new ArrayList<Specimen.SpecimenAttribute>();
        for (Specimen s : specimens) {
            attributes.addAll(s.getAttributesForUser(id));
        }
        return attributes;
    }

    public String getGenusSpecies() {
        if (this.specificEpithet == null) {
            // Rattrapage v1
            StringBuilder sb = new StringBuilder();
            if (this.family != null && !"null".equals(this.family)) {
                sb.append(this.family).append(" ");
            }
            if (this.genus != null && !"null".equals(this.genus)) {
                sb.append(this.genus);
            }
            if (sb.length() == 0) {
                sb.append("indet.");
            }
            return sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            if (this.genus != null && !"null".equals(this.genus)) {
                sb.append(this.genus).append(" ");
            }
            if (this.specificEpithet != null && !"null".equals(this.specificEpithet)) {
                sb.append(this.specificEpithet);
            }
            if (sb.length() == 0) {
                sb.append("indet.");
            }
            return sb.toString();
        }
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
