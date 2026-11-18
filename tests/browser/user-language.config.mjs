import { defineConfig } from '@playwright/test'
import base from './playwright.config.mjs'

export default defineConfig({
  ...base,
  webServer: {
    command: 'node fixtures/user-language/server.mjs',
    url: 'http://127.0.0.1:4193',
    reuseExistingServer: false,
    timeout: 30000
  },
  projects: [
    {
      name: 'user-language',
      testMatch: 'user-language.spec.mjs',
      use: { baseURL: 'http://127.0.0.1:4193' }
    }
  ]
})
