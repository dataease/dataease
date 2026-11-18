import { test, expect } from '@playwright/test'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHmac } from 'node:crypto'
import { enterOrganization } from '../fixtures/configuration-security/organization.mjs'
const root = fileURLToPath(new URL('../../../', import.meta.url))
const settings = Object.fromEntries(
  fs
    .readFileSync(path.join(root, '.codex/playwright_mcp/.env.local'), 'utf8')
    .split('\n')
    .filter((line) => /^TEST_(USERNAME|PASSWORD)=/.test(line))
    .map((line) => {
      const i = line.indexOf('=')
      return [line.slice(0, i), JSON.parse(line.slice(i + 1))]
    })
)
const output = path.join(root, 'tests/browser/output/error-contract-auth')
const name = 'codex-auth-' + Date.now()
let adminContext,
  admin,
  password,
  resource,
  aged,
  agedContext,
  agedExpires,
  agedToken,
  base,
  app,
  proxyInfo,
  lastLogin = 0
const contexts = []
const observations = []
const owned = {
  userId: null,
  resourceId: null,
  appId: null,
  roles: [],
  orgId: null
}
const saveOwned = () => {
  fs.mkdirSync(output, { recursive: true })
  fs.writeFileSync(
    path.join(output, 'owned-resources.json'),
    JSON.stringify(owned),
    { mode: 0o600 }
  )
}
const call = (page, url, data, local = true) =>
  page.evaluate(
    async ({ url, data, local }) => {
      const { default: request } = await import('/src/config/axios/index.ts')
      try {
        const result = await request[data === undefined ? 'get' : 'post']({
          url,
          data,
          handleErrorLocally: local
        })
        return { ok: true, result }
      } catch (e) {
        return {
          ok: false,
          status: e.httpStatus,
          code: e.code,
          category: e.category,
          presented: e.presented,
          reason: e.response?.headers?.['x-de-error-reason'],
          requestId: e.requestId,
          message: e.message
        }
      }
    },
    { url, data, local }
  )
const api = async (url, data) => {
  const result = await call(admin, url, data)
  expect(result.ok, 'admin fixture API succeeds').toBe(true)
  return result.result
}
const prepare = async (browser) => {
  const ctx = await browser.newContext({ baseURL: base, locale: 'zh-CN' })
  contexts.push(ctx)
  const page = await ctx.newPage()
  await page.goto('/#/login', { waitUntil: 'domcontentloaded' })
  await expect(page.locator('input[type=password]')).toBeVisible({
    timeout: 30000
  })
  return { ctx, page }
}
const login = async (page, account = name, pwd = password, origin = 0) =>
  page.evaluate(
    async ({ account, pwd, origin }) => {
      const { ensureDekey, rsaEncryp } = await import(
        '/src/utils/encryption.ts'
      )
      await ensureDekey()
      const { default: request } = await import('/src/config/axios/index.ts')
      try {
        const response = await request.post({
          url: '/login/localLogin',
          data: { name: rsaEncryp(account), pwd: rsaEncryp(pwd), origin },
          handleErrorLocally: true
        })
        if (!response.data?.token)
          return {
            ok: false,
            noToken: true,
            mfa: !!response.data?.mfa,
            invalidPassword: !!response.data?.invalidPwd
          }
        const { useUserStoreWithOut } = await import(
          '/src/store/modules/user.ts'
        )
        const store = useUserStoreWithOut()
        store.setToken(response.data.token)
        store.setExp(response.data.exp)
        store.setTime(Date.now())
        const payload = JSON.parse(
          atob(
            response.data.token
              .split('.')[1]
              .replace(/-/g, '+')
              .replace(/_/g, '/')
          )
        )
        store.setUid(String(payload.uid))
        history.replaceState(null, '', '/#/auth-regression-context')
        return { ok: true, expires: payload.exp * 1000 }
      } catch (e) {
        return {
          ok: false,
          status: e.httpStatus,
          code: e.code,
          category: e.category
        }
      }
    },
    { account, pwd, origin }
  )
