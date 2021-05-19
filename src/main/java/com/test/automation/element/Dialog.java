package com.test.automation.element;

import org.openqa.selenium.By;

import java.util.Map;

/**
 * Created by tmuminova on 4/9/20.
 */
public abstract class Dialog extends CompoundElement {
    public Dialog(String name, By locator, Map<String, IElement> elementsMap) {
        super(name, locator, elementsMap);
    }

    public abstract Dialog waitForDialog();

    public abstract void close();
}
