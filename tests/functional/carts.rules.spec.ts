import { test, expect, request } from "@playwright/test";

import { createAuthorizedContext } from "../../utils/auth";
import { createContext } from "../../utils/clients/baseApi";
import { CartClient } from "../../utils/clients/cartClient";
import { ProductClient } from "../../utils/clients/productClient";
import { UserClient } from "../../utils/clients/userClient";
import { createProductPayload, createUserPayload } from "../../utils/dataFactory";

test("nao deve permitir dois carrinhos para o mesmo usuario e deve respeitar integridade", async ({ baseURL }) => {
  const adminApi = await createAuthorizedContext(baseURL);
  const productClient = new ProductClient(adminApi);
  const product = createProductPayload({ quantidade: 2 });
  const created = await productClient.create(product);
  expect(created.status).toBe(201);
  const productId = created.body._id;

  const anon = await request.newContext({ baseURL });
  const userClient = new UserClient(anon);
  const user = createUserPayload({ administrador: "false" });
  const createdUser = await userClient.create(user);
  expect(createdUser.status).toBe(201);
  const login = await userClient.login(user.email, user.password);
  expect(login.status).toBe(200);
  const token = login.body.authorization;
  const userCtx = await createContext(baseURL, token);
  const cartClient = new CartClient(userCtx);

  const first = await cartClient.create(productId, 1, token);
  expect(first.status).toBe(201);
  const second = await cartClient.create(productId, 1, token);
  expect(second.status).toBe(400);

  const tryDeleteWhileActive = await adminApi.delete(`/produtos/${productId}`);
  expect(tryDeleteWhileActive.status()).toBe(400);

  const cancel = await cartClient.cancel(token);
  expect(cancel.status).toBe(200);

  const del = await adminApi.delete(`/produtos/${productId}`);
  expect(del.status()).toBe(200);

  await anon.dispose();
  await userCtx.dispose();
  await adminApi.dispose();
});
