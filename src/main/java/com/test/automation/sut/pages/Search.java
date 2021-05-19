package com.test.automation.sut.pages;

import com.test.automation.element.ElementsMapBuilder;
import com.test.automation.page.Page;
import com.test.automation.utils.Pair;
import org.openqa.selenium.By;

/**
 * Created by tmuminova on 4/22/20.
 */
public class Search extends Page<Search> {
    public static final Pair
            PAGE_LOCATOR = Pair.of("ACCOUNTS_PAGE", By.tagName("body"));

    public Search() {
        super(PAGE_LOCATOR, new ElementsMapBuilder());
    }
}