const session = async (browser) => {
  const { ctx, page } = await prepare(browser)
  const delay = lastLogin + 1200 - Date.now()
  if (delay > 0) await new Promise((r) => setTimeout(r, delay))
  const authenticated = await login(
    page,
    settings.TEST_USERNAME,
    settings.TEST_PASSWORD
  )
  lastLogin = Date.now()
  expect(authenticated.ok, JSON.stringify(authenticated)).toBe(true)
  await page.evaluate(async (proxy) => {
    const { useUserStoreWithOut } = await import('/src/store/modules/user.ts')
    useUserStoreWithOut().setProxyInfo(proxy)
    const router =
      document.querySelector('#app').__vue_app__.config.globalProperties.$router
    await router.push('/401')
  }, proxyInfo)
  return { ctx, page }
}
const restricted = async (browser) => {
  const { ctx, page } = await prepare(browser)
  const enc = (value) =>
    Buffer.from(JSON.stringify(value)).toString('base64url')
  const body =
    enc({ alg: 'HS256', typ: 'JWT' }) +
    '.' +
    enc({
      account: name,
      appId: app.appId,
      iat: Math.floor(Date.now() / 1000),
      exp: Math.floor(Date.now() / 1000) + 300
    })
  const jwt =
    body +
    '.' +
    createHmac('sha256', app.appSecret).update(body).digest('base64url')
  await page.evaluate(async (jwt) => {
    ;(await import('/src/store/modules/embedded.ts'))
      .useEmbedded()
      .setToken(jwt)
    history.replaceState(null, '', '/#/auth-regression-context')
  }, jwt)
  expect((await call(page, '/user/info')).ok).toBe(true)
  return { ctx, page }
}
const cacheState = (page) =>
  page.evaluate(async () => {
    const { useCache } = await import('/src/hooks/web/useCache.ts')
    const { useRequestStoreWithOut } = await import(
      '/src/store/modules/request.ts'
    )
    return {
      embedded: !!(await import('/src/store/modules/embedded.ts')).useEmbedded()
        .getToken,
      token: !!useCache().wsCache.get('user.token'),
      hash: location.hash,
      queued: useRequestStoreWithOut().getRequestList.length
    }
  })
const switchAdminOrg = async (oid) =>
  admin.evaluate(async (oid) => {
    const { ensureDekey, rsaEncryp } = await import('/src/utils/encryption.ts')
    await ensureDekey()
    ;(await import('/src/store/modules/user.ts'))
      .useUserStoreWithOut()
      .setProxyInfo({
        proxy: true,
        proxyOid: String(oid),
        proxySecret: rsaEncryp(String(oid))
      })
  }, oid)
