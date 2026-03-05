import { test, expect, request } from "@playwright/test";

import { createUserPayload } from "../../utils/dataFactory";

test("deve autenticar com sucesso e retornar token", async ({ baseURL }) => {
  const api = await request.newContext({ baseURL });
  const user = createUserPayload({ administrador: "true" });
  await api.post("/usuarios", { data: user });
  const response = await api.post("/login", {
    data: { email: user.email, password: user.password }
  });
  const body = await response.json();
  expect(response.status()).toBe(200);
  expect(body.authorization).toBeTruthy();
  await api.dispose();
});

test("nao deve permitir acesso a rota protegida sem token", async ({ baseURL }) => {
  const api = await request.newContext({ baseURL });
  const response = await api.post("/produtos", {
    data: {
      nome: "Produto bloqueado",
      preco: 100,
      descricao: "Sem token",
      quantidade: 1
    }
  });
  const body = await response.json();
  expect(response.status()).toBe(401);
  expect(body.message).toContain("Token");
  await api.dispose();
});
