package com.test.automation.sut.steps;

import static com.test.automation.sut.PageNavigator.PageType;
import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import com.test.automation.element.Element;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

/**
 * Created by tmuminova on 4/21/20.
 */
public class GenericSteps extends BaseSteps {
    private final static Logger logger = Logger.getLogger(GenericSteps.class);

    @When("I maximize browser window")
    @Then("I maximize browser window")
    public static void maximizeBrowserWindow() {
        getDriver().maximizeWindow();
    }

    @Given("{I have |}refreshed the page")
    @When("{I |}refresh the page")
    public static void refreshPage() {
        getDriver().navigate().refresh();
        getDriver().sleep(1000);
    }

    @Then("{I |}am on $name page")
    @Given("{I |}am on $name page")
    @When("{I |}am on $name page")
    public static void onPage(PageType pageType) {
        getSut().getPageNavigator().navigateTo(pageType);
        logger.info("Navigating to " + pageType + " with " + getSut().getPageNavigator().getNavigatingUrl(pageType));
    }

    @Then("$name page is opened")
    public static void thenPageIsOpened(PageType pageType) {
        assertIfNot(wd -> getSut().getPageNavigator().isItCurrentPage(pageType), "Opened page: " + pageType.toString());
        logger.info("Opened " + pageType + " with url: " + getDriver().getCurrentUrl());
    }

    @Then("page title is '$pageTitle'")
    public static void getPageTitle(String pageTitle) {
        assertEquals("page title check", pageTitle, getDriver().getTitle());
    }

    @When("{I |}type '$text' into $name element without clearing")
    public static void whenTypeIntoElementWithout(String text, String name) {
        getElement(name).waitUntilVisible().sendKeys(text);
    }

    @When("I execute '$script' script")
    public static void executeScript(String script){
        getDriver().getJavascriptExecutor().executeScript(script);
    }

    @Then("{I |}click on $name element")
    @When("{I |}click on $name element")
    public static void whenClickElement(String name) {
        getElement(name).waitUntilVisible().click();
    }

    @When("{I |}double click $name element")
    public static void whenDoubleClickElement (String name) {
        getElement(name).doubleClick();
    }

    @Alias("$name element is ${visible|(not visible)} on the page")
    @Then("I wait for $name element to be ${visible|(not visible)} on the page")
    public static void waitForElementToBeNotVisibleOrVisible(String name, String option) {
        final Element element = getElement(name);
        logger.info("Waiting for element "+name+" with locator "+element.getLocator().toString()+" to be " + option);

        if (option.contains("not")){
            element.waitUntilInvisible();
            assertFalse("Element '" + name + "' is visible on page", element.isVisible());
        } else {
            element.waitUntilVisible();
            assertTrue("Element '" + name + "' is not visible on page", element.isVisible());
        }
    }

    @Then("$name element does not exist on the page")
    public static void thenElementDoesNotExist(String name) {
        try {
            Thread.sleep(1000);
            if (getElement(name).isPresent()) {
                fail("Element '" + name + "' exists on the page");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Given("{I |}waited for $duration seconds")
    @When("{I |}wait for $duration seconds")
    @Then("{I |}wait for $duration seconds")
    public static void sleep(int duration) {
        if (getDriver() == null) {
            try{
                Thread.sleep(duration * 1000);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        else
            getDriver().sleep(duration * 1000);
    }
}
