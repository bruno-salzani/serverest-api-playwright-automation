import { APIRequestContext, request } from "@playwright/test";
import { allure } from "allure-playwright";

export type HttpMethod = "get" | "post" | "put" | "delete";

export const createContext = async (baseURL?: string, token?: string) => {
  const headers: Record<string, string> = {};
  if (token) headers.Authorization = token;
  headers["X-Project-Author"] = "Bruno Salzani";
  return request.newContext({ baseURL, extraHTTPHeaders: headers });
};

export const apiCall = async <T = any>(
  ctx: APIRequestContext,
  method: HttpMethod,
  url: string,
  opts: { data?: unknown; headers?: Record<string, string>; retries?: number } = {}
): Promise<{ status: number; body: T }> => {
  const retries = opts.retries ?? (process.env.CI ? 2 : 0);
  let attempt = 0;
  const caller: (u: string, o: any) => Promise<import("@playwright/test").APIResponse> =
    (ctx as any)[method].bind(ctx);
  while (true) {
    const res = await caller(url, {
      data: opts.data,
      headers: opts.headers
    });
    const status = res.status();
    try {
      const text = await res.text();
      const body = text ? (JSON.parse(text) as T) : ({} as T);
      if (status >= 400) {
        await attachFailure(url, method, opts.data, status, body);
      }
      if ((status === 429 || status >= 500) && attempt < retries) {
        await delay(300 * Math.pow(2, attempt));
        attempt++;
        continue;
      }
      return { status, body };
    } catch {
      if ((status === 429 || status >= 500) && attempt < retries) {
        await delay(300 * Math.pow(2, attempt));
        attempt++;
        continue;
      }
      return { status, body: {} as T };
    }
  }
};

const delay = (ms: number) => new Promise((r) => setTimeout(r, ms));

const attachFailure = async (
  url: string,
  method: string,
  req: unknown,
  status: number,
  res: unknown
) => {
  await allure.attachment(
    `request ${method.toUpperCase()} ${url}`,
    JSON.stringify(req ?? {}, null, 2),
    "application/json"
  );
  await allure.attachment(
    `response ${status} ${method.toUpperCase()} ${url}`,
    JSON.stringify(res ?? {}, null, 2),
    "application/json"
  );
};
