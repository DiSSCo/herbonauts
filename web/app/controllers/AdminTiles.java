package controllers;

import com.google.gson.JsonElement;
import play.Logger;
import play.Play;
import play.libs.WS;

import java.util.HashMap;
import java.util.Map;

public class AdminTiles extends Application {

    public static void tilesAdmin() {
        Security.forbiddenIfNotAdmin();
        render();
    }

    public static void tilesRun() {

        Security.forbiddenIfNotAdmin();

        String tilesCleanerEndpoint = Play.configuration.getProperty("tilesCleaner.endpoint");
        String tilesCleanerApiKey = Play.configuration.getProperty("tilesCleaner.apiKey", "");

        Logger.info("launch tiles clean (%s)", tilesCleanerEndpoint);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("apiKey", tilesCleanerApiKey);

        WS.url(tilesCleanerEndpoint + "/cleaner/run").headers(headers).post().getJson();

        tilesStatus();
    }

    public static void tilesStatus() {

        Security.forbiddenIfNotLeader();

        Logger.info("Get status");

        String tilesCleanerEndpoint = Play.configuration.getProperty("tilesCleaner.endpoint");
        JsonElement usageStatus = WS.url(tilesCleanerEndpoint + "/cleaner/status").get().getJson();

        renderJSON(usageStatus);
    }

    public static void tilesReport() {

        Security.forbiddenIfNotLeader();

        String tilesCleanerEndpoint = Play.configuration.getProperty("tilesCleaner.endpoint");
        String tilesCleanerApiKey = Play.configuration.getProperty("tilesCleaner.apiKey", "");

        Logger.info("get tiles usage report (%s)", tilesCleanerEndpoint);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("apiKey", tilesCleanerApiKey);

        JsonElement usageReport = WS.url(tilesCleanerEndpoint + "/cleaner/usage").headers(headers).get().getJson();


        /*
        tilesCleaner.diskThresholdInBytes.warning=10737418240 # 10 Go
        tilesCleaner.diskThresholdInBytes.danger=21474836480 # 20 Go
        tilesCleaner.averageSpecimenSizeInBytes=3145728 # 3 Mo
        */

        // Status
        Long freeSpaceInBytes = usageReport.getAsJsonObject().get("freeSpaceInBytes").getAsLong();

        Long warningThreshold = Long.valueOf(Play.configuration.getProperty("tilesCleaner.diskThresholdInBytes.warning", "53687091200"));
        Long dangerThreshold = Long.valueOf(Play.configuration.getProperty("tilesCleaner.diskThresholdInBytes.danger", "10737418240"));
        Long unusableSpaceInBytes = Long.valueOf(Play.configuration.getProperty("tilesCleaner.diskThresholdInBytes.unusable", "10737418240"));

        String status = "success";
        if (freeSpaceInBytes - unusableSpaceInBytes < dangerThreshold) {
            status = "danger";
        } else if (freeSpaceInBytes - unusableSpaceInBytes < warningThreshold) {
            status = "warning";
        }

        usageReport.getAsJsonObject().addProperty("status", status);


        // Average specimen space remaining
        Long averageSpecimenSizeInBytes = Long.valueOf(Play.configuration.getProperty("tilesCleaner.averageSpecimenSizeInBytes", "3145728"));
        Long approxSpecimenCountLeft = (freeSpaceInBytes - unusableSpaceInBytes) / averageSpecimenSizeInBytes;

        usageReport.getAsJsonObject().addProperty("approxSpecimenCountLeft", approxSpecimenCountLeft);

        //


        renderJSON(usageReport);
    }



}
