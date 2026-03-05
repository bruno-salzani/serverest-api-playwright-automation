import { test, expect, request } from "@playwright/test";

import cartConcludeSchema from "../../schemas/cart-conclude.schema.json";
import cartCreateSchema from "../../schemas/cart-create.schema.json";
import { createAuthorizedContext } from "../../utils/auth";
import { createProductPayload, createUserPayload } from "../../utils/dataFactory";
import { validateSchema } from "../../utils/schemaValidator";

test("deve validar contrato de criacao e conclusao de carrinho", async ({ baseURL }) => {
  const admin = await createAuthorizedContext(baseURL);
  const payload = createProductPayload({ quantidade: 3 });
  const productResp = await admin.post("/produtos", { data: payload });
  const product = (await productResp.json()) as { _id: string };
  expect(productResp.status()).toBe(201);

  const anon = await request.newContext({ baseURL });
  const user = createUserPayload({ administrador: "false" });
  await anon.post("/usuarios", { data: user });
  const login = await anon.post("/login", {
    data: { email: user.email, password: user.password }
  });
  const token = ((await login.json()) as { authorization: string }).authorization;
  const userCtx = await request.newContext({
    baseURL,
    extraHTTPHeaders: { Authorization: token }
  });

  const createCart = await userCtx.post("/carrinhos", {
    data: { produtos: [{ idProduto: product._id, quantidade: 1 }] }
  });
  const createBody = await createCart.json();
  const createVal = validateSchema(cartCreateSchema, createBody);
  expect(createCart.status()).toBe(201);
  expect(createVal.valid, JSON.stringify(createVal.errors)).toBe(true);

  const conclude = await userCtx.delete("/carrinhos/concluir-compra");
  const concludeBody = await conclude.json();
  const concludeVal = validateSchema(cartConcludeSchema, concludeBody);
  expect(conclude.status()).toBe(200);
  expect(concludeVal.valid, JSON.stringify(concludeVal.errors)).toBe(true);

  await userCtx.dispose();
  await anon.dispose();
  await admin.delete(`/produtos/${product._id}`);
  await admin.dispose();
});
