import fs from "fs/promises";
import path from "path";
import process from "process";
import { fileURLToPath } from "url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const SWAGGER_URL = process.env.SWAGGER_URL || "https://serverest.dev/swagger.json";
const OUT_PATH = path.resolve(__dirname, "..", "schemas", "swagger.remote.json");

const requiredByLocal = {
  "/usuarios": {
    get: { status: "200", properties: ["quantidade", "usuarios"] },
    post: { status: "201", properties: ["message", "_id"] }
  },
  "/login": {
    post: { status: "200", properties: ["message", "authorization"] }
  }
};

async function main() {
  const res = await fetch(SWAGGER_URL);
  if (!res.ok) {
    console.error(`Falha ao baixar swagger: ${res.status}`);
    process.exit(2);
  }
  const swagger = await res.json();
  await fs.writeFile(OUT_PATH, JSON.stringify(swagger, null, 2));
  const paths = swagger.paths || {};
  let issues = 0;

  for (const [endpoint, ops] of Object.entries(requiredByLocal)) {
    if (!paths[endpoint]) {
      console.error(`Endpoint ausente no OpenAPI: ${endpoint}`);
      issues++;
      continue;
    }
    for (const [method, req] of Object.entries(ops)) {
      const opNode = paths[endpoint][method];
      if (!opNode) {
        console.error(`Método ausente no OpenAPI: ${method.toUpperCase()} ${endpoint}`);
        issues++;
        continue;
      }
      const responses = opNode.responses || {};
      const respNode = responses[req.status];
      if (!respNode) {
        console.error(
          `Status ${req.status} ausente para ${method.toUpperCase()} ${endpoint} no OpenAPI`
        );
        issues++;
        continue;
      }
      const content = (respNode.content && respNode.content["application/json"]) || {};
      const schema = content.schema || {};
      const props = (schema.properties && Object.keys(schema.properties)) || [];
      for (const p of req.properties) {
        if (!props.includes(p)) {
          console.warn(
            `Propriedade esperada não encontrada no schema do OpenAPI: ${endpoint} ${method.toUpperCase()} ${req.status} -> ${p}`
          );
          issues++;
        }
      }
    }
  }

  if (issues > 0) {
    console.error(
      `Drift detectado: ${issues} incompatibilidades encontradas. Verifique os JSON Schemas locais em ./schemas.`
    );
    process.exit(1);
  } else {
    console.log("Sem drift detectado entre OpenAPI e schemas monitorados.");
  }
}

main().catch((e) => {
  console.error(e);
  process.exit(2);
});
