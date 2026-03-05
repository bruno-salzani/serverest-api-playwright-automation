import { faker } from "@faker-js/faker";

export type UserPayload = {
  nome: string;
  email: string;
  password: string;
  administrador: "true" | "false";
};

export type ProductPayload = {
  nome: string;
  preco: number;
  descricao: string;
  quantidade: number;
};

export const createUserPayload = (overrides: Partial<UserPayload> = {}): UserPayload => ({
  nome: faker.person.fullName(),
  email: faker.internet.email().toLowerCase(),
  password: faker.internet.password({ length: 10 }),
  administrador: "true",
  ...overrides
});

export const createProductPayload = (
  overrides: Partial<ProductPayload> = {}
): ProductPayload => ({
  nome: `${faker.commerce.productName()} ${Date.now()}`,
  preco: faker.number.int({ min: 50, max: 500 }),
  descricao: faker.commerce.productDescription(),
  quantidade: faker.number.int({ min: 1, max: 50 }),
  ...overrides
});
