package models.recolnat;

import conf.Herbonautes;
import models.Specimen;
import play.db.jpa.GenericModel;
import play.mvc.Router;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@PersistenceUnit(name = "recolnat")
@Table(name = "OPERATION")
@SequenceGenerator(name = "OPERATION_ID_SEQ", sequenceName = "OPERATION_ID_SEQ")
public class RecolnatOperation extends GenericModel {

    public static Integer OPERATION_TYPE_INSERT = 1;
    public static Integer OPERATION_TYPE_UPDATE = 2;
    public static Integer OPERATION_TYPE_DELETE = 3;

    public static String TABLE_SPECIMENS = "SPECIMENS";
    public static String TABLE_RECOLTES = "RECOLTES";
    public static String TABLE_LOCALISATIONS = "LOCALISATIONS";
    public static String TABLE_DETERMINATIONS= "DETERMINATIONS";

    public static Integer HERBONAUTES_MODULE_ID = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "OPERATION_ID_SEQ")
    public Long id;

    @Column(name = "OCCURRENCEID")
    public UUID occurrenceId;

    @Column(name = "TRANSFERDATE")
    public Date transferDate;

    @Column(name = "TABLE_NAME")
    public String tableName;

    @Column(name = "COLUMN_NAME")
    public String columnName;

    @Column(name = "MODULEID")
    public Integer moduleId;

    @Column(name = "REFERENCEURL")
    public String referenceUrl;

    @Column(name = "OPERATION_TYPE_ID")
    public Integer operationTypeId;


    public static RecolnatOperation base(Specimen specimenH, RecolnatSpecimen specimen) {
        RecolnatOperation operation = new RecolnatOperation();
        operation.occurrenceId = specimen.occurenceId;
        operation.transferDate = new Date();
        operation.moduleId = HERBONAUTES_MODULE_ID;
        operation.referenceUrl = Router.getBaseUrl() + "/specimens/" + specimenH.getInstitute()
                +"/"+specimenH.getCollection() + "/" + specimenH.getCode();
        return operation;
    }

    public static RecolnatOperation update(Specimen specimenH, RecolnatSpecimen specimen, String table, String column) {
        RecolnatOperation operation = base(specimenH, specimen);
        operation.operationTypeId = OPERATION_TYPE_UPDATE;
        operation.tableName = table;
        operation.columnName = column;
        return operation;
    }



}
