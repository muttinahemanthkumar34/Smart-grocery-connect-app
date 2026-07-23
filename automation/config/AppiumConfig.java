package config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AppiumConfig {
    public static final String APPIUM_SERVER_URL = "http://127.0.0.1:4723/";
    public static final String DEVICE_NAME = "Android Emulator";
    public static final String PLATFORM_NAME = "Android";
    public static final String PLATFORM_VERSION = "12.0";
    public static final String APP_PACKAGE = "com.SIMATS.Groceryconnect";
    public static final String APP_ACTIVITY = ".activities.SplashActivity";

    public static URL getAppiumUrl() {
        try {
            return new URL(APPIUM_SERVER_URL);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium Server URL format: " + APPIUM_SERVER_URL, e);
        }
    }

    public static Map<String, Object> getAndroidCapabilities() {
        Map<String, Object> caps = new HashMap<>();
        caps.put("platformName", PLATFORM_NAME);
        caps.put("appium:deviceName", DEVICE_NAME);
        caps.put("appium:platformVersion", PLATFORM_VERSION);
        caps.put("appium:appPackage", APP_PACKAGE);
        caps.put("appium:appActivity", APP_ACTIVITY);
        caps.put("appium:automationName", "UiAutomator2");
        caps.put("appium:noReset", false);
        caps.put("appium:fullReset", false);
        caps.put("appium:newCommandTimeout", 300);
        return caps;
    }
}
