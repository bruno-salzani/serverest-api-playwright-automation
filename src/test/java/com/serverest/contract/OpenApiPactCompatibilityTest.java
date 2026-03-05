package com.serverest.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class OpenApiPactCompatibilityTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void pactMustBeCompatibleWithOpenApi() throws IOException {
        Map<String, Map<String, Set<String>>> oas = loadOpenApiIndex();

        Path pactDir = Path.of("target", "pacts");
        if (!Files.exists(pactDir)) {
            return;
        }

        File[] pacts = pactDir.toFile().listFiles((d, n) -> n.endsWith(".json"));
        if (pacts == null || pacts.length == 0) {
            return;
        }

        for (File pact : pacts) {
            JsonNode root = MAPPER.readTree(pact);
            JsonNode interactions = root.get("interactions");
            if (interactions == null || !interactions.isArray()) continue;
            for (JsonNode it : interactions) {
                String method = it.at("/request/method").asText("").toLowerCase();
                String path = it.at("/request/path").asText("");
                String status = it.at("/response/status").asText("");

                Assertions.assertFalse(method.isBlank(), "Pact interaction without method");
                Assertions.assertFalse(path.isBlank(), "Pact interaction without path");
                Assertions.assertFalse(status.isBlank(), "Pact interaction without status");

                Map<String, Set<String>> methods = oas.get(path);
                Assertions.assertNotNull(methods, "Path not found in OpenAPI: " + path);

                Set<String> responses = methods.get(method);
                Assertions.assertNotNull(responses, String.format("Method '%s' not defined for path '%s' in OpenAPI", method, path));

                Assertions.assertTrue(responses.contains(status),
                        String.format("Status %s not allowed for %s %s in OpenAPI. Allowed: %s",
                                status, method.toUpperCase(), path, responses));
            }
        }
    }

    private Map<String, Map<String, Set<String>>> loadOpenApiIndex() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("schemas/swagger.json")) {
            if (is == null) {
                throw new IllegalStateException("schemas/swagger.json not found on classpath");
            }
            JsonNode root = MAPPER.readTree(is);
            JsonNode paths = root.get("paths");
            Map<String, Map<String, Set<String>>> index = new HashMap<>();
            Iterator<String> fieldNames = paths.fieldNames();
            while (fieldNames.hasNext()) {
                String p = fieldNames.next();
                JsonNode item = paths.get(p);
                Map<String, Set<String>> methods = new HashMap<>();
                for (Iterator<String> it = item.fieldNames(); it.hasNext(); ) {
                    String op = it.next();
                    JsonNode opNode = item.get(op);
                    JsonNode responses = opNode.get("responses");
                    Set<String> codes = new HashSet<>();
                    responses.fieldNames().forEachRemaining(codes::add);
                    methods.put(op.toLowerCase(), codes);
                }
                index.put(p, methods);
            }
            return index;
        }
    }
}
