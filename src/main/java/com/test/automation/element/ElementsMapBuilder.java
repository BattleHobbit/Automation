package com.test.automation.element;

import com.google.common.collect.ImmutableMap;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Created by tmuminova on 4/8/20.
 */
public class ElementsMapBuilder extends ImmutableMap.Builder<String, IElement> {
    private WebElement parentWebElement;

    public ElementsMapBuilder() {
        super();
        this.parentWebElement = null;
    }

    public ElementsMapBuilder withParent(WebElement parentWebElement) {
        this.parentWebElement = parentWebElement;
        return this;
    }

    public ElementsMapBuilder addElement(IElement element) {
        this.put(element.getName(), element);
        return this;
    }

    public ElementsMapBuilder addCompoundElement(String name, By locator, ElementsMapBuilder elementsMapBuilder) {
        this.put(name,
                new CompoundElement.CompoundElementBuilder(name, locator, elementsMapBuilder).build());
        return this;
    }

    public ElementsMapBuilder addCompoundElement(ICompoundElement compoundElement) {
        this.addElement(compoundElement);
        return this;
    }

    public ElementsMapBuilder addElement(String name, By locator) {
        return addElement(new Element(this.parentWebElement, name, locator));
    }


}
