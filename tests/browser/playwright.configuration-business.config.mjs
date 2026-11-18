import { defineConfig } from '@playwright/test'
import base from './playwright.configuration-live.config.mjs'
export default defineConfig({
  ...base,
  testMatch: 'configuration-security.business.spec.mjs',
  timeout: 180000,
  outputDir: 'output/configuration-business',
  use: { ...base.use, actionTimeout: 10000, navigationTimeout: 30000 }
})
