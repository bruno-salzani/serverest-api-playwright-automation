# ADR 0001: Deep Linking Jaeger no Allure

Status: Aceito

Contexto
- Já utilizamos `traceparent` (W3C) para rastreabilidade.
- Em falhas no CI, desenvolvedores precisavam abrir o Jaeger e pesquisar manualmente pelo trace.

Decisão
- Capturar `traceId` no `TracingFilter` e anexar um link clicável no Allure.
- Base do Jaeger é configurável via `jaeger.baseUrl` (Owner).
- Link é habilitado por `jaeger.linkEnabled=true`.

Consequências
- MTTD menor: clique direto no trace exato da execução.
- Padronização: o `traceId` é propagado via MDC para outros filtros/logs.

Config
- Propriedades:
  - `jaeger.baseUrl` (default: `http://localhost:16686`)
  - `jaeger.linkEnabled` (default: `true`)

