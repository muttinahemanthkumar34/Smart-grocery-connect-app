package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    // Locators
    private By emailField = By.id("com.SIMATS.Groceryconnect:id/login_email");
    private By passwordField = By.id("com.SIMATS.Groceryconnect:id/login_password");
    private By loginButton = By.id("com.SIMATS.Groceryconnect:id/login_btn");
    private By forgotPasswordLink = By.id("com.SIMATS.Groceryconnect:id/forgot_password_link");
    private By otpInputField = By.id("com.SIMATS.Groceryconnect:id/otp_input_code");
    private By verifyOtpButton = By.id("com.SIMATS.Groceryconnect:id/otp_submit_btn");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void enterEmail(String email) {
        writeText(emailField, email);
    }

    public void enterPassword(String password) {
        writeText(passwordField, password);
    }

    public void clickLogin() {
        click(loginButton);
    }

    public void clickForgotPassword() {
        click(forgotPasswordLink);
    }

    public void enterOtp(String otp) {
        writeText(otpInputField, otp);
    }

    public void clickVerifyOtp() {
        click(verifyOtpButton);
    }
}
