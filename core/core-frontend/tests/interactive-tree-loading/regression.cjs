// Run: node --test tests/interactive-tree-loading/regression.cjs
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const vm = require('node:vm')
const { test } = require('node:test')
const ts = require('typescript')
const pinia = require('pinia')

const flags = ['dashboard', 'dataV', 'dataset', 'datasource']
const tree = flag => [{ weight: 7, children: [{ id: flag, leaf: true, weight: 7 }] }]
const code = ts.transpileModule(
  fs.readFileSync(path.resolve(__dirname, '../../src/store/modules/interactive.ts'), 'utf8'),
  { compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2020 } }
).outputText

const setup = ({ allowed = true, embedded = false, iframe = false } = {}) => {
  const requests = []
  const cache = new Map()
  const instance = pinia.createPinia()
  const request = param =>
    new Promise((resolve, reject) => requests.push({ param, resolve, reject }))
  const modules = {
    pinia,
    '@/store': { store: instance },
    '@/api/visualization/dataVisualization': {
      queryTreeApi: request,
      queryBusiTreeApi: request
    },
    '@/api/dataset': { getDatasetTree: request },
    '@/api/datasource': { listDatasources: request },
    '@/store/modules/permission': { pathValid: () => allowed },
    '@/hooks/web/useCache': { useCache: () => ({ wsCache: { set: (k, v) => cache.set(k, v) } }) },
    '@/store/modules/app': { useAppStoreWithOut: () => ({ getIsIframe: iframe }) }
  }
  const exports = {}
  vm.runInNewContext(code, {
    exports,
    window: { DataEaseBi: embedded },
    require: id => {
      assert.ok(id in modules, `Unexpected import: ${id}`)
      return modules[id]
    }
  })
  return { store: exports.interactiveStore(instance), requests, cache }
}

test('starts all four requests before any resolves and waits for the slowest', async () => {
  const { store, requests, cache } = setup()
  let done = false
  const loading = store.initInteractive().then(() => (done = true))
  assert.equal(requests.length, 4)
  assert.deepEqual(requests.map(r => r.param.busiFlag).sort(), [...flags].sort())
  for (const request of requests.slice(1)) request.resolve(tree(request.param.busiFlag))
  await new Promise(resolve => setImmediate(resolve))
  assert.equal(done, false)
  assert.equal(Object.keys(store.data).length, 3)
  requests[0].resolve(tree(requests[0].param.busiFlag))
  await loading
  flags.forEach((flag, index) => {
    assert.equal(store.data[index].leafNodeCount, 1)
    assert.equal(store.data[index].treeNodes[0].children[0].id, flag)
  })
  assert.equal(cache.get('panel-weight').dashboard, 7)
  assert.equal(cache.get('screen-weight').dataV, 7)
})

test('retains cached trees and only requests missing trees', async () => {
  const { store, requests } = setup()
  await store.setInteractive({ busiFlag: 'dashboard' }, tree('cached-dashboard'))
  const cached = store.data[0]
  const loading = store.initInteractive()
  assert.equal(requests.length, 3)
  assert.ok(requests.every(r => r.param.busiFlag !== 'dashboard'))
  requests.forEach(r => r.resolve(tree(r.param.busiFlag)))
  await loading
  assert.equal(store.data[0], cached)
  await store.initInteractive()
  assert.equal(requests.length, 3)
})

test('denied menus make no requests and clear panel/screen weights', async () => {
  const { store, requests, cache } = setup({ allowed: false })
  await store.initInteractive()
  assert.equal(requests.length, 0)
  flags.forEach((_, index) => assert.equal(store.data[index].menuAuth, false))
  assert.equal(Object.keys(cache.get('panel-weight')).length, 0)
  assert.equal(Object.keys(cache.get('screen-weight')).length, 0)
})

test('embedded and iframe access retain the existing menu bypass', async () => {
  for (const option of [{ embedded: true }, { iframe: true }]) {
    const { store, requests } = setup({ allowed: false, ...option })
    const loading = store.initInteractive()
    assert.equal(requests.length, 4)
    requests.forEach(r => r.resolve(tree(r.param.busiFlag)))
    await loading
  }
})

test('propagates request failure while independent trees can finish', async () => {
  const { store, requests } = setup()
  const loading = store.initInteractive()
  const error = new Error('tree request failed')
  const rejected = assert.rejects(loading, error)
  requests[0].reject(error)
  requests.slice(1).forEach(r => r.resolve(tree(r.param.busiFlag)))
  await rejected
  assert.equal(store.data[3], undefined)
  assert.equal(Object.keys(store.data).length, 3)
})

test('refresh retains one batched request and replaces existing trees', async () => {
  const { store, requests } = setup()
  await store.setInteractive({ busiFlag: 'dashboard' }, tree('old-dashboard'))
  const loading = store.initInteractive(true)
  assert.equal(requests.length, 1)
  assert.deepEqual(Object.keys(requests[0].param), flags)
  requests[0].resolve(Object.fromEntries(flags.map(flag => [flag, tree(flag)])))
  await loading
  flags.forEach((flag, index) => {
    assert.equal(store.data[index].treeNodes[0].children[0].id, flag)
  })
})

test('invalidated batched responses cannot restore cleared trees or permission weights', async () => {
  const { store, requests, cache } = setup()
  let current = true
  const loading = store.initInteractive(true, () => current)
  current = false
  store.clear()
  requests[0].resolve(Object.fromEntries(flags.map(flag => [flag, tree('stale-' + flag)])))
  await loading
  assert.equal(Object.keys(store.data).length, 0)
  assert.equal(Object.keys(cache.get('panel-weight')).length, 0)
  assert.equal(Object.keys(cache.get('screen-weight')).length, 0)
})

