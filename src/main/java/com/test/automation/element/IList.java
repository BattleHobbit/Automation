package com.test.automation.element;

import java.util.List;

/**
 * Created by tmuminova on 4/8/20.
 */

public interface IList extends ICompoundElement {
    int getListSize();

    List<? extends IElement> getList();

    IElement getListItem(int position);

    IElement getListItem(String itemName);

    boolean itemExists(String itemName);
}
