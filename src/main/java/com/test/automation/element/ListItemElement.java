package com.test.automation.element;

import org.openqa.selenium.WebElement;

import java.util.Map;

/**
 * Created by tmuminova on 4/10/20.
 */
public abstract class ListItemElement extends CompoundElement {

    public ListItemElement(WebElement webElement, Map<String, IElement> elementsMap) {
        super(webElement, elementsMap);
    }

    public abstract String getListItemName();

}
