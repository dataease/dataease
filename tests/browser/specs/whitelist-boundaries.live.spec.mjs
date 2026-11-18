import { test, expect } from '@playwright/test'
import fs from 'node:fs'
import { fileURLToPath } from 'node:url'
import { enterOrganization } from '../fixtures/configuration-security/organization.mjs'

const root = fileURLToPath(new URL('../../../', import.meta.url))
const settings = Object.fromEntries(
  fs
    .readFileSync(root + '.codex/playwright_mcp/.env.local', 'utf8')
    .split('\n')
    .filter((line) => /^TEST_(USERNAME|PASSWORD)=/.test(line))
    .map((line) => {
      const index = line.indexOf('=')
      return [line.slice(0, index), JSON.parse(line.slice(index + 1))]
    })
)

async function login(page) {
  await page.goto('/#/login')
  await expect(page.getByRole('textbox', { name: '账号/邮箱' })).toBeVisible({
    timeout: 30000
  })
  await page
    .getByRole('textbox', { name: '账号/邮箱' })
    .fill(settings.TEST_USERNAME)
  await page
    .getByRole('textbox', { name: '密码', exact: true })
    .fill(settings.TEST_PASSWORD)
  const response = page.waitForResponse((r) =>
    r.url().includes('/login/localLogin')
  )
  await page.getByRole('button', { name: '登录', exact: true }).click()
  expect((await (await response).json()).code).toBe(0)
  await page.waitForURL((url) => !url.hash.includes('login'))
}

async function api(page, url, data) {
  return page.evaluate(
    async ({ url, data }) => {
      const { default: request } = await import('/src/config/axios/index.ts')
      const response =
        data === undefined
          ? await request.get({ url, timeout: 15000 })
          : await request.post({ url, data, timeout: 15000 })
      return response.data
    },
    { url, data }
  )
}

async function navigate(page, route) {
  await page.goto('/#' + route)
}

async function authorize(page) {
  await login(page)
  await enterOrganization(page)
}

test('existing dashboard map rendering and real map resources', async ({
  page,
  request
}) => {
  const id = process.env.WHITELIST_DASHBOARD_ID
  test.skip(
    !id,
    'Set WHITELIST_DASHBOARD_ID to an authorized existing dashboard'
  )
  await login(page)
  const tree = await api(page, '/dataVisualization/tree', {
    busiFlag: 'dashboard',
    resourceTable: 'snapshot'
  })
  function findPath(rows, parents = []) {
    for (const row of rows) {
      const path = [...parents, row]
      if (String(row.id) === id) return path
      const found = findPath(row.children || [], path)
      if (found) return found
    }
    return null
  }
  const targetPath = findPath(tree)
  console.log(
    'Dashboard organization lookup:',
    JSON.stringify({
      found: !!targetPath,
      roots: tree.map((row) => ({ id: row.id, orgRoot: row.orgRoot })),
      path: targetPath?.map((row) => ({ id: row.id, orgRoot: row.orgRoot }))
    })
  )
  expect(
    targetPath,
    'Target dashboard must be accessible in the organization tree'
  ).toBeTruthy()
  const organization = targetPath.find((row) => row.orgRoot) || targetPath[0]
  await page.evaluate(async (oid) => {
    const { ensureDekey, rsaEncryp } = await import('/src/utils/encryption.ts')
    const { useUserStoreWithOut } = await import('/src/store/modules/user.ts')
    await ensureDekey()
    useUserStoreWithOut().setProxyInfo({
      proxy: true,
      proxyOid: String(oid),
      proxySecret: rsaEncryp(String(oid))
    })
  }, organization.id)
  const mapResponses = []
  const errors = []
  page.on('response', (response) => {
    const path = new URL(response.url()).pathname
    if (/\/(?:map|geo)\/.*\.json$/.test(path)) {
      mapResponses.push({ path, status: response.status() })
    }
  })
  page.on('pageerror', (error) => errors.push(error.message))
  await page.goto('/#/panel/index?dvId=' + encodeURIComponent(id))
  await expect
    .poll(() => mapResponses.filter((row) => row.status === 200).length, {
      timeout: 45000
    })
    .toBeGreaterThan(0)
  await expect(page.locator('canvas').first()).toBeVisible({ timeout: 30000 })
  await page.screenshot({
    path: root + 'tests/browser/output/whitelist-dashboard-map.png',
    fullPage: true
  })
  const view = await page.evaluate(async () => {
    const { dvMainStoreWithOut } = await import(
      '/src/store/modules/data-visualization/dvMain.ts'
    )
    const store = dvMainStoreWithOut()
    return {
      charts: Object.values(store.canvasViewInfo || {}).map((chart) => ({
        type: chart.type
      })),
      canvases: [...document.querySelectorAll('canvas')].map((canvas) => ({
        width: canvas.width,
        height: canvas.height
      }))
    }
  })
  for (const resource of [
    ...new Map(mapResponses.map((row) => [row.path, row])).values()
  ]) {
    const anonymous = await request.get(resource.path)
    expect(anonymous.status(), resource.path).toBe(200)
    expect((await anonymous.json()).type).toBe('FeatureCollection')
  }
  expect(mapResponses.every((row) => row.status === 200)).toBe(true)
  console.log(
    'Dashboard map result:',
    JSON.stringify({ resources: mapResponses, view, pageErrors: errors })
  )
  expect(errors).toEqual([])
})

