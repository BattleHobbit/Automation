package com.test.automation.element;

import com.test.automation.page.IPage;
import com.test.automation.sut.steps.BaseSteps;
import com.test.automation.utils.Pair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.awt.Rectangle;
import java.awt.*;

/**
 * Created by tmuminova on 4/9/20.
 */
public class Element implements IElement {

    private static final Logger logger = LogManager.getLogger(Element.class);

    protected String name;
    protected By locator;
    protected WebElement webElement;
    protected WebElement parentWebElement;
    protected final ExpectedCondition<Boolean> EXISTS = driver -> {
        init();
        return true;
    };

    protected final ExpectedCondition<Boolean> ENABLED = driver -> init().isEnabled();

    protected final ExpectedCondition<Boolean> DISABLED = driver -> !init().isEnabled();

    protected final ExpectedCondition<Boolean> VISIBLE = driver -> init().isDisplayed();

    protected final ExpectedCondition<Boolean> HIDDEN = driver -> !init().isDisplayed();

    protected final ExpectedCondition<WebElement> HAS_ELEMENT = driver -> init();

    public Element(){ throw new UnsupportedOperationException();}

    public Element(String name, By locator) {
        this.name = name;
        this.locator = locator;
    }

    public Element(Pair<String, By> pair) {
        this(pair.fst, pair.snd);
    }

    public Element(WebElement parentWebElement, Pair<String, By> pair) {
        this(parentWebElement, pair.fst, pair.snd);
    }

    public Element(WebElement parentWebElement, String name, By locator) {
        this.parentWebElement = parentWebElement;
        this.name = name;
        this.locator = locator;
    }

    public Element(WebElement webElement) {
        this.webElement = webElement;
    }

    protected IPage getCurrentPage() {
        return BaseSteps.getSut().getPageNavigator().getCurrentPage();
    }

    public Element click() {
        init(); // makes sure we are not clicking on a stale element
        logger.info("Clicking element: " + this.toString());
        // If we are running test, sometimes browser can't click the elements because it thinks they are invisible
        // Click through Javascript instead
        if(this.webElement.getTagName().equals("svg")){
            logger.info("###### ELEMENT IS SVG");
            this.webElement.click();
        } else {
            try {
                BaseSteps.getDriver().clickThroughJavascript(this.webElement);
            } catch (ElementNotInteractableException e) {
                logger.error("javascript click didn't work. Trying to click thru regular click");
                this.webElement.click();
            } catch (StaleElementReferenceException e) {
                logger.info("StaleElementReferenceException caught. Sleeping 0.5 sec and clicking the element again.");
                BaseSteps.getDriver().sleep(500);
                BaseSteps.getDriver().clickThroughJavascript(this.webElement);
            } catch (Exception e) {
                throw e;
            }
        }
        return this;
    }

    // Some of webElements doesn't respond to javascript click
    // So it is method for  browser to click without calling js executor from method above ^
    public Element notJavaScriptClick(){
        init();
        logger.info("###### Clicking element: " + this.toString());
        this.webElement.click();
        return this;
    }

    public boolean isPresent() throws InterruptedException {
        boolean elementPresent = false;
        for(int i = 3; i > 0; i--){
            try {
                BaseSteps.getDriver().turnImplicitlyWaitOff();
                elementPresent = (init() != null);
                break;
            }
            catch (Exception e){
                logger.info("Element is not found. Trying " + i + " more times");
                Thread.sleep(1000);
            }
        }
        return elementPresent;
    }

    public boolean isVisible() {
        boolean elementVisible;
        BaseSteps.getDriver().turnImplicitlyWaitOff();
        try {
            elementVisible = init().isDisplayed();
        } catch (WebDriverException ex) {
            elementVisible = false;
        } catch (NullPointerException ex) {
            logger.info("init() returned null for element " + name);
            elementVisible = false;
        }
        BaseSteps.getDriver().turnImplicitlyWaitOn();
        return elementVisible;
    }

    public boolean isEnabled() {
        return (init().isEnabled() && !this.getAttribute("class").contains("disabled"));
    }

    public By getLocator() {
        return this.locator;
    }

    public String getAttribute(String attribute) {
        return init().getAttribute(attribute);
    }

    public String getText() {
        return init().getText();
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return init().getAttribute("value");
    }

    public void setValue(String value) {
        BaseSteps.getDriver().setValueThroughJavascript(value, init());
    }

    public String getCssValue(String name) {
        return init().getCssValue(name);
    }

    public WebElement init() {
        return forceInit();
    }

    public WebElement forceInit() {
        if (parentWebElement != null) {
            webElement = parentWebElement.findElement(locator); // locator is relative to parent
        } else {
            if (locator != null)
                webElement = BaseSteps.getDriver().findElement(locator);
        }
        return webElement;
    }

    protected void waitForCondition(ExpectedCondition<Boolean> condition) {
        BaseSteps.getDriver().waitUntil(condition);
    }

    public Element waitUntilExists() {
        waitForCondition(EXISTS);
        return this;
    }

    public Element waitUntilEnabled() {
        waitForCondition(ENABLED);
        return this;
    }

    public Element waitUntilDisabled() {
        waitForCondition(DISABLED);
        return this;
    }

    public Element waitUntilVisible() {
        BaseSteps.getDriver().waitUntilElementAppearVisible(locator);
        return this;
    }

    public Element waitUntilClickable() {
        BaseSteps.getDriver().waitUntilClickable(locator);
        return this;
    }

