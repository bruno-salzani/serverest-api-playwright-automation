package com.serverest.config;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import io.restassured.filter.Filter;
import com.serverest.utils.RequestResponseLogger;
import com.serverest.utils.RetryFilter;
import com.serverest.utils.LatencyRecorder;
import com.serverest.utils.ServeRestContainer;
import com.serverest.utils.TracingFilter;

public final class Specs {

    private Specs() {
    }

    public static RequestSpecification requestSpec() {
        ServeRestContainer.ensureRunning();
        
        Filter latency = (req, res, ctx) -> {
            long start = System.currentTimeMillis();
            var response = ctx.next(req, res);
            LatencyRecorder.record(System.currentTimeMillis() - start);
            return response;
        };

        // Note: SwaggerValidationFilter requires the spec file to be present.
        // We will assume it's in resources/schemas/swagger.json for contract testing.
        // If not present, we should handle gracefully or ensure it is downloaded.
        
        return new RequestSpecBuilder()
                .setBaseUri(Config.getBaseUrl())
                .setContentType(ContentType.JSON)
                .addFilter(new TracingFilter()) // 1. Trace ID first
                .addFilter(new AllureRestAssured())
                .addFilter(new RequestResponseLogger())
                .addFilter(new RetryFilter(Config.get().retryMaxRetries(), Config.get().retryBackoffMillis()))
                .addFilter(latency)
                .build();
    }

    public static ResponseSpecification responseSpec() {
        return new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .build();
    }
}
