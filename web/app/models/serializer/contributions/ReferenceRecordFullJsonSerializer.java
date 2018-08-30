package models.serializer.contributions;

import com.google.gson.*;
import models.questions.ContributionQuestion;
import models.references.ReferenceRecord;

import java.lang.reflect.Type;

public class ReferenceRecordFullJsonSerializer implements JsonSerializer<ReferenceRecord> {

    public static ReferenceRecordFullJsonSerializer instance = new ReferenceRecordFullJsonSerializer();

    private final JsonParser jsonParser = new JsonParser();

    private ReferenceRecordFullJsonSerializer() {}

    public static ReferenceRecordFullJsonSerializer get() {
        return instance;
    }

    @Override
    public JsonElement serialize(ReferenceRecord record,
                                 Type type,
                                 JsonSerializationContext context) {

        JsonObject obj = new JsonObject();

        obj.addProperty("id", record.getId());
        obj.addProperty("value", record.getValue());
        obj.addProperty("label", record.getLabel());
        obj.add("parent", context.serialize(record.getParent()));
        obj.add("info", context.serialize(record.getInfo()));
        if (record.getLastUpdateDate() != null) {
            obj.addProperty("lastUpdateDate", record.getLastUpdateDate().getTime());
        }
        /*if (record.getInfoJson() != null) {
            JsonElement infoJson = jsonParser.parse(record.getInfoJson());
            obj.add("infoJson", infoJson);
        }*/

        return obj;
    }

}
