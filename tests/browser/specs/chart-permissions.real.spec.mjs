import { test, expect } from '@playwright/test'
import fs from 'node:fs'
import { createHmac } from 'node:crypto'
import { createRequire } from 'node:module'
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
test.describe.configure({ mode: 'serial' })
const name = 'codex-cp-' + Date.now()
let context, page, dataset, userContext, userPage, proxy, app, fields, chartId
const owned = {
  canvasId: null,
  datasetId: null,
  userId: null,
  roleId: null,
  appId: null,
  rowIds: [],
  columnIds: []
}
const ExcelJS = createRequire(import.meta.url)(
  path.join(root, 'core/core-frontend/node_modules/exceljs')
)
const output = path.join(root, 'tests/browser/output/chart-permissions')
const persist = () => {
  fs.mkdirSync(output, { recursive: true })
  fs.writeFileSync(
    path.join(output, 'owned-resources.json'),
    JSON.stringify(owned),
    { mode: 0o600 }
  )
}
test.beforeAll(async ({ browser, baseURL }) => {
  context = await browser.newContext({ baseURL, locale: 'zh-CN' })
  page = await context.newPage()
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
    path: path.join(root, 'tests/browser/.auth/chart-permissions.json')
  })
  fs.chmodSync(
    path.join(root, 'tests/browser/.auth/chart-permissions.json'),
    0o600
  )
})

const call = (target, url, data) =>
  target.evaluate(
    async ({ url, data }) => {
      const { default: request } = await import('/src/config/axios/index.ts')
      try {
        return {
          ok: true,
          ...(await request[data === undefined ? 'get' : 'post']({
            url,
            data,
            handleErrorLocally: true
          }))
        }
      } catch (e) {
        return {
          ok: false,
          status: e.httpStatus,
          code: e.code,
          category: e.category,
          message: e.message
        }
      }
    },
    { url, data }
  )
const api = async (url, data) => {
  const r = await call(page, url, data)
  expect(r.ok, JSON.stringify({ url, status: r.status, code: r.code })).toBe(
    true
  )
  return r.data
}
const grant = async (weight) =>
  api('/auth/saveSubjectPermission', {
    id: owned.roleId,
    type: 1,
    flag: 'DATASET',
    oid: proxy.proxyOid,
    system: false,
    permissions: [{ id: dataset.id, weight, ext: 0, oid: proxy.proxyOid }]
  })
const chart = async (target, options = {}) =>
  target.evaluate(
    async ({ dataset, fields, chartId, options, canvasId }) => {
      const { BASE_VIEW_CONFIG } = await import(
        '/src/views/chart/components/editor/util/chart.ts'
      )
      const { getData } = await import('/src/api/chart.ts')
      const field = (key) =>
        fields.find((f) => f.originName === key || f.name === key)
      const view = {
        ...structuredClone(BASE_VIEW_CONFIG),
        id: chartId,
        sceneId: canvasId || 0,
        tableId: dataset.id,
        type: options.type || 'bar',
        render: 'antv',
        xAxis: [
          {
            ...field(options.dimension || 'region'),
            sort: options.sort || 'asc'
          }
        ],
        yAxis: [
          {
            ...field(options.metric || 'amount'),
            summary: options.summary || 'sum',
            sort: 'none'
          }
        ],
        ...options.view
      }
      try {
        return { ok: true, result: await getData(view) }
      } catch (e) {
        return {
          ok: false,
          status: e.httpStatus,
          code: e.code,
          category: e.category,
          message: e.message?.slice(0, 200)
        }
      }
    },
    { dataset, fields, chartId, options, canvasId: owned.canvasId }
  )
