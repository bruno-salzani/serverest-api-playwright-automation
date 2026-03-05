# ServeRest API Automation (Playwright + TypeScript) &middot; by Bruno Salzani

Suite de testes de API para o ServeRest focada em engenharia de qualidade, confiabilidade e velocidade de feedback. Construída com Playwright Test (APIRequestContext), escrita em TypeScript, com relatórios Allure e validações de contrato via JSON Schema (Ajv).

Principais pilares:
- Testes funcionais e de fluxo ponta a ponta de APIs.
- Validação de contratos com JSON Schema.
- Geração de dados dinâmica com Faker.
- Relatórios Allure prontos para CI.
- Pipeline simples, rápido e confiável (GitHub Actions).

Autor: Bruno Salzani

---

## Requisitos
- Node.js 18+
- NPM 9+
 - Java 17+ (apenas para a suíte Java opcional)

Opcional:
- Docker (para executar ServeRest local via docker-compose)
- Java não é necessário para esta suíte

---

## Instalação
- Instale dependências:

```bash
npm install
```

- (Opcional) Instale navegadores do Playwright:

```bash
npx playwright install --with-deps
```

---

## Ambiente Local (Opcional)
- Suba o ServeRest via Docker Compose:

```bash
docker compose up -d serverest
```

- Aponte a suíte para o ambiente local:

```bash
set BASE_URL=http://localhost:3000    # Windows (PowerShell use: $env:BASE_URL="http://localhost:3000")
export BASE_URL=http://localhost:3000 # Linux/macOS
npm test
```

---

## Execução
- Testes em linha de comando:

```bash
npm test
```

- Modo UI do Playwright:

```bash
npm run test:ui
```

- Lint e Typecheck:

```bash
npm run lint
npm run typecheck
```

### Suíte Java (Opcional)
- Requisitos: Java 17+, Maven Wrapper (incluso)
- Executar testes:

```bash
# Windows
.\mvnw.cmd -q test

# Linux/macOS
./mvnw -q test
```

- Gerar relatório Allure da suíte Java:

```bash
# Windows
.\mvnw.cmd allure:report

# Linux/macOS
./mvnw allure:report
```

- Notas:
  - O pom usa JUnit 5, Rest-Assured, Allure, e plugins de qualidade (Jacoco, Checkstyle, SpotBugs).
  - Perf (Gatling) está configurado via plugin; use quando necessário:
    - Windows: .\mvnw.cmd gatling:test
    - Linux/macOS: ./mvnw gatling:test

---

## Relatórios
- Allure (gera e abre relatório local):

```bash
npm run report:allure
```

Artefatos brutos são salvos em allure-results/ a cada execução.

---

