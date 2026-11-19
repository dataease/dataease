import { defineConfig } from '@playwright/test'
export default defineConfig({
  testDir: './specs',
  testMatch: 'sync-datasource-import.spec.mjs',
  workers: 1,
  retries: 0,
  reporter: 'dot',
  outputDir: './output/sync-datasource-import-results',
  use: {
    channel: 'chrome',
    baseURL: 'http://127.0.0.1:4213',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure'
  },
  webServer: {
    command: 'node fixtures/sync-datasource-import/server.mjs',
    url: 'http://127.0.0.1:4213',
    reuseExistingServer: Boolean(process.env.SYNC_IMPORT_SERVER),
    timeout: 30000
  }
})
