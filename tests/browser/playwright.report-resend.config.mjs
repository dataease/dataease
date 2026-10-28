import { defineConfig } from '@playwright/test'
export default defineConfig({
  testDir: './specs',
  testMatch: 'report-resend.spec.mjs',
  workers: 1,
  retries: 0,
  reporter: 'dot',
  outputDir: './output/report-resend/results',
  webServer: {
    command: 'node fixtures/report-resend/server.mjs',
    url: 'http://127.0.0.1:4196/__resend',
    reuseExistingServer: process.env.REPORT_RESEND_SERVER === '1'
  },
  use: {
    channel: 'chrome',
    baseURL: 'http://127.0.0.1:4196',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure'
  }
})
