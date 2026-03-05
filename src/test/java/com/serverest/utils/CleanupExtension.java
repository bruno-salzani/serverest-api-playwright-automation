package com.serverest.utils;

import com.serverest.config.Specs;
import com.serverest.constants.Routes;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Set;

import static io.restassured.RestAssured.given;

public class CleanupExtension implements AfterEachCallback {
    private static volatile String adminToken;
    private static volatile boolean janitorRan = false;

    @Override
    public void afterEach(ExtensionContext context) {
        String token = getAdminToken();
        if (token == null || token.isBlank()) {
            return;
        }
        Set<String> productIds = TestDataRegistry.consumeProductIds();
        for (String id : productIds) {
            given()
                    .spec(Specs.requestSpec())
                    .header("Authorization", token)
                    .when()
                    .delete(Routes.PRODUCTS + "/" + id);
        }
        Set<String> userIds = TestDataRegistry.consumeUserIds();
        for (String id : userIds) {
            given()
                    .spec(Specs.requestSpec())
                    .header("Authorization", token)
                    .when()
                    .delete(Routes.USERS + "/" + id);
        }

        if (com.serverest.config.Config.get().janitorEnabled() && !janitorRan) {
            janitorRan = true;
            runJanitor(token);
        }
    }

    private static String getAdminToken() {
        if (adminToken == null) {
            synchronized (CleanupExtension.class) {
                if (adminToken == null) {
                    adminToken = AuthHelper.createAdminAndGetToken();
                }
            }
        }
        return adminToken;
    }

    @SuppressWarnings("unchecked")
    private void runJanitor(String token) {
        String prefix = "test_" + RunContext.runId() + "_";
        try {
            var users = given()
                    .spec(Specs.requestSpec())
                    .when()
                    .get(Routes.USERS)
                    .then()
                    .statusCode(200)
                    .extract()
                    .path("usuarios");
            if (users instanceof java.util.List<?> list) {
                for (Object o : list) {
                    if (o instanceof java.util.Map<?, ?> m) {
                        Object email = m.get("email");
                        Object id = m.get("_id");
                        if (email instanceof String e && id instanceof String i && e.contains(prefix)) {
                            given()
                                    .spec(Specs.requestSpec())
                                    .header("Authorization", token)
                                    .when()
                                    .delete(Routes.USERS + "/" + i)
                                    .then()
                                    .statusCode(org.hamcrest.Matchers.anyOf(org.hamcrest.Matchers.is(200), org.hamcrest.Matchers.is(204), org.hamcrest.Matchers.is(202)));
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        try {
            var products = given()
                    .spec(Specs.requestSpec())
                    .when()
                    .get(Routes.PRODUCTS)
                    .then()
                    .statusCode(200)
                    .extract()
                    .path("produtos");
            if (products instanceof java.util.List<?> list) {
                for (Object o : list) {
                    if (o instanceof java.util.Map<?, ?> m) {
                        Object nome = m.get("nome");
                        Object id = m.get("_id");
                        if (nome instanceof String n && id instanceof String i && n.contains(prefix)) {
                            given()
                                    .spec(Specs.requestSpec())
                                    .header("Authorization", token)
                                    .when()
                                    .delete(Routes.PRODUCTS + "/" + i)
                                    .then()
                                    .statusCode(org.hamcrest.Matchers.anyOf(org.hamcrest.Matchers.is(200), org.hamcrest.Matchers.is(204), org.hamcrest.Matchers.is(202)));
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}
