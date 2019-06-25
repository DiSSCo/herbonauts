package models.recolnat;

import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@PersistenceUnit(name = "recolnat")
@Table(name = "RECOLTES")
public class RecolnatRecolte extends GenericModel {

    @Id
    @Column(name = "EVENTID")
    public UUID eventId;

    @Column(name = "RECORDEDBY")
    public String recordedBy;

    @Column(name = "FIELDNUMBER")
    public String fieldNumber;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "LOCALISATIONID")
    public RecolnatLocation location;

    @Column(name = "SDAY")
    public Integer startDay;

    @Column(name = "SMONTH")
    public Integer startMonth;

    @Column(name = "SYEAR")
    public Integer startYear;

    @Column(name = "EDAY")
    public Integer endDay;

    @Column(name = "EMONTH")
    public Integer endMonth;

    @Column(name = "EYEAR")
    public Integer endYear;

    @Column(name = "MODIFIED")
    public Date modified;

    @Column(name = "STARTEVENTDATE")
    public Date startEventDate;

    @Column(name = "ENDEVENTDATE")
    public Date endEventDate;

    @Column(name = "EVENTDATE")
    public String eventDate;


}
