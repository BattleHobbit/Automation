package com.test.automation.sut.pages;

import com.test.automation.element.ElementsMapBuilder;
import com.test.automation.page.Page;
import com.test.automation.sut.elements.SearchPanel;
import com.test.automation.sut.elements.ServicesPanel;
import com.test.automation.utils.Pair;
import org.openqa.selenium.By;

/**
 * Created by tmuminova on 4/22/20.
 */
public class YandexStart extends Page<YandexStart> {
    public static final Pair
            PAGE_LOCATOR = Pair.of("YANDEX_START_PAGE", By.tagName("body"));
    public static final SearchPanel
            SEARCH_PANEL = new SearchPanel("SEARCH_PANEL");
    public static final ServicesPanel
            SERVICES_PANEL = new ServicesPanel("SERVICES_PANEL");

    public YandexStart() {
        super(PAGE_LOCATOR, new ElementsMapBuilder()
        .addCompoundElement(SEARCH_PANEL)
        .addCompoundElement(SERVICES_PANEL));
    }
}
