module.exports = {
  root: true,
  env: { node: true, es2021: true },
  parserOptions: { ecmaVersion: 2021, sourceType: "module" },
  extends: ["eslint:recommended", "plugin:import/recommended", "prettier"],
  plugins: ["unused-imports"],
  rules: {
    "no-unused-vars": "off",
    "unused-imports/no-unused-imports": "error",
    "import/order": [
      "error",
      { "newlines-between": "always", "alphabetize": { "order": "asc", "caseInsensitive": true } }
    ]
  },
  ignorePatterns: ["allure-results/**", "test-results/**", "playwright-report/**", "node_modules/**"]
};
