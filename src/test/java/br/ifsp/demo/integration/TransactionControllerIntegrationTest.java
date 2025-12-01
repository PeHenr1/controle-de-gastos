package br.ifsp.demo.integration;

import br.ifsp.demo.security.auth.AuthRequest;
import br.ifsp.demo.security.auth.AuthResponse;
import br.ifsp.demo.security.auth.RegisterUserRequest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("ApiTest")
@Tag("IntegrationTest")
class TransactionControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private String authToken;
    private String expectedUserId;

    @BeforeAll
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        String USER = "hello@test.com";
        String PASS = "12345678";
        registerUser(USER, PASS);
        this.authToken = authenticate(USER, PASS);
        this.expectedUserId = fetchAuthenticatedUserId();
    }

    private void registerUser(String email, String pass) {
        RegisterUserRequest req = new RegisterUserRequest("Test", "User", email, pass);

        given()
                .contentType(ContentType.JSON)
                .body(req)
                .post("/api/v1/register")
                .then()
                .statusCode(isOneOf(201, 409));
    }

    private String authenticate(String email, String pass) {
        var req = new AuthRequest(email, pass);

        AuthResponse resp = given()
                .contentType(ContentType.JSON)
                .body(req)
                .post("/api/v1/authenticate")
                .then()
                .statusCode(200)
                .extract()
                .as(AuthResponse.class);

        return resp.token();
    }

    private String fetchAuthenticatedUserId() {
        return given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/hello")
                .then()
                .statusCode(200)
                .extract()
                .asString()
                .replace("Hello: ", "");
    }

    @Test
    @DisplayName("Should Return Hello Message With UserId")
    void shouldReturnHelloMessageWithUserId() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/hello")
                .then()
                .statusCode(200)
                .body(equalTo("Hello: " + expectedUserId));
    }
}
