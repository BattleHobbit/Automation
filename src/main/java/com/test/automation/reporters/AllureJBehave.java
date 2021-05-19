package com.test.automation.reporters;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.*;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.*;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.StepCollector;

import static io.qameta.allure.util.ResultsUtils.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.*;
import static java.util.Comparator.comparing;

/**
 * Created by tmuminova on 4/16/20.
 */
public class AllureJBehave implements StoryReporter {
    private static final Logger logger = Logger.getLogger(AllureJBehave.class);

    private final AllureLifecycle lifecycle;

    private final ThreadLocal<Story> currentStory = new InheritableThreadLocal<>();

    private final ThreadLocal<Scenario> currentScenario = new InheritableThreadLocal<>();

    private final Map<Scenario, List<String>> scenarioUuids = new ConcurrentHashMap<>();

    private final ThreadLocal<Deque<Story>> givenStories = ThreadLocal.withInitial(
            LinkedList::new);

    private Status scenarioStatus = null;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public AllureJBehave() {
        this(Allure.getLifecycle());
    }

    public AllureJBehave(final AllureLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    private static ReportHelperSingleton reportHelperSingleton = ReportHelperSingleton.getInstance();
    private ArrayList<Link> issueLinks;

    @Override
    public void storyNotAllowed(Story story, String s) {

    }

    @Override
    public void storyCancelled(Story story, StoryDuration storyDuration) {

    }

    @Override
    public void beforeStory(final Story story, final boolean givenStory) {
        if (givenStory) {
            givenStories.get().push(story);
        } else {
            currentStory.set(story);
            reportHelperSingleton.setCurrentStory(story);
        }
    }

    @Override
    public void afterStory(final boolean givenStory) {
        if (givenStory) {
            givenStories.get().pop();
        } else {
            currentStory.remove();
        }
    }

    @Override
    public void narrative(Narrative narrative) {

    }

    @Override
    public void lifecyle(Lifecycle lifecycle) {

    }

    @Override
    public void scenarioNotAllowed(Scenario scenario, String s) {

    }

    @Deprecated
    @Override
    public void beforeScenario(String s) {

    }

    @Override
    public void beforeScenario(final Scenario scenario) {
        if (isGivenStory()) {
            return;
        }
        currentScenario.set(scenario);
        setScenarioStatus(null);
        reportHelperSingleton.setCurrentScenario(scenario);
        reportHelperSingleton.currentStepCount = 0;
        reportHelperSingleton.selectWebSteps();
        reportHelperSingleton.setIsWebTest();
        reportHelperSingleton.setCurrentScenarioTitle(scenario.getTitle());
        reportHelperSingleton.setCurrentScenarioMeta(scenario.getMeta());
        if (reportHelperSingleton.requiresVideoRecording()) {
            reportHelperSingleton.recordVideo();
        }
        else {
            logger.info("Screen recording is not required for scenario");
        }

        if (notParameterised(scenario)) {
            Map<String, String> scenarioParameters;
            Meta scenarioMeta = reportHelperSingleton.getCurrentScenarioMeta();
            if (scenarioMeta != null && !scenarioMeta.isEmpty()){
                scenarioParameters = new HashMap<>();
                for (String metaName : scenarioMeta.getPropertyNames()){
                    scenarioParameters.put(metaName,scenarioMeta.getProperty(metaName));
                }
            }
            else scenarioParameters = emptyMap();
            final String uuid = UUID.randomUUID().toString();
            usingWriteLock(() -> scenarioUuids.put(scenario, new ArrayList<>(singletonList(uuid))));
            startTestCase(uuid, scenario, scenarioParameters);
        } else {
            usingWriteLock(() -> scenarioUuids.put(scenario, new ArrayList<>()));
        }
    }

    @Override
    @Deprecated
    public void scenarioMeta(Meta meta) {

    }

    @Override
    public void beforeExamples(final List<String> steps, final ExamplesTable table) {
        if (isGivenStory()) {
            return;
        }
        final Scenario scenario = currentScenario.get();
        lock.writeLock().lock();
        try {
            scenarioUuids.put(scenario, new ArrayList<>());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    @Deprecated
    public void example(final Map<String, String> tableRow) {
        if (isGivenStory()) {
            return;
        }
        final Scenario scenario = currentScenario.get();
        final String uuid = UUID.randomUUID().toString();
        usingWriteLock(() -> scenarioUuids.getOrDefault(scenario, new ArrayList<>()).add(uuid));
        startTestCase(uuid, scenario, tableRow);
    }

    @Override
    public void example(Map<String, String> map, int i) {
        if (isGivenStory()) {
            return;
        }
        final Scenario scenario = currentScenario.get();
        final String uuid = UUID.randomUUID().toString();
        usingWriteLock(() -> scenarioUuids.getOrDefault(scenario, new ArrayList<>()).add(uuid));
        startTestCase(uuid, scenario, map);

    }

    @Override
    public void afterExamples() {

    }

    @Override
    public void beforeStorySteps(StepCollector.Stage stage) {

    }

    @Override
    public void afterStorySteps(StepCollector.Stage stage) {

    }

    @Override
    public void beforeScenarioSteps(StepCollector.Stage stage) {

    }

    @Override
    public void afterScenarioSteps(StepCollector.Stage stage) {

    }

    @Override
    public void beforeGivenStories() {

    }

    @Override
    public void givenStories(GivenStories givenStories) {

    }

    @Override
    public void givenStories(List<String> list) {

    }

    @Override
    public void afterGivenStories() {

    }

    @Override
    public void beforeStep(final String step) {
        final String stepUuid = UUID.randomUUID().toString();
        ++reportHelperSingleton.currentStepCount;
        logger.warn("\n*******************************");
        logger.warn("****** Starting step " + reportHelperSingleton.currentStepCount + ": " + step);
        logger.warn("*******************************\n");
        getLifecycle().startStep(stepUuid, new StepResult().setName(step));
    }

    @Override
    public void successful(final String step) {
        getLifecycle().updateTestCase(result -> result.setStatus(Status.PASSED));
        getLifecycle().updateStep(result -> result.setStatus(Status.PASSED));
        getLifecycle().stopStep();
        setScenarioStatus(Status.PASSED);
    }

    @Override
    public void afterScenario() {
        if (isGivenStory()) {
            return;
        }
        final Scenario scenario = currentScenario.get();
        if(reportHelperSingleton.requiresVideoRecording()
                &&  reportHelperSingleton.getVideoRecorder().isRecording){
            logger.info("Stopping video recording");
            try {
                reportHelperSingleton.getVideoRecorder().stopRecording();
                if(getScenarioStatus().equals(Status.FAILED)) {
                    Path pathToRecording = new File(reportHelperSingleton.getMovieFolder() + "/screencast.avi").toPath();
                    try (InputStream is = Files.newInputStream(pathToRecording)) {
                        Allure.addAttachment("Screencast_" + reportHelperSingleton.getScenarioID(reportHelperSingleton.getCurrentScenario()), is);
                        logger.info("Screen recording is saved at Allure Results" + reportHelperSingleton.getMovieFolder());
                    }
                    logger.info("Screen recording is saved at " + reportHelperSingleton.getMovieFolder());
                    Files.delete(pathToRecording);
                }
            } catch (Exception ex) {
                throw new RuntimeException("Error stopping video recording: " + ex.toString());
            }
        }
        saveLog();
        usingReadLock(() -> {
            final List<String> uuids = scenarioUuids.getOrDefault(scenario, emptyList());
            uuids.forEach(this::stopTestCase);
        });
        currentScenario.remove();
        usingWriteLock(() -> scenarioUuids.remove(scenario));
    }

    @Override
    public void ignorable(final String step) {
        beforeStep(step);
        getLifecycle().stopStep();
    }

    @Override
    public void comment(String s) {

    }

    @Override
    public void pending(final String step) {
        beforeStep(step);
        getLifecycle().updateStep(result -> result.setStatus(Status.SKIPPED));
        setScenarioStatus(Status.SKIPPED);
        getLifecycle().stopStep();
    }

    @Override
    public void notPerformed(final String step) {
        beforeStep(step);
        getLifecycle().stopStep();
    }

    public Status getScenarioStatus() {
        return scenarioStatus;
    }

    public void setScenarioStatus(Status scenarioStatus) {
        this.scenarioStatus = scenarioStatus;
    }

    @Override
    public void failedOutcomes(String s, OutcomesTable outcomesTable) {

    }

    @Override
    public void restarted(String s, Throwable throwable) {

    }

    @Override
    public void restartedStory(Story story, Throwable throwable) {

    }

    @Override
    public void dryRun() {

    }

    @Override
    public void pendingMethods(List<String> list) {

    }

    @Override
    public void failed(final String step, final Throwable cause) {
        File screenshotFile = reportHelperSingleton.takeScreenshot();
        if(screenshotFile != null) {
            try (InputStream is = Files.newInputStream(screenshotFile.toPath())) {
                Allure.addAttachment("Step_" + reportHelperSingleton.currentStepCount, is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final Throwable unwrapped = cause instanceof UUIDExceptionWrapper
                ? cause.getCause()
                : cause;


        final Status status;
        boolean expected = reportHelperSingleton.selectExpectedTest();

        if (!expected) {
            status = getStatus(unwrapped).orElse(Status.FAILED);
        }
        else if(!reportHelperSingleton.getExpectedSteps().equals("")
                && !reportHelperSingleton.getExpectedSteps().contains(String.valueOf(reportHelperSingleton.currentStepCount))){
            logger.info("Failure on step " + reportHelperSingleton.currentStepCount + " is NOT expected");
            status = getStatus(unwrapped).orElse(Status.FAILED);
        }
        else {
            logger.info("Failure on step " + reportHelperSingleton.currentStepCount + " is expected");
            status = Status.SKIPPED;
        }
        final StatusDetails statusDetails = getStatusDetails(unwrapped).orElseGet(StatusDetails::new);

        getLifecycle().updateStep(result -> {
            result.setStatus(status);
            result.setStatusDetails(statusDetails);
        });

        getLifecycle().updateTestCase(result -> {
            result.setStatus(status);
            result.setStatusDetails(statusDetails);
        });

        setScenarioStatus(Status.FAILED);

        getLifecycle().stopStep();
    }


    public AllureLifecycle getLifecycle() {
        return lifecycle;
    }

    private String findParentSuiteLabel(String storyPath){
        String[] storyPathArray = storyPath.split("/");
        String suite = storyPathArray[storyPathArray.length - 2];
        return suite;
    }

    protected void startTestCase(final String uuid,
                                 final Scenario scenario,
                                 final Map<String, String> tableRow) {
        final Story story = currentStory.get();

        final String name = scenario.getTitle();
        final String fullName = String.format("%s: %s", story.getName(), name);

        final List<Parameter> parameters = tableRow.entrySet().stream()
                .map(entry -> new Parameter().setName(entry.getKey()).setValue(entry.getValue()))
                .collect(Collectors.toList());

        String[] storyPathArray = story.getPath().split("/");

        ArrayList<Label> aLabels = new ArrayList<>();
        aLabels.add(createFrameworkLabel("JBehave"));
        aLabels.add(createStoryLabel(story.getName()));
        aLabels.add(createHostLabel());
        aLabels.add(createThreadLabel());
        try {
            logger.info("Setting PackageLabel = " + storyPathArray[1]);
            aLabels.add(createPackageLabel(storyPathArray[1]));
            logger.info("Setting ParentSuiteLabel = " + storyPathArray[1]);
            aLabels.add(createParentSuiteLabel(storyPathArray[1]));
            if (storyPathArray.length > 2){
                logger.info("Setting SuiteLabel = " + storyPathArray[2]);
                aLabels.add(createSuiteLabel(storyPathArray[2]));
            }
            if (storyPathArray.length > 3) {
                logger.info("Setting SubSuiteLabel = " + storyPathArray[3]);
                aLabels.add(createSubSuiteLabel(storyPathArray[3]));
            }
        }
        catch (Exception e){
            logger.warn("Something happened during adding labels to scenario",e);
        }

        issueLinks = new ArrayList<>(reportHelperSingleton.getIssuesLinksFromScenario(currentScenario.get()));
        if(!issueLinks.isEmpty()){
            logger.info("Current scenario has expected failures. It will not be restarted if it fails");
            System.setProperty("RESTART_COUNT","0");
        }

        final List<Label> labels = aLabels;

        final String historyId = getHistoryId(fullName, parameters);

        final TestResult result = new TestResult()
                .setUuid(uuid)
                .setName(name)
                .setFullName(fullName)
                .setStage(Stage.SCHEDULED)
                .setLabels(labels)
                .setLinks(issueLinks)
                .setParameters(parameters)
                .setDescription(story.getDescription().asString())
                .setHistoryId(historyId);

        getLifecycle().scheduleTestCase(result);
        getLifecycle().startTestCase(result.getUuid());
    }

    protected boolean notParameterised(final Scenario scenario) {
        return scenario.getExamplesTable().getRowCount() == 0;
    }

    protected String getHistoryId(final String fullName, final List<Parameter> parameters) {
        final MessageDigest digest = getMd5Digest();
        digest.update(fullName.getBytes(UTF_8));
        parameters.stream()
                .sorted(comparing(Parameter::getName).thenComparing(Parameter::getValue))
                .forEachOrdered(parameter -> {
                    digest.update(parameter.getName().getBytes(UTF_8));
                    digest.update(parameter.getValue().getBytes(UTF_8));
                });
        final byte[] bytes = digest.digest();
        return bytesToHex(bytes);
    }

    private void saveLog() {
        RollingFileAppender fileAppender = (RollingFileAppender) Logger.getRootLogger().getAppender("FILE");
        File srcFilePath = new File(fileAppender.getFile());
        try (InputStream is = Files.newInputStream(srcFilePath.toPath())) {
            logger.info("Saving debuglog to Allure results folder");
            String category = this.currentScenario.get().getMeta().getProperty("category");
            String podName = System.getenv("MY_POD_NAME");
            String attachmentName = podName + "_debuglog_" + (category == "" ? "no_category" : category);
            Allure.addAttachment(attachmentName, is);
            logger.info("Debuglogs '" + attachmentName + "' are saved");
        } catch (Exception e) {
            logger.error("Can't attach debuglog to test results");
            e.printStackTrace();
        }
    }

    protected void stopTestCase(final String uuid) {
        getLifecycle().stopTestCase(uuid);
        getLifecycle().writeTestCase(uuid);
    }

    private void usingWriteLock(final Runnable runnable) {
        lock.writeLock().lock();
        try {
            runnable.run();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private boolean isGivenStory() {
        return !givenStories.get().isEmpty();
    }

    private void usingReadLock(final Runnable runnable) {
        lock.readLock().lock();
        try {
            runnable.run();
        } finally {
            lock.readLock().unlock();
        }
    }

}