const values = (r) => {
  expect(r.ok, JSON.stringify(r)).toBe(true)
  expect(r.result.code).not.toBe(50002)
  return r.result.data.data
    .map((x) => [x.field, Number(x.value)])
    .sort((a, b) => a[0].localeCompare(b[0]))
}
const ruleTree = (key, value, term = 'eq') => ({
  logic: 'and',
  items: [
    {
      type: 'item',
      fieldId: fields.find((f) => f.originName === key).id,
      filterType: 'logic',
      term,
      value
    }
  ]
})
const row = async (
  type = 'user',
  tree = ruleTree('region', 'East'),
  extra = {}
) => {
  const authTargetId = type === 'user' ? owned.userId : owned.roleId
  const current = await api(
    '/dataset/rowPermissions/dataSetRowPermissionInfo',
    { datasetId: dataset.id, authTargetType: type, authTargetId }
  )
  const data = {
    ...current,
    datasetId: dataset.id,
    authTargetType: type,
    authTargetId,
    enable: true,
    expressionTree: JSON.stringify(tree),
    whiteListUser: '[]',
    whiteListRole: '[]',
    whiteListDept: '[]',
    ...extra
  }
  await api('/dataset/rowPermissions/save', data)
  const saved = await api('/dataset/rowPermissions/dataSetRowPermissionInfo', {
    datasetId: dataset.id,
    authTargetType: type,
    authTargetId
  })
  if (!owned.rowIds.includes(saved.id)) {
    owned.rowIds.push(saved.id)
    persist()
  }
  return saved
}
const clearRows = async () => {
  for (const id of [...owned.rowIds]) {
    await api('/dataset/rowPermissions/delete', { id, datasetId: dataset.id })
    owned.rowIds = owned.rowIds.filter((x) => x !== id)
    persist()
  }
}
const column = async (columns, extra = {}) => {
  const type = extra.authTargetType || 'user'
  const query = {
    datasetId: dataset.id,
    authTargetType: type,
    authTargetId: type === 'user' ? owned.userId : owned.roleId
  }
  const current = await api('/dataset/columnPermissions/info', query)
  await api('/dataset/columnPermissions/save', {
    ...current,
    ...query,
    enable: true,
    permissions: JSON.stringify({ enable: extra.enable !== false, columns }),
    whiteListUser: '[]',
    ...extra
  })
  const saved = await api('/dataset/columnPermissions/info', query)
  if (!owned.columnIds.includes(saved.id)) {
    owned.columnIds.push(saved.id)
    persist()
  }
  return saved
}
const clearColumns = async () => {
  for (const id of [...owned.columnIds]) {
    await api('/dataset/columnPermissions/delete', {
      id,
      datasetId: dataset.id
    })
    owned.columnIds = owned.columnIds.filter((x) => x !== id)
    persist()
  }
}

test.afterAll(async () => {
  const removed = { ...owned }
  await userContext?.close()
  if (owned.canvasId) {
    await api('/dataVisualization/deleteLogic', {
      id: owned.canvasId,
      busiFlag: 'dashboard'
    })
    owned.canvasId = null
    persist()
  }
  if (owned.datasetId) {
    await clearRows()
    await clearColumns()
  }
  if (owned.appId) {
    await api('/embedded/delete/' + owned.appId, {})
    owned.appId = null
    persist()
  }
  if (owned.userId) {
    await api('/user/delete/' + owned.userId, {})
    owned.userId = null
    persist()
  }
  if (owned.roleId) {
    await api('/role/delete/' + owned.roleId, {})
    owned.roleId = null
    persist()
  }
  if (owned.datasetId) {
    const result = await page.evaluate(async (id) => {
      const { default: request } = await import('/src/config/axios/index.ts')
      return request.post({ url: '/datasetTree/delete', data: { id } })
    }, owned.datasetId)
    expect(result.code).toBe(0)
    owned.datasetId = null
    persist()
  }
  if (removed.datasetId) {
    const flat = (xs) => (xs || []).flatMap((x) => [x, ...flat(x.children)])
    const datasets = await api('/datasetTree/tree', { busiFlag: 'dataset' })
    expect(flat(datasets).some((x) => String(x.id) === removed.datasetId)).toBe(
      false
    )
    expect(
      (
        await api(
          '/dataset/rowPermissions/pager/' + removed.datasetId + '/1/100'
        )
      ).records
    ).toEqual([])
    expect(
      (
        await api(
          '/dataset/columnPermissions/pager/' + removed.datasetId + '/1/100'
        )
      ).records
    ).toEqual([])
  }
  if (removed.roleId)
    expect(
      (await api('/role/query')).some((x) => x.id === removed.roleId)
    ).toBe(false)
  if (removed.appId)
    expect(
      (await api('/embedded/pager/1/100', { keyword: name })).records.some(
        (x) => x.id === removed.appId
      )
    ).toBe(false)
  fs.writeFileSync(
    path.join(output, 'cleanup-verified.json'),
    JSON.stringify({
      datasetAbsent: true,
      rowRulesAbsent: true,
      columnRulesAbsent: true,
      roleAbsent: true,
      appAbsent: true
    }),
    { mode: 0o600 }
  )
  await context?.close()
})
test('inspect chart query prerequisites without returning connections or row values', async () => {
  const result = await page.evaluate(async () => {
    const { default: request } = await import('/src/config/axios/index.ts')
    const clean = (items) =>
      (items || []).map((x) => ({
        id: String(x.id),
        type: x.type,
        nodeType: x.nodeType,
        leaf: x.leaf,
        children: clean(x.children)
      }))
    const engine = (await request.get({ url: '/engine/getEngine' })).data
    const ds = (
      await request.post({
        url: '/datasource/tree',
        data: { busiFlag: 'datasource' }
      })
    ).data
    const datasets = (
      await request.post({
        url: '/datasetTree/tree',
        data: { busiFlag: 'dataset' }
      })
    ).data
    return {
      engineConfigured: !!engine?.type,
      datasources: clean(ds),
      datasets: clean(datasets)
    }
  })
  const out = path.join(root, 'tests/browser/output/chart-permissions')
  fs.mkdirSync(out, { recursive: true })
  fs.writeFileSync(
    path.join(out, 'prerequisites.json'),
    JSON.stringify(result),
    { mode: 0o600 }
  )
})

