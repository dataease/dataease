module.exports = {
  testDir: __dirname,
  testMatch: 'browser.cjs',
  timeout: 30000,
  workers: 1,
  reporter: 'dot',
  outputDir: '/tmp/dataease-mobile-message-results',
  use: { channel: 'chrome', trace: 'retain-on-failure' }
}
