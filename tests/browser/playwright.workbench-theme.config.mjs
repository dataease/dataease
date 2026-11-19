import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './specs',
  testMatch: ['workbench-theme.spec.mjs'],
  workers: 1,
  retries: 0,
  reporter: 'dot',
  outputDir: './output/workbench-theme-results',
  use: {
    channel: 'chrome',
    baseURL: 'http://127.0.0.1:4196',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure'
  },
  webServer: {
    command: 'node fixtures/workbench-theme/server.mjs',
    url: 'http://127.0.0.1:4196',
    reuseExistingServer: Boolean(process.env.WORKBENCH_THEME_SERVER),
    timeout: 30000
  }
})
