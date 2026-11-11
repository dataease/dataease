/* eslint-disable @typescript-eslint/no-var-requires */
module.exports = {
  testDir: __dirname,
  testMatch: 'browser.cjs',
  workers: 1,
  timeout: 30000,
  reporter: 'dot',
  outputDir: '/tmp/dataease-geometry-search-results',
  use: { channel: 'chrome', viewport: { width: 1280, height: 900 }, trace: 'retain-on-failure' }
}
