package br.ifsp.demo.ui.pos;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPageObject {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public LoginPageObject(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void login(String email, String password) {
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[type='email']")
        ));
        emailField.sendKeys(email);

        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[type='password']")
        ));
        passwordField.sendKeys(password);

        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit']")
        ));
        submitButton.click();
    }

    public RegistrationPageObject navigateToRegistrationPage() {
        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), 'Registre-se') or contains(text(), 'Não tem conta')]")
        ));
        registerLink.click();
        return new RegistrationPageObject(driver);
    }

    public boolean isLoginPage() {
        try {
            wait.until(ExpectedConditions.urlContains("/login"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void enterCredentials(String email, String password) {
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[type='email']")
        ));
        emailField.clear();
        emailField.sendKeys(email);

        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[type='password']")
        ));
        passwordField.clear();
        passwordField.sendKeys(password);
    }

    public void clickLoginButton() {
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit']")
        ));
        submitButton.click();
    }

    public boolean isErrorDisplayed() {
        try {
            WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(), 'Falha no login') or contains(text(), 'Invalid') or contains(@class, 'destructive')]")
            ));
            return errorElement.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessage() {
        try {
            WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class, 'destructive')]")
            ));
            return errorElement.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public void authenticate(String email, String password) {
        login(email, password);
    }
}