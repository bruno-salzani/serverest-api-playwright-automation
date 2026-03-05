package com.serverest.tests;

import com.serverest.config.Specs;
import com.serverest.model.LoginRequest;
import io.qameta.allure.Description;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;

@Tag("regression")
public class LoginParameterizedTest {

    @ParameterizedTest
    @CsvSource({
            "emailinvalido@teste.com, senhaerrada",
            "sememail, teste123",
            "email@teste.com, ''"
    })
    @Description("Valida login negativo com variações de credenciais")
    void deveFalharLoginComCredenciaisInvalidas(String email, String password) {
        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        given()
                .spec(Specs.requestSpec())
                .body(loginRequest)
                .when()
                .post("/login")
                .then()
                .statusCode(401)
                .body("message", equalTo("Email e/ou senha inválidos"))
                .time(lessThan(2000L));
    }
}
