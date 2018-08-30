package models.questions.special;

import com.fasterxml.jackson.databind.JsonNode;
import models.references.ReferenceRecord;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Date;

@Entity
@DiscriminatorValue("COLLECT_DATE")
public class CollectDateStaticAnswer extends StaticAnswer<CollectDateStaticAnswer> {

    @Column(name = "START_DATE")
    private Date collectStartDate;

    @Column(name = "END_DATE")
    private Date collectEndDate;

    @Column(name = "NO_INFO")
    public Boolean noInformation;

    @Override
    protected void buildFromJson(JsonNode node) {
        if (node.hasNonNull("no_info") && node.get("no_info").asBoolean()) {
            this.noInformation = true;
        } else {
            if (node.hasNonNull("collect_date")
                    && node.get("collect_date").hasNonNull("start")
                    && node.get("collect_date").get("start").hasNonNull("ts")) {
                this.collectStartDate = new Date(node.get("collect_date").get("start").get("ts").asLong());
            }
            if (node.hasNonNull("collect_date")
                    && node.get("collect_date").hasNonNull("end")
                    && node.get("collect_date").get("end").hasNonNull("ts")) {
                this.collectEndDate = new Date(node.get("collect_date").get("end").get("ts").asLong());
            }
        }
    }

    public Boolean getNoInformation() {
        return noInformation;
    }

    public void setNoInformation(Boolean noInformation) {
        this.noInformation = noInformation;
    }

    public Date getCollectStartDate() {
        return collectStartDate;
    }

    public void setCollectStartDate(Date collectStartDate) {
        this.collectStartDate = collectStartDate;
    }

    public Date getCollectEndDate() {
        return collectEndDate;
    }

    public void setCollectEndDate(Date collectEndDate) {
        this.collectEndDate = collectEndDate;
    }
}