test('anonymous public routes and prefix rejection on the running backend', async ({
  request
}) => {
  for (const path of [
    '/typeface/defaultFont',
    '/map/worldTree',
    '/sysParameter/ui',
    '/sysParameter/defaultSettings'
  ]) {
    const response = await request.get('/api' + path)
    expect(response.status(), path).toBe(200)
    expect((await response.json()).code, path).toBe(0)
  }
  expect(
    (await (await request.post('/api/typeface/listFont', { data: {} })).json())
      .code
  ).toBe(0)
  for (const path of [
    '/typeface/listFontOther',
    '/typeface/defaultFont/other',
    '/share/proxyInfoOther',
    '/share/validate/other',
    '/typeface/downloadOther/file',
    '/exportCenter/downloadOther/1',
    '/exportCenter/download/1/other',
    '/xpackComponent/pluginStaticInfoOther/name',
    '/websocketOther',
    '/map/admin',
    '/map/worldTreeOther',
    '/map/156/156.json/other'
  ]) {
    for (const headers of [
      {},
      { 'X-DE-TOKEN': 'invalid-whitelist-regression-token' }
    ]) {
      const response = await request.get('/api' + path, { headers })
      expect(response.status(), path).toBe(401)
    }
  }
})

test('font upload, list UI, anonymous download and actual browser font loading', async ({
  page,
  request
}) => {
  await authorize(page)
  const name = `codex-whitelist-font-${Date.now()}`
  let font
  try {
    const bytes = fs.readFileSync(
      process.env.WHITELIST_TEST_FONT ||
        '/System/Library/Fonts/Supplemental/Arial.ttf'
    )
    const upload = await page.evaluate(
      async ({ bytes }) => {
        const { uploadFontFile } = await import('/src/api/font.ts')
        const form = new FormData()
        form.append(
          'file',
          new File([new Uint8Array(bytes)], 'whitelist-test.ttf', {
            type: 'font/ttf'
          })
        )
        return (await uploadFontFile(form)).data
      },
      { bytes: [...bytes] }
    )
    font = await api(page, '/typeface/create', {
      ...upload,
      name,
      isDefault: false,
      isBuiltin: false
    })
    expect(font.id).toBeTruthy()
    await navigate(page, '/sys-setting/font')
    await expect(page.getByText(name, { exact: true })).toBeVisible()
    const publicList = (
      await (await request.post('/api/typeface/listFont', { data: {} })).json()
    ).data
    expect(publicList.some((row) => row.id === font.id)).toBe(true)
    const response = await request.get(
      '/api/typeface/download/' + font.fileTransName
    )
    expect(response.status()).toBe(200)
    expect(Buffer.compare(await response.body(), bytes)).toBe(0)
    const loaded = await page.evaluate(
      async ({ name, file }) => {
        const font = new FontFace(name, `url(/api/typeface/download/${file})`)
        await font.load()
        document.fonts.add(font)
        return document.fonts.check(`16px "${name}"`)
      },
      { name, file: font.fileTransName }
    )
    expect(loaded).toBe(true)
    await api(page, '/typeface/edit', { ...font, name: name + '-renamed' })
    await page.reload()
    await expect(
      page.getByText(name + '-renamed', { exact: true })
    ).toBeVisible()
  } finally {
    if (font?.id) {
      await api(page, '/typeface/delete/' + font.id, {})
      expect(
        (await api(page, '/typeface/listFont', {})).some(
          (row) => row.id === font.id
        )
      ).toBe(false)
    }
  }
})

