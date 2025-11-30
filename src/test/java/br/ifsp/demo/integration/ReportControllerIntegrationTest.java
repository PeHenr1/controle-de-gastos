package br.ifsp.demo.integration;

import br.ifsp.demo.infra.persistence.entity.CategoryEntity;
import br.ifsp.demo.infra.persistence.entity.ExpenseEntity;
import br.ifsp.demo.security.auth.AuthRequest;
import br.ifsp.demo.security.auth.AuthResponse;
import br.ifsp.demo.security.auth.RegisterUserRequest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("ApiTest")
@Tag("IntegrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReportControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private String authToken;

    private final String USER = "report@test.com";
    private final String BASE_URL = "/api/v1/reports";

    @Autowired
    private JpaRepository<CategoryEntity, String> categoryJpa;

    @Autowired
    private JpaRepository<ExpenseEntity, String> expenseJpa;

    @BeforeAll
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        String PASS = "12345678";

        registerUser(USER, PASS);
        this.authToken = authenticate(USER, PASS);
    }

    @AfterEach
    void cleanup() {
        expenseJpa.deleteAll();
        categoryJpa.deleteAll();
    }

    private void registerUser(String email, String pass) {
        RegisterUserRequest req = new RegisterUserRequest("Test", "Report", email, pass);

        given()
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .post("/api/v1/register")
                .then()
                .statusCode(isOneOf(201, 409));
    }

    private String authenticate(String email, String pass) {
        AuthRequest req = new AuthRequest(email, pass);

        AuthResponse resp = given()
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .post("/api/v1/authenticate")
                .then()
                .statusCode(200)
                .extract()
                .as(AuthResponse.class);

        return resp.token();
    }

    @Test
    @DisplayName("Should Return Empty Report When No Expenses")
    void shouldReturnEmptyReportWhenNoExpenses() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .header("X-User", USER)
                .queryParam("start", "2025-12-01T00:00:00Z")
                .queryParam("end", "2025-12-31T23:59:59Z")
                .when()
                .get(BASE_URL + "/period")
                .then()
                .statusCode(200)
                .body("totalDebit", is(0))
                .body("totalCredit", is(0))
                .body("balance", is(0))
                .body("items.size()", is(0));
    }
}
