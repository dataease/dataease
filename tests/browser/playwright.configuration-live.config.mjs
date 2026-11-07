import { defineConfig } from '@playwright/test'
export default defineConfig({
  testDir: './specs',
  testMatch: 'configuration-security.live.spec.mjs',
  workers: 1,
  retries: 0,
  timeout: 120000,
  reporter: [['dot']],
  outputDir: 'output/configuration-live',
  use: {
    channel: 'chrome',
    headless: true,
    baseURL: process.env.CONFIGURATION_LIVE_URL || 'http://127.0.0.1:7070',
    locale: 'zh-CN',
    trace: 'off',
    screenshot: 'off'
  }
})
