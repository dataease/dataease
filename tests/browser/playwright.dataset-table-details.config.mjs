import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './specs',
  testMatch: 'dataset-table-details.spec.mjs',
  workers: 1,
  retries: 0,
  reporter: 'dot',
  outputDir: './output/dataset-table-details-results',
  use: {
    channel: 'chrome',
    baseURL: 'http://127.0.0.1:4199',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure'
  },
  webServer: {
    command: 'node fixtures/dataset-table-details/server.mjs',
    url: 'http://127.0.0.1:4199',
    reuseExistingServer: Boolean(process.env.DATASET_TABLE_DETAILS_SERVER),
    timeout: 30000
  }
})
