package com.serverest.contract;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "ServeRestProvider")
public class LoginContractTest {

    @Pact(provider = "ServeRestProvider", consumer = "ServeRestConsumer")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("User exists")
                .uponReceiving("A login request")
                .path("/login")
                .method("POST")
                .headers(headers)
                .body("{\"email\": \"fulano@qa.com\", \"password\": \"teste\"}")
                .willRespondWith()
                .status(200)
                .body("{\"message\": \"Login realizado com sucesso\", \"authorization\": \"Bearer token123\"}")
                .toPact();
    }

    @Test
    void testLoginContract(MockServer mockServer) {
        RestAssured.baseURI = mockServer.getUrl();

        given()
                .contentType("application/json")
                .body("{\"email\": \"fulano@qa.com\", \"password\": \"teste\"}")
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body("message", equalTo("Login realizado com sucesso"));
    }
}