test('real WebSocket STOMP connection and application UI loading', async ({
  page,
  request
}) => {
  let connected = false
  let subscriptions = 0
  page.on('websocket', (socket) => {
    socket.on('framereceived', (frame) => {
      if (String(frame.payload).includes('CONNECTED')) connected = true
    })
    socket.on('framesent', (frame) => {
      if (String(frame.payload).includes('SUBSCRIBE')) subscriptions++
    })
  })
  await authorize(page)
  await expect.poll(() => connected, { timeout: 30000 }).toBe(true)
  await expect
    .poll(() => subscriptions, { timeout: 10000 })
    .toBeGreaterThanOrEqual(2)
  await navigate(page, '/toolbox/log')
  await expect(page.getByText('操作日志', { exact: true }).first()).toBeVisible(
    { timeout: 30000 }
  )
  const info = await request.get('http://localhost:8100/websocket/info')
  expect(info.status()).toBe(200)
  expect((await info.json()).websocket).toBe(true)
})

test('published test dashboard share preflight and password validation in anonymous browser', async ({
  page,
  browser
}) => {
  await authorize(page)
  // A previous interrupted run may have reached the product's create step before browser teardown.
  const existing = await api(page, '/dataVisualization/tree', {
    busiFlag: 'dashboard'
  })
  const flatten = (rows) =>
    rows.flatMap((row) => [row, ...flatten(row.children || [])])
  for (const row of flatten(existing).filter((row) =>
    row.name?.startsWith('codex-whitelist-share-')
  )) {
    await api(page, '/dataVisualization/deleteLogic', {
      id: row.id,
      busiFlag: 'dashboard'
    })
  }
  const name = `codex-whitelist-share-${Date.now()}`
  let id
  let anonymous
  try {
    const tree = await api(page, '/dataVisualization/tree', {
      busiFlag: 'dashboard'
    })
    const canvasStyleData = await page.evaluate(async () => {
      const { DEFAULT_CANVAS_STYLE_DATA_LIGHT } = await import(
        '/src/views/chart/components/editor/util/dataVisualization.ts'
      )
      return JSON.stringify({
        ...DEFAULT_CANVAS_STYLE_DATA_LIGHT,
        width: 1200,
        height: 800,
        dvType: 'dashboard'
      })
    })
    id = await api(page, '/dataVisualization/saveCanvas', {
      name,
      pid: tree[0].id,
      type: 'dashboard',
      nodeType: 'leaf',
      componentData: '[]',
      canvasStyleData,
      mobileLayout: false,
      canvasViewInfo: {},
      optType: 'create'
    })
    expect(id).toBeTruthy()
    await api(page, '/dataVisualization/updatePublishStatus', {
      id,
      name,
      type: 'dashboard',
      status: 1,
      activeViewIds: [],
      mobileLayout: false
    })
    await api(page, '/share/switcher/' + id, {})
    await api(page, '/share/editPwd', {
      resourceId: id,
      pwd: 'WL8953',
      autoPwd: false
    })
    const detail = await api(page, '/share/detail/' + id)
    expect(detail.uuid).toBeTruthy()
    anonymous = await browser.newContext()
    const visitor = await anonymous.newPage()
    const visitorErrors = []
    visitor.on('pageerror', (error) => visitorErrors.push(error.message))
    const preview = visitor.waitForResponse((r) =>
      r.url().includes('/share/proxyInfo')
    )
    await visitor.goto(`http://localhost:7070/#/de-link/${detail.uuid}`)
    expect((await (await preview).json()).code).toBe(0)
    const password = visitor.locator('input[type="password"]')
    await expect(password).toBeVisible({ timeout: 30000 })
    await password.fill('wrong8953')
    let pending = visitor
      .waitForResponse((r) => r.url().includes('/share/validate'), {
        timeout: 15000
      })
      .then((r) => r.json())
    await visitor.getByRole('button', { name: '确定', exact: true }).click()
    expect((await pending).data).toBe(false)
    await password.fill('WL8953')
    pending = visitor.waitForResponse(
      (r) => r.url().includes('/share/validate'),
      { timeout: 15000 }
    )
    const accepted = visitor
      .waitForResponse((r) => r.url().includes('/share/proxyInfo'), {
        timeout: 20000
      })
      .then((r) => r.json())
    await visitor.getByRole('button', { name: '确定', exact: true }).click()
    expect((await pending).status()).toBe(200)
    expect((await accepted).data.pwdValid).toBe(true)
    await expect(password).not.toBeVisible({ timeout: 30000 })
    try {
      await expect(visitor.locator('#preview-canvas-main').first()).toBeVisible(
        { timeout: 15000 }
      )
    } catch (error) {
      console.log(
        'Share preview diagnostic:',
        JSON.stringify({
          text: await visitor.locator('.link-container').innerText(),
          errors: visitorErrors
        })
      )
      throw error
    }
  } finally {
    await anonymous?.close()
    if (id)
      await api(page, '/dataVisualization/deleteLogic', {
        id,
        busiFlag: 'dashboard'
      })
  }
})

