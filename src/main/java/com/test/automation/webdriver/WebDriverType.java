package com.test.automation.webdriver;

/**
 * Created by tmuminova on 4/7/20.
 */

public enum WebDriverType {
    CHROME("chrome"), FIREFOX("firefox"), SAFARI("safari");
    private final String browser;


    WebDriverType(String browser) {
        this.browser = browser;
    }

    public static WebDriverType getByValue(String value) {
        for (WebDriverType element : WebDriverType.values()) {
            if (element.toString().equalsIgnoreCase(value)) {
                return element;
            }
        }
        throw new IllegalArgumentException(value);
    }
}

