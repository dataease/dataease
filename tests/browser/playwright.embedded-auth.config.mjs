import { defineConfig } from '@playwright/test'
import base from './playwright.config.mjs'
export default defineConfig({
  ...base,
  webServer: {
    command: 'node fixtures/hmac/server.mjs',
    url: 'http://127.0.0.1:4197',
    reuseExistingServer: false,
    timeout: 30000
  },
  projects: [
    {
      name: 'embedded-auth',
      testMatch: 'embedded-auth.spec.mjs',
      use: { baseURL: 'http://127.0.0.1:4197' }
    }
  ]
})
