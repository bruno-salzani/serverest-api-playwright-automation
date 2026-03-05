package com.serverest.tests;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class PaymentIntegrationTest {

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void setup() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterAll
    static void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void shouldApprovePaymentExternalService() {
        // Stubbing the external payment service
        stubFor(post(urlEqualTo("/api/v1/payment"))
                .withRequestBody(containing("credit_card"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"APPROVED\", \"transactionId\": \"123456\"}")));

        // Test the integration (simulating our backend calling the payment service)
        given()
                .baseUri("http://localhost:8089")
                .contentType("application/json")
                .body("{\"amount\": 100, \"credit_card\": \"411111111111\"}")
                .when()
                .post("/api/v1/payment")
                .then()
                .statusCode(200)
                .body("status", equalTo("APPROVED"));
    }
}
