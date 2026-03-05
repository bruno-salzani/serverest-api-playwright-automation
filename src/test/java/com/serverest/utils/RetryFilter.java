package com.serverest.utils;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class RetryFilter implements Filter {
    private final int maxRetries;
    private final long backoffMillis;
    private final Set<String> methods;

    public RetryFilter(int maxRetries, long backoffMillis) {
        this.maxRetries = maxRetries;
        this.backoffMillis = backoffMillis;
        this.methods = parseMethods(System.getProperty("rest.retry.methods", "GET,HEAD"));
    }

    public RetryFilter() {
        this.maxRetries = Integer.parseInt(System.getProperty("rest.retry.maxRetries", "2"));
        this.backoffMillis = Long.parseLong(System.getProperty("rest.retry.backoffMillis", "200"));
        this.methods = parseMethods(System.getProperty("rest.retry.methods", "GET,HEAD"));
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        String method = requestSpec.getMethod() == null ? "" : requestSpec.getMethod().toUpperCase(Locale.ROOT);
        if (!methods.contains(method)) {
            return ctx.next(requestSpec, responseSpec);
        }
        int attempt = 0;
        Response response = ctx.next(requestSpec, responseSpec);
        while (attempt < maxRetries && shouldRetry(response)) {
            attempt++;
            try {
                Thread.sleep(backoffMillis);
            } catch (InterruptedException ignored) {
            }
            response = ctx.next(requestSpec, responseSpec);
        }
        return response;
    }

    private boolean shouldRetry(Response response) {
        int code = response.getStatusCode();
        return code >= 500 || code == 429;
    }

    private static Set<String> parseMethods(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }
}
