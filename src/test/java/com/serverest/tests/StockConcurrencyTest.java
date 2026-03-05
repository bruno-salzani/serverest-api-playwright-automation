package com.serverest.tests;

import com.serverest.config.Specs;
import com.serverest.model.LoginRequest;
import com.serverest.model.LoginResponse;
import com.serverest.model.Product;
import com.serverest.model.User;
import com.serverest.utils.AuthHelper;
import com.serverest.utils.CleanupExtension;
import com.serverest.utils.DataFactory;
import com.serverest.utils.TestDataRegistry;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("regression")
@ExtendWith(CleanupExtension.class)
public class StockConcurrencyTest {

    @Test
    void deveRespeitarControleDeEstoqueEmConcorrencia() throws InterruptedException {
        String adminToken = AuthHelper.createAdminAndGetToken();
        Product product = Product.builder()
                .nome("Produto Conc " + System.currentTimeMillis())
                .preco(100)
                .descricao("Concorrencia")
                .quantidade(2)
                .build();

        String productId = given()
                .spec(Specs.requestSpec())
                .header("Authorization", adminToken)
                .body(product)
                .when()
                .post("/produtos")
                .then()
                .statusCode(201)
                .extract()
                .path("_id");
        TestDataRegistry.registerProductId(productId);

        User user = DataFactory.createDefaultUser();
        given().spec(Specs.requestSpec()).body(user).when().post("/usuarios").then().statusCode(201);
        String token = given().spec(Specs.requestSpec()).body(new LoginRequest(user.getEmail(), user.getPassword()))
                .when().post("/login").then().statusCode(200).extract().as(LoginResponse.class).getAuthorization();

        Map<String, Object> cartPayload = Map.of("produtos", List.of(Map.of("idProduto", productId, "quantidade", 1)));

        AtomicInteger success = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(2);

        Runnable r = () -> {
            try {
                int code = given()
                        .spec(Specs.requestSpec())
                        .header("Authorization", token)
                        .body(cartPayload)
                        .when()
                        .post("/carrinhos")
                        .getStatusCode();
                if (code == 201) {
                    success.incrementAndGet();
                    given().spec(Specs.requestSpec()).header("Authorization", token).when().delete("/carrinhos/cancelar-compra");
                }
            } finally {
                latch.countDown();
            }
        };

        new Thread(r).start();
        new Thread(r).start();
        latch.await();

        assertEquals(2, success.get());
    }
}
