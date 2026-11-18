import { test, expect } from '@playwright/test'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { enterOrganization } from '../fixtures/configuration-security/organization.mjs'
const root = fileURLToPath(new URL('../../../', import.meta.url))
const settings = Object.fromEntries(
  fs
    .readFileSync(path.join(root, '.codex/playwright_mcp/.env.local'), 'utf8')
    .split('\n')
    .filter((line) => /^TEST_(USERNAME|PASSWORD)=/.test(line))
    .map((line) => {
      const index = line.indexOf('=')
      return [line.slice(0, index), JSON.parse(line.slice(index + 1))]
    })
)
let context, page
const traceRecords = []
const owned = []
const ownedFiles = []
const call = (url, data, method = 'post', local = true) =>
  page.evaluate(
    async ({ url, data, method, local }) => {
      const { default: request } = await import('/src/config/axios/index.ts')
      const { DataEaseError } = await import('/src/config/axios/error.ts')
      try {
        const result = await request[method]({
          url,
          data,
          handleErrorLocally: local
        })
        return { ok: true, result }
      } catch (error) {
        return {
          ok: false,
          typed: error instanceof DataEaseError,
          code: error.code,
          status: error.httpStatus,
          category: error.category,
          requestId: error.requestId,
          message: error.message,
          presented: error.presented,
          reason: error.response?.headers?.['x-de-error-reason'],
          server: error.response?.headers?.server,
          bodyRequestId: error.response?.data?.requestId,
          headerRequestId: error.response?.headers?.['x-de-request-id'],
          data: error.response?.data?.data
        }
      }
    },
    { url, data, method, local }
  )

test.beforeAll(async ({ browser, baseURL }) => {
  context = await browser.newContext({ baseURL, locale: 'zh-CN' })
  page = await context.newPage()
  page.on('response', (response) => {
    const headers = response.headers()
    if (response.status() >= 400 && headers['x-de-request-id'])
      traceRecords.push({
        path: new URL(response.url()).pathname.replace(/\d{9,}/g, ':id'),
        status: response.status(),
        requestId: headers['x-de-request-id'],
        server: headers.server
      })
  })
  await page.goto('/#/login', { waitUntil: 'domcontentloaded' })
  await expect(page.locator('input[type=password]')).toBeVisible({
    timeout: 30000
  })
  await page
    .locator('input')
    .filter({ visible: true })
    .first()
    .fill(settings.TEST_USERNAME)
  await page.locator('input[type=password]').fill(settings.TEST_PASSWORD)
  const response = page.waitForResponse((r) =>
    r.url().includes('/login/localLogin')
  )
  await page.getByRole('button', { name: /登\s*录|Login/i }).click()
  const login = await response
  expect(login.status()).toBe(200)
  expect((await login.json()).code).toBe(0)
  await page.waitForFunction(() => !location.hash.includes('login'))
  await enterOrganization(page)
  fs.mkdirSync(path.join(root, 'tests/browser/.auth'), { recursive: true })
  await context.storageState({
    path: path.join(root, 'tests/browser/.auth/error-contract-live.json')
  })
  fs.chmodSync(
    path.join(root, 'tests/browser/.auth/error-contract-live.json'),
    0o600
  )
})

test.afterAll(async () => {
  if (page && !page.isClosed()) {
    for (const id of owned.reverse()) {
      const result = await call('/dataVisualization/deleteLogic', {
        id,
        busiFlag: 'dashboard'
      })
      expect(result.ok, 'synthetic dashboard cleanup').toBe(true)
    }
  }
  fs.writeFileSync(
    path.join(
      root,
      'tests/browser/output/error-contract-live/request-traces.json'
    ),
    JSON.stringify(traceRecords),
    { mode: 0o600 }
  )
  for (const file of ownedFiles) {
    const candidate = path.join('/opt/dataease3.0/data/static-resource', file)
    if (
      fs.existsSync(candidate) &&
      fs.lstatSync(candidate).isFile() &&
      !fs.lstatSync(candidate).isSymbolicLink()
    )
      fs.unlinkSync(candidate)
  }
  fs.writeFileSync(
    path.join(
      root,
      'tests/browser/output/error-contract-live/owned-resources.json'
    ),
    '[]',
    { mode: 0o600 }
  )
  await context?.close()
})