test.beforeAll(async ({ browser, baseURL }) => {
  base = baseURL
  saveOwned()
  const prepared = await prepare(browser)
  adminContext = prepared.ctx
  admin = prepared.page
  expect(
    await login(admin, settings.TEST_USERNAME, settings.TEST_PASSWORD)
  ).toMatchObject({ ok: true })
  await admin.goto('/')
  await enterOrganization(admin)
  const proxy = await admin.evaluate(async () =>
    (await import('/src/hooks/web/useCache.ts'))
      .useCache()
      .wsCache.get('user.proxyInfo')
  )
  proxyInfo = proxy
  const engine = await call(admin, '/engine/getEngine')
  fs.writeFileSync(
    path.join(output, 'local-prerequisites.json'),
    JSON.stringify({
      engineConfigured: !!engine.result?.data?.type,
      engineType: engine.result?.data?.type || null
    }),
    { mode: 0o600 }
  )
  password = (await api('/user/defaultPwd')).data
  const created = await api('/user/create', {
    account: name,
    name,
    email: name + '@example.invalid',
    roleIds: [],
    enable: true,
    mfaEnable: false,
    defaultOid: proxy.proxyOid,
    variables: []
  })
  owned.userId = String(created.data)
  saveOwned()
  await api('/embedded/create', { name, domain: base, secretLength: 32 })
  const applications = (
    await api('/embedded/pager/1/100', { keyword: name })
  ).data.records.filter((item) => item.name === name)
  expect(applications.length).toBe(1)
  app = applications[0]
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
    if (!parent) throw new Error('No test dashboard directory')
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
      contentId: 'auth-' + Date.now(),
      mobileLayout: false
    }
    return {
      ...data,
      id: String(
        (await request.post({ url: '/dataVisualization/saveCanvas', data }))
          .data
      )
    }
  }, name)
  owned.resourceId = resource.id
  saveOwned()
  await api('/dataVisualization/updatePublishStatus', {
    id: resource.id,
    name,
    type: 'dashboard',
    status: 1,
    mobileLayout: false,
    activeViewIds: []
  })
  const natural = await session(browser)
  aged = natural.page
  agedContext = natural.ctx
  agedExpires = (await cacheState(aged)).token
    ? await aged.evaluate(async () => {
        const { useCache } = await import('/src/hooks/web/useCache.ts')
        const cache = useCache().wsCache
        const payload = JSON.parse(
          atob(
            cache
              .get('user.token')
              .split('.')[1]
              .replace(/-/g, '+')
              .replace(/_/g, '/')
          )
        )
        cache.set('user.exp', 0)
        return payload.exp * 1000
      })
    : 0
  agedToken = await aged.evaluate(async () =>
    (await import('/src/hooks/web/useCache.ts'))
      .useCache()
      .wsCache.get('user.token')
  )
  expect(agedExpires).toBeGreaterThan(Date.now())
  // JWTs issued for the same account within one second can be identical. Keep the aging token independent.
  await new Promise((resolve) => setTimeout(resolve, 2100))
})
test.afterAll(async () => {
  for (const ctx of contexts)
    if (ctx !== adminContext) await ctx.close().catch(() => {})
  if (admin && !admin.isClosed()) {
    expect(
      (await login(admin, settings.TEST_USERNAME, settings.TEST_PASSWORD)).ok
    ).toBe(true)
    await switchAdminOrg(proxyInfo.proxyOid)
    if (owned.appId) {
      await api('/embedded/delete/' + owned.appId, {})
      owned.appId = null
      saveOwned()
    }
    for (const rid of owned.roles) {
      await api('/role/delete/' + rid, {})
    }
    owned.roles = []
    saveOwned()
    if (owned.userId) {
      await api('/user/enable', { id: owned.userId, enable: false })
      await api('/user/delete/' + owned.userId, {})
      owned.userId = null
      saveOwned()
    }
    if (owned.orgId) {
      await switchAdminOrg(owned.orgId)
      await api('/org/page/delete', { id: owned.orgId, delResource: true })
      owned.orgId = null
      saveOwned()
      await switchAdminOrg(proxyInfo.proxyOid)
    }
    if (owned.resourceId) {
      await api('/dataVisualization/deleteLogic', {
        id: owned.resourceId,
        busiFlag: 'dashboard'
      })
      owned.resourceId = null
      saveOwned()
    }
  }
  fs.writeFileSync(
    path.join(output, 'observations-' + process.pid + '.json'),
    JSON.stringify(observations),
    { mode: 0o600 }
  )
  password = null
  await adminContext?.close()
})
test('initial-password policy rejects temporary account before issuing login token', async ({
  browser
}) => {
  const { page } = await prepare(browser)
  expect(await login(page)).toMatchObject({
    ok: false,
    noToken: true,
    invalidPassword: true
  })
  expect((await cacheState(page)).token).toBe(false)
})
test('wrong password and nonexistent account cannot obtain login token', async ({
  browser
}) => {
  const { page } = await prepare(browser)
  const wrong = await login(page, name, 'synthetic-wrong-password')
  observations.push({ scenario: 'wrong-password', ...wrong })
  expect(wrong).toMatchObject({
    ok: false,
    status: 400,
    code: 40001,
    category: 'VALIDATION'
  })
  const missing = await login(
    page,
    name + '-missing',
    'synthetic-wrong-password'
  )
  observations.push({ scenario: 'unknown-account', ...missing })
  expect(missing).toMatchObject({
    ok: false,
    status: 400,
    code: 40001,
    category: 'VALIDATION'
  })
  expect((await cacheState(page)).token).toBe(false)
})
test('disabled temporary user cannot login; reenable restores initial-password policy', async ({
  browser
}) => {
  const { page } = await prepare(browser)
  await api('/user/enable', { id: owned.userId, enable: false })
  try {
    const disabled = await login(page)
    observations.push({ scenario: 'disabled-login', ...disabled })
    expect(disabled).toMatchObject({
      ok: false,
      status: 403,
      code: 40001,
      category: 'FORBIDDEN'
    })
  } finally {
    await api('/user/enable', { id: owned.userId, enable: true })
  }
  expect(await login(page)).toMatchObject({
    ok: false,
    noToken: true,
    invalidPassword: true
  })
})
test('six simultaneous ordinary requests perform one real refresh and all resolve', async ({
  browser
}) => {
  const { page } = await session(browser)
  let refreshes = 0
  page.on('request', (r) => {
    if (r.url().includes('/login/refresh')) refreshes++
  })
  const result = await page.evaluate(async () => {
    const { useCache } = await import('/src/hooks/web/useCache.ts')
    useCache().wsCache.set('user.time', Date.now() - 91000)
    const { default: request } = await import('/src/config/axios/index.ts')
    const results = await Promise.allSettled(
      Array.from({ length: 6 }, () =>
        request.get({ url: '/user/info', handleErrorLocally: true })
      )
    )
    return {
      passed: results.filter(
        (x) => x.status === 'fulfilled' && x.value.code === 0
      ).length
    }
  })
  expect(result.passed).toBe(6)
  expect(refreshes).toBe(1)
  expect(await cacheState(page)).toMatchObject({
    token: true,
    queued: 0
  })
})
test('revoked session refresh rejects all waiting requests and drains refresh queue', async ({
  browser
}) => {
  const { page } = await session(browser)
  expect((await call(page, '/logout')).ok).toBe(true)
  let refreshes = 0
  page.on('request', (r) => {
    if (r.url().includes('/login/refresh')) refreshes++
  })
  const result = await page.evaluate(async () => {
    const { useCache } = await import('/src/hooks/web/useCache.ts')
    useCache().wsCache.set('user.time', Date.now() - 91000)
    const { default: request } = await import('/src/config/axios/index.ts')
    const results = await Promise.allSettled(
      Array.from({ length: 6 }, () => request.get({ url: '/user/info' }))
    )
    return {
      rejected: results.filter(
        (x) => x.status === 'rejected' && x.reason.httpStatus === 401
      ).length
    }
  })
  expect(result.rejected).toBe(6)
  expect(refreshes).toBe(1)
  expect(await cacheState(page)).toMatchObject({
    token: false,
    queued: 0
  })
})
test('logout followed by protected request yields 401 and clears session to login', async ({
  browser
}) => {
  const { page } = await session(browser)
  expect((await call(page, '/logout')).ok).toBe(true)
  const result = await call(page, '/user/info', undefined, false)
  expect(result).toMatchObject({
    ok: false,
    status: 401,
    category: 'UNAUTHENTICATED'
  })
  await expect
    .poll(() => cacheState(page))
    .toMatchObject({ token: false, queued: 0 })
  await expect
    .poll(async () => (await cacheState(page)).hash)
    .toContain('login')
})
for (const scenario of ['menu', 'read', 'manage', 'detail'])
  test(
    'restricted identity API permission rejection: ' + scenario,
    async ({ browser }) => {
      const { page } = await restricted(browser)
      if (scenario === 'detail')
        expect(
          (
            await call(
              admin,
              '/dataVisualization/viewDetailList/' + resource.id
            )
          ).ok
        ).toBe(true)
      const result =
        scenario === 'menu'
          ? await call(page, '/role/query')
          : scenario === 'read'
          ? await call(page, '/dataVisualization/findById', {
              id: resource.id,
              busiFlag: 'dashboard',
              source: 'main-edit',
              resourceTable: 'snapshot'
            })
          : scenario === 'manage'
          ? await call(page, '/dataVisualization/updateCanvas', {
              ...resource,
              checkHistory: true,
              expectedContentId: resource.contentId,
              contentId: 'forbidden-write'
            })
          : await call(page, '/dataVisualization/viewDetailList/' + resource.id)
      observations.push({ scenario: 'permission-' + scenario, ...result })
      expect(result).toMatchObject({
        ok: false,
        status: 403,
        category: 'FORBIDDEN'
      })
      expect((await cacheState(page)).embedded).toBe(true)
      expect((await call(page, '/user/info')).ok).toBe(true)
      const unchanged = await api('/dataVisualization/findById', {
        id: resource.id,
        busiFlag: 'dashboard',
        source: 'main-edit',
        resourceTable: 'snapshot'
      })
      expect(unchanged.data.contentId).toBe(resource.contentId)
    }
  )
