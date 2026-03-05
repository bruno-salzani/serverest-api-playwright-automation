import { test, expect, request } from "@playwright/test";

import productCreateSchema from "../../schemas/product-create.schema.json";
import productsListSchema from "../../schemas/products-list.schema.json";
import { createAuthorizedContext } from "../../utils/auth";
import { createProductPayload } from "../../utils/dataFactory";
import { validateSchema } from "../../utils/schemaValidator";

test("deve validar contrato de listagem de produtos", async ({ baseURL }) => {
  const api = await request.newContext({ baseURL });
  const response = await api.get("/produtos");
  const body = await response.json();
  const { valid, errors } = validateSchema(productsListSchema, body);
  expect(response.status()).toBe(200);
  expect(valid, JSON.stringify(errors)).toBe(true);
  await api.dispose();
});

test("deve validar contrato de cadastro de produto (admin)", async ({ baseURL }) => {
  const api = await createAuthorizedContext(baseURL);
  const payload = createProductPayload();
  const response = await api.post("/produtos", { data: payload });
  const body = await response.json();
  const { valid, errors } = validateSchema(productCreateSchema, body);
  expect(response.status()).toBe(201);
  expect(valid, JSON.stringify(errors)).toBe(true);
  // cleanup
  if (body?._id) {
    await api.delete(`/produtos/${body._id}`);
  }
  await api.dispose();
});

