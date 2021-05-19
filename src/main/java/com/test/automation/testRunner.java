package com.test.automation;

import com.github.valfirst.jbehave.junit.monitoring.JUnitReportingRunner;
import com.test.automation.reporters.AllureJBehave;
import com.test.automation.reporters.CustomRandomParamConverter;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.failures.PassingUponPendingStep;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.*;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.MarkUnmatchedStepsAsPending;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.StepFinder;
import org.junit.runner.RunWith;

import static com.test.automation.utils.ClassUtils.getInstantiatedClasses;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

/**
 * Created by tmuminova on 4/20/20.
 */
@RunWith(JUnitReportingRunner.class)
public class testRunner extends JUnitStories {
    private final URL CLASS_FILE;
    private final static Logger logger = Logger.getLogger(testRunner.class);
    AllureJBehave allureJBehave;

    public testRunner() {
        super();
        System.setProperty("allure.results.directory","target/allure-results");
        //System.setProperty("allure.link.issue.pattern","http://jira.yandex.com/browse/{}");
        String storyRunTime = System.getProperty("storyTimeoutInSecs","600");
        logger.info("###### timeout is: " + storyRunTime);
        CLASS_FILE = codeLocationFromClass(this.getClass());
        Embedder embedder = configuredEmbedder();
        embedder.useMetaFilters(metaFilters());
        embedder.embedderControls()
                .useStoryTimeouts(storyRunTime) // 15 minutes
                .doIgnoreFailureInView(true)
                .doIgnoreFailureInStories(true)
                .useThreads(1)
                .doFailOnStoryTimeout(true);
    }

    protected List<String> metaFilters() {
        String metaFilter = System.getProperty("meta.filter", "*");
        if (!(metaFilter.startsWith("+") || metaFilter.startsWith("-"))) {
            metaFilter = "+category " + metaFilter;
        }
        logger.info("Scenario ID to run: " + metaFilter.split(" ")[1]);
        return Arrays.asList(metaFilter);
    }

    public static Object[] storySteps() {
        try {
            return getInstantiatedClasses("com.test.automation.sut.steps");
        } catch (Exception ex) {
            throw new RuntimeException("Could not find steps classes: " + ex.toString());
        }
    }

    @Override
    public Configuration configuration() {
        //do not reinitialize configuration
        if (super.hasConfiguration()){
            logger.info("JBehave configuration is already initialized");
            return super.configuration();
        }
        else {
            allureJBehave = new AllureJBehave();
            StoryReporterBuilder reporterBuilder = new StoryReporterBuilder()
                    .withCodeLocation(CLASS_FILE)
                    .withMultiThreading(true)
                    .withFailureTrace(true)
                    .withFailureTraceCompression(false);
                reporterBuilder.withReporters(allureJBehave);
            Configuration jBehaveConfiguration = new MostUsefulConfiguration();
            jBehaveConfiguration
                    .useStoryControls(new StoryControls()
                            .doDryRun(false)
                            .doSkipScenariosAfterFailure(false)
                            .doResetStateBeforeStory(true))
                    .useFailureStrategy(new PassingUponPendingStep())
                    .useKeywords(new LocalizedKeywords())
                    .useStoryParser(new RegexStoryParser())
                    .usePendingStepStrategy(new PassingUponPendingStep())
                    .useStepCollector(new MarkUnmatchedStepsAsPending(new StepFinder(new StepFinder
                            .ByPriorityField())))
                    .useViewGenerator(new FreemarkerViewGenerator())
                    .useParameterControls(new ParameterControls()
                            .useDelimiterNamedParameters(true))
                    .useStepPatternParser(new RegexPrefixCapturingPatternParser())
                    .useStoryReporterBuilder(reporterBuilder)
                    .useParameterConverters(new ParameterConverters()
                            .addConverters(new CustomRandomParamConverter()));
            logger.info("JBehave is configured.");
            return jBehaveConfiguration;
        }
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(), storySteps());
    }

    protected List<String> storyPaths() {
        return new StoryFinder().findPaths(CLASS_FILE.getFile(),
                Collections.singletonList("**/" + System.getProperty("story.name", "*") + ".story"),
                Collections.singletonList(""));
    }
}
