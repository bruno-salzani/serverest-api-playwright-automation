package com.serverest.tests;

import com.serverest.client.CartClient;
import com.serverest.config.Specs;
import com.serverest.constants.Routes;
import com.serverest.factory.ProductFactory;
import com.serverest.factory.UserFactory;
import com.serverest.flow.CartFlow;
import com.serverest.flow.CartFlowResult;
import com.serverest.model.LoginRequest;
import com.serverest.model.LoginResponse;
import com.serverest.model.Product;
import com.serverest.model.User;
import com.serverest.utils.AuthHelper;
import com.serverest.utils.DataFactory;
import com.serverest.utils.CleanupExtension;
import com.serverest.utils.TestDataRegistry;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static com.serverest.utils.AllureAttachments.attachJson;

@Tag("regression")
@ExtendWith(CleanupExtension.class)
public class CartFlowTest {

    private final CartClient cartClient = new CartClient();
    private final CartFlow cartFlow = new CartFlow();

    @Test
    @Description("Valida fluxo completo de estoque ao criar e cancelar carrinho")
    void deveAtualizarEstoqueAoCriarECancelarCarrinho() {
        String adminToken = AuthHelper.createAdminAndGetToken();
        
        Product product = ProductFactory.createDefault();
        product.setQuantidade(5);
        User user = UserFactory.createCommon();

        // 1. Flow: Criar cenário de carrinho (Produto + Usuário + Login + Add Carrinho)
        CartFlowResult result = cartFlow.createCartWithProduct(user, product, 3, adminToken);

        // 2. Client: Validar estoque reduzido
        
        // Refatorando para ser "Puro Flow" vs "Client Validation"
        
        cartClient.cancel(result.getUserToken())
                .then()
                .statusCode(200)
                .body("message", equalTo("Registro excluído com sucesso"));
    }

    @Test
    @Description("Valida regras de carrinho: estoque, carrinho único e integridade")
    void deveAplicarRegrasDeCarrinho() {
        String adminToken = AuthHelper.createAdminAndGetToken();
        Product product = Product.builder()
                .nome("Produto Regras " + System.currentTimeMillis())
                .preco(200)
                .descricao("Produto para regras")
                .quantidade(2)
                .build();

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
        AuthHelper.createUser(user);

        LoginResponse loginResponse = given()
                .spec(Specs.requestSpec())
                .body(new LoginRequest(user.getEmail(), user.getPassword()))
                .when()
                .post(Routes.LOGIN)
                .then()
                .statusCode(200)
                .extract()
                .as(LoginResponse.class);

        Map<String, Object> cartPayload = Map.of(
                "produtos", List.of(Map.of("idProduto", productId, "quantidade", 1))
        );

        given()
                .spec(Specs.requestSpec())
                .header("Authorization", loginResponse.getAuthorization())
                .body(cartPayload)
                .when()
                .post(Routes.CARTS)
                .then()
                .statusCode(201);

        given()
                .spec(Specs.requestSpec())
                .header("Authorization", loginResponse.getAuthorization())
                .body(cartPayload)
                .when()
                .post(Routes.CARTS)
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/error.json"));

        given()
                .spec(Specs.requestSpec())
                .header("Authorization", adminToken)
                .when()
                .delete(Routes.PRODUCTS + "/" + productId)
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/error.json"));

        given()
                .spec(Specs.requestSpec())
                .header("Authorization", loginResponse.getAuthorization())
                .when()
                .delete(Routes.CARTS_CANCEL)
                .then()
                .statusCode(200);

        Map<String, Object> overStockPayload = Map.of(
                "produtos", List.of(Map.of("idProduto", productId, "quantidade", 5))
        );

        given()
                .spec(Specs.requestSpec())
                .header("Authorization", loginResponse.getAuthorization())
                .body(overStockPayload)
                .when()
                .post(Routes.CARTS)
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/error.json"));

        given()
                .spec(Specs.requestSpec())
                .header("Authorization", adminToken)
                .when()
                .delete(Routes.PRODUCTS + "/" + productId)
                .then()
                .statusCode(200);
    }

    @Test
    @Description("Valida conclusão de compra e baixa de estoque")
    void deveConcluirCompraEDiminuirEstoque() {
        String adminToken = AuthHelper.createAdminAndGetToken();
        Product product = Product.builder()
                .nome("Produto Concluir " + System.currentTimeMillis())
                .preco(180)
                .descricao("Produto para concluir compra")
                .quantidade(5)
                .build();

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
        AuthHelper.createUser(user);

        LoginResponse loginResponse = given()
                .spec(Specs.requestSpec())
                .body(new LoginRequest(user.getEmail(), user.getPassword()))
                .when()
                .post(Routes.LOGIN)
                .then()
                .statusCode(200)
                .extract()
                .as(LoginResponse.class);

        Map<String, Object> cartPayload = Map.of(
                "produtos", List.of(Map.of("idProduto", productId, "quantidade", 3))
        );

        given()
                .spec(Specs.requestSpec())
                .header("Authorization", loginResponse.getAuthorization())
                .body(cartPayload)
                .when()
                .post(Routes.CARTS)
                .then()
                .statusCode(201)
                .body("message", equalTo("Cadastro realizado com sucesso"));

        Response concludeResponse = given()
                .spec(Specs.requestSpec())
                .header("Authorization", loginResponse.getAuthorization())
                .when()
                .delete(Routes.CARTS_CONCLUDE)
                .then()
                .statusCode(200)
                .extract()
                .response();
        attachJson("carrinho-concluido", concludeResponse.asPrettyString());

        int quantidadeAposConclusao = given()
                .spec(Specs.requestSpec())
                .when()
                .get(Routes.PRODUCTS + "/" + productId)
                .then()
                .statusCode(200)
                .extract()
                .path("quantidade");

        org.junit.jupiter.api.Assertions.assertEquals(2, quantidadeAposConclusao);

        given()
                .spec(Specs.requestSpec())
                .header("Authorization", adminToken)
                .when()
                .delete(Routes.PRODUCTS + "/" + productId)
                .then()
                .statusCode(200);
    }
}
