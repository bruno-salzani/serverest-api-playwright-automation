import { request, APIRequestContext } from "@playwright/test";
import fs from "fs/promises";
import path from "path";
import dotenv from "dotenv";
import { createUserPayload } from "./utils/dataFactory";

dotenv.config();

type LoginResponse = {
  authorization: string;
  message: string;
};

const baseURL = process.env.BASE_URL ?? "https://serverest.dev";

const ensureAuthDir = async () => {
  await fs.mkdir(path.resolve(".auth"), { recursive: true });
};

const createAdminUser = async () => {
  const api = await request.newContext({ baseURL });
  const payload = createUserPayload({ administrador: "true" });
  const response = await api.post("/usuarios", { data: payload });
  const body = (await response.json()) as { _id: string };
  return { api, payload, userId: body._id };
};

const loginAdmin = async (
  api: APIRequestContext,
  email: string,
  password: string
) => {
  const response = await api.post("/login", { data: { email, password } });
  const body = (await response.json()) as LoginResponse;
  return body.authorization;
};

const globalSetup = async () => {
  await ensureAuthDir();
  const { api, payload, userId } = await createAdminUser();
  const token = await loginAdmin(api, payload.email, payload.password);
  await fs.writeFile(
    path.resolve(".auth", "token.json"),
    JSON.stringify(
      {
        token,
        userId,
        email: payload.email,
        password: payload.password
      },
      null,
      2
    )
  );
  await fs.mkdir(path.resolve("allure-results"), { recursive: true });
  await fs.writeFile(
    path.resolve("allure-results", "environment.properties"),
    ["Author=Bruno Salzani", "Project=ServeRest API Playwright"].join("\n"),
    "utf-8"
  );
  await api.dispose();
};

export default globalSetup;
