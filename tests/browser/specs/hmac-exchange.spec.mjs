import { test, expect } from '@playwright/test'
import { createHmac } from 'node:crypto'

const secret = 'synthetic+/HMAC密钥=fixture'
test.beforeEach(async ({ page }) => {
  await page.route('**/xpackModel', route => route.fulfill({ json: { code: 0, data: true } }))
  await page.goto('/')
  await page.waitForFunction(() => window.hmacFixture)
})

const sign = page => page.evaluate(async () => {
  const config = { baseURL: '/api', method: 'post', headers: { 'X-DE-TOKEN': 'synthetic-token' } }
  await window.hmacFixture.securityConfig(config, '/api/chart/data')
  return config.headers
})

const intercept = async (page, request, mutate = value => value) => {
  const counts = { status: 0, exchange: 0 }
  await page.route('**/perSetting/hmac/info', route => {
    counts.status++
    return route.fulfill({ json: { code: 0, data: { version: 2, enabled: true } } })
  })
  await page.route('**/perSetting/hmac/exchange', async route => {
    counts.exchange++
    const response = await request.post('http://127.0.0.1:4197/fixture-envelope', { data: route.request().postDataJSON() })
    await route.fulfill({ json: { code: 0, data: mutate(await response.json()) } })
  })
  return counts
}

test('Java envelope interoperates with browser; concurrent signing and cache invalidation', async ({ page, request }) => {
  const counts = await intercept(page, request)
  // Observe only key metadata, never export or retain key material.
  await page.evaluate(() => {
    if (!crypto.subtle) return
    const sign = crypto.subtle.sign.bind(crypto.subtle)
    crypto.subtle.sign = (algorithm, key, data) => {
      window.keyMetadata = { extractable: key.extractable, usages: key.usages }
      return sign(algorithm, key, data)
    }
  })
  const results = await Promise.all([sign(page), sign(page), sign(page)])
  expect(counts).toEqual({ status: 1, exchange: 1 })
  for (const headers of results) {
    const expected = createHmac('sha256', secret)
      .update(`dataease-key\nPOST /de2api/chart/data\nX-Date: ${headers['X-Date']}\n`).digest('base64')
    expect(headers.Authorization).toContain(`signature="${expected}"`)
    expect(headers['X-DE-TOKEN']).toBe('synthetic-token')
  }
  if (await page.evaluate(() => !!crypto.subtle)) {
    expect(await page.evaluate(() => window.keyMetadata)).toEqual({ extractable: false, usages: ['sign'] })
  }
  expect(await page.evaluate(() => window.de_secret_key)).toBeUndefined()
  await page.evaluate(() => window.hmacFixture.invalidateHmac())
  await sign(page)
  expect(counts).toEqual({ status: 2, exchange: 2 })
  await page.evaluate(async () => {
    await window.hmacFixture.securityConfig({ baseURL: '/other', method: 'get' }, '/other/chart/data')
  })
  expect(counts).toEqual({ status: 3, exchange: 3 })
})

test('disabled HMAC needs no exchange or Web Crypto', async ({ page }) => {
  await page.route('**/perSetting/hmac/info', route => route.fulfill({ json: { code: 0, data: { version: 2, enabled: false } } }))
  await page.evaluate(() => Object.defineProperty(window, 'isSecureContext', { value: false }))
  expect((await sign(page)).Authorization).toBeUndefined()
})

test('community capability skips absent enterprise endpoints', async ({ page }) => {
  await page.unroute('**/xpackModel')
  await page.route('**/xpackModel', route => route.fulfill({ json: { code: 0, data: null } }))
  let enterpriseRequests = 0
  await page.route('**/perSetting/hmac/**', route => { enterpriseRequests++; return route.abort() })
  expect((await sign(page)).Authorization).toBeUndefined()
  expect(enterpriseRequests).toBe(0)
})

for (const mode of ['requestId', 'ciphertext', 'version', 'tag', 'enabled']) {
  test(`rejects ${mode} tampering and retries failed initialization`, async ({ page, request }) => {
    let corrupt = true
    const counts = await intercept(page, request, envelope => {
      if (corrupt) {
        if (mode === 'tag') {
          const bytes = Buffer.from(envelope.ciphertext, 'base64')
          bytes[bytes.length - 1] ^= 1
          envelope.ciphertext = bytes.toString('base64')
        } else {
          envelope[mode] = mode === 'version' ? 1 : mode === 'requestId' ? 'mismatch' : mode === 'enabled' ? false : 'AAAA'
        }
      }
      return envelope
    })
    await expect(sign(page)).rejects.toThrow()
    corrupt = false
    expect((await sign(page)).Authorization).toContain('Signature')
    expect(counts.exchange).toBe(2)
  })
}

test('absolute embedded and relative mobile paths retain gateway signature format', async ({ page, request }) => {
  await intercept(page, request)
  const headers = await page.evaluate(async () => {
    const embedded = { baseURL: '/api', method: 'get', headers: { 'X-EMBEDDED-TOKEN': 'synthetic' } }
    await window.hmacFixture.securityConfig(embedded, 'https://embedded.example/tenant/de2api/chart/data?x=1')
    const mobile = { baseURL: '/api', method: 'get' }
    await window.hmacFixture.securityConfig(mobile, './de2api/chart/data')
    return [embedded.headers, mobile.headers]
  })
  for (const [index, path] of ['/tenant/de2api/chart/data?x=1', '/de2api/chart/data'].entries()) {
    const expected = createHmac('sha256', secret)
      .update(`dataease-key\nGET ${path}\nX-Date: ${headers[index]['X-Date']}\n`).digest('base64')
    expect(headers[index].Authorization).toContain(`signature="${expected}"`)
  }
  expect(headers[0]['X-EMBEDDED-TOKEN']).toBe('synthetic')
})

test('missing secure random source is rejected without sending unsigned', async ({ page }) => {
  await page.route('**/perSetting/hmac/info', route => route.fulfill({ json: { code: 0, data: { version: 2, enabled: true } } }))
  await page.evaluate(() => Object.defineProperty(window, 'crypto', { value: undefined }))
  await expect(sign(page)).rejects.toThrow('random_source_required')
})

test('actual HTTP context signs without SubtleCrypto', async ({ page, request }, testInfo) => {
  await intercept(page, request)
  if (testInfo.project.name === 'hmac-http') {
    expect(await page.evaluate(() => window.isSecureContext)).toBe(false)
    expect(await page.evaluate(() => typeof crypto.subtle)).toBe('undefined')
  }
  expect((await sign(page)).Authorization).toContain('Signature')
})

test('network errors and legacy responses never become an unsigned success', async ({ page }) => {
  await page.route('**/perSetting/hmac/info', route => route.abort())
  await expect(sign(page)).rejects.toThrow()
  await page.unroute('**/perSetting/hmac/info')
  await page.route('**/perSetting/hmac/info', route => route.fulfill({ json: { code: 0, data: 'legacy-obfuscated-key' } }))
  await expect(sign(page)).rejects.toThrow('initialization_failed')
})
