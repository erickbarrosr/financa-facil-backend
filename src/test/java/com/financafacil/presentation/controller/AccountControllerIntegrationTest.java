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
class AccountControllerIntegrationTest {

    static {
        System.setProperty("TESTCONTAINERS_RYUK_DISABLED", "true");
        System.setProperty("DOCKER_HOST", "unix:///var/run/docker.sock");
        System.setProperty("api.version", "1.44");
    }

    @LocalServerPort
    int port;

    @MockBean
    JavaMailSender javaMailSender;

    private String accessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
        // Registrar e obter token (uso de email único por teste para evitar conflito)
        String email = "accounttest_" + System.currentTimeMillis() + "@test.com";
        accessToken = given().contentType(ContentType.JSON)
            .body("{\"name\":\"Account Test User\",\"email\":\"" + email + "\",\"password\":\"password123\"}")
            .post("/auth/register")
            .jsonPath().getString("data.accessToken");
    }

    @Test
    void createAndListAccounts() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body("{\"name\":\"Nubank\",\"type\":\"checking\",\"initialBalance\":1000.00,\"color\":\"#8B5CF6\"}")
        .when()
            .post("/accounts")
        .then()
            .statusCode(201)
            .body("data.name", equalTo("Nubank"))
            .body("data.balance", equalTo(1000.0f));

        given()
            .header("Authorization", "Bearer " + accessToken)
        .when()
            .get("/accounts")
        .then()
            .statusCode(200)
            .body("data", hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    void deleteAccount_returns404_whenNotOwner() {
        given()
            .header("Authorization", "Bearer " + accessToken)
        .when()
            .delete("/accounts/" + java.util.UUID.randomUUID())
        .then()
            .statusCode(404);
    }

    @Test
    void createAccount_returns422_whenMissingFields() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body("{\"name\":\"\",\"type\":\"checking\"}")
        .when()
            .post("/accounts")
        .then()
            .statusCode(422);
    }

    @Test
    void updateAccount_returnsUpdatedAccount() {
        // First create an account
        String accountId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body("{\"name\":\"Old Name\",\"type\":\"checking\",\"initialBalance\":500.00,\"color\":\"#000000\"}")
            .post("/accounts")
            .jsonPath().getString("data.id");

        // Then update it
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body("{\"name\":\"New Name\",\"type\":\"wallet\",\"color\":\"#FFFFFF\"}")
        .when()
            .put("/accounts/" + accountId)
        .then()
            .statusCode(200)
            .body("data.name", equalTo("New Name"))
            .body("data.type", equalTo("wallet"));
    }

    @Test
    void getBalance_returnsBalance() {
        String accountId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body("{\"name\":\"Savings\",\"type\":\"checking\",\"initialBalance\":250.00,\"color\":\"#123456\"}")
            .post("/accounts")
            .jsonPath().getString("data.id");

        given()
            .header("Authorization", "Bearer " + accessToken)
        .when()
            .get("/accounts/" + accountId + "/balance")
        .then()
            .statusCode(200)
            .body("data", equalTo(250.0f));
    }

    @Test
    void listAccounts_returns403_withoutToken() {
        given()
        .when()
            .get("/accounts")
        .then()
            .statusCode(403);
    }
}
