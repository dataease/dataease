const path = require('node:path')
const os = require('node:os')
const frontend = path.resolve(__dirname, '../..')

module.exports = {
  testDir: __dirname,
  testMatch: 'regression.cjs',
  workers: 1,
  reporter: 'dot',
  outputDir: path.join(os.tmpdir(), 'dataease-mfa-unbind-enter-results'),
  use: {
    channel: 'chrome',
    baseURL: 'http://127.0.0.1:7098',
    screenshot: 'only-on-failure'
  },
  webServer: {
    command: `node "${path.join(frontend, 'node_modules/vite/bin/vite.js')}" --config "${path.join(
      __dirname,
      'vite.config.mjs'
    )}"`,
    url: 'http://127.0.0.1:7098',
    reuseExistingServer: false,
    timeout: 30000
  }
}
