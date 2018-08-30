package models.recolnat;

import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@PersistenceUnit(name = "recolnat")
@Table(name = "DETERMINATIONS")
public class RecolnatDetermination extends GenericModel {

    @Id
    @Column(name = "IDENTIFICATIONID")
    public UUID identificationID;

    @Column(name = "IDENTIFIEDBY")
    public String identifiedBy;

    @ManyToOne
    @JoinColumn(name = "OCCURRENCEID")
    public RecolnatSpecimen specimen;

    @Column(name = "MODIFIED")
    public Date modified;

    @Column(name = "IDENTIFICATIONVERIFSTATUS")
    public Boolean verifStatus;

}
