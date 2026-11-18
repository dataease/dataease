import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './specs',
  testMatch: ['report-dingtalk-group.spec.mjs'],
  workers: 1,
  retries: 0,
  reporter: 'dot',
  outputDir: './output/report-dingtalk-results',
  use: {
    channel: 'chrome',
    baseURL: 'http://127.0.0.1:4195',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure'
  },
  webServer: {
    command: 'node fixtures/report-dingtalk/server.mjs',
    url: 'http://127.0.0.1:4195',
    reuseExistingServer: Boolean(process.env.REPORT_DINGTALK_SERVER),
    timeout: 30000
  }
})
