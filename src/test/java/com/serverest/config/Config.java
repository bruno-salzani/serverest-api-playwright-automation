package com.serverest.config;

import org.aeonbits.owner.ConfigFactory;

public final class Config {
    private static volatile ServerestConfig CONFIG = ConfigFactory.create(ServerestConfig.class);

    private Config() {
    }

    public static String getBaseUrl() {
        return CONFIG.baseUrl();
    }

    public static ServerestConfig get() {
        return CONFIG;
    }

    public static void reload() {
        CONFIG = ConfigFactory.create(ServerestConfig.class);
    }
}