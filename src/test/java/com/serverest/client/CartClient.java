package com.serverest.client;

import com.serverest.config.Specs;
import com.serverest.constants.Routes;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class CartClient {

    public Response create(String productId, int quantity, String token) {
        Map<String, Object> cartPayload = Map.of(
                "produtos", List.of(Map.of("idProduto", productId, "quantidade", quantity))
        );
        return given()
                .spec(Specs.requestSpec())
                .header("Authorization", token)
                .body(cartPayload)
                .when()
                .post(Routes.CARTS);
    }

    public Response cancel(String token) {
        return given()
                .spec(Specs.requestSpec())
                .header("Authorization", token)
                .when()
                .delete(Routes.CARTS_CANCEL);
    }

    public Response conclude(String token) {
        return given()
                .spec(Specs.requestSpec())
                .header("Authorization", token)
                .when()
                .delete(Routes.CARTS_CONCLUDE);
    }
}