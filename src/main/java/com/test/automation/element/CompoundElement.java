package com.test.automation.element;

import com.test.automation.utils.PropertiesUtil;
import com.test.automation.utils.Pair;
import com.test.automation.sut.steps.BaseSteps;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import java.util.*;

/**
 * Created by tmuminova on 4/9/20.
 */
public class CompoundElement extends Element implements ICompoundElement {
    protected final Map<String, IElement> elementsMap;
    private static final String REGEX_PATTERN = "[a-zA-Z]+\\.[a-zA-Z]+\\s*\\:\\s*";

    protected CompoundElement(String name, By locator, Map<String, IElement> elementsMap) {
        super(name, locator);
        this.elementsMap = elementsMap;
    }

    protected CompoundElement(Pair<String, By> pair, Map<String, IElement> elementsMap) {
        super(pair);
        this.elementsMap = elementsMap;
    }

    // Anonymous element that we can not address directly from a story. For example, a list item
    protected CompoundElement(WebElement webElement, Map<String, IElement> elementsMap) {
        super(webElement);
        this.elementsMap = elementsMap;
    }

    public Element getElement(String name){
        return (Element) this.getElement(name,"");
    }

    public IElement getElement(String name, String... splitPattern) {
        String splitChar = splitPattern.length==0 || splitPattern[0]=="" ? " -> " : splitPattern[0];
        String[] elementList = name.split(splitChar);

        IElement foundElement = null;
        int positionOfElementNotFound = 0;
        Map<String, IElement> elementSpecificElementMap = elementsMap;
        int lastElementOfTheList = elementList.length-1;

        if(elementList.length == 1){
            foundElement = elementSpecificElementMap.get(elementList[0]);
        }else{
            for(int i=0;i<lastElementOfTheList;i++){
                elementSpecificElementMap = ((CompoundElement)elementSpecificElementMap.get(elementList[i])).elementsMap;
            }
            foundElement = elementSpecificElementMap.get(elementList[lastElementOfTheList]);
        }
        if (foundElement == null){
            throw new RuntimeException(String.format("%s element doesn't have element %s.\n Possible elements are %s",
                    elementList[elementList.length-2],elementList[lastElementOfTheList],elementSpecificElementMap.keySet().toString()));
        }
        return foundElement;
    }

    public Element getElement(Pair<String, By> pair) {
        return getElement(pair.fst);
    }

    public <T> T getElementAs(Pair<String, By> pair, Class<T> tClass) {
        return (T) getElement(pair.fst);
    }

    public ICompoundElement getCompElement(String name) {
        return (ICompoundElement) getElement(name);
    }

    public ICompoundElement getCompElement(Pair<String, By> pair) {
        return (ICompoundElement) getElement(pair);
    }

    public Map<String, IElement> getElements() {
        return elementsMap;
    }

    public static class CompoundElementBuilder {
        private final String name;
        private final By locator;
        private final ElementsMapBuilder elementsMapBuilder;

        public CompoundElementBuilder(Pair<String, By> pair, ElementsMapBuilder elementsMapBuilder) {
            this(pair.fst, pair.snd, elementsMapBuilder);
        }

        public CompoundElementBuilder(String name, By locator, ElementsMapBuilder elementsMapBuilder) {
            this.elementsMapBuilder = elementsMapBuilder;
            this.name = name;
            this.locator = locator;
        }

        public CompoundElement build() {
            return new CompoundElement(name, locator, elementsMapBuilder.build());
        }
    }

    /**
     * this method allows you to have parametrized element
     * just put %s pattern to the locator and provide string for replacing the pattern as second
     * parameter "String ... params"
     * EXAMPLE :
     * xpath for element ELEMENT is "//div[contains('text(),'%s'')]"
     * to run this method "parametrizedElement(ELEMENT, "Hi_I'm_string")"
     * so the method will return WebElement with xpath "//div[contains('text(),'Hi_I'm_string'')]"
     * @param elementName - Element
     * @param params - string(strings) to replace parameters
     * @return returns WebElement for now
     */
    public WebElement parameterizedWebElement(IElement elementName, String ... params){
        if(elementName.getLocator().toString().isEmpty()){
            throw new IllegalArgumentException("###### there is no locator in element "+elementName.getName());
        }
        return BaseSteps.getDriver().findElement(substitudeParams(elementName.getLocator(), params));
    }

    /**
     * the same as parameterizedWebElement method but it returns a List<> of WebElements
     * @param elementName
     * @param params
     * @return List<WebElement>
     */
    public List<WebElement> parameterizedWebElements(IElement elementName, String ... params){
        if(elementName.getLocator().toString().isEmpty()){
            throw new IllegalArgumentException("###### there is no locator in element "+elementName.getName());
        }
        return BaseSteps.getDriver().findElements(substitudeParams(elementName.getLocator(), params));
    }

    /**
     * the same as parameterizedWebElement method but it returns Element instead of WebElement
     * @param elementName
     * @param params
     * @return Element object
     */
    public Element parameterizedElement(IElement elementName, String ... params){
        if(elementName.getLocator().toString().isEmpty()){
            throw new IllegalArgumentException("###### there is no locator in element "+elementName.getName());
        }
        return new Element("TEMP_PARAMETERIZED_ELEMENT", substitudeParams(elementName.getLocator(), params));
    }

    public By substitudeParams(By parameterizedLocator, String ... params) {
        String selector = getPureLocator(parameterizedLocator);
        String replacedParams = PropertiesUtil.replaceParameters(selector, params);
        switch (parameterizedLocator.getClass().getSimpleName()) {
            case "ByXPath":
                return By.xpath(replacedParams);
            case "ByCssSelector":
                return By.cssSelector(replacedParams);
            case "ById":
                return By.id(replacedParams);
            case "ByClassName":
                return By.className(replacedParams);
            case "ByLinkText":
                return By.linkText(replacedParams);
            case "ByPartialLinkText":
                return By.partialLinkText(replacedParams);
            case "ByTagName":
                return By.tagName(replacedParams);
            default:
                throw new IllegalArgumentException("###### unable to indicate locator type ");
        }
    }

    /**
     * return only locator string
     * @param locator
     * @return
     */
    private String getPureLocator(By locator) {
        if(locator.toString().isEmpty()){
            throw new IllegalArgumentException("###### locator is empty");
        }
        String[] result = locator.toString().split(REGEX_PATTERN);
        return result[1];
    }

}

