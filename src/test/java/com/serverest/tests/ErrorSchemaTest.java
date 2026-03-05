package com.serverest.tests;

import com.serverest.config.Specs;
import com.serverest.constants.Routes;
import com.serverest.model.LoginRequest;
import com.serverest.model.User;
import com.serverest.utils.CleanupExtension;
import com.serverest.utils.DataFactory;
import com.serverest.utils.TestDataRegistry;
import io.qameta.allure.Description;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

@Tag("contract")
@ExtendWith(CleanupExtension.class)
public class ErrorSchemaTest {

    @Test
    @Description("Valida contrato de erro para login inválido")
    void deveValidarContratoDeErroNoLogin() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("emailinvalido@teste.com")
                .password("senhaerrada")
                .build();

        given()
                .spec(Specs.requestSpec())
                .body(loginRequest)
                .when()
                .post(Routes.LOGIN)
                .then()
                .statusCode(401)
                .body(matchesJsonSchemaInClasspath("schemas/error.json"));
    }

    @Test
    @Description("Valida contrato de erro para email duplicado")
    void deveValidarContratoDeErroNoEmailDuplicado() {
        User user = DataFactory.createDefaultUser();
        String userId = given()
                .spec(Specs.requestSpec())
                .body(user)
                .when()
                .post(Routes.USERS)
                .then()
                .statusCode(201)
                .extract()
                .path("_id");
        TestDataRegistry.registerUserId(userId);

        given()
                .spec(Specs.requestSpec())
                .body(user)
                .when()
                .post(Routes.USERS)
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/error.json"));
    }
}
