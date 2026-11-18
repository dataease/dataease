import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './specs',
  testMatch: 'tapd-1077772.live.spec.mjs',
  workers: 1,
  retries: 0,
  timeout: 240_000,
  expect: { timeout: 10_000 },
  reporter: [
    ['dot'],
    ['json', { outputFile: 'output/delay-queue-live/results.json' }],
    ['html', { outputFolder: 'output/delay-queue-live/report', open: 'never' }]
  ],
  outputDir: 'output/delay-queue-live/artifacts',
  use: {
    baseURL: process.env.PLAYWRIGHT_BASE_URL || 'http://homeserver.local:9080',
    channel: 'chrome',
    headless: true,
    locale: 'zh-CN',
    actionTimeout: 15_000,
    navigationTimeout: 25_000,
    trace: 'off',
    screenshot: 'only-on-failure'
  }
})
