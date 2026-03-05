import { test, expect, request } from "@playwright/test";

import loginSchema from "../../schemas/login.schema.json";
import { createUserPayload } from "../../utils/dataFactory";
import { validateSchema } from "../../utils/schemaValidator";

test("deve validar contrato de login", async ({ baseURL }) => {
  const api = await request.newContext({ baseURL });
  const user = createUserPayload({ administrador: "true" });
  await api.post("/usuarios", { data: user });
  const startedAt = Date.now();
  const response = await api.post("/login", {
    data: { email: user.email, password: user.password }
  });
  const elapsed = Date.now() - startedAt;
  const body = await response.json();
  const { valid, errors } = validateSchema(loginSchema, body);
  expect(response.status()).toBe(200);
  expect(valid, JSON.stringify(errors)).toBe(true);
  expect(elapsed).toBeLessThan(2000);
  await api.dispose();
});
