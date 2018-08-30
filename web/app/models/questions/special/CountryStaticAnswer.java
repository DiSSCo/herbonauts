package models.questions.special;

import com.fasterxml.jackson.databind.JsonNode;
import models.Country;
import models.contributions.Contribution;
import models.references.ReferenceRecord;

import javax.persistence.*;

@Entity
@DiscriminatorValue("COUNTRY")
public class CountryStaticAnswer extends StaticAnswer<CountryStaticAnswer> {

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
                !node.hasNonNull("country") ||
                !node.get("country").hasNonNull("label")) {
            this.noInformation = true;
        } else {
            this.textValue = node.get("country").get("label").asText();
            this.referenceRecord = new ReferenceRecord(node.get("country").get("id").asLong());
        }
    }

    @Override
    public String toString() {
        return "[text:" + textValue + ",noInfo:" + noInformation + "]";
    }
}
