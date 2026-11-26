import { defineConfig } from "@playwright/test";
import base from "./playwright.config.mjs";
export default defineConfig({
  ...base,
  webServer: {
    command: "node fixtures/plugin-icon/server.mjs",
    url: "http://127.0.0.1:4198",
    reuseExistingServer: false,
    timeout: 30000,
  },
  projects: [
    {
      name: "plugin-icon",
      testMatch: "plugin-icon.spec.mjs",
      use: { baseURL: "http://127.0.0.1:4198" },
    },
  ],
});