test('synthetic SQL dataset executes through the configured MySQL provider', async () => {
  dataset = await page.evaluate(async () => {
    const { default: request } = await import('/src/config/axios/index.ts')
    const flat = (xs) => (xs || []).flatMap((x) => [x, ...flat(x.children)])
    const sources = flat(
      (
        await request.post({
          url: '/datasource/tree',
          data: { busiFlag: 'datasource' }
        })
      ).data
    )
    const source = sources.find((x) => x.leaf && x.type === 'mysql')
    if (!source) throw new Error('No existing MySQL source')
    const tree = flat(
      (
        await request.post({
          url: '/datasetTree/tree',
          data: { busiFlag: 'dataset' }
        })
      ).data
    )
    const parent = tree.find((x) => x.id !== '0' && !x.leaf)
    const id = String(Date.now() * 1000)
    const sql =
      "SELECT 'East' AS region, 'A' AS category, 10 AS amount, '13800000001' AS secret UNION ALL SELECT 'East','B',20,'13800000002' UNION ALL SELECT 'West','A',30,'13800000003' UNION ALL SELECT 'West','B',40,'13800000004'"
    const table = {
      id,
      name: 'synthetic_rows',
      tableName: 'synthetic_rows',
      datasourceId: String(source.id),
      type: 'sql',
      isCross: false,
      info: JSON.stringify({ table: 'synthetic_rows', sql: btoa(sql) }),
      sqlVariableDetails: '[]'
    }
    const fields = (
      await request.post({ url: '/datasetData/tableField', data: table })
    ).data.map((f, i) => ({
      ...f,
      id: String(Number(id) + i + 1),
      datasetTableId: id,
      datasourceId: String(source.id),
      checked: true
    }))
    const data = {
      name: 'codex-chart-permissions-' + Date.now(),
      pid: String(parent.id),
      nodeType: 'dataset',
      isCross: false,
      union: [
        {
          currentDs: table,
          currentDsField: fields.map((f) => f.id),
          currentDsFields: fields,
          childrenDs: []
        }
      ],
      allFields: [
        ...fields,
        {
          ...fields.find((f) => f.originName === 'amount'),
          id: String(Number(id) + 5),
          name: 'double_amount',
          originName: btoa(
            '[' + fields.find((f) => f.originName === 'amount').id + '] * 2'
          ),
          extField: 2,
          deType: 3,
          deExtractType: 3
        }
      ]
    }
    return (await request.post({ url: '/datasetTree/create', data })).data
  })
  owned.datasetId = String(dataset.id)
  persist()
  fields = await api('/datasetTree/details/' + dataset.id, {}).then((x) =>
    x.allFields.map((f) =>
      f.extField === 2
        ? {
            ...f,
            originName: Buffer.from(f.originName, 'base64').toString('utf8')
          }
        : f
    )
  )
  chartId = String(Date.now() * 1000 + 100)
  const result = await page.evaluate(async (id) => {
    const { default: request } = await import('/src/config/axios/index.ts')
    return request.post({ url: '/datasetTree/get/' + id, data: {} })
  }, dataset.id)
  fs.writeFileSync(
    path.join(output, 'synthetic-preview.json'),
    JSON.stringify(result),
    { mode: 0o600 }
  )
  expect(result.code).toBe(0)
})

