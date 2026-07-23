package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class WebLoginPage extends BasePage {

    // Locators for web-based elements
    private By webEmailField = By.id("login-email");
    private By webPasswordField = By.id("login-password");
    private By webSubmitBtn = By.id("login-submit-btn");
    private By forgotPasswordLink = By.id("forgot-password-link");

    public WebLoginPage(WebDriver driver) {
        super(driver);
    }

    public void loginOnWeb(String email, String password) {
        writeText(webEmailField, email);
        writeText(webPasswordField, password);
        click(webSubmitBtn);
    }

    public void triggerPasswordReset() {
        click(forgotPasswordLink);
    }
}
