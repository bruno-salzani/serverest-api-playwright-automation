package com.serverest.flow;

import com.serverest.client.CartClient;
import com.serverest.client.ProductClient;
import com.serverest.client.UserClient;
import com.serverest.model.LoginResponse;
import com.serverest.model.Product;
import com.serverest.model.User;
import com.serverest.utils.TestDataRegistry;
import io.qameta.allure.Step;

public class CartFlow {

    private final UserClient userClient;
    private final ProductClient productClient;
    private final CartClient cartClient;

    public CartFlow() {
        this.userClient = new UserClient();
        this.productClient = new ProductClient();
        this.cartClient = new CartClient();
    }

    @Step("Criar usuário, produto e adicionar ao carrinho")
    public CartFlowResult createCartWithProduct(User user, Product product, int quantity, String adminToken) {
        // 1. Criar Produto (como admin)
        String productId = productClient.create(product, adminToken)
                .then()
                .statusCode(201)
                .extract()
                .path("_id");
        TestDataRegistry.registerProductId(productId);

        // 2. Criar Usuário
        String userId = userClient.register(user)
                .then()
                .statusCode(201)
                .extract()
                .path("_id");
        TestDataRegistry.registerUserId(userId);

        // 3. Login
        String token = userClient.login(user.getEmail(), user.getPassword())
                .then()
                .statusCode(200)
                .extract()
                .as(LoginResponse.class)
                .getAuthorization();

        // 4. Adicionar ao Carrinho
        String cartId = cartClient.create(productId, quantity, token)
                .then()
                .statusCode(201)
                .extract()
                .path("_id");

        return CartFlowResult.builder()
                .userToken(token)
                .productId(productId)
                .cartId(cartId)
                .build();
    }
}
