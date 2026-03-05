import { defineConfig } from "@playwright/test";
import dotenv from "dotenv";
import { z } from "zod";

dotenv.config();

const envSchema = z.object({
  BASE_URL: z.string().url().default("https://serverest.dev"),
  API_TIMEOUT: z.coerce.number().default(30000)
});

const env = envSchema.parse(process.env);
const baseURL = env.BASE_URL;

export default defineConfig({
  testDir: "./tests",
  timeout: env.API_TIMEOUT,
  retries: process.env.CI ? 2 : 0,
  reporter: [["list"], ["allure-playwright"]],
  globalSetup: require.resolve("./global-setup"),
  use: {
    baseURL
  }
});
