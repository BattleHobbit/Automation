package com.test.automation.page;

import com.test.automation.element.CompoundElement;
import com.test.automation.element.ElementsMapBuilder;
import com.test.automation.webdriver.ExtendedWebDriver;
import com.test.automation.utils.Pair;
import com.test.automation.sut.steps.BaseSteps;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import static org.awaitility.Awaitility.await;


/**
 * Created by tmuminova on 4/8/20.
 */
public abstract class Page<T> extends CompoundElement implements IPage<T> {
    private final static Logger logger = Logger.getLogger(Page.class);

    protected Page(Pair<String, By> pair, ElementsMapBuilder elementsMapBuilder) {
        super(pair, elementsMapBuilder.build());
    }

    public T get() {
        try {
            isLoaded();
            return (T) this;
        } catch (Throwable e) {
            load();
        }
        isLoaded();
        return (T) this;
    }

    public boolean isLoaded() throws Error {
        return this.isVisible();
    }

    public T load() {
        this.waitUntilVisible();
        return (T) this;
    }

    /***
     * Override this method for every Page subclass which is used as Boron screen.
     * It should identify required screen uniquely.
     * @return
     */
    public boolean screenIsVisibleInCurrentWindow() {
        return this.isVisible();
    }

    /***
     * Tries to find current page in all browser windows by switching between windowHandles and checking screenIsVisibleInCurrentWindow
     * @return true when the page is found, driver stays in the context of this window. false if not found, driver tries to switch back to initial window handle
     */
    public boolean screenIsVisible() {
        ExtendedWebDriver driver = BaseSteps.getDriver();
        String initialHandle = null;
        try {
            initialHandle = driver.getWindowHandle();
            // check the screen in the current window first
            if (this.screenIsVisibleInCurrentWindow()) return true;
        } catch (NoSuchWindowException ex) {
            logger.info("Failed to check screen visibility in initial window handle - it doesn't exist");
        } catch (WebDriverException ex) {
            logger.info("Failed to check screen visibility in initial window. Exception: " + ex.getMessage());
        }

        for (String winHandle : driver.getWindowHandles()) {
            try {
                driver.switchTo().window(winHandle);
                if (this.screenIsVisibleInCurrentWindow()) return true;
            } catch (NoSuchWindowException ex) {
                logger.info("Failed to check screen visibility in window handle: " + winHandle);
            } catch (WebDriverException ex) {
                logger.info("Failed to check screen visibility in window. Exception: " + ex.getMessage());
            }
        }

        if (initialHandle != null) {
            try {
                driver.switchTo().window(initialHandle);
            } catch (NoSuchWindowException ex) {
                logger.info("Failed to switch back to initial window handle: " + initialHandle);
            } catch (WebDriverException ex) {
                logger.info("Failed to switch back to initial window handle. Exception: " + ex.getMessage());
            }
        }
        return false;
    }

    /***
     * Waits for the page to be visible in any of driver windows
     * @return the page, driver is in context of the required window
     */
    public T waitUtilScreenIsVisible() {
        ExtendedWebDriver driver = BaseSteps.getDriver();
        boolean success = waitFor(this::screenIsVisible, driver.longTimeoutSEC(), driver.getPullUpIntervalMS());
        if (!success) {
            throw new TimeoutException("TimeOut while waiting util " + this.name + " screen is visible.");
        }
        return (T) this;
    }



    /***
     * Waits for the page to not be visible in any of driver windows
     */
    public void waitUtilScreenIsNotVisible() {
        ExtendedWebDriver driver = BaseSteps.getDriver();
        boolean success = waitFor(() -> !this.screenIsVisible(), driver.longTimeoutSEC(), driver.getPullUpIntervalMS());
        if (!success) {
            throw new TimeoutException("TimeOut while waiting util " + this.name + " screen is not visible.");
        }
    }

    public static boolean waitFor(Callable<Boolean> function, long timeOutInSeconds, long sleepInMillis) {
        try {
            await().atMost(timeOutInSeconds, TimeUnit.SECONDS).pollDelay(sleepInMillis, TimeUnit.MILLISECONDS).until(function);
        } catch (Throwable ex) {
            return false;
        }
        return true;
    }

    @Override
    public String getText() {
        int retries = 3;
        while (true) {
            try {
                String pageText = BaseSteps.getDriver().findElement(By.tagName("body")).getText();
                return pageText;
            } catch (StaleElementReferenceException e) {
                if(--retries > 0){
                    logger.error("Caught StaleElementReferenceException. Will retry to getText() of page " + retries + "more times");
                }
                else {
                    throw e;
                }
            }
        }
    }
}