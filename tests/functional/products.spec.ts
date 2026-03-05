import { test, expect } from "@playwright/test";

import { createAuthorizedContext } from "../../utils/auth";
import { createProductPayload } from "../../utils/dataFactory";

test("deve criar editar e excluir produto como admin", async ({ baseURL }) => {
  const api = await createAuthorizedContext(baseURL);
  const payload = createProductPayload();
  const createResponse = await api.post("/produtos", { data: payload });
  const created = (await createResponse.json()) as { _id: string };
  expect(createResponse.status()).toBe(201);
  const updateResponse = await api.put(`/produtos/${created._id}`, {
    data: { ...payload, nome: `${payload.nome} Atualizado` }
  });
  const updateBody = await updateResponse.json();
  expect(updateResponse.status()).toBe(200);
  expect(updateBody.message).toBe("Registro alterado com sucesso");
  const deleteResponse = await api.delete(`/produtos/${created._id}`);
  const deleteBody = await deleteResponse.json();
  expect(deleteResponse.status()).toBe(200);
  expect(deleteBody.message).toBe("Registro excluído com sucesso");
  await api.dispose();
});
