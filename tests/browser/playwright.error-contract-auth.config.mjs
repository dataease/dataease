import { defineConfig } from '@playwright/test'
export default defineConfig({
  testDir: './specs',
  testMatch: 'error-contract.auth.spec.mjs',
  workers: 1,
  retries: 0,
  timeout: 60000,
  reporter: [
    ['dot'],
    [
      'json',
      {
        outputFile:
          process.env.ERROR_CONTRACT_AUTH_REPORT ||
          'output/error-contract-auth/results.json'
      }
    ]
  ],
  outputDir: 'output/error-contract-auth/artifacts',
  use: {
    channel: 'chrome',
    baseURL: process.env.ERROR_CONTRACT_LIVE_URL || 'http://127.0.0.1:7070',
    headless: true,
    locale: 'zh-CN',
    trace: 'off',
    screenshot: 'off'
  }
})
