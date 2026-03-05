package com.serverest.tests;

import com.serverest.config.Specs;
import com.serverest.constants.Routes;
import com.serverest.model.LoginRequest;
import com.serverest.model.LoginResponse;
import com.serverest.model.Product;
import com.serverest.model.User;
import com.serverest.model.UserCreateResponse;
import com.serverest.utils.AuthHelper;
import com.serverest.utils.CleanupExtension;
import com.serverest.utils.DataFactory;
import com.serverest.utils.TestDataRegistry;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;

@Tag("regression")
@ExtendWith(CleanupExtension.class)
public class EndToEndTest {

    @Test
    void deveCriarUsuarioLoginCadastrarProdutoEValidarProduto() {
        String adminToken = AuthHelper.createAdminAndGetToken();
        Product product = DataFactory.createProduct();

        String productId = given()
                .spec(Specs.requestSpec())
                .header("Authorization", adminToken)
                .body(product)
                .when()
                .post(Routes.PRODUCTS)
                .then()
                .statusCode(201)
                .extract()
                .path("_id");
        TestDataRegistry.registerProductId(productId);

        User user = DataFactory.createDefaultUser();
        UserCreateResponse createdUser = AuthHelper.createUser(user);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(user.getEmail())
                .password(user.getPassword())
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

        given()
                .spec(Specs.requestSpec())
                .header("Authorization", loginResponse.getAuthorization())
                .when()
                .get(Routes.PRODUCTS + "/" + productId)
                .then()
                .spec(Specs.responseSpec())
                .statusCode(200)
                .body("_id", equalTo(productId))
                .time(lessThan(2000L));

        given()
                .spec(Specs.requestSpec())
                .header("Authorization", adminToken)
                .when()
                .delete("/produtos/" + productId)
                .then()
                .statusCode(200);

        given()
                .spec(Specs.requestSpec())
                .header("Authorization", adminToken)
                .when()
                .delete("/usuarios/" + createdUser.get_id())
                .then()
                .statusCode(200);
    }
}
