<<<<<<< HEAD
# 🚀 API Test Automation – ServeRest (Playwright + TypeScript)
[![CI](https://github.com/OWNER/REPO/actions/workflows/main.yml/badge.svg)](https://github.com/OWNER/REPO/actions/workflows/main.yml)
[![Allure Pages](https://img.shields.io/badge/Allure-Pages-blueviolet)](https://OWNER.github.io/REPO/)
=======
# ServeRest API Automation (Playwright + TypeScript) &middot;
>>>>>>> bdfd5f2559e1d171e3cbf1f9b6e0e611be903ac2

![Status](https://img.shields.io/badge/Status-Ativo-brightgreen)
![Framework](https://img.shields.io/badge/Framework-Playwright-green)
![Stack](https://img.shields.io/badge/Stack-Node.js%20%7C%20TypeScript-blue)
![Relatórios](https://img.shields.io/badge/Reports-Allure-purple)
![Java](https://img.shields.io/badge/Java-Su%C3%ADte%20Opcional-yellow)

Automação de APIs e fluxo E2E para o ServeRest, priorizando confiabilidade, leitura do código e execução reprodutível. A suíte principal usa Playwright Test com APIRequestContext; há também uma suíte Java opcional (JUnit5 + RestAssured) e integração Allure em ambas.

---

# 🎯 Objetivo do Projeto

Garantir a qualidade dos principais fluxos e endpoints:
- Autenticação (positivo e negativo) e proteção de rotas
- Usuários: cadastro, duplicidade, atualização e exclusão
- Produtos: cadastro (admin), filtros e CRUD
- Carrinho: criação, regras de integridade, conclusão/cancelamento
- Contratos: validação com JSON Schema (Ajv)
- Relatórios Allure com evidências úteis

Foco em:
- Feedback rápido e determinístico
- Abstração via Clients para reduzir acoplamento
+- Fail‑fast de variáveis de ambiente
- Reaproveitamento de dados e estabilidade (retries)

---

# 🧠 Estratégia e Arquitetura

Camadas principais:
1. API Clients (utils/clients) para Usuarios/Produtos/Carrinho
2. Fábrica de dados (utils/dataFactory) com Faker
3. Validações de contrato (schemas + Ajv)
4. Configuração central (playwright.config.ts) com validação Zod
5. Global Setup (global-setup.ts) para provisionar e salvar token admin
6. Retries inteligentes no client (429/5xx) e no CI
7. Drift Detection (scripts/drift-detect.mjs) comparando schemas com OpenAPI
8. Suíte Java (JUnit5 + RestAssured) com Allure e plugins de qualidade

Diretrizes técnicas:
- Chaves estáveis em payloads/schemas
- Assertivas claras e mensagens descritivas
- Sincronização nativa do Playwright (sem waits artificiais)
- Execução headless e em interface (UI Mode)

---

# 🔄 Fluxos Cobertos

1. Autenticação
   - Sucesso retorna token
   - Negativo recusa credenciais inválidas
   - Proteção de rota sem token
2. Usuários
   - Cadastro com sucesso + duplicidade
   - Atualização e exclusão
3. Produtos
   - Cadastro somente admin
   - Filtros por nome
4. Carrinho
   - Criação e conclusão/cancelamento
   - Regras: um carrinho por usuário e integridade (bloqueio de exclusão de produto ativo)
5. Contratos
   - Login, usuários, produtos, carrinho (criação e conclusão)

---

# 📁 Estrutura do Projeto

```
tests/
  contract/                # Validações de contrato (JSON Schema + Ajv)
  functional/              # Testes funcionais de endpoints
  e2e/                     # Fluxos ponta a ponta
schemas/                   # JSON Schemas de respostas
utils/                     # Clients, fábricas de dados, auth helpers, validators
scripts/                   # Drift detection (OpenAPI)
global-setup.ts            # Cria usuário admin e salva token
playwright.config.ts       # Config Playwright + validação Zod

src/test/java/             # Suíte Java opcional (JUnit5 + RestAssured)
.github/workflows/         # CI (Playwright + Java + perf opcional)
```

Arquivos de destaque:
- [playwright.config.ts](file:///d:/Projects/serverest-api-playwright-automation/playwright.config.ts)
- [global-setup.ts](file:///d:/Projects/serverest-api-playwright-automation/global-setup.ts)
- [utils/clients/baseApi.ts](file:///d:/Projects/serverest-api-playwright-automation/utils/clients/baseApi.ts)
- [utils/dataFactory.ts](file:///d:/Projects/serverest-api-playwright-automation/utils/dataFactory.ts)
- [utils/auth.ts](file:///d:/Projects/serverest-api-playwright-automation/utils/auth.ts)

---

# ⚙️ Funcionalidades Automatizadas

## Autenticação
- Geração e uso de token admin no setup
- Cenários positivo, negativo e rota protegida

## Usuários e Produtos
- CRUD essencial para usuários
- Produtos com restrição de admin e filtros

## Carrinho
- Criação, regras (um por usuário) e conclusão/cancelamento
- Bloqueio de exclusão de produto com carrinho ativo

## Contratos (Ajv)
- Schemas em /schemas; validação com mensagens de erro claras

## Observabilidade
- Allure com anexos de request/response em falhas
- Environment com Author (Bruno Salzani) nos relatórios

---

# 🧪 Boas Práticas

- Abstração de chamadas via Clients (User/Product/Cart)
- Schemas JSON revisáveis e simples o bastante para estabilidade
- Zod para fail‑fast de variáveis (BASE_URL, API_TIMEOUT)
- Retries para 429/5xx (client + CI)
- ESLint (imports) e TypeScript strict
- Drift Detection contra OpenAPI

---

# 📊 Riscos e Mitigações

- Flakiness por instabilidade pública do ServeRest → retries 429/5xx e CI com tentativas
- Deriva entre schema local e API real → script de drift detection
- Ambientes variáveis → BASE_URL configurável; suporte a docker-compose local

---

# 🛠️ Tecnologias

- Playwright Test (API)
- Node.js + TypeScript
- Ajv (JSON Schema), Zod (Env), Faker
- Allure (reports)
- GitHub Actions (CI)
- Java opcional: JUnit5, RestAssured, Allure, Gatling (perf)

---

# ▶️ Como Executar

1) Instalar dependências
```
npm install
```

2) (Opcional) Instalar navegadores Playwright
```
npx playwright install --with-deps
```

3) Executar suíte (CLI)
```
npm test
```

4) Modo interface (UI)
```
npm run test:ui
```

5) Lint e Typecheck
```
npm run lint
npm run typecheck
```

6) Ambiente local (opcional)
```
docker compose up -d serverest
# Windows PowerShell:
$env:BASE_URL="http://localhost:3000"; npm test
# Linux/macOS:
BASE_URL=http://localhost:3000 npm test
```

### Suíte Java (Opcional)
- Requisitos: Java 17+
- Executar:
```
# Windows
.\mvnw.cmd -q test
# Linux/macOS
./mvnw -q test
```
- Relatório Allure (Java):
```
.\mvnw.cmd allure:report     # Windows
./mvnw allure:report         # Linux/macOS
```

---

# 📄 Relatórios

- Allure (local):
```
npm run report:allure
```
- Artefatos são gerados em allure-results/; no CI os resultados de Playwright e Java são publicados como artefatos separados.

---

# 📦 Pipelines (CI/CD)

Workflow principal:
- Node.js com cache de dependências
- Lint e typecheck
- Instala browsers do Playwright
- Roda testes Playwright com BASE_URL de GitHub Variables
- Publica Allure (Playwright)
- Roda suíte Java e publica Allure (Java)

Arquivo: [.github/workflows/main.yml](file:///d:/Projects/serverest-api-playwright-automation/.github/workflows/main.yml)

Performance (Smoke):
- Workflow opcional com k6: [.github/workflows/perf.yml](file:///d:/Projects/serverest-api-playwright-automation/.github/workflows/perf.yml)
- Script: [perf/k6-smoke.js](file:///d:/Projects/serverest-api-playwright-automation/perf/k6-smoke.js)

Configuração no GitHub:
- Defina a variável BASE_URL em Settings → Secrets and variables → Actions → Variables
- Artefatos:
  - “allure-results” (Playwright)
  - “java-allure-results” (Java)
- Badge de CI:
  https://github.com/OWNER/REPO/actions/workflows/main.yml/badge.svg (substitua OWNER/REPO)

---

# 🤝 Conclusão

Automação consistente dos fluxos essenciais do ServeRest com foco em clareza, manutenção simples e execução confiável em diferentes ambientes.

Autor: Bruno Salzani