test('real permission failure presents one permission dialog without clearing session', async ({
  browser
}) => {
  const { page } = await restricted(browser)
  expect(await call(page, '/role/query', undefined, false)).toMatchObject({
    ok: false,
    status: 403,
    category: 'FORBIDDEN'
  })
  await expect(page.locator('.ed-message-box')).toHaveCount(1)
  expect((await cacheState(page)).embedded).toBe(true)
  expect((await cacheState(page)).hash).not.toContain('login')
  await page
    .locator('.ed-message-box')
    .getByRole('button', { name: '取消', exact: true })
    .click()
  expect((await call(page, '/user/info')).ok).toBe(true)
})
test('a valid ordinary token naturally expires on backend and returns 401 without frontend renewal', async () => {
  test.setTimeout(140000)
  const remaining = agedExpires - Date.now() + 1300
  expect(remaining).toBeLessThan(120000)
  if (remaining > 0)
    await new Promise((resolve) => setTimeout(resolve, remaining))
  let refreshes = 0
  aged.on('request', (r) => {
    if (r.url().includes('/login/refresh')) refreshes++
  })
  const result = await call(aged, '/user/info', undefined, false)
  expect(result).toMatchObject({
    ok: false,
    status: 401,
    category: 'UNAUTHENTICATED'
  })
  expect(refreshes).toBe(0)
  expect((await cacheState(aged)).token).toBe(false)
})

