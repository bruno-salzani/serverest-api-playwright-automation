package com.serverest.tests;

import com.serverest.config.Specs;
import com.serverest.constants.Routes;
import com.serverest.model.Product;
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
import static org.hamcrest.Matchers.notNullValue;

@Tag("smoke")
@Tag("regression")
@ExtendWith(CleanupExtension.class)
public class ProductTest {

    @Test
    void deveCriarEditarExcluirProdutoComoAdmin() {
        String token = AuthHelper.createAdminAndGetToken();
        Product product = DataFactory.createProduct();

        String productId = given()
                .spec(Specs.requestSpec())
                .header("Authorization", token)
                .body(product)
                .when()
                .post(Routes.PRODUCTS)
                .then()
                .spec(Specs.responseSpec())
                .statusCode(201)
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .body("_id", notNullValue())
                .time(lessThan(2000L))
                .extract()
                .path("_id");
        TestDataRegistry.registerProductId(productId);

        Product updated = Product.builder()
                .nome(product.getNome() + " Atualizado")
                .preco(product.getPreco())
                .descricao(product.getDescricao())
                .quantidade(product.getQuantidade())
                .build();

        given()
                .spec(Specs.requestSpec())
                .header("Authorization", token)
                .body(updated)
                .when()
                .put(Routes.PRODUCTS + "/" + productId)
                .then()
                .spec(Specs.responseSpec())
                .statusCode(200)
                .body("message", equalTo("Registro alterado com sucesso"))
                .time(lessThan(2000L));

        given()
                .spec(Specs.requestSpec())
                .header("Authorization", token)
                .when()
                .delete(Routes.PRODUCTS + "/" + productId)
                .then()
                .spec(Specs.responseSpec())
                .statusCode(200)
                .body("message", equalTo("Registro excluído com sucesso"))
                .time(lessThan(2000L));
    }
}
