module.exports = {
  testDir: __dirname,
  testMatch: 'browser.cjs',
  workers: 1,
  timeout: 30000,
  reporter: 'dot',
  outputDir: '/tmp/dataease-i18n-hmr-results',
  use: { channel: 'chrome', viewport: { width: 900, height: 700 }, trace: 'retain-on-failure' }
}