## Configuração
- Variáveis de ambiente:
  - BASE_URL: URL base da API (padrão: https://serverest.dev). Pode ser definida em .env.

Exemplo .env:
```
BASE_URL=https://serverest.dev
```

---

## Estrutura do Projeto
```
tests/
  contract/               # Validações de contrato (JSON Schema + Ajv)
  functional/             # Testes funcionais de endpoints
  e2e/                    # Fluxos ponta a ponta
schemas/                  # JSON Schemas de respostas
utils/                    # Fábricas de dados, auth helpers, validators
global-setup.ts           # Criação e login de usuário admin; salva token
playwright.config.ts      # Configuração do Playwright
```

Arquivos de destaque:
- [playwright.config.ts](file:///d:/Projects/serverest-api-playwright-automation/playwright.config.ts)
- [global-setup.ts](file:///d:/Projects/serverest-api-playwright-automation/global-setup.ts)
- [utils/dataFactory.ts](file:///d:/Projects/serverest-api-playwright-automation/utils/dataFactory.ts)
- [utils/auth.ts](file:///d:/Projects/serverest-api-playwright-automation/utils/auth.ts)

---

## Pipelines (CI/CD)
Workflow principal:
- Instala dependências com cache.
- Executa lint e typecheck.
- Instala navegadores do Playwright (compatibilidade).
- Roda testes com BASE_URL configurável via GitHub Variables.
- Publica artefatos do Allure.
- Executa suíte Java e publica os Allure da suíte Java.

Arquivo: [.github/workflows/main.yml](file:///d:/Projects/serverest-api-playwright-automation/.github/workflows/main.yml)

Performance (Smoke):
- Workflow opcional roda um smoke de performance com k6 contra BASE_URL.
- Arquivo: [.github/workflows/perf.yml](file:///d:/Projects/serverest-api-playwright-automation/.github/workflows/perf.yml)
- Script: [perf/k6-smoke.js](file:///d:/Projects/serverest-api-playwright-automation/perf/k6-smoke.js)

Configuração no GitHub:
- Defina a variável de repositório BASE_URL em Settings → Secrets and variables → Actions → Variables.
- Artefatos do Allure ficam disponíveis em cada execução do workflow (aba “Artifacts”).
- Para badge do CI, use:  
  https://github.com/OWNER/REPO/actions/workflows/main.yml/badge.svg  
  Substitua OWNER/REPO pelo seu repositório.

Links úteis (após rodar o CI):
- Allure (Playwright): Artifact “allure-results”
- Allure (Java): Artifact “java-allure-results”

---

## Boas Práticas Implementadas
- Reutilização de contexto autenticado via token (.auth/token.json).
- Geração de massa com Faker e nomes únicos.
- Schemas JSON simples e estritos o suficiente para evitar regressões sem flakiness.
- Tempo de execução e SLAs validados em alguns testes sensíveis (ex.: < 2s).
- ESLint com regras de imports e remoção de imports não utilizados.
- TypeScript estrito (strict: true).
- Camada de Clients para APIs (User/Product/Cart) com retry inteligente e evidências Allure em falhas.
- Validação de variáveis de ambiente com Zod (fail-fast, mensagens claras).
- Script de Drift Detection para sincronismo com OpenAPI do ServeRest.
- Retries adaptativos em CI (retries: 2).

---

## Dicas de Troubleshooting
- Erros 429/5xx esporádicos do ambiente público do ServeRest podem ocorrer. Reexecute.
- Se o Allure não abrir automaticamente, verifique se o Java está instalado localmente para o allure CLI. No CI, apenas os artefatos brutos são publicados.
- Para isolar do ambiente público, considere usar o docker-compose incluso para subir um ServeRest local.

---

## Camada de Clients (Abstração de API)
- Motivação: evitar acoplamento direto aos endpoints em testes. Se a rota mudar, você altera o Client, não 10 testes.
- Local: utils/clients/
  - [baseApi.ts](file:///d:/Projects/serverest-api-playwright-automation/utils/clients/baseApi.ts): criação de contexto e `apiCall` com retries 429/5xx e anexos Allure automáticos em falha.
  - [userClient.ts](file:///d:/Projects/serverest-api-playwright-automation/utils/clients/userClient.ts)
  - [productClient.ts](file:///d:/Projects/serverest-api-playwright-automation/utils/clients/productClient.ts)
  - [cartClient.ts](file:///d:/Projects/serverest-api-playwright-automation/utils/clients/cartClient.ts)

Exemplo de uso (já aplicado em um E2E):
```
const productClient = new ProductClient(adminApi);
const created = await productClient.create(productPayload);
expect(created.status).toBe(201);
```

---

## Validação de Env Vars (Zod)
- Em [playwright.config.ts](file:///d:/Projects/serverest-api-playwright-automation/playwright.config.ts), as variáveis são validadas com Zod.
- Padrões:
  - BASE_URL: default https://serverest.dev
  - API_TIMEOUT: default 30000

---

## Drift Detection (OpenAPI)
- Baixa o swagger oficial e verifica compatibilidade básica com os Schemas principais.
- Comando:

```bash
npm run drift:check
```

- Saída não afeta a execução da suíte, mas ajuda a detectar mudanças na API cedo.

---

## Convenções de Qualidade e Commits
- Lint: ESLint v9 com flat config (remoção de imports não utilizados e ordenação).
- Tipagem: TypeScript strict.
- Hooks: Husky executa lint e typecheck no pre-commit.
- Commits: commitlint (padrão convencional). Exemplos:
  - feat(api): adiciona validação de contrato de login
  - fix(users): corrige mensagem ao excluir usuário

---

## Scripts Disponíveis
- test: Executa a suíte Playwright.
- test:ui: Abre a UI do Playwright.
- lint: Lint em arquivos de testes TypeScript.
- typecheck: Checagem de tipos.
- report:allure: Gera e abre o relatório Allure localmente.
- drift:check: Verifica “drift” contra o OpenAPI do ServeRest.

---

## Créditos
Projeto criado por Bruno Salzani, para demonstrar práticas modernas de automação de testes de API com Playwright, priorizando clareza, confiabilidade e feedback rápido.
