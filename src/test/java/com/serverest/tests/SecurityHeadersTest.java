package com.serverest.tests;

import com.serverest.config.Specs;
import com.serverest.constants.Routes;
import com.serverest.utils.CleanupExtension;
import io.qameta.allure.Description;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;

@Tag("regression")
@ExtendWith(CleanupExtension.class)
public class SecurityHeadersTest {

    @Test
    @Description("Valida Content-Type e CORS nas respostas")
    void deveValidarHeadersDeResposta() {
        given()
                .spec(Specs.requestSpec())
                .when()
                .get(Routes.USERS)
                .then()
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .header("Content-Type", containsString("charset"))
                .header("Access-Control-Allow-Origin", notNullValue());
    }

    @Test
    @Description("Valida retorno 405 em método não permitido")
    void deveRetornar405ParaMetodoInvalido() {
        given()
                .spec(Specs.requestSpec())
                .when()
                .put(Routes.LOGIN)
                .then()
                .statusCode(405);
    }
}
