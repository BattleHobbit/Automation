package com.test.automation.sut;

import com.test.automation.sut.pages.Search;
import com.test.automation.sut.pages.YandexStart;
import com.test.automation.webdriver.ExtendedWebDriver;
import com.test.automation.page.IPage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.test.automation.sut.steps.BaseSteps.getDriver;

/**
 * Created by tmuminova on 4/13/20.
 */

public class PageNavigator {
    private static final Logger logger = LogManager.getLogger(PageNavigator.class);
    private final Map<PageType, String> pageLinkMap = new HashMap<>();
    private final Map<PageType, Class> pageClassMap = new HashMap<>();
    private final Map<PageType, IPage> pageObjectMap = new HashMap<>();
    private PageType currentPageType;
    URL currentBaseUrl = null;

    public PageNavigator(URL baseUrl) {
        currentBaseUrl = baseUrl;
        this.currentPageType = null;
        initPageLinkMap(baseUrl);
        initPageClassMap();
    }

    private void initPageLinkMap(URL baseUrl) {
        pageLinkMap.put(null, baseUrl.toString());
        pageLinkMap.put(PageType.YANDEX_START, baseUrl.toString());
        pageLinkMap.put(PageType.SEARCH, baseUrl + "search/");
    }

    private void initPageClassMap() {
        pageClassMap.put(PageType.YANDEX_START, YandexStart.class);
        pageClassMap.put(PageType.SEARCH, Search.class);
    }


    public <T> T getPage(Class<T> pageClassToProxy) {
        try {
            try {
                Constructor<T> constructor = pageClassToProxy.getConstructor(ExtendedWebDriver.class);
                return constructor.newInstance();
            } catch (NoSuchMethodException e) {
                return pageClassToProxy.getDeclaredConstructor().newInstance();
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    public IPage getPage(PageType pageType) {
        logger.debug("Getting page: " + pageType);
        if (!pageObjectMap.containsKey(pageType)) {
            synchronized (pageObjectMap) {
                Class pageClassToProxy = pageClassMap.get(pageType);
                pageObjectMap.put(pageType, (IPage) getPage(pageClassToProxy));
            }
        }
        return pageObjectMap.get(pageType);
    }

    public PageType getCurrentPageType() {
        return currentPageType;
    }

    public IPage getCurrentPage() {
        return getPage(currentPageType);
    }

    public void setCurrentPage(PageType pageType) {
        currentPageType = pageType;
    }

    /**
     *
     * @param pageType
     * @return - string of navigating url
     * to see exact url to which script is navigating for
     */
    public String getNavigatingUrl(PageType pageType){
        String navigatingUrl = pageLinkMap.get(pageType);
        return navigatingUrl;
    }

    public IPage navigateTo(PageType pageType) {
        logger.debug("Navigating to page: " + pageType);
        final String url = pageLinkMap.get(pageType);
        final IPage page = getPage(pageType);
        if (!isItCurrentUrl(url)) {
            getDriver().get(url);
        }
        page.get();
        currentPageType = pageType;
        return page;
    }


    public boolean isItCurrentUrl(String desiredUrl) {
        String currentPage = getDriver().getCurrentUrl().split("[?#]")[0];
        logger.info("Current page URL: " + currentPage);

        final String desiredPage = desiredUrl.split("[?#]")[0];
        logger.info("###### current page is \""+currentPage+"\" | desired page is \""+desiredPage+"\"");
        return currentPage.equalsIgnoreCase(desiredPage);
    }

    public boolean isItCurrentPage(PageType pageType) {
        String url = pageLinkMap.get(pageType);
        boolean areWeThereYet = isItCurrentUrl(url);
        if (areWeThereYet)
            currentPageType = pageType;
        return areWeThereYet;
    }


    private String executeScript(String script){
        ExtendedWebDriver driver = getDriver();
        return (String) driver.getJavascriptExecutor().executeScript(script);
    }


    public enum PageType {
        YANDEX_START, SEARCH
    }
}
