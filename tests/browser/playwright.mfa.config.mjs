import { defineConfig } from '@playwright/test'
import base from './playwright.config.mjs'
export default defineConfig({ ...base,
  webServer: { command: 'node fixtures/mfa/server.mjs', url: 'http://127.0.0.1:4190', reuseExistingServer: false, timeout: 30000 },
  projects: [{ name: 'mfa', testMatch: 'mfa-submit.spec.mjs', use: { baseURL: 'http://127.0.0.1:4190' } }]
})
