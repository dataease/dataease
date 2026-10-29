module.exports = {
  testDir: __dirname,
  testMatch: 'browser.cjs',
  timeout: 30000,
  workers: 1,
  reporter: 'dot',
  outputDir: '/tmp/dataease-chart-viewport-results',
  use: { channel: 'chrome', viewport: { width: 1000, height: 700 }, trace: 'retain-on-failure' }
}
