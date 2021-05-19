package com.test.automation.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * Created by tmuminova on 4/16/20.
 */
public class TestConstants {
    private static final Logger logger = LogManager.getLogger(TestConstants.class);
    public static final String DEFAULT_URL = "https://yandex.ru/";

    public static String SITE_URL = System.getProperty("webdriver.base.url", DEFAULT_URL);
    public static final String PROPKEY_CURRENT_STORY_NAME = "current_story_name";

    // Passwords
    public static final String DEFAULT_PASSWORD = "1qaz2wsx";

}
