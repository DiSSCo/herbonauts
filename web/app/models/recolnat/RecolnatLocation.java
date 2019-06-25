package models.recolnat;

import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@PersistenceUnit(name = "recolnat")
@Table(name = "LOCALISATIONS")
public class RecolnatLocation extends GenericModel {

    @Id
    @Column(name = "LOCALISATIONID")
    public UUID localisationId;

    @Column(name = "COUNTRY")
    public String country;

    @Column(name = "COUNTRYCODE")
    public String countryCode;

    @Column(name = "STATEPROVINCE")
    public String stateProvince;

    @Column(name = "VERBATIMLOCALITY")
    public String verbatimLocality;

    @Column(name = "DECIMALLATITUDE")
    public Double decimalLatitude;

    @Column(name = "DECIMALLONGITUDE")
    public Double decimalLongitude;

    @Column(name = "MODIFIED")
    public Date modified;

}
