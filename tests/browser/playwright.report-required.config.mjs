import { defineConfig } from '@playwright/test'
export default defineConfig({
  testDir: './specs',
  testMatch: 'report-required.spec.mjs',
  workers: 1,
  retries: 0,
  reporter: 'dot',
  outputDir: './output/report-required-results',
  use: {
    channel: 'chrome',
    baseURL: 'http://127.0.0.1:4196',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
  },
  webServer: {
    command: 'node fixtures/report-required/server.mjs',
    url: 'http://127.0.0.1:4196',
    reuseExistingServer: Boolean(process.env.REPORT_REQUIRED_SERVER),
    timeout: 30000,
  },
})
