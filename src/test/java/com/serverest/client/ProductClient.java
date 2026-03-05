package com.serverest.client;

import com.serverest.config.Specs;
import com.serverest.constants.Routes;
import com.serverest.model.Product;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class ProductClient {

    public Response create(Product product, String token) {
        return given()
                .spec(Specs.requestSpec())
                .header("Authorization", token)
                .body(product)
                .when()
                .post(Routes.PRODUCTS);
    }

    public Response delete(String id, String token) {
        return given()
                .spec(Specs.requestSpec())
                .header("Authorization", token)
                .when()
                .delete(Routes.PRODUCTS + "/" + id);
    }

    public Response list() {
        return given()
                .spec(Specs.requestSpec())
                .when()
                .get(Routes.PRODUCTS);
    }
}