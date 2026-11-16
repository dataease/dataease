import { defineConfig } from '@playwright/test'
import base from './playwright.config.mjs'

export default defineConfig({
  ...base,
  webServer: {
    command: 'node fixtures/preview-sheet-tab/server.mjs',
    url: 'http://127.0.0.1:4192',
    reuseExistingServer: false,
    timeout: 30_000
  },
  projects: [
    {
      name: 'preview-sheet-tab',
      testMatch: 'preview-sheet-tab.spec.mjs',
      use: { baseURL: 'http://127.0.0.1:4192' }
    }
  ]
})
