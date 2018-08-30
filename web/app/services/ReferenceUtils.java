package services;

import libs.Json;
import models.references.ReferenceRecord;
import models.references.ReferenceRecordInfo;
import play.Logger;
import play.cache.Cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReferenceUtils {


    public static Map<String, List<String>> getSynonyms() {

        Map<String, List<String>> synonyms = (Map<String, List<String>>) Cache.get("ref_synonyms");

        if (synonyms == null) {

            synonyms = new HashMap<String, List<String>>();

            Logger.info("Build synonyms from reference");

            List<ReferenceRecord> records = ReferenceRecord.find("reference.name = 'synonym'").fetch();

            for (ReferenceRecord record : records) {
                List<String> strs = new ArrayList<String>();

                if (record.getInfo() != null) {
                    for (ReferenceRecordInfo info : record.getInfo()) {
                        strs.add(info.getName());
                    }
                }
                synonyms.put(record.getValue(), strs);

            }

            Logger.info("Build synonyms from reference %s", synonyms);

            Cache.set("ref_synonyms", synonyms, "30min");

        }

        return synonyms;
    }


    public static String getInstitutionLabel(String code) {

        String label = (String) Cache.get("institution_label_" + code);

        if (label == null) {

            ReferenceRecord record = ReferenceRecord.find("reference.name = 'institution' and value = ?", code).first();

            if (record != null) {
                label = record.getLabel();
            } else {
                label = code;
            }

            Cache.set("institution_label_" + code, label, "1min");

        }

        return label;
    }

}
