package com.serverest.config;

import org.aeonbits.owner.Config;

@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
        "system:properties",
        "system:env",
        "classpath:config.properties"
})
public interface ServerestConfig extends Config {

    @Key("baseUrl")
    @DefaultValue("https://serverest.dev")
    String baseUrl();

    @Key("jaeger.baseUrl")
    @DefaultValue("http://localhost:16686")
    String jaegerBaseUrl();

    @Key("jaeger.linkEnabled")
    @DefaultValue("true")
    boolean jaegerLinkEnabled();

    @Key("run.id")
    String runId();

    @Key("janitor.enabled")
    @DefaultValue("true")
    boolean janitorEnabled();

    @Key("use.testcontainers")
    @DefaultValue("false")
    boolean useTestcontainers();

    @Key("serverest.image")
    String serverestImage();

    @Key("log.dir")
    @DefaultValue("target/logs")
    String logDir();

    @Key("rest.retry.maxRetries")
    @DefaultValue("2")
    int retryMaxRetries();

    @Key("rest.retry.backoffMillis")
    @DefaultValue("200")
    long retryBackoffMillis();

    @Key("latency.p95.max")
    @DefaultValue("2000")
    long latencyP95Max();

    @Key("latency.p99.max")
    @DefaultValue("3000")
    long latencyP99Max();
}
