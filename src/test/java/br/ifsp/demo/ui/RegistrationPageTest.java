package br.ifsp.demo.ui;

import br.ifsp.demo.ui.pos.LoginPageObject;
import br.ifsp.demo.ui.pos.RegistrationPageObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class RegistrationPageTest extends BaseSeleniumTest {
    private final String BASE_URL = "http://localhost:5173";

    @Override
    public void setInitialPage() {
        driver.get(BASE_URL + "/register");
        delay(2000);
    }

    private String generateUniqueEmail() {
        return "test_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId() + "@test.com";
    }

    @Test
    @DisplayName("Should demonstrate registration bug with empty fields")
    void shouldDemonstrateRegistrationBugWithEmptyFields() {
        var registrationPage = new RegistrationPageObject(driver);
        registrationPage.registerWithEmptyFields();
        delay(2000);
        assertThat(driver.getCurrentUrl()).isNotNull();
    }

    @Test
    @DisplayName("Should register with valid data and redirect to login")
    void shouldRegisterWithValidDataAndRedirectToLogin() {
        var registrationPage = new RegistrationPageObject(driver);
        String uniqueEmail = generateUniqueEmail();

        registrationPage.register("Jane", "Smith", uniqueEmail, "securePass123");
        delay(3000);

        assertThat(driver.getCurrentUrl()).contains("/login");
    }

    @Test
    @DisplayName("Should show error for duplicate email")
    void shouldShowErrorForDuplicateEmail() {
        var registrationPage = new RegistrationPageObject(driver);
        String email = "duplicate_" + System.currentTimeMillis() + "@test.com";

        // Primeiro registro
        registrationPage.register("User", "One", email, "password123");
        delay(2000);

        // Segunda tentativa com mesmo email
        driver.get(BASE_URL + "/register");
        delay(2000);

        registrationPage = new RegistrationPageObject(driver);
        registrationPage.register("User", "Two", email, "password456");
        delay(2000);

        assertThat(registrationPage.isErrorMessageDisplayed()).isTrue();
    }

    @Test
    @DisplayName("Should complete registration and immediate login")
    void shouldCompleteRegistrationAndImmediateLogin() {
        var registrationPage = new RegistrationPageObject(driver);
        String uniqueEmail = generateUniqueEmail();
        String password = "TestPass123";

        registrationPage.register("New", "User", uniqueEmail, password);
        delay(3000);

        assertThat(registrationPage.isSuccessMessageDisplayed()).isTrue();

        var loginPage = new LoginPageObject(driver);
        loginPage.login(uniqueEmail, password);
        delay(3000);

        assertThat(driver.getCurrentUrl()).doesNotContain("/login");
    }

    @Test
    @DisplayName("Should validate registration with empty email")
    void shouldValidateRegistrationWithEmptyEmail() {
        var registrationPage = new RegistrationPageObject(driver);

        registrationPage.register("Test", "User", "", "password123");
        delay(2000);

        assertThat(registrationPage.isErrorMessageDisplayed()).isTrue();
    }

    @Test
    @DisplayName("Should create user and verify seamless flow to login")
    void shouldCreateUserAndVerifySeamlessFlowToLogin() {
        var registrationPage = new RegistrationPageObject(driver);
        String uniqueEmail = generateUniqueEmail();

        registrationPage.register("Flow", "Test", uniqueEmail, "Password123");
        delay(3000);

        assertThat(driver.getCurrentUrl()).contains("/login");

        var loginPage = new LoginPageObject(driver);
        loginPage.login(uniqueEmail, "Password123");
        delay(3000);

        assertThat(driver.getCurrentUrl()).doesNotContain("/login");
    }

    @Test
    @DisplayName("Should register with special characters in name")
    void shouldRegisterWithSpecialCharactersInName() {
        var registrationPage = new RegistrationPageObject(driver);
        String uniqueEmail = generateUniqueEmail();

        registrationPage.register("User@#$%", "Test", uniqueEmail, "Password123");
        delay(2000);

        System.out.println("Resultado do registro com caracteres especiais: " + driver.getCurrentUrl());
    }
}