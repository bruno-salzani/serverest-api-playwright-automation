package com.serverest.performance;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class BasicSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl(System.getProperty("baseUrl", "https://serverest.dev"))
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder scn = scenario("Carga Constante - Listagem de Usuários")
            .exec(
                    http("Listar Usuários")
                            .get("/usuarios")
                            .check(status().is(200))
                            .check(jmesPath("quantidade").exists())
            )
            .pause(1);

    {
        setUp(
                scn.injectOpen(
                        rampUsers(10).during(10), // Warm-up
                        constantUsersPerSec(5).during(20) // Carga constante
                )
        ).protocols(httpProtocol)
         .assertions(
                 global().responseTime().percentile3().lt(500), // p95 < 500ms
                 global().successfulRequests().percent().gt(99.0) // 99% sucesso
         );
    }
}