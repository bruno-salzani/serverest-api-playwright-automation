import { request, APIRequestContext } from "@playwright/test";
import fs from "fs/promises";
import path from "path";

const authFilePath = path.resolve(".auth", "token.json");

type TokenState = {
  token: string;
  userId: string;
  email: string;
  password: string;
};

export const readTokenState = async (): Promise<TokenState> => {
  const raw = await fs.readFile(authFilePath, "utf-8");
  return JSON.parse(raw) as TokenState;
};

export const createAuthorizedContext = async (
  baseURL?: string
): Promise<APIRequestContext> => {
  const { token } = await readTokenState();
  return request.newContext({
    baseURL,
    extraHTTPHeaders: {
      Authorization: token,
      "X-Project-Author": "Bruno Salzani"
    }
  });
};
