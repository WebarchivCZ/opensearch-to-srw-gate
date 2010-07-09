package cz.webarchiv.SRW;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author xrosecky
 */
public class Util {

    public static String getRequired(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException(String.format("Key %s is missing in properties.", key));
        }
        return value;
    }

    public static Map<String, String> parseMap(Properties props, String prefix) {
        Map<String, String> result = new HashMap<String, String>();
        for (String propName : props.stringPropertyNames()) {
            if (propName.startsWith(prefix)) {
                String value = props.getProperty(propName);
                String key = propName.substring(prefix.length());
                result.put(key, value);
            }
        }
        return result;
    }

}
