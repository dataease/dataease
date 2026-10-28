import { defineConfig } from '@playwright/test'

const channel = process.env.PLAYWRIGHT_CHANNEL || 'chrome'
const demoPort = process.env.PLAYWRIGHT_DEMO_PORT || '4189'
const demoUrl = `http://127.0.0.1:${demoPort}`

export default defineConfig({
  testDir: './specs',
  fullyParallel: false,
  workers: 1,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  timeout: 30_000,
  expect: { timeout: 5_000 },
  reporter: [
    ['dot'],
    ['html', { outputFolder: 'output/report', open: 'never' }]
  ],
  outputDir: 'output/test-results',
  use: {
    browserName: 'chromium',
    channel: channel === 'chromium' ? undefined : channel,
    headless: true,
    viewport: { width: 1440, height: 900 },
    actionTimeout: 10_000,
    navigationTimeout: 20_000,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'off'
  },
  webServer: {
    command: 'node fixtures/server.mjs',
    url: `${demoUrl}/healthz`,
    reuseExistingServer: false,
    timeout: 10_000,
    env: { PLAYWRIGHT_DEMO_PORT: demoPort }
  },
  projects: [
    {
      name: 'tooling',
      testMatch: 'tooling.spec.mjs',
      use: { baseURL: demoUrl }
    },
    {
      name: 'app',
      testMatch: 'app.smoke.spec.mjs',
      use: {
        baseURL: process.env.PLAYWRIGHT_BASE_URL || 'http://127.0.0.1:8080',
        storageState: process.env.PLAYWRIGHT_STORAGE_STATE || undefined
      }
    }
  ]
})
