package models.serializer.contributions;

import com.google.gson.*;
import models.references.ReferenceRecord;

import java.lang.reflect.Type;

public class ReferenceRecordSimpleJsonSerializer implements JsonSerializer<ReferenceRecord> {

    public static ReferenceRecordSimpleJsonSerializer instance = new ReferenceRecordSimpleJsonSerializer();

    private final JsonParser jsonParser = new JsonParser();

    private ReferenceRecordSimpleJsonSerializer() {}

    public static ReferenceRecordSimpleJsonSerializer get() {
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
        //obj.add("parent", context.serialize(record.getParent()));
        //obj.add("info", context.serialize(record.getInfo()));
        /*if (record.getInfoJson() != null) {
            JsonElement infoJson = jsonParser.parse(record.getInfoJson());
            obj.add("infoJson", infoJson);
        }*/

        return obj;
    }

}
