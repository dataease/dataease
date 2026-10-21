import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './specs',
  testMatch: 'tapd-1077734.spec.mjs',
  workers: 1,
  retries: 0,
  reporter: [['dot']],
  outputDir: 'output/ticket-results',
  use: {
    baseURL: 'http://127.0.0.1:4194',
    channel: 'chrome',
    headless: true,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure'
  },
  webServer: {
    command: 'node fixtures/ticket/server.mjs',
    url: 'http://127.0.0.1:4194',
    reuseExistingServer: !process.env.CI
  }
})
