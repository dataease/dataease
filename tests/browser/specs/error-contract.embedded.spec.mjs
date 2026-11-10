import { test, expect } from '@playwright/test'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHmac } from 'node:crypto'
import { enterOrganization } from '../fixtures/configuration-security/organization.mjs'
const root = fileURLToPath(new URL('../../../', import.meta.url))
const host = 'http://127.0.0.1:4209'
const output = path.join(root, 'tests/browser/output/error-contract-embedded')
const credentials = Object.fromEntries(
  fs
    .readFileSync(path.join(root, '.codex/playwright_mcp/.env.local'), 'utf8')
    .split('\n')
    .filter((line) => /^TEST_(USERNAME|PASSWORD)=/.test(line))
    .map((line) => {
      const i = line.indexOf('=')
      return [line.slice(0, i), JSON.parse(line.slice(i + 1))]
    })
)
let adminContext, admin, app, resource, proxyInfo, bi
const name = 'codex-error-contract-embedded-' + Date.now()
const owned = { appId: null, resourceId: null }
const saveOwned = () => {
  fs.mkdirSync(output, { recursive: true })
  fs.writeFileSync(
    path.join(output, 'owned-resources.json'),
    JSON.stringify(owned),
    { mode: 0o600 }
  )
}
const api = (url, data, method = 'post') =>
  admin.evaluate(
    async ({ url, data, method }) => {
      const { default: request } = await import('/src/config/axios/index.ts')
      return request[method]({ url, data, handleErrorLocally: true })
    },
    { url, data, method }
  )
