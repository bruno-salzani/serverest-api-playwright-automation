import { test, expect, request } from "@playwright/test";

import { createAuthorizedContext } from "../../utils/auth";
import { createUserPayload } from "../../utils/dataFactory";

test("deve cadastrar usuario com sucesso", async ({ baseURL }) => {
  const api = await request.newContext({ baseURL });
  const payload = createUserPayload();
  const response = await api.post("/usuarios", { data: payload });
  const body = await response.json();
  expect(response.status()).toBe(201);
  expect(body.message).toBe("Cadastro realizado com sucesso");
  expect(body._id).toBeTruthy();
  await api.dispose();
});

test("nao deve cadastrar usuario com email duplicado", async ({ baseURL }) => {
  const api = await request.newContext({ baseURL });
  const payload = createUserPayload();
  await api.post("/usuarios", { data: payload });
  const response = await api.post("/usuarios", { data: payload });
  const body = await response.json();
  expect(response.status()).toBe(400);
  expect(body.message).toBe("Este email já está sendo usado");
  await api.dispose();
});

test("deve atualizar e remover usuario", async ({ baseURL }) => {
  const api = await request.newContext({ baseURL });
  const payload = createUserPayload({ administrador: "false" });
  const createResponse = await api.post("/usuarios", { data: payload });
  const created = (await createResponse.json()) as { _id: string };
  const authorized = await createAuthorizedContext(baseURL);
  const updateResponse = await authorized.put(`/usuarios/${created._id}`, {
    data: { ...payload, nome: `${payload.nome} Atualizado` }
  });
  const updateBody = await updateResponse.json();
  expect(updateResponse.status()).toBe(200);
  expect(updateBody.message).toBe("Registro alterado com sucesso");
  const deleteResponse = await authorized.delete(`/usuarios/${created._id}`);
  const deleteBody = await deleteResponse.json();
  expect(deleteResponse.status()).toBe(200);
  expect(deleteBody.message).toBe("Registro excluído com sucesso");
  await authorized.dispose();
  await api.dispose();
});
