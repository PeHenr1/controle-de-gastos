package br.ifsp.demo.ui;

import br.ifsp.demo.ui.pos.LoginPageObject;
import br.ifsp.demo.ui.pos.MainPageObject;
import br.ifsp.demo.ui.pos.RegistrationPageObject;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class MainPageTest extends BaseSeleniumTest {
    private final String BASE_URL = "http://localhost:5173";

    private String[] createUniqueUser() {
        String uniqueEmail = "user_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId() + "@test.com";
        String password = "TestPass123";
        String name = "TestUser";

        driver.get(BASE_URL + "/register");
        delay(1000);

        var registrationPage = new RegistrationPageObject(driver);
        registrationPage.register(name, "Test", uniqueEmail, password);
        delay(2000);

        driver.get(BASE_URL + "/login");
        delay(1000);

        var loginPage = new LoginPageObject(driver);
        loginPage.login(uniqueEmail, password);
        delay(2000);

        return new String[]{uniqueEmail, password};
    }

    private String createTestCategory(MainPageObject mainPage) {
        String categoryName = "CategoriaTeste_" + System.currentTimeMillis();
        mainPage.createCategory(categoryName);
        delay(1500);
        return categoryName;
    }

    @Override
    public void setInitialPage() {
    }

    @Test
    @DisplayName("Should display user information after login")
    void shouldDisplayUserInformationAfterLogin() {
        String[] credentials = createUniqueUser();
        var mainPage = new MainPageObject(driver);

        String loggedUser = mainPage.getLoggedUser();
        assertThat(loggedUser).contains(credentials[0]);
    }

    @Test
    @DisplayName("Should display initial balance as zero for new user")
    void shouldDisplayBalanceInformation() {
        createUniqueUser();
        var mainPage = new MainPageObject(driver);

        String balanceStr = mainPage.getBalance();
        String incomeStr = mainPage.getTotalIncome();
        String expensesStr = mainPage.getTotalExpenses();

        double balance = extractNumericValue(balanceStr);
        double income = extractNumericValue(incomeStr);
        double expenses = extractNumericValue(expensesStr);

        assertThat(balance).isZero();
        assertThat(income).isZero();
        assertThat(expenses).isZero();
    }

    @Test
    @DisplayName("Should add new expense transaction and update balance correctly")
    void shouldAddNewExpenseTransaction() {
        createUniqueUser();
        var mainPage = new MainPageObject(driver);

        String categoryName = createTestCategory(mainPage);

        double initialBalance = extractNumericValue(mainPage.getBalance());
        assertThat(initialBalance).isZero();

        double expenseValue = 150.75;
        mainPage.addDebitTransaction("Compra supermercado", String.valueOf(expenseValue), categoryName);
        delay(2000);

        double finalBalance = extractNumericValue(mainPage.getBalance());
        double totalExpenses = extractNumericValue(mainPage.getTotalExpenses());

        assertThat(finalBalance).isEqualTo(-expenseValue, Offset.offset(0.01));
        assertThat(totalExpenses).isEqualTo(expenseValue, Offset.offset(0.01));
    }

    @Test
    @DisplayName("Should add new income transaction and update balance correctly")
    void shouldAddNewIncomeTransaction() {
        createUniqueUser();
        var mainPage = new MainPageObject(driver);

        String categoryName = createTestCategory(mainPage);

        assertThat(extractNumericValue(mainPage.getBalance())).isZero();

        double incomeValue = 2500.00;
        mainPage.addCreditTransaction("Salário", String.valueOf(incomeValue), categoryName);
        delay(2000);

        double finalBalance = extractNumericValue(mainPage.getBalance());
        double totalIncome = extractNumericValue(mainPage.getTotalIncome());

        assertThat(finalBalance).isEqualTo(incomeValue, Offset.offset(0.01));
        assertThat(totalIncome).isEqualTo(incomeValue, Offset.offset(0.01));
    }

    @Test
    @DisplayName("Should calculate balance correctly after multiple transactions")
    void shouldCalculateBalanceCorrectlyAfterMultipleTransactions() {
        createUniqueUser();
        var mainPage = new MainPageObject(driver);

        String categoryName = createTestCategory(mainPage);

        mainPage.addCreditTransaction("Salário", "3000.00", categoryName);
        delay(1000);
        mainPage.addCreditTransaction("Bônus", "500.00", categoryName);
        delay(1000);
        mainPage.addDebitTransaction("Aluguel", "1200.00", categoryName);
        delay(1000);
        mainPage.addDebitTransaction("Mercado", "350.50", categoryName);
        delay(1000);

        double expectedCredits = 3000.00 + 500.00;
        double expectedDebits = 1200.00 + 350.50;
        double expectedBalance = expectedCredits - expectedDebits;

        double actualBalance = extractNumericValue(mainPage.getBalance());
        double actualIncome = extractNumericValue(mainPage.getTotalIncome());
        double actualExpenses = extractNumericValue(mainPage.getTotalExpenses());

        assertThat(actualBalance).isEqualTo(expectedBalance, Offset.offset(0.01));
        assertThat(actualIncome).isEqualTo(expectedCredits, Offset.offset(0.01));
        assertThat(actualExpenses).isEqualTo(expectedDebits, Offset.offset(0.01));
    }

    @Test
    @DisplayName("Should complete end-to-end financial workflow")
    void shouldCompleteEndToEndFinancialWorkflow() {
        createUniqueUser();
        var mainPage = new MainPageObject(driver);
        String categoryName = "CategoriaWorkflow" + System.currentTimeMillis();

        mainPage.createCategory(categoryName);
        delay(2000);

        mainPage.setGoal(categoryName, "2025-12", "1000.00");
        delay(2000);

        double transactionAmount = 300.00;
        mainPage.addDebitTransaction("Gasto workflow", String.valueOf(transactionAmount), categoryName);
        delay(2000);

        double balance = extractNumericValue(mainPage.getBalance());
        assertThat(balance).isEqualTo(-transactionAmount, Offset.offset(0.01));

        String goalEval = mainPage.getGoalEvaluation();
        assertThat(goalEval).contains("Dentro da meta");
    }

    @Test
    @DisplayName("Should complete end-to-end financial workflow")
    void shouldCompleteEndToEndFinancialWorkflowAndReturnNegativeBalance() {
        createUniqueUser();
        var mainPage = new MainPageObject(driver);
        String categoryName = "CategoriaWorkflow" + System.currentTimeMillis();

        mainPage.createCategory(categoryName);
        delay(2000);

        mainPage.setGoal(categoryName, "2025-12", "1000.00");
        delay(2000);

        double transactionAmount = 1100.00;
        mainPage.addDebitTransaction("Gasto workflow", String.valueOf(transactionAmount), categoryName);
        delay(2000);

        double balance = extractNumericValue(mainPage.getBalance());
        assertThat(balance).isEqualTo(-transactionAmount, Offset.offset(0.01));

        String goalEval = mainPage.getGoalEvaluation();
        delay(2000);
        assertThat(goalEval).contains("Meta EXCEDIDA");
    }

    @Test
    @DisplayName("Should maintain session in multiple tabs")
    void shouldMaintainSessionInMultipleTabs() {
        createUniqueUser();
        var mainPage = new MainPageObject(driver);
        String originalWindow = driver.getWindowHandle();

        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.open('" + BASE_URL + "')");

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        delay(1500);
        driver.get(BASE_URL);
        delay(1500);

        assertThat(mainPage.isMainPage()).isTrue();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @DisplayName("Should handle logout")
    void shouldHandleLogout() {
        createUniqueUser();
        var mainPage = new MainPageObject(driver);
        mainPage.logout();
        delay(1000);
        assertThat(driver.getCurrentUrl()).contains("/login");
    }

    private double extractNumericValue(String monetaryValue) {
        if (monetaryValue == null || monetaryValue.isEmpty()) {
            return 0.0;
        }
        String clean = monetaryValue.replaceAll("[R$\\s\\u00A0]", "")
                .replace(".", "")
                .replace(",", ".")
                .trim();
        try {
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}