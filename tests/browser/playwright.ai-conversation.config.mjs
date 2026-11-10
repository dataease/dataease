import { defineConfig } from '@playwright/test'
import aiService from './playwright.ai-service.config.mjs'
export default defineConfig({
  ...aiService,
  projects: [
    {
      name: 'ai-conversation',
      testMatch: 'ai-conversation.spec.mjs',
      use: { baseURL: 'http://127.0.0.1:4198' }
    }
  ]
})
