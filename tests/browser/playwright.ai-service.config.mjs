import { defineConfig } from '@playwright/test'
import base from './playwright.config.mjs'
const baseURL = 'http://127.0.0.1:' + (process.env.AI_BROWSER_TEST_PORT || '4198')
export default defineConfig({
  ...base,
  webServer: {
    command: 'node fixtures/ai-service/server.mjs',
    url: baseURL,
    reuseExistingServer: false,
    timeout: 30000
  },
  projects: [
    {
      name: 'ai-service',
      testMatch: 'ai-service.spec.mjs',
      use: { baseURL }
    }
  ]
})