test('real application mounts, authenticated menus and datasource page render', async () => {
  const errors = []
  page.on('pageerror', (e) => errors.push(e.message))
  await page.goto('/#/data/datasource')
  await expect(page.locator('#app')).not.toBeEmpty()
  await expect(page.getByText('数据源', { exact: true }).first()).toBeVisible()
  const result = await call('/menu/query', undefined, 'get')
  expect(result.ok).toBe(true)
  expect(result.result.code).toBe(0)
  expect(errors).toEqual([])
})

test('real gateway forwards parameter errors and request identity', async () => {
  const result = await call('/threshold/preview', {})
  expect(result.ok).toBe(false)
  expect(result.typed).toBe(true)
  expect(result.status).toBe(400)
  expect(result.category).toBe('VALIDATION')
  expect(result.requestId).toMatch(/^[a-f0-9]{32}$/)
  expect(result.headerRequestId).toBe(result.bodyRequestId)
  expect(result.server).toMatch(/APISIX/)
  expect(result.message).not.toMatch(/Cannot invoke|Exception|java\./)
})

test('nonexistent role is a typed 404 without NPE', async () => {
  const result = await call('/auth/subjectPermission', {
    id: '999999999999999999',
    type: 1,
    flag: 'MENU',
    system: true
  })
  expect(result.ok).toBe(false)
  expect(result.status).toBe(404)
  expect(result.category).toBe('NOT_FOUND')
  expect(result.message).not.toMatch(/Cannot invoke|Exception/)
})

for (const [file, expected, category] of [
  ['ok.csv', 200, null],
  ['failed.csv', 502, 'UPSTREAM'],
  ['slow.csv', 504, 'UPSTREAM_TIMEOUT'],
  ['invalid.xlsx', 400, 'VALIDATION']
]) {
  test(`real remote file ${file}: ${expected}`, async () => {
    const result = await page.evaluate(async (file) => {
      const { loadRemoteFile } = await import('/src/api/datasource.ts')
      try {
        const response = await loadRemoteFile({
          url: 'http://127.0.0.1:4208/' + file,
          userName: '',
          passwd: '',
          datasourceId: 0,
          editType: 0,
          maxFileSizeMb: 1,
          transferTimeoutSeconds: 1
        })
        return {
          ok: true,
          code: response.code,
          sheets: response.data?.sheets?.length
        }
      } catch (error) {
        return {
          ok: false,
          status: error.httpStatus,
          category: error.category,
          message: error.message,
          requestId: error.requestId,
          headerId: error.response?.headers?.['x-de-request-id']
        }
      }
    }, file)
    if (expected === 200) {
      expect(result.ok).toBe(true)
      expect(result.code).toBe(0)
      expect(result.sheets).toBeGreaterThan(0)
    } else {
      expect(result.ok).toBe(false)
      expect(result.status).toBe(expected)
      expect(result.category).toBe(category)
      expect(result.requestId).toBe(result.headerId)
      expect(result.message).not.toMatch(/Cannot invoke|127\.0\.0\.1|Exception/)
    }
  })
}

let dashboard
const ensureDashboard = async () => {
  if (dashboard) return dashboard
  dashboard = await page.evaluate(async () => {
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
    if (!parent)
      throw new Error('No authorized organization dashboard directory')
    const base = {
      name: 'codex-error-contract-' + Date.now(),
      type: 'dashboard',
      nodeType: 'leaf',
      pid: String(parent.id),
      componentData: '[]',
      canvasViewInfo: {},
      canvasStyleData: JSON.stringify({
        ...dvMainStoreWithOut().canvasStyleData,
        dvType: 'dashboard'
      }),
      contentId: 'initial-' + Date.now(),
      mobileLayout: false
    }
    const response = await request.post({
      url: '/dataVisualization/saveCanvas',
      data: base
    })
    return { ...base, id: String(response.data) }
  })
  owned.push(dashboard.id)
  fs.writeFileSync(
    path.join(
      root,
      'tests/browser/output/error-contract-live/owned-resources.json'
    ),
    JSON.stringify(owned),
    { mode: 0o600 }
  )
  return dashboard
}
const readDashboard = async () => {
  const resource = await ensureDashboard()
  const response = await call('/dataVisualization/findById', {
    id: resource.id,
    busiFlag: 'dashboard',
    source: 'main-edit',
    resourceTable: 'snapshot'
  })
  expect(response.ok, 'read synthetic dashboard').toBe(true)
  return response.result.data
}