const token = (claims = {}, secret = app.appSecret) => {
  const encode = (value) =>
    Buffer.from(JSON.stringify(value)).toString('base64url')
  const body =
    encode({ alg: 'HS256', typ: 'JWT' }) +
    '.' +
    encode({
      account: credentials.TEST_USERNAME,
      appId: app.appId,
      iat: Math.floor(Date.now() / 1000),
      exp: Math.floor(Date.now() / 1000) + 300,
      ...claims
    })
  return (
    body + '.' + createHmac('sha256', secret).update(body).digest('base64url')
  )
}
test.beforeAll(async ({ browser, baseURL }) => {
  bi = baseURL
  saveOwned()
  adminContext = await browser.newContext({ baseURL, locale: 'zh-CN' })
  admin = await adminContext.newPage()
  await admin.goto('/#/login', { waitUntil: 'domcontentloaded' })
  await expect(admin.locator('input[type=password]')).toBeVisible({
    timeout: 30000
  })
  await admin
    .locator('input')
    .filter({ visible: true })
    .first()
    .fill(credentials.TEST_USERNAME)
  await admin.locator('input[type=password]').fill(credentials.TEST_PASSWORD)
  const login = admin.waitForResponse((r) =>
    r.url().includes('/login/localLogin')
  )
  await admin.getByRole('button', { name: /登\s*录|Login/i }).click()
  expect((await (await login).json()).code).toBe(0)
  await admin.waitForFunction(() => !location.hash.includes('login'))
  await enterOrganization(admin)
  proxyInfo = await admin.evaluate(async () =>
    (await import('/src/hooks/web/useCache.ts'))
      .useCache()
      .wsCache.get('user.proxyInfo')
  )
  expect(
    (await api('/embedded/create', { name, domain: host, secretLength: 32 }))
      .code
  ).toBe(0)
  const matches = (
    await api('/embedded/pager/1/100', { keyword: name })
  ).data.records.filter((item) => item.name === name)
  expect(matches.length).toBe(1)
  app = matches[0]
  owned.appId = app.id
  saveOwned()
  resource = await admin.evaluate(async (name) => {
    const { default: request } = await import('/src/config/axios/index.ts')
    const { dvMainStoreWithOut } = await import(
      '/src/store/modules/data-visualization/dvMain.ts'
    )
    const tree = (
      await request.post({
        url: '/auth/busiResource',
        data: { flag: 'panel', system: false }
      })
    ).data
    const parent = tree[0]?.children?.find((item) => !item.leaf)
    if (!parent) throw new Error('No authorized test directory')
    const data = {
      name,
      type: 'dashboard',
      nodeType: 'leaf',
      pid: String(parent.id),
      componentData: '[]',
      canvasViewInfo: {},
      canvasStyleData: JSON.stringify({
        ...dvMainStoreWithOut().canvasStyleData,
        dvType: 'dashboard'
      }),
      contentId: 'embedded-' + Date.now(),
      mobileLayout: true
    }
    const result = await request.post({
      url: '/dataVisualization/saveCanvas',
      data
    })
    return { ...data, id: String(result.data) }
  }, name)
  owned.resourceId = resource.id
  saveOwned()
  expect(
    (
      await api('/dataVisualization/updatePublishStatus', {
        id: resource.id,
        name,
        type: 'dashboard',
        status: 1,
        mobileLayout: true,
        activeViewIds: []
      })
    ).code
  ).toBe(0)
})
test.afterAll(async () => {
  if (admin && !admin.isClosed()) {
    if (owned.resourceId) {
      expect(
        (
          await api('/dataVisualization/deleteLogic', {
            id: owned.resourceId,
            busiFlag: 'dashboard'
          })
        ).code
      ).toBe(0)
      owned.resourceId = null
      saveOwned()
    }
    if (owned.appId) {
      expect((await api('/embedded/delete/' + owned.appId, {})).code).toBe(0)
      owned.appId = null
      saveOwned()
    }
  }
  app = null
  await adminContext?.close()
})
const guest = async (browser) => {
  const context = await browser.newContext({ baseURL: bi, locale: 'zh-CN' })
  await context.addInitScript((proxy) => {
    // Organization selection only. Never transfer the administrator's login token.
    const pathname = location.pathname.replace('mobile.html', '')
    const match = pathname.match(/^\/([^/]+)/)
    const prefix = match ? match[1] + '_' : 'de_v3_'
    localStorage.setItem(
      prefix + 'user.proxyInfo',
      JSON.stringify({ c: Date.now(), e: 0, v: proxy })
    )
  }, proxyInfo)
  return context
}
const embeddedCall = async (browser, jwt, url = '/user/info', data) => {
  const context = await guest(browser)
  try {
    const page = await context.newPage()
    await page.goto(host, { waitUntil: 'domcontentloaded' })
    return await page.evaluate(
      async ({ jwt, url, data, bi }) => {
        const { useEmbedded } = await import(
          bi + '/src/store/modules/embedded.ts'
        )
        useEmbedded().setToken(jwt)
        useEmbedded().setBaseUrl(bi)
        const { default: request } = await import(
          bi + '/src/config/axios/index.ts'
        )
        try {
          const result = await request[data ? 'post' : 'get']({
            url,
            data,
            handleErrorLocally: true
          })
          return { ok: true, code: result.code, hasData: !!result.data }
        } catch (e) {
          return {
            ok: false,
            status: e.httpStatus,
            category: e.category,
            reason: e.response?.headers?.['x-de-error-reason'],
            requestId: e.requestId,
            typed: e.name
          }
        }
      },
      { jwt, url, data, bi }
    )
  } finally {
    await context.close()
  }
}
test('valid application token authenticates independently of admin login', async ({
  browser
}) => {
  const result = await embeddedCall(browser, token())
  expect(result).toMatchObject({ ok: true, code: 0, hasData: true })
})
for (const kind of ['signature', 'expired', 'unknown-account'])
  test(kind + ' token is rejected by real gateway', async ({ browser }) => {
    const jwt =
      kind === 'signature'
        ? token({}, 'synthetic-wrong-secret')
        : kind === 'expired'
        ? token({ iat: 1, exp: 2 })
        : token({ account: name + '-missing' })
    const result = await embeddedCall(browser, jwt)
    expect(result.ok).toBe(false)
    expect(result.status).toBe(401)
    expect(result.category).toBe('UNAUTHENTICATED')
    expect(result.reason).toBe('UNAUTHENTICATED')
    expect(result.requestId).toMatch(/^[a-f0-9]{32}$/i)
  })
