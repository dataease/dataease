import { defineConfig } from '@playwright/test'
import base from './playwright.config.mjs'
const testMatch = ['error-contract.spec.mjs', 'hmac-errors.spec.mjs']
export default defineConfig({
  ...base,
  webServer: {
    command: 'node fixtures/hmac/server.mjs',
    url: 'http://127.0.0.1:4197',
    reuseExistingServer: false,
    timeout: 30000
  },
  projects: [
    { name: 'error-contract', testMatch, use: { baseURL: 'http://127.0.0.1:4197' } },
    {
      name: 'error-contract-http',
      testMatch,
      use: {
        baseURL: 'http://hmac-http.test:4197',
        launchOptions: {
          args: ['--no-proxy-server', '--host-resolver-rules=MAP hmac-http.test 127.0.0.1']
        }
      }
    }
  ]
})