test('malformed RSA login payload is a validation failure rather than an authentication loop', async ({
  browser
}) => {
  const { page } = await prepare(browser)
  expect(
    await call(page, '/login/localLogin', {
      name: 'synthetic-invalid-rsa',
      pwd: 'synthetic-invalid-rsa',
      origin: 0
    })
  ).toMatchObject({ ok: false, status: 400, category: 'VALIDATION' })
  expect((await cacheState(page)).token).toBe(false)
})

test('naturally expired ordinary JWT refreshes inside the remaining server session window', async ({
  browser
}) => {
  test.setTimeout(140000)
  const remaining = agedExpires - Date.now() + 1300
  if (remaining > 0)
    await new Promise((resolve) => setTimeout(resolve, remaining))
  const { page } = await prepare(browser)
  await page.evaluate(async (token) => {
    const { useUserStoreWithOut } = await import('/src/store/modules/user.ts')
    const store = useUserStoreWithOut()
    const payload = JSON.parse(
      atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))
    )
    store.setToken(token)
    store.setUid(String(payload.uid))
    store.setExp(payload.exp * 1000)
    store.setTime(Date.now() - 91000)
  }, agedToken)
  let refreshes = 0
  page.on('request', (r) => {
    if (r.url().includes('/login/refresh')) refreshes++
  })
  expect((await call(page, '/user/info')).ok).toBe(true)
  expect(refreshes).toBe(1)
  const rotated = await page.evaluate(
    async (old) =>
      (await import('/src/hooks/web/useCache.ts'))
        .useCache()
        .wsCache.get('user.token') !== old,
    agedToken
  )
  expect(rotated).toBe(true)
  expect(await cacheState(page)).toMatchObject({
    token: true,
    queued: 0
  })
})

