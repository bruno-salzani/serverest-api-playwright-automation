package com.serverest.utils;

import com.serverest.config.Config;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

import io.qameta.allure.Allure;

public class TracingFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(TracingFilter.class);

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext context) {
        String traceId = MDC.get("correlationId");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString().replace("-", "");
            MDC.put("correlationId", traceId);
        }
        
        // W3C Trace Context (traceparent)
        // version-traceid-parentid-flags
        // 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01
        String parentId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String traceParent = String.format("00-%s-%s-01", traceId, parentId);

        requestSpec.header("traceparent", traceParent);
        requestSpec.header("X-Correlation-ID", traceId);

        LOG.debug("Injected Trace Headers: traceparent={}", traceParent);
        
        if (Config.get().jaegerLinkEnabled()) {
            String base = Config.get().jaegerBaseUrl();
            if (base != null && !base.isBlank()) {
                String jaegerUrl = base.replaceAll("/+$", "") + "/trace/" + traceId;
                Allure.link("Jaeger Trace", jaegerUrl);
            }
        }

        return context.next(requestSpec, responseSpec);
    }
}
