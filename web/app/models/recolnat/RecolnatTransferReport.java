package models.recolnat;

import play.db.jpa.JPA;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RecolnatTransferReport {

    public Integer specimenCompleteCount = 0;
    public Integer specimenPartialCount = 0;
    public Integer specimenEmptyCount = 0;

    public Integer questionTransfered = 0;
    public Integer questionNotTransfered = 0;

    public Map<String, Integer> noTransferCause = new HashMap<String, Integer>();
    public Map<String, Integer> tranferByQuestion = new HashMap<String, Integer>();


    private static void incr(Map<String, Integer> map, String key) {
        Integer i = map.get(key);
        if (i == null) {
            i = 0;
        }
        i++;
        map.put(key, i);
    }

    public static RecolnatTransferReport getReport(Long missionId) {
        RecolnatTransferReport report = new RecolnatTransferReport();


        List<RecolnatTransfer> transfers = JPA.em().createNativeQuery("select t.* " +
                "from h_recolnat_transfer t inner join h_specimen s on t.specimen_id = s.id " +
                "where s.mission_id = :missionId", RecolnatTransfer.class)
                .setParameter("missionId", missionId)
                .getResultList();

        //report.transferList = transfers;

        HashMap<Long, SpecimenTranfer> specimenTransfers = new HashMap<Long, SpecimenTranfer>();

        for (RecolnatTransfer t : transfers) {
            SpecimenTranfer st = specimenTransfers.get(t.specimenId);
            if (st == null) {
                st = new SpecimenTranfer();
            }
            specimenTransfers.put(t.specimenId, st);
            st.questions++;
            if (t.transferDone) {
                report.questionTransfered++;
                st.transfers++;
                incr(report.tranferByQuestion, t.questionName);
            } else {
                report.questionNotTransfered++;
                incr(report.noTransferCause, t.noTransferCause.toString());
            }


        }

        for (SpecimenTranfer t : specimenTransfers.values()) {
            if (t.transfers >= (t.questions - 1)) {
                report.specimenCompleteCount++;
            } else if (t.transfers > 0) {
                report.specimenPartialCount++;
            } else {
                report.specimenEmptyCount++;
            }
        }




        return report;
    }

    public static class SpecimenTranfer {
        public Integer questions = 0;
        public Integer transfers = 0;
    }

}



