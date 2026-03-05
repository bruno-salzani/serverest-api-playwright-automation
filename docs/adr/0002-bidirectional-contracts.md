# ADR 0002: Pact Bi-Direcional validado contra OpenAPI

Status: Aceito

Contexto
- Tests Pact Consumer podem ficar “verdes” mesmo quando o Swagger muda silenciosamente.
- Queremos garantir compatibilidade estrita entre contratos e a especificação oficial do ServeRest.

Decisão
- Adicionar um teste JUnit que varre `target/pacts/*.json` e valida:
  - Path existe no OpenAPI (`schemas/swagger.json`).
  - Método está definido para o path.
  - Status de resposta é permitido pela especificação.
- Sem dependência de Broker para essa verificação local.

Consequências
- Quebra precoce no PR quando houver divergência de contrato.
- Evolução segura do provider com feedback imediato ao consumidor.

