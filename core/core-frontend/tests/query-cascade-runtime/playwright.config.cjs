const path = require('node:path')
module.exports = {
  testDir: __dirname,
  testMatch: 'browser.cjs',
  timeout: 60000,
  workers: 1,
  reporter: 'dot',
  outputDir: '/tmp/dataease-cascade-browser-results',
  use: { channel: 'chrome', viewport: { width: 1200, height: 850 }, trace: 'retain-on-failure' }
}