test('create, save and reopen an independent dashboard', async () => {
  const resource = await ensureDashboard()
  const saved = await call('/dataVisualization/updateCanvas', {
    ...resource,
    checkHistory: true,
    expectedContentId: resource.contentId,
    contentId: 'saved-' + Date.now()
  })
  expect(saved.ok).toBe(true)
  const reopened = await readDashboard()
  expect(reopened.name).toBe(resource.name)
  expect(reopened.contentId).toMatch(/^saved-/)
  expect(JSON.parse(reopened.componentData)).toEqual([])
})

test('stale save returns 409 and cannot overwrite persisted content', async () => {
  const resource = await ensureDashboard()
  const before = await readDashboard()
  const result = await call('/dataVisualization/updateCanvas', {
    ...resource,
    checkHistory: true,
    expectedContentId: 'stale-version',
    contentId: 'must-not-persist',
    name: resource.name + '-rejected'
  })
  expect(result.ok).toBe(false)
  expect(result.status).toBe(409)
  expect(result.code).toBe(50005)
  expect(result.category).toBe('CONFLICT')
  expect(result.data.currentContentId).toBe(before.contentId)
  const after = await readDashboard()
  expect(after.contentId).toBe(before.contentId)
  expect(after.name).toBe(before.name)
})

test('two concurrent real saves allow one writer and reject the stale writer', async () => {
  const resource = await ensureDashboard()
  const before = await readDashboard()
  const results = await Promise.all(
    ['first', 'second'].map((marker) =>
      call('/dataVisualization/updateCanvas', {
        ...resource,
        checkHistory: true,
        expectedContentId: before.contentId,
        contentId: marker + '-' + Date.now()
      })
    )
  )
  expect(results.filter((result) => result.ok)).toHaveLength(1)
  expect(results.filter((result) => result.status === 409)).toHaveLength(1)
  const after = await readDashboard()
  expect(after.contentId).toMatch(/^(first|second)-/)
})

test('actual editor: cancel preserves input/cache; confirm saves and reopens', async () => {
  const resource = await ensureDashboard()
  const loaded = page.waitForResponse(
    (response) =>
      response.url().includes('/dataVisualization/findById') &&
      response.request().postDataJSON()?.id === resource.id
  )
  await page.goto('/#/dashboard?resourceId=' + resource.id + '&opt=edit')
  await loaded
  await expect(page.locator('.ed-loading-mask:visible')).toHaveCount(0)
  await page.waitForFunction(
    (id) =>
      document.querySelector('#app')?.__vue_app__ &&
      document.body.textContent.includes('保存'),
    resource.id
  )
  await page.waitForFunction(async (id) => {
    const { dvMainStoreWithOut } = await import(
      '/src/store/modules/data-visualization/dvMain.ts'
    )
    return String(dvMainStoreWithOut().dvInfo.id) === id
  }, resource.id)
  const before = await readDashboard()
  const localName = resource.name + '-local'
  await page.locator('#canvas-name').dblclick()
  await page.locator('#canvas-name input').fill(localName)
  await page.locator('#canvas-name input').blur()
  await expect(page.locator('#canvas-name')).toHaveText(localName)
  await page.evaluate(
    async ({ id }) => {
      const { snapshotStoreWithOut } = await import(
        '/src/store/modules/data-visualization/snapshot.ts'
      )
      const { useCache } = await import('/src/hooks/web/useCache.ts')
      snapshotStoreWithOut().recordSnapshotCache('renderChart')
      snapshotStoreWithOut().styleChangeTimes = 1
      useCache().wsCache.set('DE-DV-CATCH-' + id, { synthetic: true })
    },
    { id: resource.id }
  )
  const other = await call('/dataVisualization/updateCanvas', {
    ...resource,
    checkHistory: true,
    expectedContentId: before.contentId,
    contentId: 'another-tab-' + Date.now()
  })
  expect(other.ok).toBe(true)
  await page
    .getByRole('button', { name: '保存', exact: true })
    .and(page.locator('.ed-button--primary'))
    .click()
  const dialog = page.getByRole('dialog').filter({ hasText: '已被他人更新' })
  await expect(dialog).toBeVisible()
  await dialog.getByRole('button', { name: /取消/ }).click()
  const retained = await page.evaluate(async (id) => {
    const { dvMainStoreWithOut } = await import(
      '/src/store/modules/data-visualization/dvMain.ts'
    )
    const { useCache } = await import('/src/hooks/web/useCache.ts')
    return {
      name: dvMainStoreWithOut().dvInfo.name,
      cache: !!useCache().wsCache.get('DE-DV-CATCH-' + id)
    }
  }, resource.id)
  expect(retained.name).toBe(localName)
  expect(retained.cache).toBe(true)
  await expect(page.locator('.ed-loading-mask:visible')).toHaveCount(0)
  await page
    .getByRole('button', { name: '保存', exact: true })
    .and(page.locator('.ed-button--primary'))
    .click()
  await expect(dialog).toBeVisible()
  await dialog.getByRole('button', { name: /确定|确认/ }).click()
  await expect.poll(async () => (await readDashboard()).name).toBe(localName)
  await expect(page.locator('.ed-loading-mask:visible')).toHaveCount(0)
  await page.reload()
  await expect(page.getByText(localName, { exact: true }).first()).toBeVisible()
})

