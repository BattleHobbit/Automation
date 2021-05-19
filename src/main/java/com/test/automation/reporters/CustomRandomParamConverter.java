package com.test.automation.reporters;
import com.test.automation.sut.steps.BaseSteps;
import org.apache.commons.lang.RandomStringUtils;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.ParameterConverters;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tmuminova on 4/20/20.
 */
public class CustomRandomParamConverter implements ParameterConverters.ParameterConverter {

    @Override
    public boolean accept(Type type) {
        String className = ((Class) type).getSimpleName();
        return className.equalsIgnoreCase("String") ||
                className.equalsIgnoreCase("ExamplesTable");
    }

    @Override
    public Object convertValue(String value, Type type) {
        String className = ((Class) type).getSimpleName();

        if (className.equalsIgnoreCase("String")) {
            if (!value.startsWith("$"))
                return value;

            String valueCandidate = (String) BaseSteps.getSut().getProperty(value);
            if (!valueCandidate.isEmpty())
                return valueCandidate;
            else {
                valueCandidate = RandomStringUtils.randomAlphanumeric(10);
                BaseSteps.getSut().setProperty(value, valueCandidate);
                return valueCandidate;
            }
        } else if (className.equalsIgnoreCase("ExamplesTable")) {
            // All variables should already be set. Just substitute, don't generate new values
            String valueCandidate = value;
            Pattern pattern = Pattern.compile("(\\$[\\w]+)");
            Matcher matcher = pattern.matcher(value);
            while (matcher.find()) {
                String key = matcher.group(1);
                String paramValue = (String) BaseSteps.getSut().getProperty(key);
                if (!paramValue.isEmpty()) {
                    // Append the value to variable name, for proper reporting by the FTL template
                    valueCandidate = valueCandidate.replace(key, paramValue);
                }
            }
            return new ExamplesTable(valueCandidate);
        } else
            throw new RuntimeException("Should not be here. className=" + className);
    }
}
