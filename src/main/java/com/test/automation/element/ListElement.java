package com.test.automation.element;

import com.test.automation.sut.steps.BaseSteps;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by tmuminova on 4/10/20.
 */
public abstract class ListElement extends CompoundElement implements IList {

    private static final Logger logger = LogManager.getLogger(ListElement.class);
    protected final List<Element> itemList = new Vector<>();
    protected final Map<String, Integer> itemIndices = new HashMap<>();

    private final By parameterizedItemLocator;

    public ListElement(String name, By locator, Map<String, IElement> elementsMap) {
        this(name, locator, null, elementsMap);
    }

    public ListElement(String name, By locator, By parameterizedItemLocator, Map<String, IElement> elementsMap) {
        super(name, locator, elementsMap);
        this.parameterizedItemLocator = parameterizedItemLocator;
    }

    public int getListSize() {
        return getList().size();
    }

    public List<? extends Element> getList() {
        if (itemList.isEmpty())
            initTable();
        return itemList;
    }

    public Element getListItem(int position) {
        return getList().get(position - 1);
    }

    public Element getListItem(String itemName) {
        if (null == parameterizedItemLocator) {
            initTable();
            int index = getListItemIndexByName(itemName);
            if (index != -1)
                return getList().get(index - 1);
            else
                throw new InvalidParameterException("Item '" + itemName + "' not found in "
                        + this.getName() + ". Possible choices are: "
                        + itemIndices.keySet().toString());
        } else {
            WebElement itemWebElement;
            WebElement listWebElement = init();
            try {
                BaseSteps.getDriver().turnImplicitlyWaitOff();
                itemWebElement = listWebElement.findElement(substitudeParams(parameterizedItemLocator, itemName));
                BaseSteps.getDriver().turnImplicitlyWaitOn();
            } catch (WebDriverException e) {
                throw new InvalidParameterException("Item '" + itemName + "' is not found in " + this.getName()
                        + "\nGot WebDriverException exception: " + e.getMessage());
            }
            return initListItem(itemWebElement, 1);
        }
    }

    public int getListItemIndexByName(String itemName) {
        Integer index = itemIndices.get(itemName);
        if (index != null)
            return index;
        else {
            logger.info("Item '" + itemName + "' not found in " + this.getName()
                    + ". Possible choices are: " + itemIndices.keySet().toString());
            return -1;
        }
    }

    public boolean itemExists(String itemName) {
        if (null == parameterizedItemLocator) {
            return (initTable().getListItemIndexByName(itemName) != -1);
        } else {
            try {
                getListItem(itemName);
                return true;
            } catch (InvalidParameterException e) {
                return false;
            }
        }
    }

    protected abstract By getItemLocator();

    protected abstract ListItemElement initListItem(WebElement webElement, int itemNumber);

    public ListElement initTable() {
        itemList.clear();
        itemIndices.clear();
        int i = 0;
        this.waitUntilVisible();
        for (WebElement element: init().findElements(getItemLocator())) {
            ListItemElement widget = initListItem(element, ++i);
            itemList.add(widget);
            itemIndices.put(widget.getListItemName(), i);
        }
        return this;
    }

    public void clickItem(String itemName) throws InterruptedException {
        final IElement drawerItem = getListItem(itemName);
        drawerItem.click();
    }

}
