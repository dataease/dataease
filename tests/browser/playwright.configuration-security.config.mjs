import { defineConfig } from '@playwright/test'
import base from './playwright.config.mjs'
export default defineConfig({
  ...base,
  webServer: {
    command: 'node fixtures/configuration-security/server.mjs',
    url: 'http://127.0.0.1:4198',
    reuseExistingServer: process.env.CONFIGURATION_SECURITY_FIXTURE === '1',
    timeout: 30000
  },
  projects: [
    {
      name: 'configuration-local',
      testMatch: 'configuration-security.spec.mjs',
      use: { baseURL: 'http://127.0.0.1:4198' }
    },
    {
      name: 'configuration-http',
      testMatch: 'configuration-security.spec.mjs',
      use: {
        baseURL: 'http://configuration-http.test:4198',
        launchOptions: {
          args: [
            '--no-proxy-server',
            '--host-resolver-rules=MAP configuration-http.test 127.0.0.1'
          ]
        }
      }
    }
  ]
})
