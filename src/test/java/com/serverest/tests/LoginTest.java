package com.serverest.tests;

import com.serverest.client.UserClient;
import com.serverest.config.Specs;
import com.serverest.factory.UserFactory;
import com.serverest.model.User;
import com.serverest.utils.CleanupExtension;
import com.serverest.utils.TestDataRegistry;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

@Tag("smoke")
@Tag("regression")
@ExtendWith(CleanupExtension.class)
public class LoginTest {

    private final UserClient userClient = new UserClient();

    @Test
    void deveGerarTokenComCredenciaisValidas() {
        User user = UserFactory.createAdmin();

        String userId = userClient.register(user)
                .then()
                .statusCode(201)
                .extract()
                .path("_id");
        TestDataRegistry.registerUserId(userId);

        userClient.login(user.getEmail(), user.getPassword())
                .then()
                .spec(Specs.responseSpec())
                .statusCode(200)
                .body("authorization", notNullValue())
                .time(lessThan(2000L));
    }

    @Test
    void deveRetornar401ComCredenciaisInvalidas() {
        userClient.login("emailinvalido@teste.com", "senhaerrada")
                .then()
                .spec(Specs.responseSpec())
                .statusCode(401)
                .body("message", equalTo("Email e/ou senha inválidos"))
                .time(lessThan(2000L));
    }
}