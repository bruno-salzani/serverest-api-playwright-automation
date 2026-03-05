import { test, expect } from "@playwright/test";

import { createAuthorizedContext } from "../../utils/auth";
import { createProductPayload } from "../../utils/dataFactory";

test("deve filtrar produtos por nome", async ({ baseURL }) => {
  const api = await createAuthorizedContext(baseURL);
  const payload = createProductPayload({ quantidade: 1 });
  const create = await api.post("/produtos", { data: payload });
  const created = (await create.json()) as { _id: string };
  expect(create.status()).toBe(201);

  const list = await api.get(`/produtos?nome=${encodeURIComponent(payload.nome)}`);
  expect(list.status()).toBe(200);
  const body = (await list.json()) as { produtos: Array<{ _id: string }> };
  const ids = body.produtos?.map((p) => p._id) ?? [];
  expect(ids).toContain(created._id);

  await api.delete(`/produtos/${created._id}`);
  await api.dispose();
});

