package com.serverest.utils;

import com.serverest.config.Config;

import java.util.Locale;
import java.util.UUID;

public final class RunContext {
    private static final String RUN_ID = initRunId();

    private RunContext() {
    }

    public static String runId() {
        return RUN_ID;
    }

    public static String emailWithRunId(String localPartBase) {
        String lp = localPartBase.replaceAll("[^a-zA-Z0-9._-]", "").toLowerCase(Locale.ROOT);
        return String.format("%s+test_%s@qa.com", lp, RUN_ID);
    }

    private static String initRunId() {
        String v = System.getProperty("RUN_ID");
        if (v == null || v.isBlank()) {
            v = System.getenv("RUN_ID");
        }
        if (v == null || v.isBlank()) {
            String cfg = Config.get().runId();
            if (cfg != null && !cfg.isBlank()) {
                v = cfg;
            }
        }
        if (v == null || v.isBlank()) {
            v = UUID.randomUUID().toString().substring(0, 8);
        }
        return v.toLowerCase(Locale.ROOT);
    }
}
