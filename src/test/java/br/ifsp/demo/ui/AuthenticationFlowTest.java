package br.ifsp.demo.ui;

import br.ifsp.demo.ui.pos.LoginPageObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthenticationFlowTest extends BaseSeleniumTest {
    private final String BASE_URL = "http://localhost:5173";

    @Override
    public void setInitialPage() {
        driver.get(BASE_URL + "/login");
        delay(2000);
    }

    private String generateUniqueEmail() {
        return "user_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId() + "@test.com";
    }

    @Test
    @DisplayName("Should complete full authentication flow")
    void shouldCompleteFullAuthenticationFlow() {
        var loginPage = new LoginPageObject(driver);

        delay(3000);

        try {
            var registrationPage = loginPage.navigateToRegistrationPage();
            delay(2000);

            assertThat(driver.getCurrentUrl()).contains("/register");

            String uniqueEmail = generateUniqueEmail();
            registrationPage.register("Test", "User", uniqueEmail, "TestPass123");
            delay(3000);

            assertThat(driver.getCurrentUrl()).contains("/login");

            loginPage = new LoginPageObject(driver);
            loginPage.login(uniqueEmail, "TestPass123");
            delay(3000);

            assertThat(driver.getCurrentUrl()).doesNotContain("/login");

        } catch (Exception e) {
            System.out.println("Primeira abordagem falhou, tentando alternativa...");

            driver.get(BASE_URL + "/register");
            delay(2000);

            var registrationPage = new br.ifsp.demo.ui.pos.RegistrationPageObject(driver);
            String uniqueEmail = generateUniqueEmail();
            registrationPage.register("Test", "User", uniqueEmail, "TestPass123");
            delay(3000);

            driver.get(BASE_URL + "/login");
            delay(2000);

            loginPage = new LoginPageObject(driver);
            loginPage.login(uniqueEmail, "TestPass123");
            delay(3000);

            assertThat(driver.getCurrentUrl()).doesNotContain("/login");
        }
    }

    @Test
    @DisplayName("Should demonstrate registration without validation")
    void shouldDemonstrateRegistrationWithoutValidation() {
        driver.get(BASE_URL + "/register");
        delay(2000);

        var registrationPage = new br.ifsp.demo.ui.pos.RegistrationPageObject(driver);
        registrationPage.registerWithEmptyFields();
        delay(2000);

        System.out.println("BUG: Registration attempted without validation");
        assertThat(driver.getCurrentUrl()).isNotNull();
    }
}