package br.ifsp.demo.integration;

import br.ifsp.demo.controller.GoalController;
import br.ifsp.demo.infra.persistence.entity.CategoryEntity;
import br.ifsp.demo.infra.persistence.entity.ExpenseEntity;
import br.ifsp.demo.infra.persistence.entity.GoalEntity;
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

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("ApiTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GoalControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private String authToken;

    private final String USER = "goal@test.com";
    private final String BASE_URL = "/api/v1/goals";

    @Autowired
    private JpaRepository<CategoryEntity, String> categoryJpa;

    @Autowired
    private JpaRepository<GoalEntity, String> goalJpa;

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
        goalJpa.deleteAll();
        categoryJpa.deleteAll();
    }

    private void registerUser(String email, String pass) {
        RegisterUserRequest req = new RegisterUserRequest("Test", "Goal", email, pass);

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
    @DisplayName("Should Reject Invalid Month Format")
    void shouldRejectInvalidMonthFormat() {

        var req = new GoalController.SetGoalRequest(
                "root-1",
                "2025/12",
                new BigDecimal("500")
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .header("X-User", USER)
                .body(req)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should Reject Non-Existing Category")
    void shouldRejectNonExistingCategory() {

        var req = new GoalController.SetGoalRequest(
                "cat-x",
                "2025-12",
                new BigDecimal("500")
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .header("X-User", USER)
                .body(req)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should Reject Category That Is Not Root")
    void shouldRejectCategoryThatIsNotRoot() {

        categoryJpa.save(new CategoryEntity(
                "r1", USER, "Compras", null, "/Compras"
        ));

        categoryJpa.save(new CategoryEntity(
                "c1", USER, "Mercado", "r1", "/Compras/Mercado"
        ));

        var req = new GoalController.SetGoalRequest(
                "c1",
                "2025-10",
                new BigDecimal("400")
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .header("X-User", USER)
                .body(req)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should Reject Non Positive Limit")
    void shouldRejectNonPositiveLimit() {

        var req = new GoalController.SetGoalRequest(
                "root-1",
                "2025-12",
                BigDecimal.ZERO
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .header("X-User", USER)
                .body(req)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(400);
    }

}
