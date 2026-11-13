import { test, expect } from '@playwright/test'

test.beforeEach(async ({ page }) => {
  await page.route('**/xpackModel', route => route.fulfill({ json: { code: 0, data: false } }))
  await page.goto('/errors.html')
  await page.waitForFunction(() => window.hmacService)
  await page.evaluate(() => {
    window.hmacErrorFixtureState = { messages: [], redirects: [], cleared: 0, dialogs: 0 }
  })
})

for (const [key, message] of [
  ['origin_mismatch', '嵌入来源校验失败'],
  ['token_expired', '嵌入凭证已过期'],
  ['token_invalid', '嵌入凭证无效'],
  ['', '嵌入鉴权失败'],
  ['unknown', '嵌入鉴权失败']
]) {
  test(`embedded 401 ${key || 'legacy'} stays embedded and rejects`, async ({ page }) => {
    await page.route('**/fixture-embedded', route =>
      route.fulfill({
        status: 401,
        headers: { 'DE-GATEWAY-FLAG': 'Invalid%20token' },
        json: {
          code: 20001,
          category: 'UNAUTHENTICATED',
          messageKey: key ? `i18n_embedded_${key}` : undefined
        }
      })
    )
    const result = await page.evaluate(async () => {
      let rejected = false
      try {
        await window.hmacService.get('/fixture-embedded', {
          headers: { 'X-EMBEDDED-TOKEN': 'synthetic-test-token' }
        })
      } catch {
        rejected = true
      }
      return { rejected, ...window.hmacErrorFixtureState }
    })
    expect(result.rejected).toBe(true)
    expect(result.messages).toHaveLength(1)
    expect(result.messages[0]).toContain(message)
    expect(result.redirects).toEqual([])
    expect(result.cleared).toBe(0)
    expect(JSON.stringify(result)).not.toContain('synthetic-test-token')
  })
}

test('ordinary gateway failure still redirects to login', async ({ page }) => {
  await page.route('**/fixture-ordinary', route =>
    route.fulfill({ status: 401, headers: { 'DE-GATEWAY-FLAG': 'Invalid%20token' }, json: {} })
  )
  const result = await page.evaluate(async () => {
    try {
      await window.hmacService.get('/fixture-ordinary')
    } catch {
      /* expected rejection */
    }
    return window.hmacErrorFixtureState
  })
  expect(result.redirects).toHaveLength(1)
  expect(result.redirects[0]).toContain('/login')
  expect(result.cleared).toBe(1)
})

test('successful embedded request keeps the existing response', async ({ page }) => {
  await page.route('**/fixture-success', route =>
    route.fulfill({ json: { code: 0, data: 'synthetic-success' } })
  )
  const result = await page.evaluate(() =>
    window.hmacService.get('/fixture-success', {
      headers: { 'X-EMBEDDED-TOKEN': 'synthetic-test-token' }
    })
  )
  expect(result.data).toBe('synthetic-success')
})

for (const status of [403, 500]) {
  test(`embedded ${status} retains permission or service failure`, async ({ page }) => {
    await page.route('**/fixture-other', route =>
      route.fulfill({
        status,
        headers: status === 403 ? { 'DE-FORBIDDEN-FLAG': 'No access permission' } : {},
        json: { code: status === 403 ? 70001 : 40001, msg: 'synthetic service error' }
      })
    )
    const result = await page.evaluate(async () => {
      try {
        await window.hmacService.get('/fixture-other', {
          headers: { 'X-EMBEDDED-TOKEN': 'synthetic-test-token' }
        })
      } catch {
        /* expected rejection */
      }
      return window.hmacErrorFixtureState
    })
    expect(result.redirects).toEqual([])
    expect(result.cleared).toBe(0)
    if (status === 403) expect(result.dialogs).toBe(1)
    else expect(result.messages).toEqual(['synthetic service error'])
  })
}

test('embedded network failure is not diagnosed as an origin error', async ({ page }) => {
  await page.route('**/fixture-offline', route => route.abort())
  const result = await page.evaluate(async () => {
    let rejected = false
    try {
      await window.hmacService.get('/fixture-offline', {
        headers: { 'X-EMBEDDED-TOKEN': 'synthetic-test-token' }
      })
    } catch {
      rejected = true
    }
    return { rejected, ...window.hmacErrorFixtureState }
  })
  expect(result.rejected).toBe(true)
  expect(result.messages).toEqual([])
  expect(result.redirects).toEqual([])
})
