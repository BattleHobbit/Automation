package com.test.automation.sut.steps;

import com.test.automation.element.CompoundElement;
import com.test.automation.element.Element;
import com.test.automation.webdriver.ExtendedWebDriver;
import com.test.automation.page.IPage;
import com.test.automation.reporters.ReportHelperSingleton;
import com.test.automation.sut.Sut;
import com.google.common.base.Function;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import static com.test.automation.webdriver.ExtendedWebDriver.shutDownAllDrivers;

/**
 * Created by tmuminova on 4/13/20.
 */
public class BaseSteps {
    private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    private final static ThreadLocal<Sut> sut = new ThreadLocal<>();
    private ReportHelperSingleton reportHelperSingleton = ReportHelperSingleton.getInstance();

    public static Sut getSut() {
        Sut currentSut = sut.get();
        if (currentSut == null) {
            currentSut = new Sut();
            sut.set(currentSut);
        }
        return currentSut;
    }

    protected void stopSut() {
        Sut currentSut = sut.get();
        if (currentSut != null && reportHelperSingleton.getIsWebTest()) {
            shutDownAllDrivers();
        }
        sut.remove();
    }

    public static ExtendedWebDriver getDriver() {
        return ExtendedWebDriver.getInstance();
    }


    public static IPage getCurrentPage() {
        return getSut().getPageNavigator().getCurrentPage();
    }

    public static Element getElement(String name) {
        return (Element) getCurrentPage().getElement(name);
    }

    public static CompoundElement getCompElement(String name) {
        return (CompoundElement) getCurrentPage().getCompElement(name);
    }

    public static void assertIfNot(Function function, String assertMessage) {
        try {
            getDriver().waitUntil(function, assertMessage);
        } catch (TimeoutException timeException) {
            throw new AssertionError(assertMessage);
        }
    }

    public static boolean waitUntil(Function function) {
        try {
            getDriver().waitUntil(function);
            return true;
        } catch (TimeoutException timeException) {
            logger.info("***** PAGE SOURCE WHEN ERROR IS DETECTED *****");
            logger.info(getDriver().getPageSource());
            logger.info("***** END PAGE SOURCE *****");
            return false;
        }
    }

    public static void selectFromDropdown(Element elementName, String optionToSelect){
        Select dropdown = new Select(getDriver().findElement(elementName.getLocator()));
        //selecting by value is more stable
        dropdown.selectByValue(optionToSelect);
    }

    public static void selectFromDropdownByText(Element elementName, String optionToSelect){
        Select dropdown = new Select(getDriver().findElement(elementName.getLocator()));
        //selecting by text
        dropdown.selectByVisibleText(optionToSelect);
    }

    public static List<String> getAllOptionsFromDropdown(Element elementName){
        Select dropdown = new Select(getDriver().findElement(elementName.getLocator()));
        List<WebElement> options = dropdown.getOptions();
        List<String> listOfOptions = new ArrayList<>();
        for(WebElement option :options){
            listOfOptions.add(option.getText());
            logger.info(option.getText() + " added to the List");
        }
        return listOfOptions;
    }
}