test('isolated role and embedded identity read only the synthetic dataset', async ({
  browser,
  baseURL
}) => {
  proxy = await page.evaluate(async () =>
    (await import('/src/hooks/web/useCache.ts'))
      .useCache()
      .wsCache.get('user.proxyInfo')
  )
  await api('/role/create', { name, typeCode: 0 })
  owned.roleId = (await api('/role/query')).find((x) => x.name === name).id
  persist()
  owned.userId = String(
    await api('/user/create', {
      account: name,
      name,
      email: name + '@example.invalid',
      roleIds: [owned.roleId],
      enable: true,
      mfaEnable: false,
      defaultOid: proxy.proxyOid,
      variables: []
    })
  )
  persist()
  await api('/embedded/create', { name, domain: baseURL, secretLength: 32 })
  app = (await api('/embedded/pager/1/100', { keyword: name })).records.find(
    (x) => x.name === name
  )
  owned.appId = app.id
  persist()
  await grant(1)
  owned.canvasId = await page.evaluate(async (name) => {
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
    const parent = tree[0]?.children?.find((x) => !x.leaf)
    if (!parent) throw new Error('No dashboard folder')
    return String(
      (
        await request.post({
          url: '/dataVisualization/saveCanvas',
          data: {
            name,
            pid: String(parent.id),
            type: 'dashboard',
            nodeType: 'leaf',
            componentData: '[]',
            canvasViewInfo: {},
            canvasStyleData: JSON.stringify({
              ...dvMainStoreWithOut().canvasStyleData,
              dvType: 'dashboard'
            }),
            contentId: 'synthetic-' + Date.now(),
            mobileLayout: false
          }
        })
      ).data
    )
  }, name)
  persist()
  await api('/auth/saveSubjectPermission', {
    id: owned.roleId,
    type: 1,
    flag: 'PANEL',
    oid: proxy.proxyOid,
    system: false,
    permissions: [
      { id: owned.canvasId, weight: 1, ext: 0, oid: proxy.proxyOid }
    ]
  })
  await api('/auth/saveSubjectPermission', {
    id: owned.roleId,
    type: 1,
    flag: 'DATASOURCE',
    oid: proxy.proxyOid,
    system: false,
    permissions: [
      { id: fields[0].datasourceId, weight: 1, ext: 0, oid: proxy.proxyOid }
    ]
  })
  await api('/auth/saveSubjectPermission', {
    id: owned.roleId,
    type: 1,
    flag: 'MENU',
    oid: proxy.proxyOid,
    system: false,
    permissions: [
      { id: '2', weight: 1, ext: 0, oid: proxy.proxyOid },
      { id: '5', weight: 1, ext: 0, oid: proxy.proxyOid }
    ]
  })
  userContext = await browser.newContext({ baseURL, locale: 'zh-CN' })
  userPage = await userContext.newPage()
  await userPage.goto('/#/login')
  await expect(userPage.locator('input[type=password]')).toBeVisible()
  const enc = (x) => Buffer.from(JSON.stringify(x)).toString('base64url')
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
  await userPage.evaluate(async (jwt) => {
    ;(await import('/src/store/modules/embedded.ts'))
      .useEmbedded()
      .setToken(jwt)
    history.replaceState(null, '', '/#/chart-permissions-context')
  }, jwt)
  expect((await call(userPage, '/user/info')).ok).toBe(true)
  expect(values(await chart(userPage))).toEqual([
    ['East', 30],
    ['West', 70]
  ])
})
test('real aggregate sum average count and category grouping match known rows', async () => {
  expect(values(await chart(page))).toEqual([
    ['East', 30],
    ['West', 70]
  ])
  expect(values(await chart(page, { summary: 'avg' }))).toEqual([
    ['East', 15],
    ['West', 35]
  ])
  expect(values(await chart(page, { summary: 'count' }))).toEqual([
    ['East', 2],
    ['West', 2]
  ])
  expect(values(await chart(page, { dimension: 'category' }))).toEqual([
    ['A', 40],
    ['B', 60]
  ])
})
test('dataset read revocation rejects chart query and restoration works', async () => {
  await grant(0)
  const result = await chart(userPage)
  expect(result.ok === false || result.result?.code === 50002).toBe(true)
  expect(result.result?.data?.data || []).toEqual([])
  await grant(1)
  expect(values(await chart(userPage))).toEqual([
    ['East', 30],
    ['West', 70]
  ])
})
test('user row permission restricts chart aggregation and dataset preview', async () => {
  await row()
  expect(values(await chart(userPage))).toEqual([['East', 30]])
  expect(values(await chart(page))).toEqual([
    ['East', 30],
    ['West', 70]
  ])
  const preview = await call(userPage, '/datasetTree/get/' + dataset.id, {})
  expect(preview.ok).toBe(true)
  expect(preview.data.data.data).toHaveLength(2)
  const regionField = preview.data.data.fields.find(
    (f) => f.originName === 'region'
  )
  expect(
    preview.data.data.data.every((r) => r[regionField.dataeaseName] === 'East')
  ).toBe(true)
  fs.writeFileSync(
    path.join(output, 'restricted-preview.json'),
    JSON.stringify(preview),
    { mode: 0o600 }
  )
})
test('nested row conjunction and no-match return exact restricted values', async () => {
  const tree = ruleTree('region', 'East')
  tree.items.push({
    type: 'tree',
    subTree: {
      logic: 'or',
      items: [
        ruleTree('category', 'B').items[0],
        ruleTree('amount', '15', 'gt').items[0]
      ]
    }
  })
  await row('user', tree)
  expect(values(await chart(userPage))).toEqual([['East', 20]])
  await row('user', ruleTree('region', 'missing'))
  expect(values(await chart(userPage))).toEqual([])
})
test('disabled row rule restores rows and role rule applies independently', async () => {
  await row('user', ruleTree('region', 'East'), { enable: false })
  expect(values(await chart(userPage))).toEqual([
    ['East', 30],
    ['West', 70]
  ])
  await row('role', ruleTree('region', 'West'))
  expect(values(await chart(userPage))).toEqual([['West', 70]])
})
test('multiple applicable row rules preserve union semantics and whitelist bypass', async () => {
  await row('user', ruleTree('region', 'East'))
  expect(values(await chart(userPage))).toEqual([
    ['East', 30],
    ['West', 70]
  ])
  await row('role', ruleTree('region', 'West'), {
    whiteListUser: JSON.stringify([owned.userId])
  })
  expect(values(await chart(userPage))).toEqual([['East', 30]])
  await clearRows()
})
test('prohibited dimension is removed from field discovery and chart payload', async () => {
  const secret = fields.find((f) => f.originName === 'secret')
  await column([{ id: secret.id, selected: true, opt: 'Prohibit' }])
  const available = await call(
    userPage,
    '/chart/listByDQ/' + dataset.id + '/' + chartId,
    {}
  )
  expect(available.ok).toBe(true)
  expect(
    [...available.data.dimensionList, ...available.data.quotaList].some(
      (f) => f.id === secret.id
    )
  ).toBe(false)
  const result = await chart(userPage, { dimension: 'secret' })
  expect(JSON.stringify(result)).not.toMatch(/1380000000[1-4]/)
  expect(values(await chart(page))).toEqual([
    ['East', 30],
    ['West', 70]
  ])
})
test('desensitized values remain masked in chart and preview', async () => {
  const secret = fields.find((f) => f.originName === 'secret')
  await column([
    {
      id: secret.id,
      selected: true,
      opt: 'Desensitization',
      desensitizationRule: { builtInRule: 'CompleteDesensitization' }
    }
  ])
  const result = await chart(userPage, { dimension: 'secret' })
  expect(result.ok).toBe(true)
  expect(result.result.code).not.toBe(50002)
  expect(JSON.stringify(result.result.data.data)).not.toMatch(/1380000000[1-4]/)
  expect(result.result.data.data.length).toBeGreaterThan(0)
  // Aggregation charts exclude desensitized axes; detail tables retain masked cells.
  expect(result.result.data.fields.some((f) => f.id === secret.id)).toBe(false)
  const table = await chart(userPage, {
    type: 'table-info',
    view: { xAxis: fields, yAxis: [], resultMode: 'all' }
  })
  expect(table.ok, JSON.stringify(table)).toBe(true)
  expect(table.result.code).not.toBe(50002)
  fs.writeFileSync(
    path.join(output, 'masked-table.json'),
    JSON.stringify(table),
    { mode: 0o600 }
  )
  expect(table.result.data.tableRow).toHaveLength(4)
  expect(
    table.result.data.tableRow.every((r) => r[secret.dataeaseName] === '******')
  ).toBe(true)
  expect(JSON.stringify(table.result.data)).not.toMatch(/1380000000[1-4]/)
  const preview = await call(userPage, '/datasetTree/get/' + dataset.id, {})
  expect(preview.ok).toBe(true)
  expect(JSON.stringify(preview.data.data)).not.toMatch(/1380000000[1-4]/)
})
test('row restriction still works when its predicate column is prohibited', async () => {
  const region = fields.find((f) => f.originName === 'region')
  await column([{ id: region.id, selected: true, opt: 'Prohibit' }])
  await row('user', ruleTree('region', 'East'))
  expect(values(await chart(userPage, { dimension: 'category' }))).toEqual([
    ['A', 10],
    ['B', 20]
  ])
  await clearRows()
  await clearColumns()
  expect(values(await chart(userPage))).toEqual([
    ['East', 30],
    ['West', 70]
  ])
})

