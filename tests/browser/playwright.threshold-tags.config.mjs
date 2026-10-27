import { defineConfig } from '@playwright/test'
export default defineConfig({
  testDir: './specs', testMatch: 'threshold-tags.spec.mjs', workers: 1, retries: 0,
  reporter: 'dot', outputDir: './output/threshold-tags-results',
  use: { channel: 'chrome', baseURL: 'http://127.0.0.1:4194', screenshot: 'only-on-failure', trace: 'retain-on-failure' },
  webServer: { command: 'node fixtures/threshold-tags/server.mjs', url: 'http://127.0.0.1:4194', reuseExistingServer: Boolean(process.env.THRESHOLD_TAGS_SERVER), timeout: 30000 }
})
