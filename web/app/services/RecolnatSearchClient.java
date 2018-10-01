package services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import play.libs.WS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RecolnatSearchClient {

    private static final String BASE_URL = "http://search.recolnat.org/";

    private ObjectMapper mapper = new ObjectMapper();

    public RecolnatSpecimen getSpecimen(String index, String id) {
        String json = WS.url(BASE_URL + index + "/specimens/" + id).get().getString();
        RecolnatSpecimen specimen = null;
        try {
            JsonNode jsonNode = mapper.readTree(json);
            //specimen = mapper.treeToValue(jsonNode.get("_source"), RecolnatSpecimen.class);
            specimen = parse(jsonNode.get("_source"));
            specimen._id = jsonNode.get("_id").asText();
        } catch (IOException e) {
            Logger.error(e, "Error parsing specimen");
        }
        return specimen;
    }

    public List<RecolnatSpecimen> search(String index, Map<String, String> query, Boolean noCollectInfo, int page, int pageSize) {
        //JsonElement result = WS.url("").post().getJson();
        //result.getAsJsonObject().get("");

        int i = (page - 1) * pageSize;

        String jsonQuery = buildJsonQuery(query, noCollectInfo);

        Logger.info("Query : %s", jsonQuery);

        WS.WSRequest body = WS.url(BASE_URL + index + String.format("/_search?from=%d&size=%d", i, pageSize))
                .body(jsonQuery);

        String json = body
                        .post()
                        .getString();

        ArrayList<RecolnatSpecimen> specimens = new ArrayList<RecolnatSpecimen>();;

        try {
            JsonNode jsonNode = mapper.readTree(json);
            for (JsonNode node : jsonNode.get("hits").get("hits")) {

                RecolnatSpecimen specimen = null;

                try {
                    specimen = parse(node.get("_source"));
                    specimen._id = node.get("_id").asText();
                    specimens.add(specimen);
                } catch (Exception e) {
                    Logger.error(e, "Error parsing specimen %s", node.get("_source"));
                }
            }
            //Logger.info("" + specimens);

        } catch (IOException e) {
            Logger.error(e, "Error parsing specimens");
        }

       // Logger.info(jsonQuery);
        //Logger.info(json);

        return specimens;
    }

    private String getValueOrArray(JsonNode node, String property) {
        if (node.get(property).isArray()) {
            Logger.info("%s is array : %s -> %s", property, node.get(property).toString(), node.get(property).get(0).asText());
            return node.get(property).get(0).asText();
        } else {
            return node.get(property).asText();
        }
    }

    private RecolnatSpecimen parse(JsonNode node) throws JsonProcessingException {
        RecolnatSpecimen specimen = mapper.treeToValue(node, RecolnatSpecimen.class);
        specimen.family = getValueOrArray(node, "family");
        specimen.genus = getValueOrArray(node, "genus");
        specimen.specificEpithet = getValueOrArray(node, "specificepithet");
        return specimen;
    }

    private String buildJsonQuery(Map<String, String> query, Boolean noCollectInfo) {
        ObjectNode jsonQuery = mapper.createObjectNode();
        ArrayNode andTerms = buildJsonTerms(query, noCollectInfo);

        jsonQuery.set("query",
            mapper.createObjectNode().set("filtered",
                mapper.createObjectNode().set("filter",
                    mapper.createObjectNode().set("and",andTerms))));

        jsonQuery.put("sort", "_score");

        return jsonQuery.toString();
    }

    private ArrayNode buildJsonTerms(Map<String, String> query, Boolean noCollectInfo) {
        ArrayList<JsonNode> terms = new ArrayList<JsonNode>();

        for (Map.Entry<String, String> e : query.entrySet()) {

            if (e.getValue() != null && e.getValue().trim().length() > 0) {

                JsonNode term = mapper.createObjectNode().set("term",
                    mapper.createObjectNode().put("_cache", false)
                                             .put(e.getKey(), e.getValue()));
                terms.add(term);
            }

        }

        // Has image
        /*    */

        JsonNode hasMedia = mapper.createObjectNode().set("term",
                mapper.createObjectNode().put("_cache", false)
                        .put("hasmedia", 1));

        terms.add(hasMedia);





        //  {
        // "exists": {
        //     "field": "L_COUNTRY"
        //   }
        // }

        //JsonNode collectCriteria = mapper.createObjectNode().set(noCollectInfo ? "missing" : "exists",
        //        mapper.createObjectNode().put("field", "country"));


        ObjectNode collectCriteria = mapper.createObjectNode();

        if (noCollectInfo) {
            ArrayNode collectionFields = mapper.createArrayNode();


            collectionFields.add(mapper.createObjectNode().set("missing", mapper.createObjectNode().put("field", "country")));
            collectionFields.add(mapper.createObjectNode().set("missing", mapper.createObjectNode().put("field", "recordedby")));

            collectCriteria.set("and", collectionFields);

        } else {
            ArrayNode collectionFields = mapper.createArrayNode();

            collectionFields.add(mapper.createObjectNode().set("exists", mapper.createObjectNode().put("field", "country")));
            collectionFields.add(mapper.createObjectNode().set("exists", mapper.createObjectNode().put("field", "recordedby")));

            collectCriteria.set("or", collectionFields);
        }

        terms.add(collectCriteria);

        return mapper.createArrayNode().addAll(terms);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecolnatResponse {

          public Long hits;


    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecolnatSpecimen {

        @JsonProperty("_id")
        public String _id;

        @JsonProperty("catalognumber")
        public String catalogNumber;

        @JsonProperty("collectioncode")
        public String collection;

        @JsonProperty("institutioncode")
        public String institution;

        //@JsonProperty("family")
        @JsonIgnore
        public String family;

        //@JsonProperty("genus")
        @JsonIgnore
        public String genus;

        //@JsonProperty("specificepithet")
        @JsonIgnore
        public String specificEpithet;

        @JsonDeserialize(contentAs = RecolnatSpecimenMedia.class)
        @JsonProperty("m_")
        public List<RecolnatSpecimenMedia> media;

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecolnatSpecimenMedia {

        @JsonProperty
        public String id;

        @JsonProperty("identifier")
        public String url;

    }

}
