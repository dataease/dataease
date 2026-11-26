import { defineConfig } from "@playwright/test";
import base from "./playwright.config.mjs";
export default defineConfig({
  ...base,
  webServer: {
    command: "node fixtures/plugin-axios/server.mjs",
    url: "http://127.0.0.1:4196",
    reuseExistingServer: false,
    timeout: 30000,
  },
  projects: [
    {
      name: "plugin-axios",
      testMatch: "plugin-axios.spec.mjs",
      use: { baseURL: "http://127.0.0.1:4196" },
    },
  ],
});