test('computed metric custom filter sorting and result limit preserve exact values', async () => {
  expect(values(await chart(page, { metric: 'double_amount' }))).toEqual([
    ['East', 60],
    ['West', 140]
  ])
  const filter = {
    logic: 'and',
    items: [
      {
        type: 'item',
        fieldId: fields.find((f) => f.originName === 'amount').id,
        filterType: 'logic',
        term: 'gt',
        value: '20'
      }
    ]
  }
  expect(values(await chart(page, { view: { customFilter: filter } }))).toEqual(
    [['West', 70]]
  )
  const limited = await chart(page, {
    sort: 'desc',
    view: { resultCount: 1, resultMode: 'custom' }
  })
  expect(limited.ok).toBe(true)
  expect(
    limited.result.data.data.map((x) => [x.field, Number(x.value)])
  ).toEqual([['West', 70]])
  const valid = await call(userPage, '/chartData/validateCalcField', {
    datasetId: dataset.id,
    chartId,
    originName: Buffer.from(
      '[' + fields.find((f) => f.originName === 'amount').id + '] * 2'
    ).toString('base64')
  })
  expect(valid.ok).toBe(true)
})
const exportRows = async () => {
  const result = await userPage.evaluate(
    async ({ dataset, fields, chartId, canvasId }) => {
      const { BASE_VIEW_CONFIG } = await import(
        '/src/views/chart/components/editor/util/chart.ts'
      )
      const { default: request } = await import('/src/config/axios/index.ts')
      const view = {
        ...structuredClone(BASE_VIEW_CONFIG),
        id: chartId,
        sceneId: canvasId,
        tableId: dataset.id,
        type: 'table-info',
        title: 'synthetic detail',
        xAxis: fields.map((f) =>
          f.extField === 2 ? { ...f, originName: btoa(f.originName) } : f
        ),
        yAxis: [],
        resultMode: 'all',
        chartExtRequest: { goPage: 1, pageSize: 100 }
      }
      try {
        const r = await request.post({
          url: '/chartData/innerExportDataSetDetails',
          data: {
            dvId: canvasId,
            busiFlag: 'dashboard',
            viewId: chartId,
            viewName: 'synthetic permission export',
            viewInfo: view,
            downloadType: 'dataset',
            dataEaseBi: true
          },
          responseType: 'blob',
          handleErrorLocally: true
        })
        const bytes = new Uint8Array(await r.data.arrayBuffer())
        let binary = ''
        for (const byte of bytes) binary += String.fromCharCode(byte)
        return {
          ok: true,
          status: r.status,
          contentType: r.data.type,
          bytes: btoa(binary)
        }
      } catch (e) {
        return {
          ok: false,
          status: e.httpStatus,
          category: e.category,
          code: e.code
        }
      }
    },
    { dataset, fields, chartId, canvasId: owned.canvasId }
  )
  if (!result.ok) return result
  const buffer = Buffer.from(result.bytes, 'base64')
  expect(buffer.subarray(0, 2).toString()).toBe('PK')
  const workbook = new ExcelJS.Workbook()
  await workbook.xlsx.load(buffer)
  const rows = []
  workbook.worksheets[0].eachRow((row) => rows.push(row.values.slice(1)))
  return { ok: true, rows }
}
const grantExport = async (ext) =>
  api('/auth/saveSubjectPermission', {
    id: owned.roleId,
    type: 1,
    flag: 'PANEL',
    oid: proxy.proxyOid,
    system: false,
    permissions: [{ id: owned.canvasId, weight: 1, ext, oid: proxy.proxyOid }]
  })