test('DIV library loads actual resource and survives destroy/reinitialize with valid embedded token', async ({
  browser
}) => {
  const context = await guest(browser)
  try {
    const page = await context.newPage()
    await page.goto(host)
    await page.addScriptTag({
      type: 'module',
      url: bi + '/src/pages/panel/main.ts'
    })
    await page.waitForFunction(() => typeof window.DataEaseBi === 'function')
    for (let i = 0; i < 2; i++) {
      const loaded = page.waitForResponse((r) =>
        r.url().includes('/dataVisualization/findById')
      )
      await page.evaluate(
        async ({ jwt, id, bi }) => {
          window.testEmbed = new window.DataEaseBi('Dashboard', {
            token: jwt,
            dvId: id,
            busiFlag: 'dashboard',
            baseUrl: bi
          })
          await window.testEmbed.initialize({ container: '#designer' })
        },
        { jwt: token(), id: resource.id, bi }
      )
      const response = await loaded
      expect(response.status()).toBe(200)
      expect((await response.json()).code).toBe(0)
      await expect(page.locator('.dashboard-preview')).toBeVisible()
      await page.evaluate(() => window.testEmbed.destroy())
      await expect(page.locator('.dashboard-preview')).toHaveCount(0)
    }
  } finally {
    await context.close()
  }
})
test('cross-origin iframe handshake validates origin and loads embedded dashboard', async ({
  browser
}) => {
  const context = await guest(browser)
  try {
    const page = await context.newPage()
    await page.goto(host)
    const checked = page.waitForResponse((r) =>
      r.url().includes('/embedded/initIframe')
    )
    const loaded = page.waitForResponse((r) =>
      r.url().includes('/dataVisualization/findById')
    )
    await page.evaluate(
      ({ jwt, id, bi }) => {
        window.addEventListener('message', (event) => {
          if (
            event.origin === bi &&
            event.data?.ready &&
            event.source === document.querySelector('iframe')?.contentWindow
          )
            event.source.postMessage(
              {
                'de-embedded': true,
                embeddedToken: jwt,
                type: 'Preview',
                dvId: id,
                busiFlag: 'dashboard'
              },
              bi
            )
        })
        const frame = document.createElement('iframe')
        frame.style.cssText = 'width:100vw;height:100vh;border:0'
        frame.src = bi + '/#/preview?dvId=' + id + '&dvType=dashboard'
        document.body.append(frame)
      },
      { jwt: token(), id: resource.id, bi }
    )
    expect((await checked).status()).toBe(200)
    expect((await loaded).status()).toBe(200)
    await expect(
      page.frameLocator('iframe').locator('.canvas-container').first()
    ).toBeVisible({ timeout: 20000 })
  } finally {
    await context.close()
  }
})

test('embedded request keeps typed parameter and missing-resource errors', async ({
  browser
}) => {
  const invalid = await embeddedCall(browser, token(), '/threshold/preview', {})
  expect(invalid).toMatchObject({
    ok: false,
    status: 400,
    category: 'VALIDATION'
  })
  const missing = await embeddedCall(
    browser,
    token(),
    '/auth/subjectPermission',
    { id: '999999999999999999', type: 1, flag: 'MENU', system: true }
  )
  expect(missing).toMatchObject({
    ok: false,
    status: 404,
    category: 'NOT_FOUND'
  })
})
test('application domain mismatch rejects a valid signed token from another browser origin', async ({
  browser
}) => {
  const context = await guest(browser)
  try {
    const page = await context.newPage()
    await page.goto('http://localhost:4209')
    const result = await page.evaluate(
      async ({ jwt, bi }) => {
        const { useEmbedded } = await import(
          bi + '/src/store/modules/embedded.ts'
        )
        useEmbedded().setToken(jwt)
        useEmbedded().setBaseUrl(bi)
        const { default: request } = await import(
          bi + '/src/config/axios/index.ts'
        )
        try {
          await request.get({ url: '/user/info', handleErrorLocally: true })
          return { ok: true }
        } catch (e) {
          return { ok: false, status: e.httpStatus, category: e.category }
        }
      },
      { jwt: token(), bi }
    )
    expect(result).toMatchObject({
      ok: false,
      status: 401,
      category: 'UNAUTHENTICATED'
    })
  } finally {
    await context.close()
  }
})
test('revoking only the temporary application rejects its previously valid token', async ({
  browser
}) => {
  const jwt = token()
  expect((await embeddedCall(browser, jwt)).ok).toBe(true)
  expect((await api('/embedded/delete/' + owned.appId, {})).code).toBe(0)
  owned.appId = null
  saveOwned()
  expect(await embeddedCall(browser, jwt)).toMatchObject({
    ok: false,
    status: 401,
    category: 'UNAUTHENTICATED'
  })
})
