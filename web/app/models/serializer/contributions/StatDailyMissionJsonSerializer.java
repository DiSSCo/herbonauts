package models.serializer.contributions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import models.quiz.Quiz;
import models.stats.StatDailyMission;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;

public class StatDailyMissionJsonSerializer implements JsonSerializer<StatDailyMission> {

    public static StatDailyMissionJsonSerializer instance = new StatDailyMissionJsonSerializer();

    private StatDailyMissionJsonSerializer() {
    }

    public static StatDailyMissionJsonSerializer get() {
        return instance;
    }

    //private SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

    @Override
    public JsonElement serialize(StatDailyMission stat,
                                 Type type,
                                 JsonSerializationContext ctx) {

        JsonObject obj = new JsonObject();

        obj.addProperty("missionId", stat.getMissionId());
        obj.addProperty("type", stat.getType().toString());
        obj.addProperty("value", stat.getValue());
        obj.addProperty("date", stat.getDate().getTime());

        return obj;

    }
}