test('actual login form wrong password presents one error and releases loading', async ({
  browser
}) => {
  const { page } = await prepare(browser)
  await page.locator('input').filter({ visible: true }).first().fill(name)
  await page.locator('input[type=password]').fill('synthetic-wrong-password')
  const button = page.getByRole('button', { name: /登\s*录|Login/i })
  const response = page.waitForResponse((r) =>
    r.url().includes('/login/localLogin')
  )
  await button.click()
  expect((await response).status()).toBe(400)
  await expect(page.locator('.ed-message--error')).toHaveCount(1)
  await expect(button).not.toHaveClass(/is-loading/)
  await expect(button).toBeEnabled()
  expect((await cacheState(page)).token).toBe(false)
  expect(page.url()).toContain('/login')
})

test('legacy login keeps its status and fields while modern login is explicit validation', async ({
  browser
}) => {
  const { page } = await prepare(browser)
  const result = await page.evaluate(async (account) => {
    const { ensureDekey, rsaEncryp } = await import('/src/utils/encryption.ts')
    await ensureDekey()
    const { service } = await import('/src/config/axios/service.ts')
    const { securityConfig } = await import('/src/config/axios/hmac.ts')
    let config, modern
    try {
      await service({
        url: '/login/localLogin',
        method: 'post',
        data: {
          name: rsaEncryp(account),
          pwd: rsaEncryp('synthetic-wrong-password'),
          origin: 0
        },
        handleErrorLocally: true
      })
    } catch (e) {
      config = e.config
      modern = { status: e.httpStatus, category: e.category }
    }
    config.headers.set('X-DE-Error-Contract', '1')
    await securityConfig(config, service.getUri(config))
    const response = await fetch(service.getUri(config), {
      method: 'POST',
      headers: config.headers.toJSON(),
      body: config.data
    })
    const body = await response.json()
    return {
      modern,
      legacy: {
        status: response.status,
        code: body.code,
        keys: Object.keys(body).sort()
      }
    }
  }, name + '-legacy')
  expect(result.modern).toEqual({ status: 400, category: 'VALIDATION' })
  expect(result.legacy).toEqual({
    status: 500,
    code: 40001,
    keys: ['code', 'data', 'msg']
  })
})
test('disabled-account login form presents one inline error without permission dialog', async ({
  browser
}) => {
  const { page } = await prepare(browser)
  await api('/user/enable', { id: owned.userId, enable: false })
  try {
    await page.locator('input').filter({ visible: true }).first().fill(name)
    await page.locator('input[type=password]').fill(password)
    const button = page.getByRole('button', { name: /登\s*录|Login/i })
    const response = page.waitForResponse((r) =>
      r.url().includes('/login/localLogin')
    )
    await button.click()
    expect((await response).status()).toBe(403)
    await expect(page.locator('.ed-message--error')).toHaveCount(1)
    await expect(page.locator('.ed-message-box')).toHaveCount(0)
    await expect(button).not.toHaveClass(/is-loading/)
    expect((await cacheState(page)).token).toBe(false)
    expect(page.url()).toContain('/login')
  } finally {
    await api('/user/enable', { id: owned.userId, enable: true })
  }
})

test('two tabs sharing login storage finish simultaneous refresh without stranded queues', async ({
  browser
}) => {
  const { ctx, page } = await session(browser)
  const second = await ctx.newPage()
  await second.goto('/#/401', { waitUntil: 'domcontentloaded' })
  await second.waitForFunction(
    () => !!document.querySelector('#app')?.__vue_app__
  )
  for (const tab of [page, second])
    await tab.evaluate(async () => {
      await import('/src/config/axios/index.ts')
      await import('/src/hooks/web/useCache.ts')
    })
  await ctx.route('**/login/refresh*', async (route) => {
    await new Promise((resolve) => setTimeout(resolve, 450))
    await route.continue()
  })
  await page.evaluate(async () =>
    (await import('/src/hooks/web/useCache.ts'))
      .useCache()
      .wsCache.set('user.time', Date.now() - 91000)
  )
  const send = (tab) =>
    tab.evaluate(async () => {
      const { default: request } = await import('/src/config/axios/index.ts')
      const results = await Promise.allSettled(
        Array.from({ length: 4 }, () =>
          request.get({ url: '/user/info', handleErrorLocally: true })
        )
      )
      return results.filter(
        (r) => r.status === 'fulfilled' && r.value.code === 0
      ).length
    })
  const completed = await Promise.race([
    Promise.all([send(page), send(second)]),
    new Promise((resolve) => setTimeout(() => resolve(null), 8000))
  ])
  const states = await Promise.all([cacheState(page), cacheState(second)])
  expect(completed, JSON.stringify(states)).toEqual([4, 4])
  expect(states.every((state) => state.queued === 0 && state.token)).toBe(true)
})

