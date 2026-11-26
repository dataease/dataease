import { defineConfig } from "@playwright/test";
export default defineConfig({
  testDir: "./specs",
  testMatch: "plugin-axios.spec.mjs",
  workers: 1,
  outputDir: "output/plugin-axios",
  reporter: "dot",
  use: { channel: "chrome", baseURL: "http://127.0.0.1:4197" },
  webServer: {
    command: "node fixtures/plugin-axios/server.mjs",
    url: "http://127.0.0.1:4197",
    reuseExistingServer: false,
  },
});
