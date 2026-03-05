package com.serverest.utils;

import com.github.javafaker.Faker;
import com.serverest.model.Product;
import com.serverest.model.User;

import java.util.Locale;
import java.util.UUID;

public final class DataFactory {
    private static final Faker FAKER = new Faker(new Locale("pt", "BR"));

    private DataFactory() {
    }

    public static User createAdminUser() {
        return User.builder()
                .nome(FAKER.name().fullName())
                .email(uniqueEmail())
                .password(FAKER.internet().password(10, 16))
                .administrador("true")
                .build();
    }

    public static User createDefaultUser() {
        return User.builder()
                .nome(FAKER.name().fullName())
                .email(uniqueEmail())
                .password(FAKER.internet().password(10, 16))
                .administrador("false")
                .build();
    }

    public static Product createProduct() {
        return Product.builder()
                .nome("test_" + RunContext.runId() + "_" + FAKER.commerce().productName() + " " + UUID.randomUUID().toString().substring(0, 8))
                .preco(FAKER.number().numberBetween(50, 500))
                .descricao(FAKER.commerce().material())
                .quantidade(FAKER.number().numberBetween(1, 50))
                .build();
    }

    private static String uniqueEmail() {
        String baseLocal = FAKER.internet().emailAddress().split("@")[0];
        return RunContext.emailWithRunId(baseLocal);
    }
}
