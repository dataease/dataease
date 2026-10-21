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
