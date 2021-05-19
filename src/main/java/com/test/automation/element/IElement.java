package com.test.automation.element;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.awt.*;

/**
 * Created by tmuminova on 4/8/20.
 */

interface IElement {
    WebElement init();

    IElement click() throws InterruptedException;

    IElement notJavaScriptClick();

    IElement hover();

    boolean isPresent() throws InterruptedException;

    boolean isVisible();

    boolean isEnabled();

    By getLocator();

    String getAttribute(String attribute);

    String getText();

    String getValue();

    void setValue(String value);

    IElement waitUntilExists();

    IElement waitUntilEnabled();

    IElement waitUntilDisabled();

    IElement waitUntilVisible();

    boolean waitUntilInvisible();

    IElement sendKeys(String keys);

    Rectangle getRect();

    String getName();

    boolean isSelected();

    IElement type(String keys) throws InterruptedException;

    void dragTo(IElement dstIElement);

    void doubleClick() throws InterruptedException;

    String getCssValue(String name);
}

