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
class AuthControllerIntegrationTest {

    @LocalServerPort int port;

    @MockBean
    JavaMailSender javaMailSender;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
    }

    @Test
    void register_returnsCreated_withAccessToken() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"name":"Test User","email":"test@integration.com","password":"password123"}
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(201)
            .body("success", equalTo(true))
            .body("data.accessToken", notNullValue())
            .body("data.user.email", equalTo("test@integration.com"));
    }

    @Test
    void register_returnsConflict_whenEmailAlreadyExists() {
        var body = """
            {"name":"User","email":"duplicate@test.com","password":"password123"}
            """;
        given().contentType(ContentType.JSON).body(body).post("/auth/register");

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(409)
            .body("success", equalTo(false));
    }

    @Test
    void login_returnsUnauthorized_withWrongPassword() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"email":"notexist@test.com","password":"wrong"}
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(401);
    }

    @Test
    void me_returnsUnauthorized_withoutToken() {
        given().get("/auth/me").then().statusCode(403);
    }
}
