import type { APIRequestContext } from "@playwright/test";
import { apiCall } from "./baseApi";
import type { ProductPayload } from "../dataFactory";

export class ProductClient {
  constructor(private ctx: APIRequestContext) {}

  async create(product: ProductPayload) {
    return apiCall<{ _id: string; message: string }>(this.ctx, "post", "/produtos", {
      data: product
    });
  }

  async delete(id: string, authToken: string) {
    return apiCall<{ message: string }>(this.ctx, "delete", `/produtos/${id}`, {
      headers: { Authorization: authToken }
    });
  }
}
