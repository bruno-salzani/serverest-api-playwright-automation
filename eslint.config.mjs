import tseslint from "typescript-eslint";
import pluginImport from "eslint-plugin-import";
import pluginUnused from "eslint-plugin-unused-imports";

export default [
  {
    files: ["tests/**/*.ts"],
    ignores: [
      "allure-results/**",
      "test-results/**",
      "playwright-report/**",
      "node_modules/**",
      "dist/**",
      "coverage/**"
    ],
    languageOptions: {
      ecmaVersion: 2021,
      sourceType: "module",
      parser: tseslint.parser
    },
    plugins: {
      import: pluginImport,
      "unused-imports": pluginUnused
    },
    rules: {
      "no-unused-vars": "off",
      "unused-imports/no-unused-imports": "error",
      "import/order": [
        "error",
        {
          "newlines-between": "always",
          "alphabetize": { "order": "asc", "caseInsensitive": true }
        }
      ]
    }
  }
];
