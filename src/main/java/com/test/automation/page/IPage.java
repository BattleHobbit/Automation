package com.test.automation.page;

import com.test.automation.element.ICompoundElement;

/**
 * Created by tmuminova on 4/8/20.
 */
public interface IPage<T> extends ICompoundElement {
    T get();

    boolean isLoaded() throws Error;

    T load();

    boolean screenIsVisible();

    T waitUtilScreenIsVisible();

    void waitUtilScreenIsNotVisible();
}
