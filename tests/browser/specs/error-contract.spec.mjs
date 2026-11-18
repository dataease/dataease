import { test, expect } from '@playwright/test'

test.beforeEach(async ({ page }) => {
  await page.route('**/xpackModel', (route) =>
    route.fulfill({ json: { code: 0, data: false } })
  )
  await page.goto('/errors.html')
  await page.waitForFunction(() => window.hmacService)
  await page.evaluate(() => {
    window.hmacErrorFixtureState = {
      messages: [],
      redirects: [],
      cleared: 0,
      dialogs: 0
    }
  })
})

for (const status of [200, 400, 403, 404, 409, 500, 502, 504]) {
  test(`preserves structured error at HTTP ${status}, presents once`, async ({
    page
  }) => {
    const code = status === 200 ? 10001 : status === 409 ? 50003 : 40001
    await page.route('**/fixture-failure', (route) => {
      expect(route.request().headers()['x-de-error-contract']).toBe('2')
      expect(route.request().headers()['x-de-request-id']).toMatch(
        /^[a-f0-9]{32}$/
      )
      return route.fulfill({
        status,
        json: {
          code,
          msg: 'safe message',
          requestId: 'request-123',
          fieldErrors: [{ field: 'name', message: 'required' }],
          retryable: status === 504
        }
      })
    })
    const result = await page.evaluate(async () => {
      try {
        await window.hmacService.get('/fixture-failure')
      } catch (error) {
        window.presentError(error, 'duplicate local message')
        return {
          typed: error instanceof window.DataEaseError,
          code: error.code,
          status: error.httpStatus,
          requestId: error.requestId,
          fields: error.fieldErrors,
          retryable: error.retryable,
          responseCode: error.response.data.code,
          cause: !!error.cause,
          ...window.hmacErrorFixtureState
        }
      }
    })
    expect(result.typed).toBe(true)
    expect(result.code).toBe(code)
    expect(result.responseCode).toBe(code)
    expect(result.status).toBe(status)
    expect(result.requestId).toBe('request-123')
    expect(result.fields[0].field).toBe('name')
    expect(result.cause).toBe(status !== 200)
    expect(result.messages).toEqual(['safe message'])
  })
}

for (const status of [200, 400]) {
  test(`local handler owns presentation at HTTP ${status}`, async ({
    page
  }) => {
    await page.route('**/fixture-failure', (route) =>
      route.fulfill({ status, json: { code: 10001, msg: 'field required' } })
    )
    const result = await page.evaluate(async () => {
      try {
        await window.hmacService('/fixture-failure', {
          handleErrorLocally: true
        })
      } catch (error) {
        const before = window.hmacErrorFixtureState.messages.length
        window.presentError(error)
        window.presentError(error)
        return { before, ...window.hmacErrorFixtureState }
      }
    })
    expect(result.before).toBe(0)
    expect(result.messages).toEqual(['field required'])
  })
}

test('conflict confirmation retries against confirmed version and retains input', async ({
  page
}) => {
  let calls = 0
  await page.route('**/fixture-save', (route) => {
    const body = route.request().postDataJSON()
    expect(body.content).toBe('unsaved input')
    if (++calls === 1)
      return route.fulfill({
        status: 409,
        json: {
          code: 50005,
          category: 'CONFLICT',
          msg: 'changed',
          data: { currentContentId: 'version-2' }
        }
      })
    expect(body.expectedContentId).toBe('version-2')
    return route.fulfill({ json: { code: 0, data: 'saved' } })
  })
  const result = await page.evaluate(async () => {
    let confirmed = 0
    const data = { expectedContentId: 'version-1', content: 'unsaved input' }
    const result = await window.saveWithConflict(
      data,
      (data) => window.hmacService.post('/fixture-save', data),
      async () => {
        confirmed++
      }
    )
    return { confirmed, data, result, ...window.hmacErrorFixtureState }
  })
  expect(result.confirmed).toBe(1)
  expect(result.result.data).toBe('saved')
  expect(result.messages).toEqual([])
  expect(calls).toBe(2)
})

test('declining overwrite does not retry or erase input', async ({ page }) => {
  let calls = 0
  await page.route('**/fixture-save', (route) => {
    calls++
    return route.fulfill({
      status: 409,
      json: {
        code: 50005,
        msg: 'changed',
        data: { currentContentId: 'version-2' }
      }
    })
  })
  const result = await page.evaluate(async () => {
    const data = { expectedContentId: 'version-1', content: 'unsaved input' }
    let rejected = false
    try {
      await window.saveWithConflict(
        data,
        (d) => window.hmacService.post('/fixture-save', d),
        () => Promise.reject('cancel')
      )
    } catch {
      rejected = true
    }
    return { rejected, data, ...window.hmacErrorFixtureState }
  })
  expect(result.rejected).toBe(true)
  expect(result.data).toEqual({
    expectedContentId: 'version-1',
    content: 'unsaved input'
  })
  expect(result.messages).toEqual([])
  expect(calls).toBe(1)
})

