import { test, expect, request } from "@playwright/test";

import userCreateSchema from "../../schemas/user-create.schema.json";
import usersListSchema from "../../schemas/users-list.schema.json";
import { createUserPayload } from "../../utils/dataFactory";
import { validateSchema } from "../../utils/schemaValidator";

test("deve validar contrato de listagem de usuarios", async ({ baseURL }) => {
  const api = await request.newContext({ baseURL });
  const startedAt = Date.now();
  const response = await api.get("/usuarios");
  const elapsed = Date.now() - startedAt;
  const body = await response.json();
  const { valid, errors } = validateSchema(usersListSchema, body);
  expect(response.status()).toBe(200);
  expect(valid, JSON.stringify(errors)).toBe(true);
  expect(elapsed).toBeLessThan(2000);
  await api.dispose();
});

test("deve validar contrato de cadastro de usuario", async ({ baseURL }) => {
  const api = await request.newContext({ baseURL });
  const payload = createUserPayload();
  const startedAt = Date.now();
  const response = await api.post("/usuarios", { data: payload });
  const elapsed = Date.now() - startedAt;
  const body = await response.json();
  const { valid, errors } = validateSchema(userCreateSchema, body);
  expect(response.status()).toBe(201);
  expect(valid, JSON.stringify(errors)).toBe(true);
  expect(elapsed).toBeLessThan(2000);
  await api.dispose();
});
