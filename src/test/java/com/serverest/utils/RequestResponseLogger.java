package com.serverest.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.serverest.config.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

public class RequestResponseLogger implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(RequestResponseLogger.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String LOG_DIR = Config.get().logDir();
    private static final String LOG_FILE = "restassured.log";

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext context) {
        String correlationId = MDC.get("correlationId");
        boolean putHere = false;
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
            MDC.put("correlationId", correlationId);
            putHere = true;
        }
        try {
            Response response = context.next(requestSpec, responseSpec);
            logRequestResponse(requestSpec, response);
            return response;
        } finally {
            if (putHere) {
                MDC.remove("correlationId");
            }
        }
    }

    private void logRequestResponse(FilterableRequestSpecification requestSpec, Response response) {
        String reqBody = truncate(maskBody(toJsonSafe(requestSpec.getBody())));
        String respBody = truncate(maskBody(response.asString()));
        
        LOG.info("Method: {} | URI: {} | Status: {}", requestSpec.getMethod(), requestSpec.getURI(), response.getStatusCode());
        LOG.debug("Request Body: {}", reqBody);
        LOG.debug("Response Body: {}", respBody);

        writeLogFile(requestSpec, response, reqBody, respBody);
    }

    private void writeLogFile(FilterableRequestSpecification requestSpec, Response response, String reqBody, String respBody) {
        try {
            Path logPath = Path.of(LOG_DIR);
            if (!Files.exists(logPath)) {
                Files.createDirectories(logPath);
            }
            File file = logPath.resolve(LOG_FILE).toFile();
            try (PrintStream out = new PrintStream(new FileOutputStream(file, true), false, StandardCharsets.UTF_8)) {
                out.println("timestamp=" + Instant.now());
                out.println("correlationId=" + MDC.get("correlationId"));
                out.println("method=" + requestSpec.getMethod());
                out.println("uri=" + requestSpec.getURI());
                out.println("requestBody=" + reqBody);
                out.println("status=" + response.getStatusCode());
                out.println("responseBody=" + respBody);
                out.println("---");
            }
        } catch (Exception ignored) {
        }
    }

    private String toJsonSafe(Object body) {
        if (body == null) {
            return "";
        }
        if (body instanceof String) {
            return (String) body;
        }
        try {
            return MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException exception) {
            return body.toString();
        }
    }

    private String maskBody(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        String masked = body.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"");
        masked = masked.replaceAll("\"authorization\"\\s*:\\s*\"[^\"]*\"", "\"authorization\":\"***\"");
        return masked;
    }

    private String truncate(String s) {
        int max = 2000;
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...(truncated)";
    }
}
