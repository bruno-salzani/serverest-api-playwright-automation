# Arquitetura de Testes Avançada (Staff Engineer Level)

Este documento detalha as decisões de design, padrões e estratégia técnica aplicados na automação de testes do ServeRest. A arquitetura foi desenhada para suportar escala, manutenibilidade e observabilidade em ambientes de Big Tech.

## 🏗️ Padrões de Design Aplicados

### 1. Service Object Pattern (DSL)
Abstraímos a complexidade do `RestAssured` em uma camada de `Client`.
- **Objetivo**: Separar a lógica de teste ("o que testar") da implementação HTTP ("como testar").
- **Implementação**: Classes em `com.serverest.client` (ex: `UserClient`, `ProductClient`).
- **Benefício**: Testes legíveis como linguagem natural e reutilização de chamadas.

### 2. Data Factory Pattern (Fluent Builder)
Centralizamos a criação de massa de dados em Factories estáticas.
- **Objetivo**: Garantir dados válidos, dinâmicos e isolados por thread.
- **Implementação**: Classes em `com.serverest.factory` (ex: `UserFactory`).
- **Benefício**: Elimina duplicação de `Faker` nos testes e previne *Flaky Tests* por colisão de dados (uso de UUID nos e-mails).

### 3. Singleton Configuration (Owner)
Gerenciamento de configuração centralizado e tipado.
- **Implementação**: `ServerestConfig` interface com `ConfigFactory`.
- **Benefício**: Suporte nativo a variáveis de ambiente, properties e override por linha de comando.

---

## 🔍 Estratégia de Qualidade (Test Strategy)

A suíte segue uma pirâmide de testes invertida focada em API, complementada por verificações não-funcionais.

| Camada | Ferramenta | Cobertura |
| :--- | :--- | :--- |
| **Funcional (E2E)** | JUnit 5 + RestAssured | Fluxos críticos (Carrinho, Compra, Cadastro). |
| **Contrato** | Swagger Validator | Validação dinâmica contra `openapi.json` oficial. |
| **Performance** | Gatling Java SDK | Testes de carga (Ramp-up + Throughput constante). |
| **Segurança** | OWASP Dependency Check | Análise estática de vulnerabilidades (SAST). |
| **Observabilidade** | Custom Tracing Filter | Injeção de W3C Trace Context (`traceparent`). |

---

## 🚀 Pipeline de CI/CD (GitHub Actions)

O pipeline foi otimizado para feedback rápido e segurança (Shift-Left).

1.  **Matrix Strategy**: Execução paralela de suítes (`Users`, `Products`, `Cart`) em containers distintos.
2.  **Caching**: Cache inteligente do Maven (`~/.m2`) para builds ultra-rápidos.
3.  **Quality Gates**:
    - **Latência**: Falha se p95 > 500ms.
    - **Cobertura**: Falha se branch coverage < 70%.
    - **Segurança**: Falha se encontrar CVEs críticas.

---

## 📊 Observabilidade e Debugging

### Distributed Tracing
Implementamos um `TracingFilter` que simula um Sidecar (Service Mesh).
- **Header**: `traceparent` (W3C Standard).
- **Correlation ID**: `X-Correlation-ID` único por request.
- **Logs**: Logs estruturados com MDC (Mapped Diagnostic Context) para rastrear a jornada do request.

### Relatórios
- **Allure**: Histórico de execução e evidências ricas.
- **JaCoCo**: Mapa de calor de cobertura de código.
- **Gatling**: Gráficos detalhados de latência e vazão.

---

## 🛠️ Tecnologias
- **Java 17 LTS**
- **RestAssured 5.x**
- **JUnit 5**
- **Gatling 3.10**
- **Testcontainers**
- **Lombok**
- **Owner**
- **SLF4J + Logback**
