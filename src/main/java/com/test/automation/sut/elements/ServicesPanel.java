package com.test.automation.sut.elements;

import com.test.automation.element.CompoundElement;
import com.test.automation.element.Element;
import com.test.automation.element.ElementsMapBuilder;
import org.openqa.selenium.By;

public class ServicesPanel extends CompoundElement {
    public static final By
            ELEMENT_LOCATOR = By.cssSelector("div.services-new__content");
    public static final Element
            Q_BUTTON = new Element("Q_BUTTON", By.cssSelector("a.home-link.services-new__item.services-new__promo"));
    public static final Element
            MARKET_BUTTON = new Element("MARKET_BUTTON", By.cssSelector("a[data-id=market]"));
    public static final Element
            VIDEO_BUTTON = new Element("VIDEO_BUTTON", By.cssSelector("a[data-id=video]"));
    public static final Element
            IMAGES_BUTTON = new Element("IMAGES_BUTTON", By.cssSelector("a[data-id=images]"));
    public static final Element
            NEWS_BUTTON = new Element("NEWS_BUTTON", By.cssSelector("a[data-id=news]"));
    public static final Element
            MAPS_BUTTON = new Element("MAPS_BUTTON", By.cssSelector("a[data-id=maps]"));
    public static final Element
            TRANSLATE_BUTTON = new Element("TRANSLATE_BUTTON", By.cssSelector("a[data-id=translate]"));
    public static final Element
            MORE_BUTTON = new Element("MORE_BUTTON", By.cssSelector("a[data-id=more]"));

    public ServicesPanel(String name) {
        super(name, ELEMENT_LOCATOR, new ElementsMapBuilder()
                .addElement(Q_BUTTON)
                .addElement(MARKET_BUTTON)
                .addElement(VIDEO_BUTTON)
                .addElement(IMAGES_BUTTON)
                .addElement(NEWS_BUTTON)
                .addElement(MAPS_BUTTON)
                .addElement(TRANSLATE_BUTTON)
                .addElement(MORE_BUTTON)
                .build());
    }

}
