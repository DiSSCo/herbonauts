package conf;

import models.Specimen;
import org.apache.commons.beanutils.BeanUtils;
import play.Logger;
import play.templates.JavaExtensions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HerbonautesExtensions extends JavaExtensions {

    public static String injectContext(String template, Specimen specimen) {
        Pattern p = Pattern.compile("(\\{\\{.*?\\}\\})");
        Matcher m = p.matcher(template) ;

        if (template == null) {
            return "";
        }

        while (m.find()) {
            String exp = m.group();
            String beanProp = exp.replaceAll("\\{\\{", "").replaceAll("\\}\\}", "").trim();
            String bean = beanProp.split("\\.")[0];
            String prop = beanProp.split("\\.")[1];
            //System.out.println(exp + " --> " + bean + " -> " + prop);

            String val = null;
            try {
                if (specimen != null) {
                    val = BeanUtils.getProperty(specimen, prop);
                }
            } catch (Exception e) {
                Logger.error(e, "Erreur pour récupérer property");
            }

            template = template.replace(exp, val != null ? val : "");
        }

        return template;
    }
}
