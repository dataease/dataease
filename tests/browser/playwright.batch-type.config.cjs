module.exports = {
  testDir: "./specs",
  testMatch: "dataset-batch-type.spec.cjs",
  workers: 1,
  retries: 0,
  timeout: 30000,
  reporter: "list",
  use: {
    baseURL: "http://127.0.0.1:4205",
    channel: "chrome",
    headless: true,
    viewport: { width: 1440, height: 900 },
  },
  outputDir: "./output/dataset-batch-type",
  webServer: {
    command: `"${process.execPath}" fixtures/dataset-editor/server.cjs`,
    url: "http://127.0.0.1:4205",
    reuseExistingServer: false,
    timeout: 30000,
  },
};
