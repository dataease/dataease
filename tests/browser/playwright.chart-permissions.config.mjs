import { defineConfig } from '@playwright/test'
export default defineConfig({
  testDir: './specs',
  testMatch: 'chart-permissions.real.spec.mjs',
  workers: 1,
  retries: 0,
  timeout: 60000,
  reporter: [
    ['dot'],
    [
      'json',
      {
        outputFile:
          process.env.CHART_PERMISSIONS_REPORT ||
          'output/chart-permissions/results.json'
      }
    ]
  ],
  outputDir: 'output/chart-permissions/artifacts',
  use: {
    channel: 'chrome',
    baseURL: process.env.ERROR_CONTRACT_LIVE_URL || 'http://127.0.0.1:7070',
    headless: true,
    locale: 'zh-CN',
    trace: 'off',
    screenshot: 'off'
  }
})
