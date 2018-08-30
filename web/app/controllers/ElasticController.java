package controllers;

import conf.Herbonautes;
import play.Play;
import play.db.jpa.NoTransaction;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Jonathan on 03/09/2015.
 */
public class ElasticController extends Application {

    public static void checkPreFlight() {
        response.setHeader("Access-Control-Allow-Origin", "*");       // Need to add the correct domain in here!!
        response.setHeader("Access-Control-Allow-Methods", "GET");   // Only allow POST
        response.setHeader("Access-Control-Max-Age", "300");          // Cache response for 5 minutes
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");         // Ensure this header is also allowed!
    }

    // Fix #370
    @NoTransaction
    public static void call() throws IOException {
        String json =  new String(request.params.get("body").getBytes(), "UTF-8");
        String stringUrl = request.getBase() + request.url.replaceAll("/elastic", "");
        stringUrl = stringUrl.replaceAll(Play.ctxPath, "");
        String elasticUrl = Play.configuration.getProperty("herbonautes.elasticsearch.host.private");
        if(!elasticUrl.endsWith("/")) elasticUrl += "/";
        if(!elasticUrl.contains("http://")) {
            elasticUrl = "http://" + elasticUrl;
        }
        stringUrl = stringUrl.replaceAll(request.getBase() + "/", elasticUrl);
        URL url = new URL(stringUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();


        // Fix #370
        con.setReadTimeout(Herbonautes.get().elasticReadTimeout);

        //  CURLOPT_POST
        con.setRequestMethod("GET");

        // CURLOPT_FOLLOWLOCATION
        con.setInstanceFollowRedirects(true);

        con.setRequestProperty("Content-length", String.valueOf(json.length()));

        con.setDoOutput(true);
        con.setDoInput(true);

        BufferedOutputStream output = new BufferedOutputStream(con.getOutputStream());
        output.write(json.getBytes("UTF-8"));
        output.close();

        // "Post data send ... waiting for reply");
        int code = con.getResponseCode(); // 200 = HTTP_OK
        System.out.println("Response    (Code):" + code);
        System.out.println("Response (Message):" + con.getResponseMessage());

        // read the response
        BufferedInputStream input = null;
        try {
            input = new BufferedInputStream(con.getInputStream());
        } catch (Exception e) {
            badRequest("No body in request");
        }
        byte[] contents = new byte[1024];
        int bytesRead = 0;
        StringBuilder resultBuf = new StringBuilder();
        while((bytesRead = input.read(contents)) != -1){
            resultBuf.append(new String(contents, 0, bytesRead, "UTF-8"));
        }
        input.close();
        response.setHeader("Access-Control-Allow-Origin", "*");       // Need to add the correct domain in here!!
        renderJSON(resultBuf.toString());
     }
}
