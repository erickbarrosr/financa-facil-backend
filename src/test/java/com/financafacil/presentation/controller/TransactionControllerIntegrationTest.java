package com.financafacil.presentation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionControllerIntegrationTest {

    static {
        System.setProperty("TESTCONTAINERS_RYUK_DISABLED", "true");
        System.setProperty("DOCKER_HOST", "unix:///var/run/docker.sock");
        System.setProperty("api.version", "1.44");
    }

    @LocalServerPort int port;

    @MockBean
    JavaMailSender javaMailSender;

    private String accessToken;
    private String accountId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
        String email = "txtest_" + System.currentTimeMillis() + "@test.com";
        accessToken = given().contentType(ContentType.JSON)
            .body("{\"name\":\"Tx User\",\"email\":\"" + email + "\",\"password\":\"password123\"}")
            .post("/auth/register")
            .jsonPath().getString("data.accessToken");

        accountId = given().contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body("{\"name\":\"Conta\",\"type\":\"checking\",\"initialBalance\":5000.00,\"color\":\"#000000\"}")
            .post("/accounts")
            .jsonPath().getString("data.id");
    }

    @Test
    void createTransaction_updatesAccountBalance() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body("{\"accountId\":\"" + accountId + "\",\"type\":\"expense\",\"amount\":500.00," +
                  "\"transactionDate\":\"2026-07-01\",\"status\":\"PAID\"}")
        .when()
            .post("/transactions")
        .then()
            .statusCode(201)
            .body("data.amount", equalTo(500.0f))
            .body("data.status", equalTo("PAID"));

        given()
            .header("Authorization", "Bearer " + accessToken)
        .when()
            .get("/accounts/" + accountId + "/balance")
        .then()
            .statusCode(200)
            .body("data", equalTo(4500.0f));
    }

    @Test
    void listTransactions_withFilter_returnsFilteredResults() {
        given()
            .header("Authorization", "Bearer " + accessToken)
            .queryParam("type", "expense")
        .when()
            .get("/transactions")
        .then()
            .statusCode(200)
            .body("data.content", notNullValue())
            .body("data.totalElements", greaterThanOrEqualTo(0));
    }

    @Test
    void createTransaction_returns403_withoutToken() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"accountId\":\"" + accountId + "\",\"type\":\"expense\",\"amount\":100.00," +
                  "\"transactionDate\":\"2026-07-01\",\"status\":\"PAID\"}")
        .when()
            .post("/transactions")
        .then()
            .statusCode(403);
    }

    @Test
    void deleteTransaction_revertsBalance() {
        // Create a transaction
        String txId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body("{\"accountId\":\"" + accountId + "\",\"type\":\"income\",\"amount\":200.00," +
                  "\"transactionDate\":\"2026-07-01\",\"status\":\"PAID\"}")
            .post("/transactions")
            .jsonPath().getString("data.id");

        // Verify balance increased
        given()
            .header("Authorization", "Bearer " + accessToken)
            .get("/accounts/" + accountId + "/balance")
        .then()
            .statusCode(200)
            .body("data", equalTo(5200.0f));

        // Delete transaction
        given()
            .header("Authorization", "Bearer " + accessToken)
        .when()
            .delete("/transactions/" + txId)
        .then()
            .statusCode(204);

        // Verify balance reverted
        given()
            .header("Authorization", "Bearer " + accessToken)
            .get("/accounts/" + accountId + "/balance")
        .then()
            .statusCode(200)
            .body("data", equalTo(5000.0f));
    }
}
