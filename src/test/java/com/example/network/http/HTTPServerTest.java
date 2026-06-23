package com.example.network.http;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.database.Db;
import com.example.database.Product;
import com.example.database.UserCredentials;
import io.restassured.RestAssured;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class HTTPServerTest {
    private static final String TEST_KEY = "keykeykey";
    private Db db;
    private StoreServerHTTP server;

    @BeforeEach
    void setUp() {
        db = new Db("test.db");
        server = new StoreServerHTTP(db);
        server.run();

        RestAssured.port = StoreServerHTTP.PORT;

        db.insertUser("root", "root");
    }

    @AfterEach
    void cleanup() {
        db.deleteAll();
        server.stop();
    }

    @Test
    void testLoginCorrectUserReturnsCorrectToken() {
        UserCredentials userCredentials = new UserCredentials("root", "root");
        String token = RestAssured.given()
                .contentType("application/json")
                .body(userCredentials)
                .when()
                .post("/login/")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("token", CoreMatchers.notNullValue())
                .extract()
                .path("token");

        String subject = JWT.require(Algorithm.HMAC256(TEST_KEY))
                .build()
                .verify(token)
                .getSubject();

        assertEquals(userCredentials.getLogin(), subject);
    }

    @Test
    void testLoginInvalidUserReturns401() {
        // Arrange
        UserCredentials badCredentials = new UserCredentials("admin", "wrong_pass");

        RestAssured.given()
                .contentType("application/json")
                .body(badCredentials)
                .when()
                .post("/login/")
                .then()
                .statusCode(401)
                .body("error", CoreMatchers.notNullValue());
    }

    @Test
    void testGetProductsWithoutTokenReturns401() {
        RestAssured.given()
                .when()
                .get("/products/1")
                .then()
                .statusCode(401);
    }

    @Test
    void testGetValidProduct() {
        Product baseProduct = new Product("Cheese", 10, 90.0, "Milk products");
        int id = db.insert(baseProduct);
        baseProduct.setId(id);

        String token = generateToken("root");

        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/products/{id}", id)
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("id", CoreMatchers.equalTo(baseProduct.getId()))
                .body("name", CoreMatchers.equalTo(baseProduct.getName()))
                .body("remainder", CoreMatchers.equalTo(baseProduct.getRemainder()))
                .body("price", CoreMatchers.equalTo((float) baseProduct.getPrice()))
                .body("category", CoreMatchers.equalTo(baseProduct.getCategory()));
    }

    @Test
    void testGetInvalidProduct() {
        String token = generateToken("root");

        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/products/{id}", 9999)
                .then()
                .statusCode(404);
    }

    @Test
    void testPutNewProduct() {
        Product baseProduct = new Product("Cheese", 10, 90.0, "Milk products");

        String token = generateToken("admin");

        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(baseProduct)
                .when()
                .put("/products/")
                .then()
                .statusCode(201)
                .contentType("application/json")
                .body("name", CoreMatchers.equalTo(baseProduct.getName()))
                .body("remainder", CoreMatchers.equalTo(baseProduct.getRemainder()))
                .body("price", CoreMatchers.equalTo((float) baseProduct.getPrice()))
                .body("category", CoreMatchers.equalTo(baseProduct.getCategory()));
    }

    @Test
    void testPutExistingProduct() {
        Product baseProduct = new Product("Cheese", 10, 90.0, "Milk products");
        db.insert(baseProduct);

        String token = generateToken("admin");

        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(baseProduct)
                .when()
                .put("/products/")
                .then()
                .statusCode(400);
    }

    @Test
    void testPostExistingProduct() {
        Product baseProduct = new Product("Cheese", 10, 90.0, "Milk products");
        int id = db.insert(baseProduct);
        baseProduct.setRemainder(30);
        baseProduct.setPrice(500.0);

        String token = generateToken("root");

        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(baseProduct)
                .when()
                .post("/products/{id}", id)
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("id", CoreMatchers.equalTo(id))
                .body("name", CoreMatchers.equalTo(baseProduct.getName()))
                .body("remainder", CoreMatchers.equalTo(baseProduct.getRemainder()))
                .body("price", CoreMatchers.equalTo((float) baseProduct.getPrice()))
                .body("category", CoreMatchers.equalTo(baseProduct.getCategory()));
    }

    @Test
    void testDeleteExistingProduct() {
        Product baseProduct = new Product("Cheese", 10, 90.0, "Milk products");
        int id = db.insert(baseProduct);

        String token = generateToken("admin");

        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/products/{id}", id)
                .then()
                .statusCode(200);

        assertTrue(db.getById(id).isEmpty());
    }

    @Test
    void testDeleteFakeProduct() {
        assertTrue(db.getById(999).isEmpty());

        String token = generateToken("admin");

        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/products/{id}", 999)
                .then()
                .statusCode(404);
    }

    private String generateToken(String username) {
        Algorithm algorithm = Algorithm.HMAC256(TEST_KEY);
        return JWT.create()
                .withSubject(username)
                .withExpiresAt(Instant.now().plusSeconds(1800))
                .sign(algorithm);
    }
}
