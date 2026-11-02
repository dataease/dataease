import { defineConfig } from '@playwright/test'
export default defineConfig({
  testDir: './specs',
  testMatch: 'embedded-live.spec.mjs',
  workers: 1,
  retries: 0,
  timeout: 30000,
  reporter: [['dot'], ['json', { outputFile: 'output/embedded-live/results.json' }]],
  outputDir: 'output/embedded-live/artifacts',
  use: { channel: 'chrome', locale: 'zh-CN', headless: true, trace: 'off', screenshot: 'off' }
})
