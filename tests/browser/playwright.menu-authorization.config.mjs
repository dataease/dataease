import { defineConfig } from "@playwright/test";
export default defineConfig({
  testDir: "./specs",
  testMatch: "menu-authorization.spec.mjs",
  workers: 1,
  reporter: "dot",
  outputDir: "./output/menu-authorization-results",
  use: {
    channel: "chrome",
    headless: true,
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
  },
});
