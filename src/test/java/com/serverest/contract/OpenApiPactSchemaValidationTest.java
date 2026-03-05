package com.serverest.contract;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.model.SimpleResponse;
import com.atlassian.oai.validator.report.ValidationReport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;

public class OpenApiPactSchemaValidationTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void pactResponsesMustConformToOpenApiSchemas() throws IOException {
        Path pactDir = Path.of("target", "pacts");
        if (!Files.exists(pactDir)) {
            return;
        }
        File[] pacts = pactDir.toFile().listFiles((d, n) -> n.endsWith(".json"));
        if (pacts == null || pacts.length == 0) {
            return;
        }

        OpenApiInteractionValidator validator =
                OpenApiInteractionValidator.createFor("schemas/swagger.json").build();
        com.fasterxml.jackson.databind.JsonNode oasRoot =
                new com.fasterxml.jackson.databind.ObjectMapper()
                        .readTree(getClass().getClassLoader().getResourceAsStream("schemas/swagger.json"));

        for (File pact : pacts) {
            JsonNode root = MAPPER.readTree(pact);
            JsonNode interactions = root.get("interactions");
            if (interactions == null || !interactions.isArray()) continue;
            for (JsonNode it : interactions) {
                String method = it.at("/request/method").asText("").toLowerCase(Locale.ROOT);
                String path = it.at("/request/path").asText("");
                String reqBody = bodyAsString(it.get("request").get("body"));
                String resBody = bodyAsString(it.get("response").get("body"));
                int status = it.at("/response/status").asInt(0);

                SimpleRequest.Builder req = new SimpleRequest.Builder(method, path);
                if (!reqBody.isBlank()) {
                    req.withBody(reqBody).withContentType("application/json");
                }
                addHeaders(req, it.get("request").get("headers"));

                SimpleResponse.Builder res = new SimpleResponse.Builder(status);
                if (!resBody.isBlank()) {
                    res.withBody(resBody).withContentType("application/json");
                }
                addHeaders(res, it.get("response").get("headers"));

                ValidationReport report = validator.validate(req.build(), res.build());
                Assertions.assertFalse(report.hasErrors(),
                        "OpenAPI schema violations:\n" + report);

                // Checagens adicionais: media types e headers obrigatórios conforme OpenAPI
                com.fasterxml.jackson.databind.JsonNode opNode = oasRoot.at("/paths").get(path);
                Assertions.assertNotNull(opNode, "Path não encontrado no OpenAPI: " + path);
                com.fasterxml.jackson.databind.JsonNode methodNode = opNode.get(method);
                Assertions.assertNotNull(methodNode, "Método não definido no OpenAPI para " + path + ": " + method);

                // Media types do request
                com.fasterxml.jackson.databind.JsonNode requestBody = methodNode.get("requestBody");
                if (requestBody != null && requestBody.has("content")) {
                    var allowedReq = requestBody.get("content").fieldNames();
                    boolean hasJson = false;
                    while (allowedReq.hasNext()) {
                        String mt = allowedReq.next();
                        if (mt.contains("json")) {
                            hasJson = true;
                            break;
                        }
                    }
                    if (!reqBody.isBlank()) {
                        Assertions.assertTrue(hasJson, "Request body presente no Pact, mas operação não aceita JSON no OpenAPI");
                    }
                }

                // Headers obrigatórios (request)
                if (methodNode.has("parameters")) {
                    var arr = methodNode.get("parameters");
                    if (arr.isArray()) {
                        for (JsonNode p : arr) {
                            boolean required = p.path("required").asBoolean(false);
                            if ("header".equalsIgnoreCase(p.path("in").asText()) && required) {
                                String headerName = p.path("name").asText();
                                JsonNode pactHeaders = it.path("request").path("headers");
                                Assertions.assertTrue(pactHeaders.has(headerName),
                                        "Header obrigatório ausente no Pact request: " + headerName);
                            }
                        }
                    }
                }

                // Media types do response
                com.fasterxml.jackson.databind.JsonNode responses = methodNode.get("responses");
                if (responses != null && responses.has(String.valueOf(status))) {
                    com.fasterxml.jackson.databind.JsonNode responseNode = responses.get(String.valueOf(status));
                    if (responseNode.has("content")) {
                        var allowedRes = responseNode.get("content").fieldNames();
                        boolean allowsJson = false;
                        while (allowedRes.hasNext()) {
                            String mt = allowedRes.next();
                            if (mt.contains("json")) {
                                allowsJson = true;
                                break;
                            }
                        }
                        if (!resBody.isBlank()) {
                            Assertions.assertTrue(allowsJson, "Response body presente no Pact, mas o status " + status + " não define JSON no OpenAPI");
                        }
                    }
                }
            }
        }
    }

    private static String bodyAsString(JsonNode node) throws IOException {
        if (node == null || node.isNull()) return "";
        if (node.isTextual()) return node.asText();
        return MAPPER.writeValueAsString(node);
    }

    private static void addHeaders(SimpleRequest.Builder b, JsonNode headers) {
        if (headers == null || headers.isNull() || !headers.isObject()) return;
        Iterator<String> names = headers.fieldNames();
        while (names.hasNext()) {
            String h = names.next();
            JsonNode v = headers.get(h);
            if (v.isArray()) {
                for (JsonNode e : v) b.withHeader(h, e.asText());
            } else {
                b.withHeader(h, v.asText());
            }
        }
    }

    private static void addHeaders(SimpleResponse.Builder b, JsonNode headers) {
        if (headers == null || headers.isNull() || !headers.isObject()) return;
        Iterator<String> names = headers.fieldNames();
        while (names.hasNext()) {
            String h = names.next();
            JsonNode v = headers.get(h);
            if (v.isArray()) {
                for (JsonNode e : v) b.withHeader(h, e.asText());
            } else {
                b.withHeader(h, v.asText());
            }
        }
    }
}