test('installed extension plugin static resource delivery', async ({
  page,
  request
}) => {
  await login(page)
  const plugins = await api(page, '/plugin/query')
  console.log('Installed extension plugin count:', plugins.length)
  test.skip(
    plugins.length === 0,
    'No installed extension plugin supplies pluginStaticInfo in this dev environment'
  )
  for (const plugin of plugins) {
    const response = await request.get(
      '/api/xpackComponent/pluginStaticInfo/' + plugin.moduleName
    )
    expect(response.status()).toBe(200)
    expect((await response.body()).length).toBeGreaterThan(100)
  }
})

test('map settings world tree and actual built-in map resource rendering', async ({
  page,
  request
}) => {
  await authorize(page)
  await navigate(page, '/sys-setting/parameter')
  await page.getByRole('tab', { name: '地图设置', exact: true }).click()
  await expect(page.getByText('世界村', { exact: true }).first()).toBeVisible({
    timeout: 15000
  })
  const response = await request.get('/api/map/000/000.json')
  if (response.status() === 404) {
    console.log(
      'Map runtime resource unavailable: /map/000/000.json returned 404'
    )
    test.skip(
      true,
      'Runtime static map files are missing; tree UI is available but built-in map rendering cannot complete'
    )
  }
  expect(response.status()).toBe(200)
  expect((await response.json()).type).toBe('FeatureCollection')
  await page.getByText('世界村', { exact: true }).first().click()
  await expect(page.locator('#map-container canvas').first()).toBeVisible({
    timeout: 20000
  })
})