test('real HTTP parameter error is presented once; local handling suppresses global message', async () => {
  await page.goto('/#/data/datasource')
  await expect(page.locator('#app')).not.toBeEmpty()
  await call('/threshold/preview', {}, 'post', false)
  await expect(page.locator('.ed-message--error:visible')).toHaveCount(1)
  await page.locator('.ed-message__closeBtn').click()
  await call('/threshold/preview', {}, 'post', true)
  await expect(page.locator('.ed-message--error:visible')).toHaveCount(0)
})

test('real intentional cancellation is silent and keeps the authenticated session', async () => {
  const result = await page.evaluate(async () => {
    const { default: request } = await import('/src/config/axios/index.ts')
    const controller = new AbortController()
    controller.abort()
    try {
      await request.get({ url: '/menu/query', signal: controller.signal })
    } catch (error) {
      return { code: error.code, hash: location.hash }
    }
  })
  expect(result.code).toBe('ERR_CANCELED')
  expect(result.hash).not.toContain('login')
  await expect(page.locator('.ed-message--error:visible')).toHaveCount(0)
})

test('real JSON error requested as Blob remains a typed rejected error', async () => {
  const result = await page.evaluate(async () => {
    const { default: request } = await import('/src/config/axios/index.ts')
    try {
      await request.post({
        url: '/threshold/preview',
        data: {},
        responseType: 'blob',
        handleErrorLocally: true
      })
    } catch (error) {
      return {
        status: error.httpStatus,
        category: error.category,
        code: error.code,
        requestId: error.requestId
      }
    }
  })
  expect(result.status).toBe(400)
  expect(result.category).toBe('VALIDATION')
  expect(result.requestId).toMatch(/^[a-f0-9]{32}$/)
})

for (const tokenType of ['user', 'embedded', 'share']) {
  test(`real gateway rejects invalid ${tokenType} identity without granting access`, async ({
    browser
  }) => {
    const isolated = await browser.newContext({
      baseURL: 'http://127.0.0.1:7070'
    })
    const anonymous = await isolated.newPage()
    try {
      await anonymous.goto('/#/login', { waitUntil: 'domcontentloaded' })
      const result = await anonymous.evaluate(async (tokenType) => {
        const { service } = await import('/src/config/axios/service.ts')
        const { useCache } = await import('/src/hooks/web/useCache.ts')
        const { useEmbedded } = await import('/src/store/modules/embedded.ts')
        const { useLinkStoreWithOut } = await import(
          '/src/store/modules/link.ts'
        )
        if (tokenType === 'user')
          useCache().wsCache.set('user.token', 'synthetic-invalid-token')
        if (tokenType === 'embedded')
          useEmbedded().token = 'synthetic-invalid-token'
        if (tokenType === 'share')
          useLinkStoreWithOut().setLinkToken('synthetic-invalid-token')
        try {
          await service({
            url: '/menu/query',
            method: 'get',
            handleErrorLocally: true
          })
        } catch (error) {
          return {
            status: error.httpStatus,
            category: error.category,
            reason: error.response?.headers?.['x-de-error-reason'],
            flag: !!error.response?.headers?.['de-gateway-flag'],
            requestId: error.requestId,
            headerId: error.response?.headers?.['x-de-request-id']
          }
        }
      }, tokenType)
      expect(result.status).toBe(401)
      expect(result.category).toBe('UNAUTHENTICATED')
      expect(result.flag).toBe(true)
      expect(result.reason).toBe('UNAUTHENTICATED')
      expect(result.requestId).toBe(result.headerId)
    } finally {
      await isolated.close()
    }
  })
}

