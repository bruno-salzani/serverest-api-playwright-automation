package com.serverest.tests;

import com.serverest.config.Specs;
import com.serverest.model.Product;
import com.serverest.model.LoginRequest;
import com.serverest.model.User;
import com.serverest.utils.AuthHelper;
import com.serverest.utils.CleanupExtension;
import com.serverest.utils.DataFactory;
import io.qameta.allure.Description;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Tag("regression")
@ExtendWith(CleanupExtension.class)
public class ProductAccessTest {

    @Test
    @Description("Valida que usuário comum não pode cadastrar produto")
    void naoDevePermitirCadastroDeProdutoParaUsuarioComum() {
        User user = DataFactory.createDefaultUser();
        AuthHelper.createUser(user);

        String token = given()
                .spec(Specs.requestSpec())
                .body(new LoginRequest(user.getEmail(), user.getPassword()))
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .extract()
                .path("authorization");

        Product product = DataFactory.createProduct();

        given()
                .spec(Specs.requestSpec())
                .header("Authorization", token)
                .body(product)
                .when()
                .post("/produtos")
                .then()
                .statusCode(403)
                .body("message", equalTo("Rota exclusiva para administradores"));
    }

    @Test
    @Description("Valida que acesso sem token é bloqueado")
    void naoDevePermitirCadastroDeProdutoSemToken() {
        Product product = DataFactory.createProduct();

        given()
                .spec(Specs.requestSpec())
                .body(product)
                .when()
                .post("/produtos")
                .then()
                .statusCode(401)
                .body("message", equalTo("Token de acesso ausente, inválido, expirado ou usuário do token não existe mais"));
    }
}
