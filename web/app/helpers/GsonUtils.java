package helpers;

import com.google.gson.GsonBuilder;
import models.serializer.adapters.HibernateProxyTypeAdapter;

/**
 * Created by Jonathan on 21/10/2015.
 */
public class GsonUtils {

    public static GsonBuilder getGsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY);
        return gsonBuilder;
    }

}
