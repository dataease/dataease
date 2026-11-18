/* eslint-disable @typescript-eslint/no-var-requires -- CommonJS test entry */
const { defineConfig } = require('../../../../tests/browser/node_modules/@playwright/test')
module.exports = defineConfig({
  testDir: __dirname,
  testMatch: 'new-window.spec.cjs',
  workers: 1,
  retries: 0,
  reporter: 'dot',
  outputDir: '../../../../tests/browser/output/new-window-origin',
  use: {
    channel: 'chrome',
    headless: true,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure'
  }
})
