package tests;

import config.AppiumConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.net.URL;
import java.util.Map;

public class BaseTest {
    protected WebDriver driver;

    @BeforeMethod
    public void setUp() {
        DesiredCapabilities caps = new DesiredCapabilities();
        Map<String, Object> capsMap = AppiumConfig.getAndroidCapabilities();
        for (Map.Entry<String, Object> entry : capsMap.entrySet()) {
            caps.setCapability(entry.getKey(), entry.getValue());
        }

        URL url = AppiumConfig.getAppiumUrl();
        driver = new RemoteWebDriver(url, caps);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
