package com.test.automation.sut.steps;

import com.test.automation.reporters.ReportHelperSingleton;
import com.test.automation.utils.TestConstants;

import org.apache.log4j.Logger;
import org.jbehave.core.annotations.*;
import org.openqa.selenium.Cookie;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by tmuminova on 4/24/20.
 */
public class BeforeAfterStoriesActions extends BaseSteps {
    private final static Logger log = Logger.getLogger(BeforeAfterStoriesActions.class);
    private ReportHelperSingleton reportHelperSingleton = ReportHelperSingleton.getInstance();

    @BeforeScenario
    public void startBrowserBeforeScenario() {
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            hostname = "unknown. Exception: " + ex.toString();
        }
        log.info("Scenario started, hostname=" + hostname);
        String currentStory = (String) getSut().getProperty(TestConstants.PROPKEY_CURRENT_STORY_NAME);
        getSut().clearProperties();
        log.info("Cleaning cache...");

        if (currentStory != null) {
            getSut().setProperty(TestConstants.PROPKEY_CURRENT_STORY_NAME, currentStory);
        }
    }

    @AfterScenario
    public void afterScenarioCleanup() {
        log.info("Scenario ended, stopping browser.");
        if(reportHelperSingleton.getIsWebTest()){
            deleteAllCookies();
        }
        stopSut();
    }

    @AfterScenario(uponType= ScenarioType.EXAMPLE)
    public void afterScenarioWithExamplesCleanup() {
        log.info("Scenario with examples ended, stopping browser.");
        if(reportHelperSingleton.getIsWebTest()){
            deleteAllCookies();
        }
        stopSut();
    }

    @BeforeStory
    public static void beforeStory() {
        log.info("Environment URL = " + System.getProperty("webdriver.base.url", "not set"));
        log.info("Story.name = " + System.getProperty("story.name", "not set"));
    }

    public static void deleteAllCookies(){
        log.info("Trying to delete all cookies");
        getDriver().manage().deleteAllCookies();

        Set<Cookie> cookies = getDriver().manage().getCookies();

        if(cookies.size() != 0) {
            log.info("Next cookies are found:" + cookies);
            for (Cookie cookie : cookies) {
                log.info("Deleting Cookie - " + cookie);
                getDriver().manage().deleteCookie(cookie);
                log.info("Cookie - " + cookie + " deleted!");
            }
        }
        log.info("All cookies should be deleted");
    }
}