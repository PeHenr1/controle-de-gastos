package br.ifsp.demo.ui;

import br.ifsp.demo.ui.pos.LoginPageObject;
import br.ifsp.demo.ui.pos.MainPageObject;
import br.ifsp.demo.ui.pos.RegistrationPageObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class LoginPageTest extends BaseSeleniumTest {
    private final String BASE_URL = "http://localhost:5173";

    @Override
    public void setInitialPage() {
        driver.get(BASE_URL + "/login");
        delay(2000);
    }

    private String generateUniqueEmail() {
        return "test_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId() + "@test.com";
    }

    @Test
    @DisplayName("Should navigate to registration page")
    void shouldNavigateToRegistrationPage() {
        var loginPage = new LoginPageObject(driver);
        var registrationPage = loginPage.navigateToRegistrationPage();
        delay(2000);
        assertThat(driver.getCurrentUrl()).contains("/register");
    }

    @Test
    @DisplayName("Should attempt login with empty credentials")
    void shouldAttemptLoginWithEmptyCredentials() {
        var loginPage = new LoginPageObject(driver);
        loginPage.login("", "");
        delay(2000);
        assertThat(loginPage.isErrorDisplayed()).isTrue();
    }

    @Test
    @DisplayName("Should login with newly created user")
    void shouldLoginWithNewlyCreatedUser() {
        String uniqueEmail = generateUniqueEmail();
        String password = "TestPass123";

        driver.get(BASE_URL + "/register");
        delay(2000);

        var registrationPage = new RegistrationPageObject(driver);
        registrationPage.register("Test", "User", uniqueEmail, password);
        delay(3000);

        driver.get(BASE_URL + "/login");
        delay(2000);

        var loginPage = new LoginPageObject(driver);
        loginPage.login(uniqueEmail, password);
        delay(3000);

        var mainPage = new MainPageObject(driver);
        assertThat(mainPage.isMainPage()).isTrue();
    }

    @Test
    @DisplayName("Should display error with invalid credentials")
    void shouldDisplayErrorWithInvalidCredentials() {
        var loginPage = new LoginPageObject(driver);
        loginPage.login("invalid_" + System.currentTimeMillis() + "@test.com", "wrongpassword");
        delay(2000);
        assertThat(loginPage.isErrorDisplayed()).isTrue();
    }

    @Test
    @DisplayName("Should persist session after refresh")
    void shouldPersistSessionAfterRefresh() {
        String uniqueEmail = generateUniqueEmail();
        String password = "TestPass123";

        driver.get(BASE_URL + "/register");
        delay(2000);

        var registrationPage = new RegistrationPageObject(driver);
        registrationPage.register("Test", "User", uniqueEmail, password);
        delay(3000);

        driver.get(BASE_URL + "/login");
        delay(2000);

        var loginPage = new LoginPageObject(driver);
        loginPage.login(uniqueEmail, password);
        delay(3000);

        driver.navigate().refresh();
        delay(2000);

        var mainPage = new MainPageObject(driver);
        assertThat(mainPage.isMainPage()).isTrue();
    }

    @Test
    @DisplayName("Should complete authentication flow with logout and relogin")
    void shouldCompleteAuthenticationFlowWithLogoutAndRelogin() {
        String uniqueEmail = generateUniqueEmail();
        String password = "TestPass123";

        driver.get(BASE_URL + "/register");
        delay(2000);

        var registrationPage = new RegistrationPageObject(driver);
        registrationPage.register("Test", "User", uniqueEmail, password);
        delay(3000);

        driver.get(BASE_URL + "/login");
        delay(2000);

        var loginPage = new LoginPageObject(driver);
        loginPage.login(uniqueEmail, password);
        delay(3000);

        var mainPage = new MainPageObject(driver);
        mainPage.logout();
        delay(2000);

        assertThat(driver.getCurrentUrl()).contains("/login");

        loginPage.login(uniqueEmail, password);
        delay(3000);

        assertThat(mainPage.isMainPage()).isTrue();
    }

    @Test
    @DisplayName("Should handle login with empty email")
    void shouldHandleLoginWithEmptyEmail() {
        var loginPage = new LoginPageObject(driver);
        loginPage.login("", "senha123");
        delay(2000);
        assertThat(loginPage.isErrorDisplayed()).isTrue();
    }

    @Test
    @DisplayName("Should handle login with empty password")
    void shouldHandleLoginWithEmptyPassword() {
        var loginPage = new LoginPageObject(driver);
        loginPage.login("test@test.com", "");
        delay(2000);
        assertThat(loginPage.isErrorDisplayed()).isTrue();
    }
}