package com.test.automation.sut;

import com.test.automation.utils.TestConstants;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tmuminova on 4/14/20.
 */
public class Sut {
    private static final String SITE_URL = System.getProperty("webdriver.base.url", TestConstants.DEFAULT_URL);
    private final Map<String, Object> propMap = new HashMap<>();
    private PageNavigator pageNavigator;
    private URL siteUrl;

    private final static Logger logger = Logger.getLogger(Sut.class);

    public Sut() {
        try {
            this.siteUrl = new URL(SITE_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        clearProperties();
    }

    public PageNavigator getPageNavigator() {
        if (pageNavigator == null) pageNavigator = new PageNavigator(this.siteUrl);
        return pageNavigator;
    }

    public Object getProperty(String key) {
        Object value = this.propMap.get(key);
        if (value == null)
            return "";
        else
            return value;
    }


    public void setProperty(String key, Object value) {
        logger.info("###### adding entree to the properties map with " +
                "\""+key+"\" and value \""+value.getClass().getName()+"\" object");
        this.propMap.put(key, value);
    }

    public Object removeProperty(String key) {
        return this.propMap.remove(key);
    }

    public void clearProperties() {
        logger.info("###### clear properties map");
        this.propMap.clear();
    }
}
