package com.test.automation.element;

import com.test.automation.utils.Pair;
import org.openqa.selenium.By;

import java.util.Map;

/**
 * Created by tmuminova on 4/8/20.
 */

public interface ICompoundElement extends IElement {
    IElement getElement(String name);

    IElement getElement(Pair<String, By> pair);

    <T> T getElementAs(Pair<String, By> pair, Class<T> tClass);

    ICompoundElement getCompElement(String name);

    ICompoundElement getCompElement(Pair<String, By> pair);

    Map<String, IElement> getElements();
}