test('SSO public initiation and invalid callback rejection without login-token gate', async ({
  request
}) => {
  const metadata = await request.get('/api/saml/metadata')
  expect(metadata.status()).not.toBe(401)
  expect(metadata.status()).toBe(200)
  expect(await metadata.text()).toContain('EntityDescriptor')
  const initiation = await request.post('/api/saml/login', {
    headers: { 'X-DE-Platform-Nonce': 'A'.repeat(43) },
    data: {},
    maxRedirects: 0
  })
  if (initiation.status() !== 200) {
    const body = await initiation.json().catch(() => ({}))
    console.log(
      'SAML initiation diagnostic:',
      JSON.stringify({
        status: initiation.status(),
        code: body.code,
        message: body.msg
      })
    )
  }
  expect(initiation.status()).toBe(200)
  const initiated = await initiation.json()
  expect(initiated.code).toBe(0)
  expect(new URL(initiated.data).searchParams.has('SAMLRequest')).toBe(true)
  const invalid = await request.post('/api/saml/sso', {
    form: { SAMLResponse: 'invalid-regression-response' },
    maxRedirects: 0
  })
  expect(invalid.status()).not.toBe(401)
  expect([301, 302, 303]).not.toContain(invalid.status())
  test.info().annotations.push({
    type: 'coverage',
    description:
      'SSO entry/invalid callback only; no real IdP sign-in or external redirect followed'
  })
})

test('custom map upload, public tree and GeoJSON, coordinate UI and cleanup', async ({
  page,
  request
}) => {
  await authorize(page)
  const name = `codex-whitelist-map-${Date.now()}`
  const tree = await api(page, '/map/worldTree')
  const codes = new Set(tree.children.map((row) => row.id.replace('geo_', '')))
  const code = ['987', '986', '985', '984'].find((value) => !codes.has(value))
  expect(code).toBeTruthy()
  let created = false
  try {
    await page.evaluate(
      async ({ code, name }) => {
        const { default: request } = await import('/src/config/axios/index.ts')
        const form = new FormData()
        form.append(
          'request',
          new Blob([JSON.stringify({ code, name, pid: '000' })], {
            type: 'application/json'
          })
        )
        form.append(
          'file',
          new File(
            [
              JSON.stringify({
                type: 'FeatureCollection',
                features: [
                  {
                    type: 'Feature',
                    properties: {
                      name,
                      adcode: code,
                      center: [0.5, 0.5],
                      centroid: [0.5, 0.5],
                      level: 'country'
                    },
                    geometry: {
                      type: 'Polygon',
                      coordinates: [
                        [
                          [0, 0],
                          [1, 0],
                          [1, 1],
                          [0, 1],
                          [0, 0]
                        ]
                      ]
                    }
                  }
                ]
              })
            ],
            'whitelist-map.json',
            { type: 'application/json' }
          )
        )
        await request.post({
          url: '/geometry/save',
          data: form,
          headersType: 'multipart/form-data'
        })
      },
      { code, name }
    )
    created = true
    const geo = await request.get(`/api/geo/${code}/${code}.json`)
    expect(geo.status()).toBe(200)
    expect((await geo.json()).features).toHaveLength(1)
    const publicTree = (await (await request.get('/api/map/worldTree')).json())
      .data
    expect(publicTree.children.some((row) => row.id === 'geo_' + code)).toBe(
      true
    )
    await navigate(page, '/sys-setting/parameter')
    await page.getByRole('tab', { name: '地图设置', exact: true }).click()
    await expect(
      page.getByText('世界村', { exact: true }).first()
    ).toBeVisible()
    await page
      .getByRole('treeitem')
      .filter({ hasText: '世界村' })
      .first()
      .locator('.ed-tree-node__expand-icon')
      .first()
      .click()
    await page.getByText(name, { exact: true }).first().click()
    await expect(page.locator('.geo-content-bottom')).toContainText(
      'FeatureCollection',
      { timeout: 15000 }
    )
    await expect(page.locator('.geo-content-middle')).toContainText(
      'geo_' + code
    )
  } finally {
    if (created) {
      await api(page, '/geometry/delete/geo_' + code, {})
      const after = await api(page, '/map/worldTree')
      expect(after.children.some((row) => row.id === 'geo_' + code)).toBe(false)
    }
  }
})

