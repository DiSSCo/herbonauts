package models.questions.special;

import com.fasterxml.jackson.databind.JsonNode;
import models.references.ReferenceRecord;
import play.Logger;

import javax.persistence.*;

@Entity
@DiscriminatorValue("GEOLOCALISATION")
public class GeolocalisationStaticAnswer extends StaticAnswer<GeolocalisationStaticAnswer> {

    @Column(name = "NO_INFO")
    public Boolean noInformation;

    @Column(name = "LATITUDE")
    public Double latitude;

    @Column(name = "LONGITUDE")
    public Double longitude;

    @Override
    protected void buildFromJson(JsonNode node) {
        if (node.hasNonNull("no_info") && node.get("no_info").asBoolean()) {
            this.noInformation = true;
        } else {

            Logger.info("NODE " + node.toString());
            Logger.info("node " + node.get("position"));
            Logger.info("node " + node.get("position").get("lat"));
            this.latitude = node.get("position").get("lat").asDouble();


            this.longitude = node.get("position").get("lng").asDouble();
        }
    }


}
