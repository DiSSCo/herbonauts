package models.recolnat;

import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@PersistenceUnit(name = "recolnat")
@Table(name = "SPECIMENS")
public class RecolnatSpecimen extends GenericModel {

    @Id
    @Column(name = "OCCURRENCEID")
    public UUID occurenceId;

    @Column(name = "CATALOGNUMBER")
    public String catalogNumber;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "EVENTID")
    public RecolnatRecolte recolte;

    @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "specimen")
    public List<RecolnatDetermination> determinations;

    @Column(name = "OCCURRENCEREMARKS")
    public String occurrenceRemarks;

    @Column(name = "MODIFIED")
    public Date modified;



}
