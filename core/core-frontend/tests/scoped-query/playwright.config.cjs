/* eslint-disable @typescript-eslint/no-var-requires -- Node CommonJS 测试配置。 */
module.exports = {
  testDir: __dirname,
  testMatch: 'browser.cjs',
  timeout: 60000,
  workers: 1,
  reporter: 'dot',
  outputDir: '/tmp/dataease-scoped-query-browser-results',
  use: { channel: 'chrome', viewport: { width: 1280, height: 900 }, trace: 'retain-on-failure' }
}