test('custom roles enforce read/manage/revoke and cross-organization isolation', async ({
  browser
}) => {
  await api('/role/create', { name: name + '-reader', typeCode: 0 })
  const reader = String(
    (await api('/role/query')).data.find((x) => x.name === name + '-reader').id
  )
  owned.roles.push(reader)
  saveOwned()
  await api('/role/create', { name: name + '-manager', typeCode: 7 })
  const manager = String(
    (await api('/role/query')).data.find((x) => x.name === name + '-manager').id
  )
  owned.roles.push(manager)
  saveOwned()
  await api('/org/page/create', {
    name: name + '-other-org',
    pid: 0,
    adminIds: []
  })
  const flatten = (items) =>
    (items || []).flatMap((item) => [item, ...flatten(item.children)])
  owned.orgId = String(
    flatten((await api('/org/page/tree')).data).find(
      (x) => x.name === name + '-other-org'
    ).id
  )
  saveOwned()
  let other
  try {
    await switchAdminOrg(owned.orgId)
    other = await admin.evaluate(async (name) => {
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
      if (!parent) throw new Error('No temporary organization directory')
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
        contentId: 'other-org-' + Date.now(),
        mobileLayout: false
      }
      return {
        ...data,
        id: String(
          (await request.post({ url: '/dataVisualization/saveCanvas', data }))
            .data
        )
      }
    }, name + '-other')
  } finally {
    await switchAdminOrg(proxyInfo.proxyOid)
  }
  const grant = async (rid, weight) =>
    api('/auth/saveSubjectPermission', {
      id: rid,
      type: 1,
      flag: 'PANEL',
      oid: proxyInfo.proxyOid,
      system: false,
      permissions: [
        { id: resource.id, weight, ext: 0, oid: proxyInfo.proxyOid }
      ]
    })
  const assign = async (rid) =>
    api('/user/edit', {
      id: owned.userId,
      account: name,
      name,
      email: name + '@example.invalid',
      roleIds: [rid],
      enable: true,
      mfaEnable: false,
      defaultOid: proxyInfo.proxyOid,
      variables: []
    })
  await grant(reader, 1)
  await grant(manager, 7)
  await assign(reader)
  const { page } = await restricted(browser)
  const read = () =>
    call(page, '/dataVisualization/findById', {
      id: resource.id,
      busiFlag: 'dashboard',
      source: 'main-edit',
      resourceTable: 'snapshot'
    })
  await expect.poll(async () => (await read()).ok).toBe(true)
  expect(
    await call(page, '/dataVisualization/updateCanvas', {
      ...resource,
      checkHistory: true,
      expectedContentId: resource.contentId,
      contentId: 'reader-must-not-write'
    })
  ).toMatchObject({ ok: false, status: 403, category: 'FORBIDDEN' })
  expect(
    await call(page, '/dataVisualization/findById', {
      id: other.id,
      busiFlag: 'dashboard',
      source: 'main-edit',
      resourceTable: 'snapshot'
    })
  ).toMatchObject({ ok: false, status: 403, category: 'FORBIDDEN' })
  await assign(manager)
  const next = 'manager-' + Date.now()
  expect(
    (
      await call(page, '/dataVisualization/updateCanvas', {
        ...resource,
        checkHistory: true,
        expectedContentId: resource.contentId,
        contentId: next
      })
    ).ok
  ).toBe(true)
  resource.contentId = next
  expect((await read()).result.data.contentId).toBe(next)
  await grant(manager, 0)
  await expect.poll(async () => (await read()).status).toBe(403)
  expect((await call(page, '/user/info')).ok).toBe(true)
})

