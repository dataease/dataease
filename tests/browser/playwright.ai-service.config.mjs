import { defineConfig } from '@playwright/test'
import base from './playwright.config.mjs'
export default defineConfig({
  ...base,
  webServer: {
    command: 'node fixtures/ai-service/server.mjs',
    url: 'http://127.0.0.1:4198',
    reuseExistingServer: false,
    timeout: 30000
  },
  projects: [
    {
      name: 'ai-service',
      testMatch: 'ai-service.spec.mjs',
      use: { baseURL: 'http://127.0.0.1:4198' }
    }
  ]
})
