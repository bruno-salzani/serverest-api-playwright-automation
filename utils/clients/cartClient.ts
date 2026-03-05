import type { APIRequestContext } from "@playwright/test";
import { apiCall } from "./baseApi";

export class CartClient {
  constructor(private ctx: APIRequestContext) {}

  async create(productId: string, quantity: number, authToken: string) {
    const payload = { produtos: [{ idProduto: productId, quantidade: quantity }] };
    return apiCall<{ _id: string; message: string }>(this.ctx, "post", "/carrinhos", {
      data: payload,
      headers: { Authorization: authToken }
    });
  }

  async conclude(authToken: string) {
    return apiCall<{ message: string }>(this.ctx, "delete", "/carrinhos/concluir-compra", {
      headers: { Authorization: authToken }
    });
  }

  async cancel(authToken: string) {
    return apiCall<{ message: string }>(this.ctx, "delete", "/carrinhos/cancelar-compra", {
      headers: { Authorization: authToken }
    });
  }
}
