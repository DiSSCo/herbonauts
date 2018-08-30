package models.questions.special;

import com.fasterxml.jackson.databind.JsonNode;
import models.references.ReferenceRecord;

import javax.persistence.*;

@Entity
@DiscriminatorValue("REGION1")
public class Region1StaticAnswer extends StaticAnswer<Region1StaticAnswer> {

    @ManyToOne
    @JoinColumn(name = "REFERENCE_RECORD_ID")
    public ReferenceRecord referenceRecord;

    @Column(name = "TEXT_VALUE")
    public String textValue;

    @Column(name = "NO_INFO")
    public Boolean noInformation;

    @Override
    protected void buildFromJson(JsonNode node) {
        if (node.hasNonNull("no_info") && node.get("no_info").asBoolean() ||
                !node.hasNonNull("region") ||
                !node.get("region").hasNonNull("label")) {
            this.noInformation = true;
        } else {
            this.textValue = node.get("region").get("label").asText();
            this.referenceRecord = new ReferenceRecord(node.get("region").get("id").asLong());
        }
    }

    @Override
    public String toString() {
        return "[text:" + textValue + ",noInfo:" + noInformation + "]";
    }
}
