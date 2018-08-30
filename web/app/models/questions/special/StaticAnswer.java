package models.questions.special;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Specimen;
import models.questions.ContributionAnswer;
import models.questions.ContributionQuestion;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name="H_CONTRIBUTION_STATIC_VALUE")
@Inheritance(strategy= InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="TYPE", discriminatorType=DiscriminatorType.STRING)
public abstract class StaticAnswer<T extends StaticAnswer> extends Model {

    @Column(name="TYPE", insertable = false, updatable = false)
    private String type;

    @Column(name = "MISSION_ID")
    private Long missionId;

    @Transient
    private Long masterId;

    //@ManyToOne
    //@JoinColumn(name = "SPECIMEN_ID")
    //private Specimen specimen;
    //

    @Column(name = "SPECIMEN_ID")
    private Long specimenId;

    protected abstract void buildFromJson(JsonNode node);

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final Map<String, String> typeByQuestionName = new HashMap<String, String>();
    static {
        typeByQuestionName.put("collector"         , "COLLECTOR");
        typeByQuestionName.put("geo"               , "GEOLOCALISATION");
        typeByQuestionName.put("collect_date"      , "COLLECT_DATE");
        typeByQuestionName.put("locality"          , "LOCALITY");
        typeByQuestionName.put("identifier"        , "IDENTIFIER");
        typeByQuestionName.put("region1"           , "REGION1");
        typeByQuestionName.put("country"           , "COUNTRY");
        typeByQuestionName.put("region2"           , "REGION2");
    }

    public static final String getTypeByQuestionName(String name) {
        return typeByQuestionName.get(name);
    }

    public static final void deleteBySpecimenId(String type, Long specimenId) {
        JPA.em().createNativeQuery("delete from H_CONTRIBUTION_STATIC_VALUE where type = ? and specimen_id = ?")
                .setParameter(1, type)
                .setParameter(2, specimenId)
                .executeUpdate();
    }

    public static final StaticAnswer buildStaticAnswer(ContributionQuestion question, ContributionAnswer answer)
    throws IOException {

        Logger.info("CREATE STATIC ANSWER " + question.getName());

        if ("country".equals(question.getName())) {

            CountryStaticAnswer staticAnswer = new CountryStaticAnswer();
            staticAnswer.buildFromJson(mapper.readTree(answer.getJsonValue()));
            return staticAnswer;

        } else if ("region1".equals(question.getName())) {

            Region1StaticAnswer staticAnswer = new Region1StaticAnswer();
            staticAnswer.buildFromJson(mapper.readTree(answer.getJsonValue()));
            return staticAnswer;

        } else if ("region2".equals(question.getName())) {

            Region2StaticAnswer staticAnswer = new Region2StaticAnswer();
            staticAnswer.buildFromJson(mapper.readTree(answer.getJsonValue()));
            return staticAnswer;

        } else if ("collector".equals(question.getName())) {

            CollectorStaticAnswer staticAnswer = new CollectorStaticAnswer();
            staticAnswer.buildFromJson(mapper.readTree(answer.getJsonValue()));
            return staticAnswer;

        } else if ("identifier".equals(question.getName())) {

            IdentifierStaticAnswer staticAnswer = new IdentifierStaticAnswer();
            staticAnswer.buildFromJson(mapper.readTree(answer.getJsonValue()));
            return staticAnswer;

        } else if ("collect_date".equals(question.getName())) {

            CollectDateStaticAnswer staticAnswer = new CollectDateStaticAnswer();
            staticAnswer.buildFromJson(mapper.readTree(answer.getJsonValue()));
            return staticAnswer;

        } else if ("locality".equals(question.getName())) {

            LocalityStaticAnswer staticAnswer = new LocalityStaticAnswer();
            staticAnswer.buildFromJson(mapper.readTree(answer.getJsonValue()));
            return staticAnswer;

        } else if ("geo".equals(question.getName())) {

            GeolocalisationStaticAnswer staticAnswer = new GeolocalisationStaticAnswer();
            staticAnswer.buildFromJson(mapper.readTree(answer.getJsonValue()));
            return staticAnswer;

        }

        return null;

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getMissionId() {
        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public Long getSpecimenId() {
        return specimenId;
    }

    public void setSpecimenId(Long specimenId) {
        this.specimenId = specimenId;
    }

    public Long getMasterId() {
        return masterId;
    }

    public void setMasterId(Long masterId) {
        this.masterId = masterId;
    }
}
