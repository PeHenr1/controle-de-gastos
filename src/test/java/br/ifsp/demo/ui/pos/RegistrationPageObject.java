package br.ifsp.demo.ui.pos;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class RegistrationPageObject {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public RegistrationPageObject(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void register(String firstName, String lastName, String email, String password) {
        WebElement firstNameField = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(text(), 'Nome')]/following::input[1]")
        ));
        firstNameField.sendKeys(firstName);

        WebElement lastNameField = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(text(), 'Sobrenome')]/following::input[1]")
        ));
        lastNameField.sendKeys(lastName);

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

    public void registerWithEmptyFields() {
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit']")
        ));
        submitButton.click();
    }

    public boolean isRegistrationPage() {
        try {
            wait.until(ExpectedConditions.urlContains("/register"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSuccessMessageDisplayed() {
        try {
            WebElement successElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(), 'Registro realizado') or contains(text(), 'successful')]")
            ));
            return successElement.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getSuccessMessage() {
        try {
            WebElement successElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(), 'Registro realizado')]")
            ));
            return successElement.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isErrorMessageDisplayed() {
        try {
            WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(), 'Falha no registro') or contains(text(), 'Erro')]")
            ));
            return errorElement.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPasswordErrorDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(), 'senha') or contains(text(), 'password')]")
            ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isNameErrorDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(), 'nome') or contains(text(), 'name')]")
            ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void registerUser(String email, String password, String name) {
        String[] nameParts = name.split(" ");
        String firstName = nameParts.length > 0 ? nameParts[0] : name;
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        register(firstName, lastName, email, password);
    }
}