package services;

import org.apache.commons.lang.StringUtils;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextUtils {

    private static HashMap<String, List<String>> EMPTY_SYNONYMS = new HashMap<String, List<String>>();

    public static boolean approxEquals(String str1, String str2) {
        if (str1 == null) {
            str1 = "";
        }
        if (str2 == null) {
            str2 = "";
        }
        return approxEquals(str1, str2, EMPTY_SYNONYMS);
    }

    public static boolean approxEquals(String str1, String str2, Map<String, List<String>> synonyms) {
        if (str1 == null) {
            str1 = "";
        }
        if (str2 == null) {
            str2 = "";
        }
        return simplify(str1, synonyms).equals(simplify(str2, synonyms));
    }

    public static String simplify(String str1, Map<String, List<String>> synonyms) {
        if (null == str1) {
            return null;
        }

        str1 = str1.trim().toLowerCase().replaceAll("\\s+", " ");

        String[] words = str1.split(" ");
        replaceSynonyms(synonyms, words); // Synonymes avec caractères spéciaux
        str1 = StringUtils.join(words, " ").trim();
        str1 = noAccents(str1);
        str1 = str1.replaceAll("[^A-Za-z0-9\\s]", " ");

        String[] words2 = str1.split(" ");
        replaceSynonyms(synonyms, words2); // Synonymes sans caractères spéciaux
        str1 = StringUtils.join(words2, " ").trim();

        return str1.trim().toLowerCase().replaceAll("\\s+", " ");
        //return str1.trim().toLowerCase();
    }

    private static void replaceSynonyms(Map<String, List<String>> synonyms, String[] words) {
        for (Map.Entry<String, List<String>> synonymEntry : synonyms.entrySet()) {
            //System.out.println("synonyms of " + synonymEntry.getKey() + " ?");
            if (synonymEntry.getValue() != null) {
                for (String s : synonymEntry.getValue()) {
                    for (int i = 0; i < words.length; i++) {
                        if (words[i].equals(s) && !synonymEntry.getKey().equals(s)) {
                            //System.out.println("- replace " + s + " by " + synonymEntry.getKey());
                            words[i] = synonymEntry.getKey();
                        }
                    }
                }
            }
        }
    }

    public static String simplify(String str1) {
        return simplify(str1, EMPTY_SYNONYMS);
    }

    public static String noAccents(String src) {
        return Normalizer
                .normalize(src, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
    }



}