const ldapCredentials = () => {
  const rows = fs
    .readFileSync(process.env.LDAP_TEST_CREDENTIAL_FILE, 'utf8')
    .trimEnd()
    .split(/\r?\n/)
    .filter(Boolean)
  if (rows.length !== 2)
    throw new Error(
      'LDAP test credentials must contain account and password on separate lines'
    )
  return { account: rows[0].trim(), password: rows[1] }
}
test('configured LDAP authenticates supplied test identity and keeps configuration unchanged', async ({
  browser
}) => {
  test.skip(
    !process.env.LDAP_TEST_CREDENTIAL_FILE,
    'Requires a local protected LDAP test credential file'
  )
  const before = (await api('/setting/authentication/grid')).data.find(
    (x) => x.name === 'ldap'
  )
  expect(before?.enable).toBe(true)
  const credentials = ldapCredentials()
  const { page } = await prepare(browser)
  const result = await login(page, credentials.account, credentials.password, 1)
  expect(result).toMatchObject({ ok: true })
  expect((await call(page, '/user/info')).ok).toBe(true)
  expect((await call(page, '/logout')).ok).toBe(true)
  expect(await call(page, '/user/info')).toMatchObject({
    ok: false,
    status: 401,
    category: 'UNAUTHENTICATED'
  })
  const after = (await api('/setting/authentication/grid')).data.find(
    (x) => x.name === 'ldap'
  )
  expect(after).toEqual(before)
})
test('configured LDAP rejects wrong password and missing identity without issuing a token', async ({
  browser
}) => {
  test.skip(
    !process.env.LDAP_TEST_CREDENTIAL_FILE,
    'Requires a local protected LDAP test credential file'
  )
  const credentials = ldapCredentials()
  const { page } = await prepare(browser)
  expect(
    await login(page, credentials.account, 'synthetic-wrong-ldap-password', 1)
  ).toMatchObject({ ok: false, status: 400, category: 'VALIDATION' })
  expect(
    await login(
      page,
      name + '-ldap-missing',
      'synthetic-wrong-ldap-password',
      1
    )
  ).toMatchObject({ ok: false, status: 400, category: 'VALIDATION' })
  expect((await cacheState(page)).token).toBe(false)
})

test('complete server session TTL expires before JWT and clears both tabs', async ({
  browser
}) => {
  test.skip(
    process.env.TEST_SHORT_SESSION !== '1',
    'Requires the authorized temporary one-minute local backend session timeout'
  )
  test.setTimeout(90000)
  const { ctx, page } = await session(browser)
  const second = await ctx.newPage()
  await second.goto('/#/401', { waitUntil: 'domcontentloaded' })
  await second.waitForFunction(
    () => !!document.querySelector('#app')?.__vue_app__
  )
  expect((await call(page, '/user/info')).ok).toBe(true)
  expect((await call(second, '/user/info')).ok).toBe(true)
  const jwtExpires = await page.evaluate(async () => {
    const { useCache } = await import('/src/hooks/web/useCache.ts')
    const token = useCache().wsCache.get('user.token')
    return (
      JSON.parse(
        atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))
      ).exp * 1000
    )
  })
  expect(jwtExpires - Date.now()).toBeGreaterThan(67000)
  let refreshes = 0
  for (const tab of [page, second])
    tab.on('request', (r) => {
      if (r.url().includes('/login/refresh')) refreshes++
    })
  await new Promise((resolve) => setTimeout(resolve, 65000))
  expect(Date.now()).toBeLessThan(jwtExpires)
  const results = await Promise.all([
    call(page, '/user/info', undefined, false),
    call(second, '/user/info', undefined, false)
  ])
  for (const result of results)
    expect(result).toMatchObject({
      ok: false,
      status: 401,
      category: 'UNAUTHENTICATED'
    })
  expect(refreshes).toBe(0)
  for (const tab of [page, second]) {
    expect(await cacheState(tab)).toMatchObject({
      token: false,
      queued: 0
    })
    await expect
      .poll(async () => (await cacheState(tab)).hash)
      .toContain('login')
  }
})
