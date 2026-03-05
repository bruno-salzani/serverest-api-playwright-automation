# 🗺️ Roadmap de Evolução da Engenharia de Qualidade

Este documento delineia o futuro da nossa estratégia de testes, focado em atingir maturidade de "Chaos Engineering" e "Observabilidade Total".

## 📍 Q3 2026 - Maturidade de Resiliência
- [ ] **Chaos Testing Automatizado**: Implementar Toxiproxy para simular latência de rede e desconexões em pipelines de CI.
- [ ] **Service Mesh Tracing**: Integrar OpenTelemetry com um coletor centralizado (Grafana Tempo) para tracing distribuído real.
- [ ] **Contract Testing Provider**: Implementar verificação do lado do Provider (Serverest) para fechar o ciclo do Pact.

## 📍 Q4 2026 - Test Intelligence
- [ ] **Test Impact Analysis (TIA)**: Implementar execução seletiva de testes baseada no git diff.
- [ ] **Flaky Test Analytics**: Dashboard histórico de testes intermitentes com análise de causa raiz automática.
- [ ] **Synthetic Monitoring**: Scripts de teste rodando periodicamente em produção (Canary).

## 📍 Backlog Técnico
- [ ] Migrar para Java 21 (Virtual Threads) para performance massiva nos testes de carga.
- [ ] Implementar validação visual (Applitools/Percy) para relatórios HTML.
- [ ] Containerizar o ambiente de desenvolvimento com DevContainers.
