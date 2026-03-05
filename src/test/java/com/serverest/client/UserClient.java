package com.serverest.client;

import com.serverest.config.Specs;
import com.serverest.constants.Routes;
import com.serverest.model.LoginRequest;
import com.serverest.model.User;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class UserClient {

    public Response register(User user) {
        return given()
                .spec(Specs.requestSpec())
                .body(user)
                .when()
                .post(Routes.USERS);
    }

    public Response login(String email, String password) {
        LoginRequest request = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();
        return given()
                .spec(Specs.requestSpec())
                .body(request)
                .when()
                .post(Routes.LOGIN);
    }

    public Response delete(String id, String token) {
        return given()
                .spec(Specs.requestSpec())
                .header("Authorization", token)
                .when()
                .delete(Routes.USERS + "/" + id);
    }

    public Response findById(String id) {
        return given()
                .spec(Specs.requestSpec())
                .when()
                .get(Routes.USERS + "/" + id);
    }
}