    public Element waitUntilTextPresent(String text){
        BaseSteps.getDriver().waitUntilTextPresent(locator,text);
        return this;
    }

    public boolean waitUntilInvisible() {
        return BaseSteps.getDriver().waitUntilElementDisappear(locator);
    }

    public Element waitUntilAttributeContains(String attribute, String attributeValue){
        BaseSteps.getDriver().waitUntilAttributeContains(locator,attribute, attributeValue);
        return this;
    }

    public Element sendKeys(String keys) {
        init().sendKeys(keys);
        return this;
    }

    //clear field if its already populated
    public void clear() throws InterruptedException {
        logger.info("Element enabled, clearing");
        if(!init().getText().equals("")){
            this.doubleClick();
            init().sendKeys("");
            init().clear();
        }
        if(!init().getText().equals("") || (null !=init().getAttribute("value") && !init().getAttribute("value").equals(""))){
            logger.info("Regular clear() didn't work. trying workaround.");
            init().sendKeys(Keys.SHIFT, Keys.END, Keys.BACK_SPACE);
            init().sendKeys(Keys.SHIFT, Keys.HOME, Keys.BACK_SPACE);
            init().clear();
        }
        logger.info("Element is cleared");
    }

    public void enter() {
        init().sendKeys(Keys.ENTER);
    }

    public Rectangle getRect() {
        Rectangle rect = new Rectangle();
        org.openqa.selenium.Point location = init().getLocation();
        org.openqa.selenium.Dimension size = webElement.getSize();
        rect.x = location.x;
        rect.y = location.y;
        rect.width = size.width;
        rect.height = size.height;
        return rect;
    }

    public boolean isSelected() {
        return init().isSelected();
    }

    public Element type(String keys) throws InterruptedException {
        logger.info("Typing '" + keys + "' into element: " + this.toString());
        this.waitUntilEnabled();
        clear();
        if (keys.endsWith(Keys.ENTER.toString())) {
            init().sendKeys(keys.substring(0, keys.length() - 1));
            init().sendKeys(Keys.ENTER);
        } else if (keys.endsWith(Keys.RETURN.toString())) {
            init().sendKeys(keys.substring(0, keys.length() - 1));
            init().sendKeys(Keys.RETURN);
        } else {
            init().sendKeys(keys);
        }

        return this;
    }

    public void dragTo(IElement dstElement) {
        try {
            final WebElement srcWebElement = this.init();
            final WebElement dstWebElement = dstElement.init();
            Actions builder = new Actions(BaseSteps.getDriver().getBrowserDriver());
            Action dragAndDrop = builder.clickAndHold(srcWebElement)
                    .moveToElement(dstWebElement)
                    .release(dstWebElement)
                    .build();

            dragAndDrop.perform();
        }catch (MoveTargetOutOfBoundsException e){
            e.printStackTrace();
        }

    }

    public Element hover() {
        final WebElement webElement = this.init();
        hoverUsingSelenium(webElement);
        return this;
    }

    public Element hoverUsingSelenium(WebElement webElement) {
        logger.info("#### hover on element " + this.getName() + " with Selenium");
        Actions builder = new Actions(BaseSteps.getDriver().getBrowserDriver());
        Action mouseOver = builder.moveToElement(webElement)
                .build();
        mouseOver.perform();
        return this;
    }

    public Element hoverUsingRobot(WebElement webElement) {
        try {
            logger.info("#### hover on element " + this.getName() + " with Robot");
            int X0 = 0, Y0 = 0;
            X0 = ((Long) BaseSteps.getDriver().getJavascriptExecutor().executeScript("return window.screenX;")).intValue();
            Y0 = ((Long) BaseSteps.getDriver().getJavascriptExecutor().executeScript("return window.screenY;")).intValue();
            Y0 = Y0 + ((Long) BaseSteps.getDriver().getJavascriptExecutor().executeScript("return (window.outerHeight - window.innerHeight);")).intValue();

            int x = X0 + webElement.getLocation().getX() + webElement.getSize().getWidth() / 2;
            int y = Y0 + webElement.getLocation().getY() + webElement.getSize().getHeight() / 2;

            final Robot robot = new Robot();
            robot.setAutoDelay(500);
            robot.mouseMove(x, y);
            robot.mouseMove(x + 1, y + 1);  // To make sure cursor was moved

            return this;
        } catch (AWTException e) {
            logger.info("### Failed to hover with robot\n" + e.toString());
            return null;
        }
    }

    public void doubleClick() {
        final WebElement webElement = this.init();
        Actions act = new Actions(BaseSteps.getDriver().getBrowserDriver());
        try{
            act.moveToElement(webElement)
                    .doubleClick(webElement)
                    .build()
                    .perform();
        }catch (ElementNotInteractableException | StaleElementReferenceException e){
            logger.info("###### trying to double click again");
            BaseSteps.getDriver().sleep(1500);
            act.moveToElement(webElement)
                    .doubleClick()
                    .build()
                    .perform();
        } catch (WebDriverException e) {
                throw e;
        }
    }

    public void rightClick() {
        final WebElement webElement = this.init();
        Actions builder = new Actions(BaseSteps.getDriver().getBrowserDriver());
        Action action = builder.contextClick(webElement).build();
        action.perform();
    }

    public String toString() {
        return "Name: " + this.name + ", Locator: " + this.locator + ", webElement:" + (this.webElement != null ? this.webElement.toString() : "none") + ", parentWebElement: " + (this.parentWebElement != null ? this.parentWebElement.toString() : "none");
    }

}
