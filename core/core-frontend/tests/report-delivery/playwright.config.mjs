import { defineConfig } from '../../../../tests/browser/node_modules/@playwright/test/index.mjs'
export default defineConfig({
  testDir: '.',
  testMatch: 'report-ui.spec.mjs',
  workers: 1,
  reporter: [['line']],
  outputDir: '/tmp/v3-report-ui-results',
  use: {
    baseURL: 'http://127.0.0.1:7096',
    channel: 'chrome',
    headless: true,
    trace: 'retain-on-failure'
  }
})
