import { defineConfig } from '@playwright/test'
import base from './playwright.config.mjs'

export default defineConfig({
  ...base,
  webServer: undefined,
  projects: [
    {
      name: 'watermark-spacing',
      testMatch: 'watermark-spacing.spec.mjs'
    }
  ]
})
