package tests;

import org.testng.annotations.Test;
import pages.LoginPage;

public class LoginTest extends BaseTest {

    @Test
    public void testValidUserLogin() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.enterEmail("hmuttina@gmail.com");
        loginPage.enterPassword("password123");
        loginPage.clickLogin();
    }

    @Test
    public void testForgotPasswordOtpReset() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.enterEmail("muttinahemanthkumar34@gmail.com");
        loginPage.clickForgotPassword();
    }
}
