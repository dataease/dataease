import { defineConfig } from '@playwright/test'
import base from './playwright.config.mjs'
export default defineConfig({
  ...base,
  webServer: {
    command: 'node fixtures/credential-display/server.mjs',
    url: 'http://127.0.0.1:4207',
    reuseExistingServer: process.env.CREDENTIAL_DISPLAY_SERVER === '1',
    timeout: 30000
  },
  projects: [
    {
      name: 'credential-display',
      testMatch: 'credential-display.spec.mjs',
      use: { baseURL: 'http://127.0.0.1:4207' }
    }
  ]
})
