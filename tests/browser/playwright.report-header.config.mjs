import { defineConfig } from '@playwright/test'
export default defineConfig({
  testDir: './specs',
  testMatch: 'report-header.spec.mjs',
  workers: 1,
  retries: 0,
  reporter: 'dot',
  outputDir: './output/report-header-results',
  use: {
    channel: 'chrome',
    baseURL: 'http://127.0.0.1:4198',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
  },
  webServer: {
    command: 'node fixtures/report-header/server.mjs',
    url: 'http://127.0.0.1:4198',
    reuseExistingServer: Boolean(process.env.REPORT_HEADER_SERVER),
    timeout: 30000,
  },
})
