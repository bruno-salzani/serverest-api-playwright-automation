import { test, expect, request } from "@playwright/test";

import { createAuthorizedContext } from "../../utils/auth";
import { createContext } from "../../utils/clients/baseApi";
import { CartClient } from "../../utils/clients/cartClient";
import { ProductClient } from "../../utils/clients/productClient";
import { UserClient } from "../../utils/clients/userClient";
import { createProductPayload, createUserPayload } from "../../utils/dataFactory";
 

test("deve criar usuario, autenticar, comprar produto e concluir carrinho", async ({ baseURL }) => {
  const adminApi = await createAuthorizedContext(baseURL);
  const productClient = new ProductClient(adminApi);
  const productPayload = createProductPayload({ quantidade: 5 });
  const createdProduct = await productClient.create(productPayload);
  expect(createdProduct.status).toBe(201);
  const productId = createdProduct.body._id;

  const anonCtx = await request.newContext({ baseURL });
  const userClient = new UserClient(anonCtx);
  const userPayload = createUserPayload({ administrador: "false" });
  const createdUser = await userClient.create(userPayload);
  expect(createdUser.status).toBe(201);

  const login = await userClient.login(userPayload.email, userPayload.password);
  expect(login.status).toBe(200);
  const userToken = login.body.authorization;

  const userCtx = await createContext(baseURL, userToken);
  const cartClient = new CartClient(userCtx);
  const cart = await cartClient.create(productId, 1, userToken);
  expect(cart.status).toBe(201);
  expect(cart.body.message).toMatch(/cadastro realizado com sucesso/i);
  const conclude = await cartClient.conclude(userToken);
  expect(conclude.status).toBe(200);
  expect(conclude.body.message).toMatch(/sucesso/i);

  await anonCtx.dispose();
  await userCtx.dispose();
  await adminApi.delete(`/produtos/${productId}`);
  await adminApi.dispose();
});
