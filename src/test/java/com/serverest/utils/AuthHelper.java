package com.serverest.utils;

import com.serverest.config.Specs;
import com.serverest.constants.Routes;
import com.serverest.model.LoginRequest;
import com.serverest.model.LoginResponse;
import com.serverest.model.User;
import com.serverest.model.UserCreateResponse;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;

public final class AuthHelper {
    private AuthHelper() {
    }

    @Step("Criar usuário admin e obter token")
    public static String createAdminAndGetToken() {
        User admin = DataFactory.createAdminUser();
        given()
                .spec(Specs.requestSpec())
                .contentType(ContentType.JSON)
                .body(admin)
                .when()
                .post(Routes.USERS)
                .then()
                .statusCode(201);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(admin.getEmail())
                .password(admin.getPassword())
                .build();

        LoginResponse loginResponse = given()
                .spec(Specs.requestSpec())
                .body(loginRequest)
                .when()
                .post(Routes.LOGIN)
                .then()
                .statusCode(200)
                .extract()
                .as(LoginResponse.class);

        return loginResponse.getAuthorization();
    }

    @Step("Criar usuário via API")
    public static UserCreateResponse createUser(User user) {
        UserCreateResponse response = given()
                .spec(Specs.requestSpec())
                .body(user)
                .when()
                .post(Routes.USERS)
                .then()
                .statusCode(201)
                .extract()
                .as(UserCreateResponse.class);
        TestDataRegistry.registerUserId(response.get_id());
        return response;
    }
}
