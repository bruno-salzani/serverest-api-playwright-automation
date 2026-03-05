package com.serverest.tests;

import com.serverest.config.Specs;
import com.serverest.model.Product;
import com.serverest.utils.AuthHelper;
import com.serverest.utils.CleanupExtension;
import com.serverest.utils.DataFactory;
import com.serverest.utils.TestDataRegistry;
import io.qameta.allure.Description;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

@Tag("regression")
@ExtendWith(CleanupExtension.class)
public class ProductFilterTest {

    @Test
    @Description("Valida cadastro de produto e presença na listagem")
    void deveCadastrarProdutoComEstoqueEListar() {
        String token = AuthHelper.createAdminAndGetToken();
        Product product = Product.builder()
                .nome("Produto Estoque " + System.currentTimeMillis())
                .preco(150)
                .descricao("Produto de teste")
                .quantidade(10)
                .build();

        String productId = given()
                .spec(Specs.requestSpec())
                .header("Authorization", token)
                .body(product)
                .when()
                .post("/produtos")
                .then()
                .statusCode(201)
                .extract()
                .path("_id");
        TestDataRegistry.registerProductId(productId);

        List<String> ids = given()
                .spec(Specs.requestSpec())
                .when()
                .get("/produtos")
                .then()
                .statusCode(200)
                .body("quantidade", notNullValue())
                .time(lessThan(2000L))
                .extract()
                .path("produtos._id");

        org.junit.jupiter.api.Assertions.assertTrue(ids.contains(productId));

        given()
                .spec(Specs.requestSpec())
                .header("Authorization", token)
                .when()
                .delete("/produtos/" + productId)
                .then()
                .statusCode(200);
    }

    @Test
    @Description("Valida filtros de busca por nome, preço e descrição")
    void deveFiltrarProdutosPorQueryParams() {
        String token = AuthHelper.createAdminAndGetToken();
        Product product = DataFactory.createProduct();

        String productId = given()
                .spec(Specs.requestSpec())
                .header("Authorization", token)
                .body(product)
                .when()
                .post("/produtos")
                .then()
                .statusCode(201)
                .extract()
                .path("_id");
        TestDataRegistry.registerProductId(productId);

        List<String> ids = given()
                .spec(Specs.requestSpec())
                .queryParam("nome", product.getNome())
                .queryParam("preco", product.getPreco())
                .queryParam("descricao", product.getDescricao())
                .when()
                .get("/produtos")
                .then()
                .statusCode(200)
                .body("quantidade", notNullValue())
                .time(lessThan(2000L))
                .extract()
                .path("produtos._id");

        org.junit.jupiter.api.Assertions.assertTrue(ids.contains(productId));

        given()
                .spec(Specs.requestSpec())
                .header("Authorization", token)
                .when()
                .delete("/produtos/" + productId)
                .then()
                .statusCode(200);
    }
}
