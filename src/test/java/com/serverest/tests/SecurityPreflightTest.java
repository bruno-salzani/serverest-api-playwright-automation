package com.serverest.tests;

import com.serverest.config.Specs;
import com.serverest.utils.CleanupExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@Tag("regression")
@ExtendWith(CleanupExtension.class)
public class SecurityPreflightTest {

    @Test
    void deveValidarPreflightCORSEmProdutos() {
        given()
                .spec(Specs.requestSpec())
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .when()
                .options("/produtos")
                .then()
                .statusCode(204)
                .header("Access-Control-Allow-Origin", notNullValue())
                .header("Access-Control-Allow-Methods", notNullValue());
    }
}
