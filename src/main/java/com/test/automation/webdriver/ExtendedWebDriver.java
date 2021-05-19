package com.test.automation.webdriver;

import com.google.common.base.Function;
import com.test.automation.reporters.ReportHelperSingleton;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Created by tmuminova on 4/7/20.
 */


public class ExtendedWebDriver implements WebDriver {
    private final WebDriver browserDriver;
    public final Boolean IS_CHROME_DRIVER;
    public final Boolean IS_FIREFOX_DRIVER;
    public final Boolean IS_SAFARI_DRIVER;
    private static ExtendedWebDriver INSTANCE;
    private final Wait<WebDriver> wait;
    private final long longTimeoutMS = 4 * 1000;
    private final long pullUpIntervalMS = 125;
    private static final Logger logger = LogManager.getLogger(ExtendedWebDriver.class);

    // -Dbrowser='cHrOmE'
    private static final WebDriverType browser = WebDriverType.getByValue(System.getProperty("browser"));

    public ExtendedWebDriver(WebDriverType browser) {
        logger.info("Browser: " + browser);
        browserDriver = DriverFactory.create(browser);
        logger.info("Driver successfully started");
        this.IS_CHROME_DRIVER = (browser.equals(WebDriverType.CHROME));
        this.IS_FIREFOX_DRIVER = (browser.equals(WebDriverType.FIREFOX));
        this.IS_SAFARI_DRIVER = (browser.equals(WebDriverType.SAFARI));
        this.wait = new WebDriverWait(browserDriver, longTimeoutSEC(), getPullUpIntervalMS());
        maximizeWindow();
    }

    public WebDriver getBrowserDriver() {
        return this.browserDriver;
    }

    public static WebDriverType getBrowser() { return browser; }

    /*
    DO NOT USE THIS METHOD DIRECTLY.
    IF YOU NEED DRIVER INSTANCE
    USE getDriver() from BaseSteps class
    */
    public static ExtendedWebDriver getInstance() {
        if (INSTANCE == null && ReportHelperSingleton.getInstance().getIsWebTest()) {
            logger.info("getInstance: INSTANCE==null");
            try {
                INSTANCE = new ExtendedWebDriver(browser);
                logger.info("getInstance: INSTANCE created");
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException("Exception when creating new WebDriver instance: " + ex.toString(), ex);
            }
        }
        return INSTANCE;
    }

    public static boolean isDriverInstanceCreated() {
        return INSTANCE != null;
    }

    public static void shutDownAllDrivers(){
        if (INSTANCE !=null){
            logger.info("###### quiting from main webDriver instance");
            INSTANCE.quit();
            INSTANCE = null;
        }
    }

    public void maximizeWindow() {
        logger.info("Maximizing window size");
        browserDriver.manage().window().maximize();
        logger.info("Window size is maximized");
    }

    public long longTimeoutSEC() {
        return longTimeoutMS / 1000;
    }


    public long getPullUpIntervalMS() {
        return pullUpIntervalMS;
    }

    public Boolean waitUntilElementDisappear(By by) {
        return waitUntil(ExpectedConditions.invisibilityOfElementLocated(by), "waitUntilElementDisappear");
    }

    public WebElement waitUntilElementAppearVisible(By by) {
        return waitUntil(ExpectedConditions.visibilityOfElementLocated(by), "waitUntilElementAppearVisible");
    }

    public WebElement waitUntilClickable(By by){
        return waitUntil(ExpectedConditions.elementToBeClickable(by),"waitUntilElementClickable");
    }

    public WebElement waitUntilClickable(WebElement element){
        return waitUntil(ExpectedConditions.elementToBeClickable(element),"waitUntilElementClickable");
    }

    public void waitUntilNumberOfElementsToBe(By by, int numOfElements) {
        waitUntil(ExpectedConditions.numberOfElementsToBe(by, numOfElements), "waitUntilNumberOfElementsToBe");
    }

