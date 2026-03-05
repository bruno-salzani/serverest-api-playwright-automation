package com.serverest.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LatencyGate {
    private static final Logger LOG = LoggerFactory.getLogger(LatencyGate.class);

    private LatencyGate() {
    }

    public static void main(String[] args) throws IOException {
        Path path = Path.of("target", "latency-summary.json");
        if (!Files.exists(path)) {
            LOG.warn("File not found; gate ignored: {}", path);
            return;
        }
        
        // We use system properties directly because exec-maven-plugin might pass arguments
        // or we can rely on Config/Owner.
        // Let's use simple System.getProperty for simplicity in main method context without heavy dependencies if possible,
        // but since we added Owner, we can use it.
        // However, Owner needs classpath scan for config.properties if we rely on that.
        // Let's stick to System properties passed via -D to Maven.
        
        String p95Prop = System.getProperty("latency.p95.max");
        String p99Prop = System.getProperty("latency.p99.max");

        if (p95Prop == null && p99Prop == null) {
            LOG.info("No latency limits configured; gate ignored.");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(Files.readString(path));
        long p95 = node.path("p95_ms").asLong(0);
        long p99 = node.path("p99_ms").asLong(0);
        boolean fail = false;

        if (p95Prop != null && !p95Prop.isBlank()) {
            long max = Long.parseLong(p95Prop);
            if (p95 > max) {
                LOG.error("p95={}ms > limit={}ms", p95, max);
                fail = true;
            }
        }
        if (p99Prop != null && !p99Prop.isBlank()) {
            long max = Long.parseLong(p99Prop);
            if (p99 > max) {
                LOG.error("p99={}ms > limit={}ms", p99, max);
                fail = true;
            }
        }
        if (fail) {
            throw new IllegalStateException("Latency Gate Failed");
        }
        LOG.info("Latency Gate Approved");
    }
}