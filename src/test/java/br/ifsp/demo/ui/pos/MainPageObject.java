package br.ifsp.demo.ui.pos;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class MainPageObject {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public MainPageObject(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public String getLoggedUser() {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(text(), 'Logado:')]")
            ));
            return element.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public String getBalance() {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//p[contains(text(), 'Saldo')]/following-sibling::p")
            ));
            return element.getText();
        } catch (Exception e) {
            return "R$ 0,00";
        }
    }

    public String getTotalIncome() {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//p[contains(text(), 'Total de Receitas')]/following-sibling::p")
            ));
            return element.getText();
        } catch (Exception e) {
            return "R$ 0,00";
        }
    }

    public String getTotalExpenses() {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//p[contains(text(), 'Total de Despesas')]/following-sibling::p")
            ));
            return element.getText();
        } catch (Exception e) {
            return "R$ 0,00";
        }
    }

    public void changeMonth(String month) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(text(), 'Mês')]/following::input")
        ));
        element.clear();
        element.sendKeys(month);
    }


    private void selectCustomDropdownOption(String labelText, String optionText) {
        WebElement triggerButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(text(), '" + labelText + "')]/..//button[@role='combobox']")
        ));
        triggerButton.click();

        WebElement optionToClick = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@role='option']//span[contains(text(), '" + optionText + "')] | //div[@role='option'][contains(., '" + optionText + "')]")
        ));
        optionToClick.click();
    }

    public void addTransaction(String description, String amount, String type, String categoryName) {
        WebElement descField = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("description")
        ));
        descField.clear();
        descField.sendKeys(description);

       WebElement amountField = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("amount")
        ));
        amountField.clear();
        amountField.sendKeys(amount);

       String typeText;
        if (type.equals("income")) {
            typeText = "Receita (CREDIT)";
        } else {
            typeText = "Despesa (DEBIT)";
        }
        selectCustomDropdownOption("Tipo", typeText);


        selectCustomDropdownOption("Categoria", categoryName);

        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(., 'Adicionar Transação')]")
        ));
        addButton.click();
        delay(1000);
    }

    public void addDebitTransaction(String description, String amount, String categoryName) {
        addTransaction(description, amount, "expense", categoryName);
    }

    public void addCreditTransaction(String description, String amount, String categoryName) {
        addTransaction(description, amount, "income", categoryName);
    }

    public void createCategory(String categoryName) {
        WebElement categoryInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(text(), 'Nova raiz')]/following::input")
        ));
        categoryInput.clear();
        categoryInput.sendKeys(categoryName);

        WebElement createButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(., 'Criar raiz')]")
        ));
        createButton.click();
        delay(1000);
    }

   public void createSubcategory(String parentCategoryName, String subcategoryName) {
        WebElement toggleCheckbox = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@type='checkbox']") // O toggle de subcategoria
        ));

        if (!toggleCheckbox.isSelected()) {
            try {
                toggleCheckbox.click();
            } catch (Exception e) {
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//input[@type='checkbox']/following-sibling::button | //label[contains(., 'Subcategoria')]")
                )).click();
            }
        }

        selectCustomDropdownOption("Categoria pai", parentCategoryName);

        WebElement subcategoryInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(text(), 'Nome da subcategoria')]/following::input")
        ));
        subcategoryInput.clear();
        subcategoryInput.sendKeys(subcategoryName);

        WebElement createButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(., 'Criar Subcategoria')]")
        ));
        createButton.click();
    }

    public void setGoal(String categoryName, String month, String limit) {
        selectCustomDropdownOption("Categoria raiz", categoryName);

        WebElement monthInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(text(), 'Mês')]/following::input")
        ));
        monthInput.clear();
        monthInput.sendKeys(month);

        WebElement limitInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(text(), 'Limite')]/following::input")
        ));
        limitInput.clear();
        limitInput.sendKeys(limit);

        WebElement setGoalButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(., 'Definir/Ajustar Meta')]")
        ));
        setGoalButton.click();
    }

    public String getGoalEvaluation() {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class, 'font-semibold') and (contains(text(), 'Meta EXCEDIDA') or contains(text(), 'Dentro da meta'))]")
            ));

            return element.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public void logout() {
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Sair')]")
        ));
        logoutButton.click();
    }

    public boolean isMainPage() {
        try {
            wait.until(ExpectedConditions.urlContains("/"));
            return !driver.getCurrentUrl().contains("/login") && !driver.getCurrentUrl().contains("/register");
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteCategory(String categoryName) {
        selectCustomDropdownOption("Selecionar categoria", categoryName);

        WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(., 'Remover')]")
        ));
        deleteButton.click();
    }

    public void renameCategory(String oldName, String newName) {
        selectCustomDropdownOption("Selecionar categoria", oldName);

        WebElement renameInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(text(), 'Renomear Categoria')]/following::input")
        ));
        renameInput.clear();
        renameInput.sendKeys(newName);

        WebElement renameButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(., 'Renomear')]")
        ));
        renameButton.click();
    }

    private void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}