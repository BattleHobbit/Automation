package com.test.automation.utils;

import java.nio.charset.StandardCharsets;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Created by tmuminova on 4/9/20.
 */
public class PropertiesUtil {
    private ResourceBundle rb;
    public final static String PARAMETERS_PATTERN = "%s";

    public PropertiesUtil(String propertiesPath){
        rb = ResourceBundle.getBundle(propertiesPath);
    }

    public static String replaceParameters(String in, String... params) {
        if (params == null) {
            return in;
        }
        for (String replaceBy : params) {
            if (in.contains(PARAMETERS_PATTERN)) {
                if (replaceBy == null) {
                    throw new IllegalArgumentException("Replacing parameter is null");
                }
                if (replaceBy != null) {
                    in = in.replaceFirst(PARAMETERS_PATTERN, replaceBy);
                }
            }
        }
        return in;
    }

    /**
     *
     * @param name - name of resource
     * @return - returns resource value
     */
    public String getResource(String name, String... params){
        try{
            String value = fixEncoding(rb.getString(name));
            if(params!=null){
                value=replaceParameters(value,params);
            }
            return value;
        }catch (NullPointerException|MissingResourceException e){
            throw new IllegalArgumentException("Can't read resource " + name);
        }
    }


    /**
     * Returns a String with fixed encoding. This is skipped if Java version is 9 or higher.
     * @param content The string to convert.
     * @return a string with the fixed encoding.
     */
    public String fixEncoding(String content) {
        if (System.getProperty("java.version").startsWith("1.")) {
            return new String(content.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        }
        return content;
    }
}