test('read-only user cannot export raw detail without export permission', async () => {
  expect(await exportRows()).toMatchObject({
    ok: false,
    status: 403,
    category: 'FORBIDDEN'
  })
})
test('real XLSX download enforces row restriction hidden column and masking', async () => {
  await grantExport(111)
  await row('user', ruleTree('region', 'East'))
  const secret = fields.find((f) => f.originName === 'secret'),
    category = fields.find((f) => f.originName === 'category')
  await column([
    {
      id: secret.id,
      selected: true,
      opt: 'Desensitization',
      desensitizationRule: { builtInRule: 'CompleteDesensitization' }
    },
    { id: category.id, selected: true, opt: 'Prohibit' }
  ])
  const result = await exportRows()
  expect(result.ok).toBe(true)
  const [headers, ...rows] = result.rows
  expect(headers).not.toContain('category')
  expect(headers).toContain('secret')
  expect(rows).toHaveLength(2)
  expect(rows.every((r) => r[headers.indexOf('region')] === 'East')).toBe(true)
  expect(
    rows.map((r) => Number(r[headers.indexOf('amount')])).sort((a, b) => a - b)
  ).toEqual([10, 20])
  expect(rows.every((r) => r[headers.indexOf('secret')] === '******')).toBe(
    true
  )
  expect(JSON.stringify(rows)).not.toMatch(/1380000000[1-4]/)
  fs.writeFileSync(
    path.join(output, 'export-assertions.json'),
    JSON.stringify({
      rows: rows.length,
      hiddenColumnAbsent: true,
      allRowsEast: true,
      allSecretsMasked: true
    }),
    { mode: 0o600 }
  )
  await clearRows()
  await clearColumns()
})
test('revoked export permission blocks download while chart reading still works', async () => {
  await grantExport(0)
  expect(await exportRows()).toMatchObject({
    ok: false,
    status: 403,
    category: 'FORBIDDEN'
  })
  expect(values(await chart(userPage))).toEqual([
    ['East', 30],
    ['West', 70]
  ])
})

