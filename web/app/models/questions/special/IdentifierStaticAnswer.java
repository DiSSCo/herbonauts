package models.questions.special;

import com.fasterxml.jackson.databind.JsonNode;
import models.references.ReferenceRecord;

import javax.persistence.*;

@Entity
@DiscriminatorValue("IDENTIFIER")
public class IdentifierStaticAnswer extends StaticAnswer<IdentifierStaticAnswer> {

    @ManyToOne
    @JoinColumn(name = "REFERENCE_RECORD_ID")
    public ReferenceRecord referenceRecord;

    @Column(name = "TEXT_VALUE")
    public String textValue;

    @Column(name = "NO_INFO")
    public Boolean noInformation;

    @Override
    protected void buildFromJson(JsonNode node) {
        if (node.hasNonNull("no_info") && node.get("no_info").asBoolean()) {
            this.noInformation = true;
        } else {
            this.textValue = node.get("identifier").get("label").asText();
            this.referenceRecord = new ReferenceRecord(node.get("identifier").get("id").asLong());
        }
    }

    @Override
    public String toString() {
        return "[text:" + textValue + ",noInfo:" + noInformation + "]";
    }
}
