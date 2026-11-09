import { defineConfig } from '@playwright/test'
import base from './playwright.config.mjs'
import { readFileSync } from 'node:fs'

if (!process.env.AI_SERVICE_LIVE_URL || !process.env.PLAYWRIGHT_STORAGE_STATE) {
  throw new Error(
    'Set AI_SERVICE_LIVE_URL and PLAYWRIGHT_STORAGE_STATE for the authenticated development environment'
  )
}
const origin = new URL(process.env.AI_SERVICE_LIVE_URL).origin
const authentication = JSON.parse(readFileSync(process.env.PLAYWRIGHT_STORAGE_STATE, 'utf8'))
const tokenEntry = authentication.origins
  ?.find((item) => item.origin === origin)
  ?.localStorage?.find((item) => item.name === 'de_v3_user.token')
let remaining = 0
try {
  const token = JSON.parse(tokenEntry.value).v
  const claims = JSON.parse(Buffer.from(token.split('.')[1], 'base64url').toString('utf8'))
  remaining = claims.exp - Math.floor(Date.now() / 1000)
} catch {
  // Let the runner explain an invalid or stale authentication snapshot before opening pages.
}
if (remaining <= 0) {
  throw new Error(
    'The saved login needs a valid, unexpired token; log in and save a fresh state before running'
  )
}
export default defineConfig({
  ...base,
  webServer: undefined,
  projects: [
    {
      name: 'ai-service-live',
      testMatch: 'ai-service.live.spec.mjs',
      use: {
        baseURL: process.env.AI_SERVICE_LIVE_URL,
        storageState: process.env.PLAYWRIGHT_STORAGE_STATE
      }
    }
  ]
})
