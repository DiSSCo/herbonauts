package models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import play.Logger;
import play.db.jpa.JPA;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Cart {

    public class CartQuery {

        public Long hits;
        public Boolean inCart;
        public List<String> selected;
        public Boolean selectedAll;
        public Map<String, String> query;
        public Boolean loaded;
        public Boolean modified;

    }

    @JsonDeserialize(contentAs = CartQuery.class)
    public List<CartQuery> queries;

    public static class MissionSpecimenCount {
        public Long missionId;
        public String missionTitle;
        public Integer commonSpecimenCount;

        public MissionSpecimenCount() {
        }

        public MissionSpecimenCount(Long missionId, String missionTitle, Integer commonSpecimenCount) {
            this.missionId = missionId;
            this.missionTitle = missionTitle;
            this.commonSpecimenCount = commonSpecimenCount;
        }
    }


    public static List<MissionSpecimenCount> getInCommonMissionCount(Long missionId) {

        String query = "" +
                "select \n" +
                "  m.id, m.title, s.cnt\n" +
                "from (\n" +
                "select \n" +
                "  mission_id, count(*) as cnt\n" +
                "from \n" +
                "  h_specimen\n" +
                "where 1=1\n" +
                "  and master_id in (\n" +
                "    select master_id from h_specimen where mission_id = ?1\n" +
                "  ) \n" +
                "group by \n" +
                "  mission_id\n" +
                ") s\n" +
                "inner join h_mission m\n" +
                "  on s.mission_id = m.id";

        List<Object[]> resultList = JPA.em().createNativeQuery(query)
                .setParameter(1, missionId)
                .getResultList();

        Logger.info("Get common count for mission %d: %d", missionId, resultList.size());


        List<MissionSpecimenCount> counts = new ArrayList<MissionSpecimenCount>();
        for (Object[] missionCount : resultList) {
            Logger.info("line > %s %s %s", missionCount[0], missionCount[1], missionCount[2]);

            BigDecimal id = (BigDecimal) missionCount[0];
            String title = (String) missionCount[1];
            BigDecimal count = (BigDecimal) missionCount[2];

            counts.add(new MissionSpecimenCount(id.longValue(), title, count.intValue()));

        }

        return counts;
    }

}
