package com.test.automation.reporters;

import com.test.automation.sut.Sut;
import com.test.automation.utils.screencast.VideoRecorder;
import com.test.automation.webdriver.ExtendedWebDriver;
import com.test.automation.sut.steps.BaseSteps;
import io.qameta.allure.model.Link;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.qameta.allure.util.ResultsUtils.createIssueLink;

/**
 * Created by tmuminova on 4/14/20.
 */
public class ReportHelperSingleton {
    private static final Logger logger = LogManager.getLogger(ReportHelperSingleton.class);

    private static ReportHelperSingleton reportHelperSingleton;

    private Story currentStory;
    private Scenario currentScenario;
    private String currentScenarioTitle;
    private Meta currentScenarioMeta;

    boolean isWebTest = true;
    String screenshotsFolder;
    private List<Integer> webSteps;
    int currentStepCount = 0;
    private String expectedSteps = "";

    private VideoRecorder videoRecorder;
    private String movieFolder;
    private ReportHelperSingleton(){

    }

    public static ReportHelperSingleton getInstance(){
        if(reportHelperSingleton == null){
            reportHelperSingleton = new ReportHelperSingleton();
        }
        return reportHelperSingleton;
    }

    void flushLog() {
        RollingFileAppender fileAppender = (RollingFileAppender) Logger.getRootLogger().getAppender("FILE");
        String srcFilePath = fileAppender.getFile();
        File destFile = new File("target/jbehave/debuglogs/" + currentStory.getPath()
                + "/Scenario_" + getScenarioID(currentScenario) + "/DebugLog.txt");
        try {
            FileUtils.copyFile(new File(srcFilePath), destFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        fileAppender.rollOver(); // to create new log for new scenario
    }

    public File takeScreenshot(){
        if(getIsWebTest() && ExtendedWebDriver.isDriverInstanceCreated()){
            if(includesWebStepsIndividualization()){
                if(this.webSteps.contains(this.currentStepCount)){
                    return makeScreenshot();
                }
            }else{
                return makeScreenshot();
            }
        }
        return null;
    }

    public File makeScreenshot() {
        final Sut sut = BaseSteps.getSut();
        File outputFile = null;
        try {
            screenshotsFolder = "target/jbehave/screenshots/" + currentStory.getPath() +
                    "/Scenario_" + getScenarioID(currentScenario);
            outputFile = new File(screenshotsFolder + "/Step_" + this.currentStepCount + ".png");
            new File(outputFile.getParent()).mkdirs(); // make necessary directories
            BaseSteps.getDriver().getScreenshotAndSaveAs(outputFile);
            return outputFile;
        } catch (Exception e) {
            logger.warn("Failed to take screenshot:\n" + e.getMessage());
            e.printStackTrace();
        }
        return outputFile;
    }

    public boolean getIsWebTest() {
        return isWebTest;
    }

    public void setIsWebTest() {
        isWebTest = !currentScenario.getMeta().getProperty("isWeb").equals("false");
    }

    public int getScenarioID(Scenario scenario){
        for (int i = 0; i < currentStory.getScenarios().size(); i++){
            if(scenario.getTitle().equals(currentStory.getScenarios().get(i).getTitle())){
                return i+1;
            }
        }
        return 1;
    }

    public Set<Link> getIssuesLinksFromScenario(Scenario currentScenario){
        String scenarioTitle = reportHelperSingleton.getCurrentScenarioTitle();
        Set<Link> links = new HashSet<>();
        if (!currentScenario.getMeta().getProperty("expected").isEmpty()) {
            logger.info("###### Scenario meta contains expected");
            String[] expectedFailures = currentScenario.getMeta().getProperty("expected").split(" ");
            for(String issue: expectedFailures) {
                Pattern pattern = Pattern.compile("((?<!([A-Za-z]{1,10})-?)[A-Z]+-\\d+)");
                Matcher matcher = pattern.matcher(issue);
                if (matcher.find())
                    links.add(createIssueLink(matcher.group(1)));
            }
        }
        else if(scenarioTitle.contains("@expected") ){
            logger.info("###### Scenario title contains expected");
            Pattern pattern = Pattern.compile("((?<!([A-Za-z]{1,10})-?)[A-Z]+-\\d+)");
            Matcher matcher = pattern.matcher(scenarioTitle);
            if (matcher.find())
                links.add(createIssueLink(matcher.group(1)));
        }
        return links;
    }

    public void selectWebSteps(){
        try {
            this.webSteps = new ArrayList<Integer>();
            String steps = currentScenario.getMeta().getProperty("webSteps");
            String[] webSteps = steps.split(",");
            for (String portion:webSteps) {
                if(portion.contains("-")){
                    String[] range = portion.split("-");
                    if(range.length < 2){
                        for(int j=Integer.parseInt(range[0]);j<=this.currentScenario.getSteps().size();j++){
                            this.webSteps.add(j);
                        }
                    }else{
                        for(int i= Integer.parseInt(range[0]);i<=Integer.parseInt(range[1]);i++){
                            this.webSteps.add(i);
                        }
                    }
                }else{
                    if(!this.webSteps.contains(Integer.parseInt(portion))){
                        this.webSteps.add(Integer.parseInt(portion));
                    }
                }
            }
        }catch (Exception e){
            this.webSteps = new ArrayList<Integer>();
        }
    }

    private boolean includesWebStepsIndividualization(){
        return !currentScenario.getMeta().getProperty("webSteps").isEmpty();
    }

    public boolean requiresVideoRecording(){
        logger.info("Checking if video should be recorded for this scenario");
        return !currentScenario.getMeta().getProperty("screenRecording").equals("false") && reportHelperSingleton.getIsWebTest();
    }

    public void recordVideo(){
        logger.info("Screen recording is required for scenario: " + this.currentScenario.getTitle());
        try {
            reportHelperSingleton.setVideoRecorder(new VideoRecorder());
            movieFolder = "target/jbehave/debuglogs/" + currentStory.getPath() + "/Scenario_" + getScenarioID(currentScenario);
            logger.info("Start recording");
            reportHelperSingleton.getVideoRecorder().startRecording(movieFolder); // put screencast next to debug log
        } catch (Exception ex) {
            logger.error("Could not start video recording: " + ex.toString());
        }
    }

    public void setVideoRecorder(VideoRecorder videoRecorder) {
        this.videoRecorder = videoRecorder;
    }

    public Boolean selectExpectedTest(){
        String scenarioTitle = reportHelperSingleton.getCurrentScenarioTitle();
        Boolean isScenarioHasExpectedFailure = false;
        String expectedString = "";
        String expectedStepsString = "";

        if(scenarioTitle.contains("@expected") ){
            logger.info("###### Scenario title contains expected");
            isScenarioHasExpectedFailure = true;
            expectedString = scenarioTitle.split("@expected")[1];
            if(expectedString.contains(",")){
                logger.info("###### looks like expected to fail step specified");
                String[] expectedSteps = expectedString.replace(" ","").split(",");
                try{
                    StringBuilder expectedStepsBuilder = new StringBuilder();
                    for (String s: expectedSteps){
                        try {
                            Integer.parseInt(s);
                            expectedStepsBuilder.append(s + " ");
                        }
                        catch (NumberFormatException e){
                            logger.error("String " + expectedSteps + " is not an integer");
                        }
                    }
                    expectedStepsString = expectedStepsBuilder.toString().trim();

                    reportHelperSingleton.setExpectedSteps(expectedStepsString);
                    logger.info("###### step #" + reportHelperSingleton.getExpectedSteps() + " is expected to fail step ");
                }
                catch (NumberFormatException e){
                    logger.error("String " + expectedSteps + " is not an integer");
                }
            }
        }
        else if (null != currentScenarioMeta && !currentScenarioMeta.getProperty("expected").isEmpty()) {
            logger.info("###### Scenario meta contains expected");
            isScenarioHasExpectedFailure = true;
            expectedString = currentScenarioMeta.getProperty("expected");
        }

        return isScenarioHasExpectedFailure;
    }

    public String getMovieFolder() {
        return movieFolder;
    }

    public void setMovieFolder(String movieFolder) {
        this.movieFolder = movieFolder;
    }

    public int getCurrentStepCount() {
        return currentStepCount;
    }

    public String getExpectedSteps() {
        return expectedSteps;
    }

    public void setExpectedSteps(String expectedSteps) {
        this.expectedSteps = expectedSteps;
    }

    public String getCurrentScenarioTitle() {
        return currentScenarioTitle;
    }

    public Story getCurrentStory() {
        return currentStory;
    }

    public void setCurrentStory(Story currentStory) {
        this.currentStory = currentStory;
    }

    public Scenario getCurrentScenario() {
        return currentScenario;
    }

    public void setCurrentScenario(Scenario currentScenario) {
        this.currentScenario = currentScenario;
    }

    public VideoRecorder getVideoRecorder() {
        return videoRecorder;
    }


    public void setCurrentScenarioTitle(String currentScenarioTitle) {
        logger.info("Current Scenario Title is - " + currentScenarioTitle);
        this.currentScenarioTitle = currentScenarioTitle;
    }

    public Meta getCurrentScenarioMeta() {
        return currentScenarioMeta;
    }

    public void setCurrentScenarioMeta(Meta currentScenarioMeta) {
        logger.info("Current Scenario has meta: " + currentScenarioMeta);
        this.currentScenarioMeta = currentScenarioMeta;
    }
}
