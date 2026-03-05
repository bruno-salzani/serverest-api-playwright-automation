## Como contribuir

1. Requisitos
- Java 17+, Node 18+, Maven (ou Docker)

2. Perfis e tags
- Perfis: `mvn test -Pdev|stage|prod`
- Tags: `mvn test -Dtags=smoke|contract` e `-DexcludeTags=...`

3. Scripts úteis
- Java: `mvn test`, `mvn allure:serve`
- Node: `npm run lint`, `npm run typecheck`, `npm test`

4. Commits
- Conventional Commits (ex.: `feat: add product filters tests`)

5. PRs
- Inclua prints/links de Allure e JaCoCo
