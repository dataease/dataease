module.exports = {
  testDir: __dirname,
  testMatch: 'browser.cjs',
  workers: 1,
  timeout: 45000,
  reporter: 'dot',
  outputDir: '/tmp/dataease-dynamic-default-results',
  use: { channel: 'chrome', viewport: { width: 1000, height: 900 }, trace: 'retain-on-failure' }
}
