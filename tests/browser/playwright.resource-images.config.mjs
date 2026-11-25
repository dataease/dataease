import { defineConfig } from '@playwright/test'
export default defineConfig({
  testDir: './specs',
  testMatch: 'resource-images.spec.mjs',
  workers: 1,
  retries: 0,
  reporter: 'dot',
  use: { channel: 'chrome', baseURL: 'http://127.0.0.1:4186', headless: true },
  webServer: {
    command: 'node fixtures/resource-images/server.mjs',
    url: 'http://127.0.0.1:4186',
    reuseExistingServer: false
  },
  outputDir: 'output/resource-images'
})
