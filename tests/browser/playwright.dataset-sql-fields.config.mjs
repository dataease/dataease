import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './specs',
  testMatch: ['dataset-sql-fields.spec.mjs'],
  workers: 1,
  retries: 0,
  reporter: 'dot',
  outputDir: './output/dataset-sql-fields-results',
  use: {
    channel: 'chrome',
    baseURL: 'http://127.0.0.1:4198',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure'
  },
  webServer: {
    command: 'node fixtures/dataset-sql-fields/server.mjs',
    url: 'http://127.0.0.1:4198',
    reuseExistingServer: Boolean(process.env.DATASET_SQL_FIELDS_SERVER),
    timeout: 30000
  }
})
