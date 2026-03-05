import type { APIRequestContext } from "@playwright/test";
import { apiCall } from "./baseApi";
import type { UserPayload } from "../dataFactory";

export class UserClient {
  constructor(private ctx: APIRequestContext) {}

  async create(user: UserPayload) {
    return apiCall<{ _id: string; message: string }>(this.ctx, "post", "/usuarios", {
      data: user
    });
  }

  async login(email: string, password: string) {
    return apiCall<{ authorization: string }>(this.ctx, "post", "/login", {
      data: { email, password }
    });
  }
}
