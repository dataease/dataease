import { defineConfig } from '@playwright/test'
import aiService from './playwright.ai-service.config.mjs'
const baseURL = 'http://127.0.0.1:' + (process.env.AI_BROWSER_TEST_PORT || '4198')
export default defineConfig({
  ...aiService,
  projects: [
    {
      name: 'ai-conversation',
      testMatch: 'ai-conversation.spec.mjs',
      use: { baseURL }
    }
  ]
})
