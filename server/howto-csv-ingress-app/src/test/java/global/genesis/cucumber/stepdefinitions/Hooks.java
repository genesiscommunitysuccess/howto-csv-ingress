package global.genesis.cucumber.stepdefinitions;

import com.microsoft.playwright.Page;
import csv_ingress.utilities.config_utilities.ConfigReader;
import io.cucumber.java.*;

import java.net.Socket;

import static csv_ingress.utilities.allure_utitilities.UITestUtils.addAttachment;
import static csv_ingress.utilities.playwright_driver.PlaywrightDriver.*;
import static global.genesis.cucumber.utils.UITestUtils.addScreenshot;

public class Hooks {
    public static final ConfigReader config_properties = new ConfigReader("src/test/resources/4-config/config.properties");

    private static boolean isApiScenario(Scenario scenario) {
        return scenario.getSourceTagNames().stream().anyMatch(t -> t.contains("@API"));
    }

    private static boolean isUiAvailable() {
        try (Socket ignored = new Socket("localhost", 6060)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @BeforeAll
    public static void beforeAll() {
        if (isUiAvailable()) {
            createPlaywright();
        }
    }

    @AfterAll
    public static void afterAll() {
        if (isUiAvailable()) {
            closePlaywright();
        }
    }

    @Before
    public void setup(Scenario scenario) {
        if (!isApiScenario(scenario)) {
            Page page = getPage();
            page.navigate(config_properties.readProperty("defaultHost"));
        }
    }

    @After
    public void teardown(Scenario scenario) {
        if (scenario.isFailed()) {
            addAttachment();
            addScreenshot(scenario);
        }
        if (!isApiScenario(scenario)) closePage();
    }
}