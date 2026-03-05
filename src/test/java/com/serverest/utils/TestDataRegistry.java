package com.serverest.utils;

import java.util.HashSet;
import java.util.Set;

public final class TestDataRegistry {
    private static final ThreadLocal<Set<String>> USER_IDS = ThreadLocal.withInitial(HashSet::new);
    private static final ThreadLocal<Set<String>> PRODUCT_IDS = ThreadLocal.withInitial(HashSet::new);

    private TestDataRegistry() {
    }

    public static void registerUserId(String userId) {
        if (userId != null && !userId.isBlank()) {
            USER_IDS.get().add(userId);
        }
    }

    public static void registerProductId(String productId) {
        if (productId != null && !productId.isBlank()) {
            PRODUCT_IDS.get().add(productId);
        }
    }

    public static Set<String> consumeUserIds() {
        Set<String> ids = new HashSet<>(USER_IDS.get());
        USER_IDS.get().clear();
        return ids;
    }

    public static Set<String> consumeProductIds() {
        Set<String> ids = new HashSet<>(PRODUCT_IDS.get());
        PRODUCT_IDS.get().clear();
        return ids;
    }
}