test('legacy consumer retains old status/body while the modern consumer has explicit classification', async () => {
  const result = await page.evaluate(async () => {
    const { service } = await import('/src/config/axios/service.ts')
    const { securityConfig } = await import('/src/config/axios/hmac.ts')
    let config
    try {
      await service({
        url: '/threshold/preview',
        method: 'post',
        data: {},
        handleErrorLocally: true
      })
    } catch (error) {
      config = error.config
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
      status: response.status,
      code: body.code,
      keys: Object.keys(body).sort(),
      message: body.msg
    }
  })
  expect(result.status).toBe(500)
  expect(result.code).toBe(40001)
  expect(result.keys).toEqual(['code', 'data', 'msg'])
  expect(result.message).not.toMatch(/Exception|Cannot invoke/)
})

test('malformed JSON receives safe parameter error through gateway', async () => {
  const result = await call('/threshold/preview', '{')
  expect(result.status).toBe(400)
  expect(result.category).toBe('VALIDATION')
  expect(result.message).not.toMatch(/Exception|java\./)
})

test('missing HTTP route remains a rejected request', async () => {
  const result = await call(
    '/error-contract-route-does-not-exist',
    undefined,
    'get'
  )
  expect(result.ok).toBe(false)
  expect(result.status).toBe(404)
  expect(result.typed).toBe(true)
})

for (const entry of ['panel', 'mobile'])
  test(
    'published synthetic dashboard renders through ' + entry + ' entry',
    async () => {
      const resource = await ensureDashboard()
      const publish = await call('/dataVisualization/updatePublishStatus', {
        id: resource.id,
        name: resource.name,
        mobileLayout: true,
        activeViewIds: [],
        type: 'dashboard',
        status: 1
      })
      expect(publish.ok).toBe(true)
      {
        const view = await context.newPage()
        const errors = []
        const diagnostics = []
        view.on('console', (message) => {
          if (['error', 'warn'].includes(message.type()))
            diagnostics.push(message.text().slice(0, 200))
        })
        view.on('response', (response) => {
          if (response.url().includes('/api/'))
            diagnostics.push([
              new URL(response.url()).pathname,
              response.status()
            ])
        })
        view.on('pageerror', (error) => errors.push(error.message))
        const waitForCanvas = () =>
          view.waitForResponse(
            (response) =>
              response.url().includes('/dataVisualization/findById'),
            { timeout: 20000 }
          )
        let loaded
        try {
          if (entry === 'panel') {
            // The authenticated host has a distinct cache namespace from the main app.
            await view.addInitScript(() => {
              for (const key of Object.keys(localStorage)) {
                if (key.startsWith('de_v3_user.'))
                  localStorage.setItem(
                    'panel.html_' + key.slice(6),
                    localStorage.getItem(key)
                  )
              }
            })
            await view.goto('/panel.html', { waitUntil: 'domcontentloaded' })
            await view.waitForFunction(
              () => typeof window.DataEaseBi === 'function'
            )
            // Start the resource deadline after the development host library has loaded.
            loaded = waitForCanvas()
            await view.evaluate(async (id) => {
              const host = document.createElement('div')
              host.id = 'designer'
              host.style.cssText = 'width:100vw;height:100vh'
              document.body.append(host)
              window.contractPanel = new window.DataEaseBi('Dashboard', {
                dvId: id,
                busiFlag: 'dashboard',
                baseUrl: ''
              })
              await window.contractPanel.initialize({ container: '#designer' })
            }, resource.id)
          } else {
            loaded = waitForCanvas()
            await view.goto(
              '/mobile.html#/preview?dvId=' + resource.id + '&dvType=dashboard',
              { waitUntil: 'domcontentloaded' }
            )
          }
          expect((await loaded).status()).toBe(200)
          await expect(
            view
              .locator(
                entry === 'panel' ? '.dashboard-preview' : '.canvas-container'
              )
              .first()
          ).toBeVisible({ timeout: 15000 })
          expect(errors).toEqual([])
        } catch (error) {
          await view.screenshot({
            path: path.join(
              root,
              'tests/browser/output/error-contract-live/' +
                entry +
                '-failure.png'
            )
          })
          fs.writeFileSync(
            path.join(
              root,
              'tests/browser/output/error-contract-live/' +
                entry +
                '-diagnostic.json'
            ),
            JSON.stringify({
              url: view.url(),
              errors,
              diagnostics,
              body: (await view.locator('body').innerText()).slice(0, 800)
            })
          )
          throw error
        } finally {
          await view.close()
        }
      }
    }
  )