for (const status of [200, 400]) {
  test(`JSON download error ${status} is rejected, not saved as a file`, async ({
    page
  }) => {
    await page.route('**/fixture-file', (route) =>
      route.fulfill({ status, json: { code: 10001, msg: 'download denied' } })
    )
    const result = await page.evaluate(async () => {
      try {
        await window.hmacService.get('/fixture-file', { responseType: 'blob' })
      } catch (error) {
        return { code: error.code, ...window.hmacErrorFixtureState }
      }
    })
    expect(result.code).toBe(10001)
    expect(result.messages).toEqual(['download denied'])
  })
}

test('successful download and legacy chart error retain original response shapes', async ({
  page
}) => {
  await page.route('**/fixture-file', (route) =>
    route.fulfill({
      contentType: 'application/octet-stream',
      body: 'file data'
    })
  )
  await page.route('**/fixture-chart', (route) =>
    route.fulfill({ json: { code: 50002, msg: 'chart details', data: null } })
  )
  const result = await page.evaluate(async () => {
    const file = await window.hmacService.get('/fixture-file', {
      responseType: 'blob'
    })
    const chart = await window.hmacService.get('/fixture-chart')
    return {
      file: await file.data.text(),
      chart,
      ...window.hmacErrorFixtureState
    }
  })
  expect(result.file).toBe('file data')
  expect(result.chart.code).toBe(50002)
  expect(result.messages).toEqual([])
})

test('intentional cancellation remains silent', async ({ page }) => {
  const result = await page.evaluate(async () => {
    const controller = new AbortController()
    controller.abort()
    try {
      await window.hmacService.get('/fixture-cancel', {
        signal: controller.signal
      })
    } catch (error) {
      return { code: error.code, ...window.hmacErrorFixtureState }
    }
  })
  expect(result.code).toBe('ERR_CANCELED')
  expect(result.messages).toEqual([])
  expect(result.redirects).toEqual([])
})

for (const reason of [
  'UNAUTHENTICATED',
  'FORBIDDEN',
  'NOT_FOUND',
  'INTERNAL'
]) {
  test(`gateway reason ${reason} preserves recovery semantics`, async ({
    page
  }) => {
    const status = {
      UNAUTHENTICATED: 401,
      FORBIDDEN: 403,
      NOT_FOUND: 404,
      INTERNAL: 500
    }[reason]
    await page.route('**/fixture-failure', (route) =>
      route.fulfill({
        status,
        headers: { 'X-DE-Error-Reason': reason },
        json: { code: 40001, category: reason, msg: 'safe message' }
      })
    )
    const result = await page.evaluate(async () => {
      try {
        await window.hmacService.get('/fixture-failure')
      } catch {
        return window.hmacErrorFixtureState
      }
    })
    expect(result.redirects.length).toBe(reason === 'UNAUTHENTICATED' ? 1 : 0)
    expect(result.cleared).toBe(reason === 'UNAUTHENTICATED' ? 1 : 0)
    expect(result.dialogs).toBe(reason === 'FORBIDDEN' ? 1 : 0)
    expect(result.messages.length).toBe(
      reason === 'NOT_FOUND' || reason === 'INTERNAL' ? 1 : 0
    )
  })
}

test('explicit quota classification does not use legacy session-expiry handling', async ({
  page
}) => {
  await page.route('**/fixture-failure', (route) =>
    route.fulfill({
      status: 409,
      json: { code: 80001, category: 'CONFLICT', msg: 'quota exceeded' }
    })
  )
  const result = await page.evaluate(async () => {
    try {
      await window.hmacService.get('/fixture-failure')
    } catch {
      return window.hmacErrorFixtureState
    }
  })
  expect(result.redirects).toEqual([])
  expect(result.cleared).toBe(0)
  expect(result.messages).toEqual(['quota exceeded'])
})

for (const modern of [false, true]) {
  test(`save capability tracks the exact response: modern=${modern}`, async ({
    page
  }) => {
    await page.route('**/fixture-check', (route) =>
      route.fulfill({
        headers: modern ? { 'X-DE-Error-Contract': '2' } : {},
        json: { code: 0, data: 'Repeat' }
      })
    )
    const result = await page.evaluate(async () => {
      const response = await window.hmacService.get('/fixture-check')
      return {
        modern: window.hasErrorContract(response),
        data: response.data,
        keys: Object.keys(response).sort()
      }
    })
    expect(result.modern).toBe(modern)
    expect(result.data).toBe('Repeat')
    expect(result.keys).toEqual(['code', 'data'])
  })
}

for (const status of [200, 403]) {
  test(`license refusal stays silent and retains session at HTTP ${status}`, async ({
    page
  }) => {
    await page.route('**/fixture-license-denied', (route) =>
      route.fulfill({
        status,
        json: {
          code: 60003,
          msg: 'synthetic license refusal',
          ...(status === 403 ? { category: 'FORBIDDEN', retryable: false } : {})
        }
      })
    )
    const result = await page.evaluate(async () => {
      try {
        await window.hmacService.get('/fixture-license-denied')
      } catch (error) {
        return {
          code: error.code,
          status: error.httpStatus,
          ...window.hmacErrorFixtureState
        }
      }
    })
    expect(result.code).toBe(60003)
    expect(result.status).toBe(status)
    expect(result.messages).toEqual([])
    expect(result.dialogs).toBe(0)
    expect(result.cleared).toBe(0)
    expect(result.redirects).toEqual([])
  })
}
