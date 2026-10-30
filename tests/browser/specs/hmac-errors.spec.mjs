import { test, expect } from '@playwright/test'

test.beforeEach(async ({ page }) => {
  await page.route('**/xpackModel', route => route.fulfill({ json: { code: 0, data: false } }))
  await page.goto('/errors.html')
  await page.waitForFunction(() => window.hmacService)
  await page.evaluate(() => { window.hmacErrorFixtureState = { messages: [], redirects: [], cleared: 0, dialogs: 0 } })
})

const requestFailure = (page, signed = true, responseType = 'json') => page.evaluate(async ({ signed, responseType }) => {
  let rejected = false
  try {
    await window.hmacService.get('/fixture-failure', {
      headers: signed ? { Authorization: 'Signature synthetic-fixture' } : {}, responseType
    })
  } catch { rejected = true }
  return { rejected, ...window.hmacErrorFixtureState }
}, { signed, responseType })

for (const type of ['legacy', 'challenge', 'code', 'clock', 'clock-code', 'challenge-clock', 'blob']) {
  test(`signature failure ${type}: clear message, rejected promise and no login redirect`, async ({ page }) => {
    let calls = 0
    await page.route('**/fixture-failure', route => {
      calls++
      const headers = type.startsWith('challenge') || type === 'blob' ? { 'WWW-Authenticate': 'hmac realm="apisix"' } : {}
      const json = type === 'code' ? { code: 'HMAC_AUTH_FAILED' }
        : type === 'clock-code' ? { code: 'HMAC_CLOCK_SKEW' }
        : type === 'clock' || type === 'challenge-clock' ? { message: "client request can't be validated: Clock skew exceeded" }
        : { message: "client request can't be validated" }
      return route.fulfill({ status: 401, headers, json })
    })
    const result = await requestFailure(page, true, type === 'blob' ? 'blob' : 'json')
    expect(result.rejected).toBe(true)
    expect(result.messages).toHaveLength(1)
    expect(result.messages[0]).toContain(type.includes('clock') ? '时间偏差过大' : '请求签名校验失败')
    expect(result.redirects).toEqual([])
    expect(result.cleared).toBe(0)
    expect(calls).toBe(1)
  })
}

test('network errors remain failures without a signature diagnosis', async ({ page }) => {
  await page.route('**/fixture-failure', route => route.abort())
  const result = await requestFailure(page)
  expect(result.rejected).toBe(true)
  expect(result.messages).toEqual([])
  expect(result.redirects).toEqual([])
})

for (const early of [false, true]) {
  test(`unsigned request rejected by HMAC gateway; i18n initialized=${!early}`, async ({ page }) => {
    if (early) await page.evaluate(() => { window.hmacFixtureI18nReady = false })
    await page.route('**/fixture-failure', route => route.fulfill({ status: 401,
      json: { message: "client request can't be validated: missing Authorization header" }
    }))
    const result = await requestFailure(page, false)
    expect(result.rejected).toBe(true)
    expect(result.messages).toHaveLength(1)
    expect(result.messages[0]).toContain('请求缺少 HMAC 签名')
    expect(result.messages[0]).toContain('HMAC 开关')
    expect(result.options[0]).toEqual({ duration: 0, grouping: true })
    expect(result.redirects).toEqual([])
    expect(result.cleared).toBe(0)
  })
}

for (const type of ['generic401', 'unsigned', 'permission403', 'login', 'permissionFlag']) {
  test(`does not misclassify ${type} as HMAC failure`, async ({ page }) => {
    const headers = type === 'login' ? { 'DE-GATEWAY-FLAG': 'Invalid token' }
      : type === 'permissionFlag' ? { 'DE-FORBIDDEN-FLAG': 'No access permission' } : {}
    await page.route('**/fixture-failure', route => route.fulfill({
      status: type === 'permission403' || type === 'permissionFlag' ? 403 : 401, headers,
      json: { message: type === 'generic401' ? 'Unauthorized' : "client request can't be validated" }
    }))
    const result = await requestFailure(page, type !== 'unsigned')
    expect(result.messages.some(message => message.includes('请求签名'))).toBe(false)
    if (type === 'login') {
      expect(result.cleared).toBe(1)
      expect(result.redirects[0]).toContain('/login')
    }
    if (type === 'permissionFlag') expect(result.dialogs).toBe(1)
  })
}
