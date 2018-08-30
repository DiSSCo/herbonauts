package helpers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import conf.Herbonautes;
import play.Logger;
import play.cache.Cache;
import play.libs.WS;

import java.util.HashMap;
import java.util.Map;

public class RecolnatAPIClient {

    public static String ENDPOINT = Herbonautes.get().recolnatApiEndpoint; //"https://api.recolnat.org/erecolnat/v1";
    public static String API_KEY = Herbonautes.get().recolnatApiKey; //"1dc0ab34-9ad0-094f-e053-8414a8c0b09b";



    public static RecolnatUser getUserByLogin(String login) {

        Logger.info("USER BY LOGIN " + login);

        JsonElement jsonUser = WS.url(ENDPOINT + "/users/login/%s", login)
                .setHeader("apiKey", API_KEY)
                .get()
                .getJson();

        Gson gson = new Gson();
        return gson.fromJson(jsonUser, RecolnatUser.class);
    }


    public static boolean updateAvatar(RecolnatUser user, String data) {

        //String body = data;

        //Logger.info("Request : %s", body);



        WS.HttpResponse response;
        Gson gson = new Gson();
        if (user.avatar == null) {

            user.avatar = null;
            user.has_avatar = false;

            response = WS.url(ENDPOINT + "/users/%s", user.user_uuid)
                    .setHeader("apiKey", API_KEY)
                    .setHeader("Content-Type", "application/json")
                    .body(gson.toJson(user))
                    .put();

        } else {

            String body = String.format("{\"has_avatar\":true,\"avatar\": { \"data\": \"%s\" }}", data);

            Logger.info("Update avatar (%s) : \n PUT %s \n %s", user.login, ENDPOINT + "/users/" + user.user_uuid, body);


            response = WS.url(ENDPOINT + "/users/" + user.user_uuid)
                    .setHeader("apiKey", API_KEY)
                    .setHeader("Content-Type", "application/json")
                    .body(body)
                    .put();
        }

        Logger.info("Response (update avatar) : %d %s", response.getStatus(), response.getStatusText());

        return response.success();
    }

    public static boolean updateUser(RecolnatUser user) {
        Gson gson = new Gson();
        String body = gson.toJson(user);

        Logger.info("update user body : %s", body);

        WS.HttpResponse response = WS.url(ENDPOINT + "/users/" + user.user_uuid)
                .setHeader("apiKey", API_KEY)
                .setHeader("Content-Type", "application/json")
                .body(body)
                .put();

        Logger.info("Response : %d %s", response.getStatus(), response.getStatusText());

        return response.success();
    }

    public static boolean changePassword(String uuid, String currentPassword, String newPassword) {

        RecolnatUser user = new RecolnatUser();
        user.currentpassword = currentPassword;
        user.password = newPassword;
        Gson gson = new Gson();
        String body = gson.toJson(user);

        Logger.info("body : %s", body);

        WS.HttpResponse response = WS.url(ENDPOINT + "/users/%s", uuid)
                .setHeader("apiKey", API_KEY)
                .setHeader("Content-Type", "application/json")
                .body(body)
                .put();

        Logger.info("Response : %d %s", response.getStatus(), response.getStatusText());

        return response.success();
    }
}
