package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import conf.Herbonautes;
import inspectors.Event;
import inspectors.InspectorChain;
import libs.Images;
import models.Image;
import models.User;
import models.quiz.Choice;
import models.quiz.Question;
import models.quiz.Quiz;
import models.serializer.*;
import models.serializer.activities.TimestampSinceJsonSerializer;
import models.serializer.contributions.SimpleQuizJsonSerializer;
import models.stats.TagUsage;
import models.stats.TopContributorStat;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.i18n.Messages;
import services.JPAUtils;
import services.Page;
import services.ReferenceUtils;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Statistiques générales
 */
public class Stats extends Application {

	public static void show() {
		render();
	}



    public static void totalDistCountries(Long missionId) {
        List countries = null;



        if (missionId == null) {
            countries = JPA.em().createNativeQuery("select TEXT_VALUE, REFERENCE_RECORD_ID, count(*) from H_CONTRIBUTION_STATIC_VALUE " +
                    "where type = 'COUNTRY' and TEXT_VALUE is not null " +
                    "group by TEXT_VALUE, REFERENCE_RECORD_ID " +
                    "order by 3 desc").getResultList();
        } else {
            countries = JPA.em().createNativeQuery("select TEXT_VALUE, REFERENCE_RECORD_ID, count(*) from H_CONTRIBUTION_STATIC_VALUE " +
                    "where type = 'COUNTRY' and TEXT_VALUE is not null and MISSION_ID = :missionId " +
                    "group by TEXT_VALUE, REFERENCE_RECORD_ID " +
                    "order by 3 desc")
                    .setParameter("missionId", missionId)
                    .getResultList();
        }
        renderJSON(countries);
    }

    public static void totalDistRegions1(Integer country, Integer missionId) {

        String querySQL = "select TEXT_VALUE, REFERENCE_RECORD_ID, count(*) from H_CONTRIBUTION_STATIC_VALUE c " +
                (country != null ? "inner join h_reference_record r on c.reference_record_id = r.id " : "") +
                "where type = 'REGION1' and TEXT_VALUE is not null " +
                (country != null ? "and r.parent_id = :refId " : "") +
                (missionId != null ? "and c.mission_id = :missionId " : "") +
                "group by TEXT_VALUE, REFERENCE_RECORD_ID " +
                "order by 3 desc";

        Query nativeQuery = JPA.em().createNativeQuery(querySQL);

        if (country != null) {
            nativeQuery.setParameter("refId", country);
        }
        if (missionId != null) {
            nativeQuery.setParameter("missionId", missionId);
        }

        List countries = nativeQuery.getResultList();

        renderJSON(countries);
    }

    public static void totalDistBotanists(Integer page, Integer pageSize, Integer missionId) {

        if (page == null) {
            page = 1;
        }

        if (pageSize == null) {
            pageSize = 10;
        }

        Query nativeQuery = JPA.em().createNativeQuery("select text_value, reference_record_id, count(*) " +
                "from H_CONTRIBUTION_STATIC_VALUE " +
                "where type IN ('COLLECTOR', 'IDENTIFIER') and reference_record_id is not null " +
                        (missionId != null ? " and mission_id = :missionId ": "") +
                "group by reference_record_id, text_value " +
                "order by 3 desc");

        if (missionId != null) {
            nativeQuery.setParameter("missionId", missionId);
        }

        List botanists = nativeQuery
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();

        renderJSON(botanists);
    }



    public static void topTagUsage(Integer page, Integer pageSize) {

        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }

        List<TagUsage> tagUsages = TagUsage.find("order by countUsage desc").fetch(page, pageSize);

        renderJSON(tagUsages, TagUsageSerializer.get());
    }

    public static void centuryDist(Long missionId) {

        String querySQL = "select TRUNC(START_DATE, 'CC'), count(*)  from H_CONTRIBUTION_STATIC_VALUE " +
                "WHERE TYPE = 'COLLECT_DATE' AND START_DATE IS NOT NULL " +
                (missionId != null ? " and MISSION_ID = :missionId " : "") +
                " AND START_DATE < :now " +
                "GROUP BY TRUNC(START_DATE, 'CC') " +
                "ORDER BY 1 desc";


        Query nativeQuery = JPA.em().createNativeQuery(querySQL);
        if (missionId != null) {
            nativeQuery.setParameter("missionId", missionId);
        }
        nativeQuery.setParameter("now", new Date());
        List centuries = nativeQuery.getResultList();
        renderJSON(centuries, TimestampJsonSerializer.get());
    }

    public static void topInstitution(Integer page, Integer pageSize) {

        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }

        List<Object[]> institutions = JPA.em().createNativeQuery("select institute, count(*) from h_specimen group by institute order by 2 desc")
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();

        for (Object[] result : institutions) {
            String label = ReferenceUtils.getInstitutionLabel((String) result[0]);
            if (label != null) {
                result[0] = label;
            }
        }

        renderJSON(institutions);
    }

    public static void lastTagUsage(Integer page, Integer pageSize) {

        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }

        List<TagUsage> tagUsages = TagUsage.find("order by lastUsage desc").fetch(page, pageSize);

        renderJSON(tagUsages, TagUsageSerializer.get());
    }




    public static void totalContributionByDay(Long missionId) {
        Query nativeQuery = JPA.em().createNativeQuery("select trunc(a.created_at, 'DD'), count(*) " +
                "from H_CONTRIBUTION_ANSWER A " +
                "where A.USER_ID IS NOT NULL AND A.DELETED != 1 " +
                        (missionId != null ? " AND MISSION_ID = :missionId " : "") +
                "group by trunc(a.created_at, 'DD') " +
                "order by 1");

        if (missionId != null) {
            nativeQuery.setParameter("missionId", missionId);
        }

        List list = nativeQuery.getResultList();

        //System.out.println(((Object[]) list.get(0))[0].getClass());

        renderJSON(list, TimestampJsonSerializer.get());
    }

    public static void specimenDetails() {
        List list = JPA.em().createNativeQuery("select sum(validated),  sum(unusable_validated), sum(conflicts) from H_CONTRIBUTION_SPECIMEN_STAT")
                .getResultList();

        renderJSON(list);
    }

    public static void topContributors(Integer page, Integer pageSize) {

        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
            pageSize = 3;
        }

        Page<TopContributorStat> top = JPAUtils.getPage(TopContributorStat.class, page, pageSize, "answerCount", "desc");
        renderJSON(top, UserSimpleJsonSerializer.get());
    }

    public static void lastRegisteredUsers(Integer page, Integer pageSize) {
        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
            pageSize = 3;
        }

        JPAUtils.PredicateBuilder predicateBuilder = new JPAUtils.PredicateBuilder() {
            @Override
            public Predicate buildPredicate(CriteriaBuilder cb, Root root) {
                return cb.isFalse(root.get("deleted"));
            }
        };


        Page<User> top = JPAUtils.getPage(User.class, page, pageSize, predicateBuilder,  "registrationDate", "desc");
        renderJSON(top, UserSimpleWithDateJsonSerializer.get());
    }

}