test('anonymous synthetic share opens and disabled share stops without reload loops', async ({
  browser
}) => {
  const resource = await ensureDashboard()
  const publication = await call('/dataVisualization/updatePublishStatus', {
    id: resource.id,
    name: resource.name,
    mobileLayout: false,
    activeViewIds: [],
    type: 'dashboard',
    status: 1
  })
  expect(publication.ok).toBe(true)
  let enabled = false
  let shareContext
  try {
    const status = await call('/share/status/' + resource.id, undefined, 'get')
    expect(status.ok).toBe(true)
    if (!status.result.data) {
      expect((await call('/share/switcher/' + resource.id)).ok).toBe(true)
      enabled = true
    }
    const details = await call('/share/detail/' + resource.id, undefined, 'get')
    expect(details.ok).toBe(true)
    shareContext = await browser.newContext({
      baseURL: 'http://127.0.0.1:7070'
    })
    const view = await shareContext.newPage()
    const proxy = view.waitForResponse((response) =>
      response.url().includes('/share/proxyInfo')
    )
    await view.goto('/#/de-link/' + details.result.data.uuid)
    const proxyBody = (await (await proxy).json()).data
    test.skip(
      proxyBody?.shareDisable ||
        proxyBody?.peRequireValid === false ||
        proxyBody?.ticketValidVO?.ticketValid === false,
      'Current environment sharing policy requires additional identity/ticket; policy was not changed'
    )
    const password = view.locator('input[type=password]')
    if (!proxyBody.pwdValid) {
      await expect(password).toBeVisible()
      await password.fill(details.result.data.pwd)
      await view.getByRole('button', { name: /确定|确认/ }).click()
    }
    await expect(view.locator('.canvas-container').first()).toBeVisible({
      timeout: 15000
    })
    expect(view.url()).not.toContain('/login')
    const outOfScope = await view.evaluate(async () => {
      const { default: request } = await import('/src/config/axios/index.ts')
      try {
        await request.get({ url: '/menu/query', handleErrorLocally: true })
        return { ok: true }
      } catch (error) {
        return {
          ok: false,
          status: error.httpStatus,
          category: error.category,
          loginFlag: !!error.response?.headers?.['de-gateway-flag']
        }
      }
    })
    expect(outOfScope.ok).toBe(false)
    expect(outOfScope.status).toBe(403)
    expect(outOfScope.loginFlag).toBe(false)
    await call('/share/switcher/' + resource.id)
    enabled = false
    let navigations = 0
    view.on('framenavigated', (frame) => {
      if (frame === view.mainFrame()) navigations++
    })
    await view.reload()
    await expect(view.locator('.canvas-container')).toHaveCount(0)
    await expect(view.locator('.ed-loading-mask:visible')).toHaveCount(0)
    expect(navigations).toBeLessThanOrEqual(2)
  } finally {
    await shareContext?.close()
    if (enabled) await call('/share/switcher/' + resource.id)
  }
})

