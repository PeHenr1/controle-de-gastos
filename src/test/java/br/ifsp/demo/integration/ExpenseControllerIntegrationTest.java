package br.ifsp.demo.integration;

import br.ifsp.demo.controller.ExpenseController;
import br.ifsp.demo.domain.model.Expense;
import br.ifsp.demo.domain.model.ExpenseType;
import br.ifsp.demo.domain.service.ExpenseService;

import br.ifsp.demo.security.auth.AuthRequest;
import br.ifsp.demo.security.auth.AuthResponse;
import br.ifsp.demo.security.auth.RegisterUserRequest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("ApiTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExpenseControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @MockBean
    private ExpenseService expenseService;

    private String authToken;
    private final String TEST_EMAIL = "expense@test.com";
    private final String BASE_URL = "/api/v1/expenses";

    @BeforeAll
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        String TEST_PASS = "12345678";
        registerUser(TEST_EMAIL, TEST_PASS);
        this.authToken = authenticate(TEST_EMAIL, TEST_PASS);
    }

    private void registerUser(String email, String pass) {
        RegisterUserRequest req = new RegisterUserRequest("Test", "User", email, pass);

        given()
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .post("/api/v1/register")
                .then()
                .statusCode(isOneOf(HttpStatus.CREATED.value(), HttpStatus.CONFLICT.value()));
    }

    private String authenticate(String email, String pass) {
        AuthRequest req = new AuthRequest(email, pass);

        AuthResponse resp = given()
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .post("/api/v1/authenticate")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().as(AuthResponse.class);

        return resp.token();
    }

    @Test
    @DisplayName("Should Create Expense And Retrieve Status 201")
    void shouldCreateExpense() {

        var req = new ExpenseController.CreateExpenseRequest(
                BigDecimal.valueOf(150.75),
                ExpenseType.DEBIT,
                "Café",
                Instant.now(),
                null
        );

        Expense saved = Expense.of(
                TEST_EMAIL,
                req.amount(),
                req.type(),
                req.description(),
                req.timestamp(),
                req.categoryId()
        ).withId("exp-1");

        when(expenseService.create(any())).thenReturn(saved);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .header("X-User", TEST_EMAIL)
                .body(req)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", equalTo("exp-1"))
                .body("amount", equalTo(150.75F))
                .body("type", equalTo("DEBIT"))
                .body("description", equalTo("Café"));

        verify(expenseService, times(1)).create(any());
    }

    @Test
    @DisplayName("Should Reject When Amount Invalid ")
    void shouldRejectWhenAmountInvalid() {

        var req = new ExpenseController.CreateExpenseRequest(
                BigDecimal.ZERO,
                ExpenseType.DEBIT,
                "desc",
                Instant.now(),
                null
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .header("X-User", TEST_EMAIL)
                .body(req)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(400);

        verify(expenseService, times(1)).create(any());
    }

    @Test
    @DisplayName("Should Reject Blank Description")
    void shouldRejectBlankDescription() {

        var req = new ExpenseController.CreateExpenseRequest(
                BigDecimal.valueOf(20),
                ExpenseType.CREDIT,
                "    ",
                Instant.now(),
                null
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .header("X-User", TEST_EMAIL)
                .body(req)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(400);

        verify(expenseService, times(1)).create(any());
    }

    @Test
    @DisplayName("Should Reject If Category Does Not Exist")
    void shouldRejectIfCategoryDoesNotExist() {

        var req = new ExpenseController.CreateExpenseRequest(
                BigDecimal.valueOf(40),
                ExpenseType.DEBIT,
                "Lanche",
                Instant.now(),
                "invalid-cat"
        );

        when(expenseService.create(any()))
                .thenThrow(new IllegalArgumentException("Categoria não encontrada"));

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .header("X-User", TEST_EMAIL)
                .body(req)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(400);

        verify(expenseService, times(1)).create(any());
    }

    @Test
    @DisplayName("Should Return 500 For Unexpected Exception")
    void shouldReturn500ForUnexpectedException() {

        var req = new ExpenseController.CreateExpenseRequest(
                BigDecimal.valueOf(80),
                ExpenseType.DEBIT,
                "Frango",
                Instant.now(),
                null
        );

        when(expenseService.create(any()))
                .thenThrow(new RuntimeException("Database down"));

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .header("X-User", TEST_EMAIL)
                .body(req)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(500);

        verify(expenseService, times(1)).create(any());
    }

}
