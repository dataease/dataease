/* eslint-disable @typescript-eslint/no-var-requires -- 独立 Playwright CLI 配置。 */
module.exports = {
  testDir: __dirname,
  testMatch: 'browser.cjs',
  timeout: 30000,
  workers: 1,
  reporter: 'dot',
  outputDir: '/tmp/dataease-edit-session-browser-results',
  use: { channel: 'chrome', viewport: { width: 1280, height: 900 }, trace: 'retain-on-failure' }
}
