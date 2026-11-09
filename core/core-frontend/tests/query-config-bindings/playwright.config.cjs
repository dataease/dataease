/* eslint-disable @typescript-eslint/no-var-requires -- Node 浏览器回归配置。 */
module.exports = {
  testDir: __dirname,
  testMatch: ['browser.cjs', 'full-dialog.cjs'],
  workers: 1,
  timeout: 60000,
  reporter: 'dot',
  outputDir: '/tmp/dataease-config-bindings-browser-results',
  use: { channel: 'chrome', viewport: { width: 1200, height: 900 }, trace: 'retain-on-failure' }
}
