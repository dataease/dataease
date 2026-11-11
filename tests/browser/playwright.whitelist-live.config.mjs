import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './specs',
  testMatch: 'whitelist-boundaries.live.spec.mjs',
  workers: 1,
  retries: 0,
  timeout: 180000,
  reporter: [['dot']],
  outputDir: 'output/whitelist-live',
  use: {
    channel: 'chrome',
    headless: true,
    baseURL: process.env.WHITELIST_LIVE_URL || 'http://localhost:7070',
    locale: 'zh-CN',
    actionTimeout: 15000,
    navigationTimeout: 30000,
    trace: 'off',
    screenshot: 'off'
  }
})
