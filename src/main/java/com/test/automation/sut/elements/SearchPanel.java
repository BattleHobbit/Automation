package com.test.automation.sut.elements;

import com.test.automation.element.CompoundElement;
import com.test.automation.element.Element;
import com.test.automation.element.ElementsMapBuilder;
import org.openqa.selenium.By;

public class SearchPanel extends CompoundElement {
    public static final By
            ELEMENT_LOCATOR = By.cssSelector("div.home-arrow__search");
    public static final Element
            SEARCH_FIELD = new Element("SEARCH_FIELD", By.cssSelector("input.input__control.input__input.mini-suggest__input"));
    public static final Element
            VOICE_SEARCH_BUTTON = new Element("VOICE_SEARCH_BUTTON", By.cssSelector("div.input__voice-search"));
    public static final Element
            KEYBOARD_BUTTON = new Element("KEYBOARD_BUTTON", By.cssSelector("i.b-ico.keyboard-loader__icon.b-ico-kbd"));
    public static final Element
            SEARCH_BUTTON = new Element("SEARCH_BUTTON", By.cssSelector("button.button.mini-suggest__button.button_theme_search.button_size_search.i-bem.button_js_inited"));

    public SearchPanel(String name) {
        super(name, ELEMENT_LOCATOR, new ElementsMapBuilder()
                .addElement(SEARCH_FIELD)
                .addElement(VOICE_SEARCH_BUTTON)
                .addElement(KEYBOARD_BUTTON)
                .addElement(SEARCH_BUTTON)
                .build());
    }

}
