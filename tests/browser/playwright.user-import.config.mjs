import { defineConfig } from '@playwright/test'
export default defineConfig({
  testDir: './specs',
  testMatch: 'user-import.spec.mjs',
  workers: 1,
  retries: 0,
  reporter: 'dot',
  outputDir: './output/user-import-results',
  use: {
    channel: 'chrome',
    baseURL: 'http://127.0.0.1:4198',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure'
  },
  webServer: {
    command: 'node fixtures/user-import/server.mjs',
    url: 'http://127.0.0.1:4198',
    reuseExistingServer: Boolean(process.env.USER_IMPORT_SERVER),
    timeout: 30000
  }
})
