import { test, expect, request } from "@playwright/test";

import { createUserPayload } from "../../utils/dataFactory";

test("nao deve autenticar com credenciais invalidas", async ({ baseURL }) => {
  const api = await request.newContext({ baseURL });
  const user = createUserPayload({ administrador: "true" });
  await api.post("/usuarios", { data: user });
  const response = await api.post("/login", {
    data: { email: user.email, password: `${user.password}_errado` }
  });
  const body = await response.json();
  expect(response.status()).toBe(401);
  expect(`${body.message}`.toLowerCase()).toMatch(/inválid|inval/i);
  await api.dispose();
});

