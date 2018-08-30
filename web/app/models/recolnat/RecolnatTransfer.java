package models.recolnat;

import jobs.RecolnatTransferJob;
import play.db.jpa.GenericModel;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "H_RECOLNAT_TRANSFER")
public class RecolnatTransfer extends Model {

    @Column(name = "SPECIMEN_ID")
    public Long specimenId;

    @Column(name = "QUESTION_NAME")
    public String questionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "NO_TRANSFER_CAUSE")
    public RecolnatTransferJob.NoTransferCause noTransferCause;

    @Column(name = "TRANSFER_DONE")
    public Boolean transferDone;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TRANSFER_DATE")
    public Date transferDate;

    public static RecolnatTransfer buildtransferFail(Long specimenId, String questionName, RecolnatTransferJob.NoTransferCause cause) {
        RecolnatTransfer transfer = new RecolnatTransfer();
        transfer.specimenId = specimenId;
        transfer.questionName = questionName;
        transfer.noTransferCause = cause;
        transfer.transferDone = false;
        transfer.transferDate = new Date();
        return transfer;
    }

    public static RecolnatTransfer buildtransferSuccess(Long specimenId, String questionName) {
        RecolnatTransfer transfer = new RecolnatTransfer();
        transfer.specimenId = specimenId;
        transfer.questionName = questionName;
        transfer.transferDone = true;
        transfer.transferDate = new Date();
        return transfer;
    }



}



