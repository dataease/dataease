module.exports = {
  testDir: __dirname,
  testMatch: 'browser.cjs',
  workers: 1,
  timeout: 45000,
  reporter: 'dot',
  outputDir: '/tmp/dataease-user-variable-search-results',
  use: { channel: 'chrome', viewport: { width: 1200, height: 1000 }, trace: 'retain-on-failure' }
}
