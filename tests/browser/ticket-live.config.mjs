import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './specs',
  testMatch: 'tapd-1077734.live.spec.mjs',
  workers: 1,
  retries: 0,
  reporter: [['dot']],
  outputDir: 'output/ticket-live-results',
  use: {
    baseURL: process.env.PLAYWRIGHT_BASE_URL || 'http://127.0.0.1:7070',
    channel: 'chrome',
    locale: 'zh-CN',
    actionTimeout: 15_000,
    headless: true,
    screenshot: 'only-on-failure',
    trace: 'off'
  }
})
