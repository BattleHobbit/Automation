package com.test.automation.webdriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.openqa.selenium.MutableCapabilities;

/**
 * Created by tmuminova on 4/7/20.
 */

public class DriverFactory {
    private static WebDriver driver;


    public static WebDriver create(WebDriverType type, MutableCapabilities... capabilities) {

        switch (type) {
            case CHROME:
                WebDriverManager.chromedriver().setup();
                if (capabilities.length > 0){
                    ChromeOptions options = new ChromeOptions();
                    options.merge(capabilities[0]);
                    driver = new ChromeDriver(options);
                } else {
                    driver = new ChromeDriver();
                }
                break;
            case FIREFOX:
                WebDriverManager.firefoxdriver().setup();
                if (capabilities.length > 0){
                    FirefoxOptions options  = new FirefoxOptions();
                    options.merge(capabilities[0]);
                    driver = new FirefoxDriver(options);
                } else {
                    driver = new FirefoxDriver();
                }
                break;
            case SAFARI:
                if (capabilities.length > 0){
                    SafariOptions options  = new SafariOptions();
                    options.merge(capabilities[0]);
                    driver = new SafariDriver(options);
                } else {
                    driver = new SafariDriver();
                }
                break;
        }
        return driver;
    }

}