test('real export task, signed anonymous download, ticket replay rejection and WebSocket notification', async ({
  page,
  request
}) => {
  let subscribed = false
  page.on('websocket', (socket) => {
    subscribed = false
    socket.on('framesent', (frame) => {
      if (String(frame.payload).includes('task-export-topic')) subscribed = true
    })
  })
  await authorize(page)
  await expect.poll(() => subscribed, { timeout: 15000 }).toBe(true)
  let id
  try {
    await page.evaluate(async () => {
      const { useEmitt } = await import('/src/hooks/web/useEmitt.ts')
      window.__whitelistExportMessages = 0
      useEmitt().emitter.on(
        'task-export-topic-call',
        () => window.__whitelistExportMessages++
      )
    })
    const before = await api(page, '/exportCenter/exportTasks/ALL/1/100', {})
    const previous = new Set(before.records.map((row) => row.id))
    const time = Date.now()
    await api(page, '/log/export', {
      keyword: `codex-whitelist-${time}`,
      time: [time - 60000, time + 60000],
      timeDesc: true
    })
    let task
    await expect
      .poll(
        async () => {
          const tasks = await api(
            page,
            '/exportCenter/exportTasks/ALL/1/100',
            {}
          )
          task = tasks.records.find(
            (row) => !previous.has(row.id) && row.exportFromType === 'log'
          )
          if (task) id = task.id
          return task?.exportStatus
        },
        { timeout: 60000, intervals: [1000, 2000] }
      )
      .toBe('SUCCESS')
    const link = await api(page, '/exportCenter/generateDownloadUri/' + id)
    const url = new URL(
      await page.evaluate(async (link) => {
        const { PATH_URL } = await import('/src/config/axios/service.ts')
        return new URL(PATH_URL + link, location.href).href
      }, link)
    )
    expect(url.pathname).toContain('/exportCenter/download/' + id)
    const missing = await request.get(url.origin + url.pathname)
    expect(missing.headers()['content-disposition']).toBeUndefined()
    const invalid = new URL(url)
    invalid.searchParams.set('ticket', 'invalid-test-ticket')
    const rejected = await request.get(invalid.toString())
    expect(rejected.headers()['content-disposition']).toBeUndefined()
    const download = await request.get(url.toString())
    expect(download.status()).toBe(200)
    expect(download.headers()['content-disposition']).toBeTruthy()
    expect((await download.body()).subarray(0, 2).toString()).toBe('PK')
    const replay = await request.get(url.toString())
    expect(replay.headers()['content-disposition']).toBeUndefined()
    const browserLink = await api(
      page,
      '/exportCenter/generateDownloadUri/' + id
    )
    const browserUrl = await page.evaluate(async (link) => {
      const { PATH_URL } = await import('/src/config/axios/service.ts')
      return new URL(PATH_URL + link, location.href).href
    }, browserLink)
    const browserDownload = page.waitForEvent('download', { timeout: 15000 })
    await page.evaluate((url) => {
      const anchor = document.createElement('a')
      anchor.href = url
      document.body.appendChild(anchor)
      anchor.click()
      anchor.remove()
    }, browserUrl)
    const artifact = await browserDownload
    expect(await artifact.failure()).toBeNull()
    expect(
      fs
        .readFileSync(await artifact.path())
        .subarray(0, 2)
        .toString()
    ).toBe('PK')
    await expect
      .poll(() => page.evaluate(() => window.__whitelistExportMessages), {
        timeout: 15000
      })
      .toBeGreaterThan(0)
  } finally {
    if (id) await api(page, '/exportCenter/delete/' + id)
  }
})
