import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './specs',
  testMatch: ['lineage-tooltip.spec.mjs'],
  workers: 1,
  retries: 0,
  reporter: 'dot',
  outputDir: './output/lineage-tooltip-results',
  use: {
    channel: 'chrome',
    baseURL: 'http://127.0.0.1:4193',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure'
  },
  webServer: {
    command: 'node fixtures/lineage-tooltip/server.mjs',
    url: 'http://127.0.0.1:4193',
    reuseExistingServer: Boolean(process.env.LINEAGE_TOOLTIP_SERVER),
    timeout: 30000
  }
})