    public Boolean waitUntilTextPresent(By by, String text){
        return waitUntil(ExpectedConditions.textToBePresentInElementLocated(by,text),"waitUntilTextPresentInElement");
    }

    public Boolean waitUntilAttributeContains(By by, String attribute, String attributeValue) {
        return waitUntil(ExpectedConditions.attributeContains(by, attribute, attributeValue), "waitUntilAttributeContains");
    }

    public <V> V waitUntil(Function<? super WebDriver, V> function) {
        return waitUntil(function, "");
    }

    public <V> V waitUntil(Function<? super WebDriver, V> function, String timeoutMessage) {
        turnImplicitlyWaitOff();
        Object result;
        try {
            result = wait.until(function);
        } catch (TimeoutException timeException) {
            turnImplicitlyWaitOn();
            throw new TimeoutException(timeException.getMessage() +
                    "\nTimeOut while waitUntil " + timeoutMessage, timeException.getCause());
        }
        turnImplicitlyWaitOn();
        return (V) result;
    }

    public JavascriptExecutor getJavascriptExecutor() {
        return (JavascriptExecutor) browserDriver;
    }

    public void turnImplicitlyWaitOff() {
        browserDriver.manage().timeouts().implicitlyWait(0, TimeUnit.MILLISECONDS);
    }

    public void turnImplicitlyWaitOn() {
        logger.info("###### setting implicitlyWait to default " + longTimeoutMS + " milliseconds");
        browserDriver.manage().timeouts().implicitlyWait(longTimeoutMS, TimeUnit.MILLISECONDS);
    }

    public void setWindowSize(int width, int height) {
        browserDriver.manage().window().setSize(new Dimension(width,height));
    }

    public void setAttributeThroughJavascript(String attribute, String value, By by) {
        setAttributeThroughJavascript(attribute, value, findElement(by));
    }

    public void setAttributeThroughJavascript(String attribute, String value, WebElement element) {
        getJavascriptExecutor().executeScript("arguments[0].setAttribute(\"" + attribute + "\",\"" + value + "\")", element);
    }

    public void removeAttributeThroughJavascript(String attribute, WebElement element) {
        getJavascriptExecutor().executeScript("arguments[0].removeAttribute(\"" + attribute + "\")", element);
    }


    public void setValueThroughJavascript(String value, WebElement element) {
        setAttributeThroughJavascript("value", value, element);
    }

    public void clickThroughJavascript(By elementLocator) {
        clickThroughJavascript(findElement(elementLocator));
    }

    public void clickThroughJavascript(WebElement element) {
        String script = "arguments[0].click();";
        getJavascriptExecutor().executeScript(script, element);
    }

    public void click(By locatorOfClickableElement) {
        try {
            WebElement element = findElement(locatorOfClickableElement);
            element.click();
        } catch (ElementNotVisibleException e) {
            throw new ElementNotVisibleException("You click on element " +
                    locatorOfClickableElement.toString() + " that currently invisible \n", e.getCause());
        }
    }

    public void scrollToElement(WebElement element) {
        getJavascriptExecutor().executeScript("arguments[0].scrollIntoView(true);", element);
    }

    public void get(String url) {
        browserDriver.get(url);
    }

    public String getCurrentUrl() {
        return browserDriver.getCurrentUrl();
    }

    public String getTitle() {
        return browserDriver.getTitle();
    }

    public List<WebElement> findElements(By by) {
        return browserDriver.findElements(by);
    }

    public WebElement findElement(By by) {
        return browserDriver.findElement(by);
    }

    public WebDriver.Navigation navigate() {
        return browserDriver.navigate();
    }

    public WebDriver.Options manage() {
        return browserDriver.manage();
    }

    public Set<String> getWindowHandles() {
        return browserDriver.getWindowHandles();
    }

    public String getWindowHandle() {
        return browserDriver.getWindowHandle();
    }

    public WebDriver.TargetLocator switchTo() {
        return browserDriver.switchTo();
    }

