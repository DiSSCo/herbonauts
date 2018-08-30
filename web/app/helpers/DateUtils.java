package helpers;

import com.google.gson.GsonBuilder;
import models.serializer.adapters.HibernateProxyTypeAdapter;
import org.joda.time.DateTime;


public class DateUtils {

    public static Long correctUTCShift(Long dateIn) {

        DateTime dt = new DateTime(dateIn);

        int h = dt.getHourOfDay();


        System.out.println("Date in " + dateIn + "; h=" + h);

        long shift = 0;
        if (h > 12) {
            shift = 24 - h;
        } else {
            shift = -h;
        }
        return dateIn + (shift * 60 * 60 * 1000);

    }

}
