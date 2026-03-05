import { test, expect, request } from "@playwright/test";

test("nao deve permitir cadastro de produto para usuario comum", async ({ baseURL }) => {
  const api = await request.newContext({ baseURL });
  const user = {
    nome: "Usuario Comum",
    email: `usuario.comum+${Date.now()}@teste.com`,
    password: "teste123",
    administrador: "false"
  };
  await api.post("/usuarios", { data: user });
  const loginResponse = await api.post("/login", {
    data: { email: user.email, password: user.password }
  });
  const loginBody = (await loginResponse.json()) as { authorization: string };
  const userApi = await request.newContext({
    baseURL,
    extraHTTPHeaders: { Authorization: loginBody.authorization }
  });
  const resp = await userApi.post("/produtos", {
    data: { nome: "Produto não autorizado", preco: 100, descricao: "x", quantidade: 1 }
  });
  const body = await resp.json();
  expect(resp.status()).toBe(403);
  expect(`${body.message}`.toLowerCase()).toMatch(/exclusiva.*administradores|admin/);
  await userApi.dispose();
  await api.dispose();
});