test('role column restriction and user whitelist change field visibility immediately', async () => {
  const category = fields.find((f) => f.originName === 'category')
  const available = async () => {
    const r = await call(
      userPage,
      '/chart/listByDQ/' + dataset.id + '/' + chartId,
      {}
    )
    expect(r.ok).toBe(true)
    return [...r.data.dimensionList, ...r.data.quotaList]
  }
  await column([{ id: category.id, selected: true, opt: 'Prohibit' }], {
    authTargetType: 'role'
  })
  expect((await available()).some((f) => f.id === category.id)).toBe(false)
  await column([{ id: category.id, selected: true, opt: 'Prohibit' }], {
    authTargetType: 'role',
    whiteListUser: JSON.stringify([owned.userId])
  })
  expect((await available()).some((f) => f.id === category.id)).toBe(true)
  expect(values(await chart(userPage, { dimension: 'category' }))).toEqual([
    ['A', 40],
    ['B', 60]
  ])
  await clearColumns()
})
test('disabled column restriction restores field and the full known chart result', async () => {
  const secret = fields.find((f) => f.originName === 'secret')
  await column([{ id: secret.id, selected: true, opt: 'Prohibit' }])
  await column([{ id: secret.id, selected: true, opt: 'Prohibit' }], {
    enable: false
  })
  const r = await chart(userPage, { dimension: 'secret' })
  expect(values(r)).toEqual([
    ['13800000001', 10],
    ['13800000002', 20],
    ['13800000003', 30],
    ['13800000004', 40]
  ])
  await clearColumns()
})