test('non-refresh initialization also discards invalidated individual responses', async () => {
  const { store, requests, cache } = setup()
  let current = true
  const loading = store.initInteractive(false, () => current)
  current = false
  store.clear()
  requests.forEach(request => request.resolve(tree(request.param.busiFlag)))
  await loading
  assert.equal(Object.keys(store.data).length, 0)
  assert.equal(Object.keys(cache.get('panel-weight')).length, 0)
  assert.equal(Object.keys(cache.get('screen-weight')).length, 0)
})

test('already invalidated initialization and direct updates have no side effects', async () => {
  const { store, requests, cache } = setup({ allowed: false })
  await store.initInteractive(true, () => false)
  await store.initInteractive(false, () => false)
  await store.loadBusiInteractive(() => false)
  await store.setInteractive({ busiFlag: 'dashboard' }, tree('stale'), () => false)
  assert.equal(requests.length, 0)
  assert.equal(Object.keys(store.data).length, 0)
  assert.equal(cache.size, 0)
})

const setupMobileInitialization = interactiveStore => {
  const source = fs.readFileSync(path.resolve(__dirname, '../../src/permissionMobile.ts'), 'utf8')
  const methods = source.slice(
    source.indexOf('let initPermissionPromise:'),
    source.indexOf('router.beforeEach(')
  )
  const menus = []
  const permission = { ready: false, routes: [] }
  const context = {
    interactiveStore,
    getRoleRouters: () => new Promise((resolve, reject) => menus.push({ resolve, reject })),
    permissionStore: {
      get getIsAddRouters() {
        return permission.ready
      },
      generateRoutes: async routes => {
        permission.routes = routes
      },
      setIsAddRouters: value => {
        permission.ready = value
      }
    }
  }
  vm.runInNewContext(
    ts.transpileModule(methods, { compilerOptions: { target: ts.ScriptTarget.ES2020 } })
      .outputText +
      ';this.api = { ensurePermissionInitialized, invalidatePermissionInitialization }',
    context
  )
  return { ...context.api, menus, permission }
}

const flush = async () => {
  for (let i = 0; i < 10; i++) await Promise.resolve()
}
const resolveTrees = (request, prefix) =>
  request.resolve(Object.fromEntries(flags.map(flag => [flag, tree(prefix + flag)])))

test('late previous-session batch cannot overwrite new-session trees or caches', async () => {
  const { store, requests, cache } = setup()
  const mobile = setupMobileInitialization(store)
  const old = mobile.ensurePermissionInitialized()
  mobile.menus[0].resolve([{ name: 'old-menu' }])
  await flush()
  assert.equal(requests.length, 1)
  store.clear()
  mobile.invalidatePermissionInitialization()
  const current = mobile.ensurePermissionInitialized()
  mobile.menus[1].resolve([{ name: 'new-menu' }])
  await flush()
  resolveTrees(requests[1], 'new-')
  await current
  resolveTrees(requests[0], 'old-')
  await old
  assert.equal(mobile.permission.ready, true)
  assert.equal(mobile.permission.routes[0].name, 'new-menu')
  flags.forEach((flag, index) => {
    assert.equal(store.data[index].treeNodes[0].children[0].id, 'new-' + flag)
  })
  assert.equal(cache.get('panel-weight')['new-dashboard'], 7)
  assert.equal(cache.get('panel-weight')['old-dashboard'], undefined)
  assert.equal(cache.get('screen-weight')['new-dataV'], 7)
  await mobile.ensurePermissionInitialized()
  assert.equal(mobile.menus.length, 2)
})

test('obsolete menu response cannot write routes or clear the current shared task', async () => {
  const { store, requests } = setup()
  const mobile = setupMobileInitialization(store)
  const old = mobile.ensurePermissionInitialized()
  mobile.invalidatePermissionInitialization()
  const current = mobile.ensurePermissionInitialized()
  mobile.menus[0].resolve([{ name: 'old-menu' }])
  await old
  assert.equal(mobile.permission.routes.length, 0)
  assert.equal(requests.length, 0)
  assert.equal(mobile.ensurePermissionInitialized(), current)
  mobile.menus[1].resolve([{ name: 'new-menu' }])
  await flush()
  resolveTrees(requests[0], 'new-')
  await current
  assert.equal(mobile.permission.ready, true)
  assert.equal(mobile.menus.length, 2)
})

test('obsolete failure cannot clear the current shared task', async () => {
  const { store, requests } = setup()
  const mobile = setupMobileInitialization(store)
  const old = mobile.ensurePermissionInitialized()
  const rejected = assert.rejects(old, /old failed/)
  mobile.invalidatePermissionInitialization()
  const current = mobile.ensurePermissionInitialized()
  mobile.menus[0].reject(new Error('old failed'))
  await rejected
  assert.equal(mobile.ensurePermissionInitialized(), current)
  mobile.menus[1].resolve([{ name: 'new-menu' }])
  await flush()
  resolveTrees(requests[0], 'new-')
  await current
})

test('failed current initialization can retry and complete normally', async () => {
  const { store, requests } = setup()
  const mobile = setupMobileInitialization(store)
  const first = mobile.ensurePermissionInitialized()
  mobile.menus[0].resolve([{ name: 'menu' }])
  await flush()
  const rejected = assert.rejects(first, /tree failed/)
  requests[0].reject(new Error('tree failed'))
  await rejected
  assert.equal(mobile.permission.ready, false)
  const retry = mobile.ensurePermissionInitialized()
  mobile.menus[1].resolve([{ name: 'menu' }])
  await flush()
  resolveTrees(requests[1], 'retry-')
  await retry
  assert.equal(mobile.permission.ready, true)
})