test('real authenticated binary download retains bytes and rejects an invalid identity', async ({
  browser
}) => {
  const resource = await ensureDashboard()
  const fileId = 'codex-error-contract-' + Date.now()
  ownedFiles.push(fileId + '.png')
  const result = await page.evaluate(
    async ({ resource, fileId }) => {
      const { default: request } = await import('/src/config/axios/index.ts')
      const png = Uint8Array.from(
        atob(
          'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/l9sAAAAASUVORK5CYII='
        ),
        (c) => c.charCodeAt(0)
      )
      const form = new FormData()
      form.append(
        'file',
        new File([png], 'synthetic.png', { type: 'image/png' })
      )
      await request.post({
        url: '/staticResource/upload/' + fileId,
        data: form,
        headersType: 'multipart/form-data'
      })
      const imagePath = '/static-resource/' + fileId + '.png'
      const base = {
        ...resource,
        id: undefined,
        name: resource.name + '-image',
        contentId: 'image-version',
        componentData: JSON.stringify([
          {
            id: fileId,
            component: 'Picture',
            propValue: { url: imagePath },
            style: { width: 1, height: 1, left: 0, top: 0 }
          }
        ])
      }
      const created = await request.post({
        url: '/dataVisualization/saveCanvas',
        data: base
      })
      try {
        const response = await request.get({
          url: imagePath,
          responseType: 'blob',
          handleErrorLocally: true
        })
        const bytes = new Uint8Array(await response.data.arrayBuffer())
        return {
          id: String(created.data),
          status: response.status,
          type: response.data.type,
          same:
            bytes.length === png.length &&
            bytes.every((value, index) => value === png[index]),
          imagePath
        }
      } catch (error) {
        return { id: String(created.data), status: error.httpStatus, imagePath }
      }
    },
    { resource, fileId }
  )
  owned.push(result.id)
  expect(result.status).toBe(200)
  expect(result.type).toBe('image/png')
  expect(result.same).toBe(true)
  const isolated = await browser.newContext({
    baseURL: 'http://127.0.0.1:7070'
  })
  const view = await isolated.newPage()
  try {
    await view.goto('/#/login', { waitUntil: 'domcontentloaded' })
    const denied = await view.evaluate(async (imagePath) => {
      const { default: request } = await import('/src/config/axios/index.ts')
      try {
        await request.get({
          url: imagePath,
          responseType: 'blob',
          handleErrorLocally: true
        })
        return { ok: true }
      } catch (error) {
        return { ok: false, status: error.httpStatus }
      }
    }, result.imagePath)
    expect(denied.ok).toBe(false)
    expect([401, 403]).toContain(denied.status)
  } finally {
    await isolated.close()
  }
})

test('incomplete publish request is a safe validation error and preserves draft', async () => {
  const resource = await ensureDashboard()
  const before = await readDashboard()
  const result = await call('/dataVisualization/updatePublishStatus', {
    id: resource.id,
    type: 'dashboard',
    status: 1
  })
  expect(result.status).toBe(400)
  expect(result.category).toBe('VALIDATION')
  expect(result.message).not.toMatch(/Cannot invoke|Exception|nameObj/)
  expect((await readDashboard()).contentId).toBe(before.contentId)
})

test('missing gateway permission discriminator is a validation error, not a null pointer', async () => {
  const resource = await ensureDashboard()
  const result = await call('/dataVisualization/updatePublishStatus', {
    id: resource.id,
    status: 1
  })
  expect(result.status).toBe(400)
  expect(result.category).toBe('VALIDATION')
  expect(result.message).not.toMatch(/Cannot invoke|Exception|resolveValue/)
})

test('unknown extension fault is sanitized and transaction rolls back', async () => {
  const resource = await ensureDashboard()
  const before = await readDashboard()
  const result = await call('/dataVisualization/updateBase', {
    id: resource.id,
    pid: resource.pid,
    type: 'dashboard',
    status: 0
  })
  expect(result.status).toBe(500)
  expect(result.category).toBe('INTERNAL')
  expect(result.message).not.toMatch(/Cannot invoke|Exception|nameObj|java\./)
  const after = await readDashboard()
  expect(after.name).toBe(before.name)
  expect(after.contentId).toBe(before.contentId)
  expect(after.status).toBe(before.status)
})
