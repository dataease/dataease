import { defineConfig } from '@playwright/test'
export default defineConfig({
  testDir: './specs',
  testMatch: 'error-contract.embedded.spec.mjs',
  workers: 1,
  retries: 0,
  timeout: 60000,
  reporter: [
    ['dot'],
    ['json', { outputFile: 'output/error-contract-embedded/results.json' }]
  ],
  outputDir: 'output/error-contract-embedded/artifacts',
  webServer: {
    command: 'node fixtures/error-contract-embedded/server.mjs',
    url: 'http://127.0.0.1:4209',
    reuseExistingServer: false
  },
  use: {
    channel: 'chrome',
    baseURL: process.env.ERROR_CONTRACT_LIVE_URL || 'http://127.0.0.1:7070',
    headless: true,
    locale: 'zh-CN',
    screenshot: 'off',
    trace: 'off'
  }
})
