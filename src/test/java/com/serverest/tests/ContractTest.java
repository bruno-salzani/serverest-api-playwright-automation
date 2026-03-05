package com.serverest.tests;

import com.serverest.config.Specs;
import com.serverest.model.LoginRequest;
import com.serverest.model.User;
import com.serverest.utils.CleanupExtension;
import com.serverest.utils.DataFactory;
import com.serverest.utils.TestDataRegistry;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.lessThan;

@Tag("contract")
@ExtendWith(CleanupExtension.class)
public class ContractTest {

    @Test
    void deveValidarContratoDeCadastroDeUsuario() {
        User user = DataFactory.createDefaultUser();

        String userId = given()
                .spec(Specs.requestSpec())
                .body(user)
                .when()
                .post("/usuarios")
                .then()
                .spec(Specs.responseSpec())
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/user-create.json"))
                .time(lessThan(2000L))
                .extract()
                .path("_id");
        TestDataRegistry.registerUserId(userId);
    }

    @Test
    void deveValidarContratoDeLogin() {
        User user = DataFactory.createAdminUser();

        String userId = given()
                .spec(Specs.requestSpec())
                .body(user)
                .when()
                .post("/usuarios")
                .then()
                .statusCode(201)
                .extract()
                .path("_id");
        TestDataRegistry.registerUserId(userId);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(user.getEmail())
                .password(user.getPassword())
                .build();

        given()
                .spec(Specs.requestSpec())
                .body(loginRequest)
                .when()
                .post("/login")
                .then()
                .spec(Specs.responseSpec())
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/login.json"))
                .time(lessThan(2000L));
    }
}
