package tests;

import org.testng.annotations.Test;
import pages.WebLoginPage;

public class WebLoginTest extends BaseTest {

    @Test
    public void testWebStorefrontLogin() {
        driver.get("http://localhost:8001");
        WebLoginPage loginPage = new WebLoginPage(driver);
        loginPage.loginOnWeb("hmuttina@gmail.com", "password");
    }

    @Test
    public void testWebForgotPasswordPrompt() {
        driver.get("http://localhost:8001");
        WebLoginPage loginPage = new WebLoginPage(driver);
        loginPage.writeText(org.openqa.selenium.By.id("login-email"), "muttinahemanthkumar34@gmail.com");
        loginPage.triggerPasswordReset();
    }
}