    /**
     * switch to the window by title
     * @param title - window title
     * @param contains - if you know only part of window title set it to "true"
     *                 then it will switch to window by partial title
     */
    public void switchToWindow(String title,String ...contains) {
        //Retry in case the page does not load immediately
        int count = 0;
        int maxTries = 3;
        ArrayList<String> titlesList = new ArrayList<>();
        while (true) {
            try {

                for (String winHandle : getWindowHandles()) {
                    switchTo().window(winHandle);
                    String currentTitle = getTitle();
                    if (currentTitle.equalsIgnoreCase(title)){
                        return;
                    }
                    if((contains.length>0)&&(contains[0].equals("true"))){
                        if(currentTitle.contains(title)){
                            return;
                        }
                    }
                    titlesList.add(getTitle());
                    Thread.sleep(1000);
                }
            } catch (Exception ex) {

            }
            if (++count >= maxTries) {
                throw new RuntimeException("Could not switch to window with title '" + title + "'. Detected pages are: " + titlesList.toString());
            }
        }
    }

    public File getScreenshotAs(OutputType<File> file) {
        File outputFile = null;
        try {
            outputFile =  ((TakesScreenshot) browserDriver).getScreenshotAs(file);
        }
        catch(Exception ex)
        {
            throw new RuntimeException("Exception when taking snapshot: " + ex.toString());
        }
        return outputFile;
    }


    public void getScreenshotAndSaveAs(File file) throws IOException {
        if(!this.IS_SAFARI_DRIVER){
            File screenShot = this.getScreenshotAs(OutputType.FILE);
            if (file.isDirectory()) {
                FileUtils.copyFileToDirectory(screenShot, file);
            } else {
                FileUtils.copyFile(screenShot, file);
            }
            FileUtils.deleteQuietly(screenShot);
        }
    }

    /**
     * getting all logs from browser console and parse them
     * @return in case of error return <>true</>
     */
    public boolean getBrowserConsoleLogsFatalError(){
        logger.info("###### CHECKING CONSOLE LOGS: ");
        //Browser browser = getDriver().getBrowser();
        if(this.IS_CHROME_DRIVER) {
            for (LogEntry logEntry : getBrowserConsoleLogs().getAll()) {
                if (logEntry.getLevel().equals(Level.SEVERE) && !logEntry.getMessage().contains("client:containers:CommEngine")) {
                    logger.info("###### THERE IS AN ERROR IN THE CONSOLE LOG:  " + logEntry.getMessage());
                    return true;
                } else if (logEntry.getLevel().equals(Level.WARNING)) {
                    logger.info("###### There is a warning in console: " + logEntry.getMessage());
                }
            }
            logger.info("###### browser console looks good :)");
            return false;
        }
        else {
            logger.info("###### current browser doesn't not support console logs yet :)");
            return false;
        }
    }

    /**
     * getting all logs from browser and put them
     * @return LogEntries list for LogEntry
     */
    public LogEntries getBrowserConsoleLogs(){
        logger.info("###### getting browser logs");
        if (this.IS_CHROME_DRIVER){
            Logs logs =  browserDriver.manage().logs();
            return logs.get(LogType.BROWSER);
        }
        else {
            logger.info("###### browser is not not supported yet, skipping getting logs");
            return null;
        }
    }

    public String getPageSource() {
        return browserDriver.getPageSource();
    }

    public void close() {
        browserDriver.close();
    }

    public void quit() {
        if (null != browserDriver) {
            browserDriver.quit();
        }
        logger.info("ExtendedWebDriver::quit() called");
    }

    public void sleep(int sleepTimeInMs) {
        boolean interrupted = false;
        logger.info("###### And I sleep for "+sleepTimeInMs/1000+" seconds");
        try {
            try {
                Thread.sleep(sleepTimeInMs);
            } catch (InterruptedException e) {
                //thread are interrupted, complete job and interrupt thread
                interrupted = true;
            }
        } finally {
            if (interrupted)
                Thread.currentThread().interrupt();
        }
    }
}