package com.serverest.utils;

import com.serverest.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;

public final class ServeRestContainer {
    private static final Logger LOG = LoggerFactory.getLogger(ServeRestContainer.class);
    private static volatile boolean started = false;
    private static GenericContainer<?> container;
    private static ToxiproxyContainer toxiproxy;
    private static Network network;

    private ServeRestContainer() {
    }

    public static void ensureRunning() {
        if (!Config.get().useTestcontainers()) {
            return;
        }
        if (started) {
            return;
        }
        synchronized (ServeRestContainer.class) {
            if (started) {
                return;
            }
            String image = Config.get().serverestImage();
            if (image == null || image.isBlank()) {
                LOG.warn("'serverest.image' not defined. Skipping container initialization.");
                return;
            }

            network = Network.newNetwork();
            
            // Chaos Testing Support
            toxiproxy = new ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.5.0")
                    .withNetwork(network);
            toxiproxy.start();

            container = new GenericContainer<>(image)
                    .withNetwork(network)
                    .withNetworkAliases("serverest")
                    .withExposedPorts(3000)
                    .waitingFor(Wait.forListeningPort());
            container.start();

            // Direct access (standard)
            int mapped = container.getMappedPort(3000);
            String baseUrl = "http://localhost:" + mapped;
            
            // Proxy access (for chaos testing)
            // We can map a proxy port if needed, but for now we expose direct access as default
            
            System.setProperty("baseUrl", baseUrl);
            Config.reload();
            LOG.info("ServeRest started at {}", baseUrl);
            started = true;
        }
    }
    
    public static ToxiproxyContainer getToxiproxy() {
        return toxiproxy;
    }

    public static void stop() {
        if (container != null) {
            container.stop();
        }
        if (toxiproxy != null) {
            toxiproxy.stop();
        }
        if (network != null) {
            network.close();
        }
    }
}