import { defineConfig } from '@playwright/test'
import base from './playwright.ai-service.config.mjs'

const baseURL = 'http://127.0.0.1:' + (process.env.AI_BROWSER_TEST_PORT || '15494')
export default defineConfig({
  ...base,
  webServer: {
    ...base.webServer,
    env: { AI_BROWSER_TEST_PORT: new URL(baseURL).port, METADATA_TASK_UI: '1' },
    url: baseURL
  },
  projects: [
    {
      name: 'datasource-metadata',
      testMatch: 'metadata-tasks.spec.mjs',
      use: { baseURL, locale: 'en-US' }
    }
  ],
  timeout: 60000
})
