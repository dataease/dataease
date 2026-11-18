// Run: node --test tests/config-request-cache/regression.cjs
const assert = require('node:assert/strict')
const fs = require('node:fs')
const vm = require('node:vm')
const ts = require('typescript')
const { test } = require('node:test')
const compile = file => ts.transpileModule(fs.readFileSync(file, 'utf8'), {
  compilerOptions: { target: ts.ScriptTarget.ES2020, module: ts.ModuleKind.CommonJS }
}).outputText
const setup = () => {
  const storage = new Map([['user.token', 'user-a'], ['user.uid', 'a'], ['user.oid', 'org-a']])
  const embedded = { baseUrl: '', token: '' }
  const link = { getLinkToken: '' }
  const context = { exports: {}, require: name => {
    if (name.includes('useCache')) return { useCache: () => ({ wsCache: { get: key => storage.get(key) } }) }
    if (name.includes('embedded')) return { useEmbedded: () => embedded }
    if (name.includes('link')) return { useLinkStoreWithOut: () => link }
    throw new Error(name)
  } }
  vm.runInNewContext(compile('src/utils/configRequestCache.ts'), context)
  const calls = []
  const cache = context.exports.createConfigRequestCache(() => new Promise((resolve, reject) => calls.push({ resolve, reject })))
  return { ...context.exports, cache, calls, storage, embedded, link }
}
const flush = async () => { for (let i = 0; i < 5; i++) await Promise.resolve() }

test('parallel reads and later navigation share one successful response, including empty data', async () => {
  const ctx = setup()
  const a = ctx.cache.get()
  const b = ctx.cache.get()
  assert.equal(a, b)
  await flush()
  assert.equal(ctx.calls.length, 1)
  ctx.calls[0].resolve([])
  const value = await a
  assert.equal(await ctx.cache.get(), value)
  assert.equal(ctx.calls.length, 1)
})
test('failure rejects all waiting callers and a later read retries', async () => {
  const ctx = setup()
  const a = ctx.cache.get()
  const b = ctx.cache.get()
  const failure = assert.rejects(a, /failed/)
  await flush()
  ctx.calls[0].reject(new Error('failed'))
  await failure
  await assert.rejects(b, /failed/)
  const retry = ctx.cache.get()
  await flush()
  assert.equal(ctx.calls.length, 2)
  ctx.calls[1].resolve('new')
  assert.equal(await retry, 'new')
})
for (const [name, change] of [
  ['account', ctx => ctx.storage.set('user.token', 'user-b')],
  ['organization', ctx => ctx.storage.set('user.oid', 'org-b')],
  ['admin proxy', ctx => ctx.storage.set('user.proxyInfo', { proxy: true, proxyOid: 'other', proxySecret: 'synthetic' })],
  ['language', ctx => ctx.storage.set('user.language', 'en')],
  ['embedded token', ctx => { ctx.embedded.token = 'synthetic-embed' }],
  ['embedded host', ctx => { ctx.embedded.baseUrl = 'https://example.invalid' }],
  ['share token', ctx => { ctx.link.getLinkToken = 'synthetic-share' }]
]) {
  test(`${name} change reloads instead of reusing the previous scope`, async () => {
    const ctx = setup()
    const a = ctx.cache.get()
    await flush()
    ctx.calls[0].resolve('old')
    await a
    change(ctx)
    const b = ctx.cache.get()
    await flush()
    assert.equal(ctx.calls.length, 2)
    ctx.calls[1].resolve('new')
    assert.equal(await b, 'new')
  })
}
test('old in-flight responses cannot overwrite refreshed configuration', async () => {
  const ctx = setup()
  const old = ctx.cache.get()
  await flush()
  const refreshed = ctx.cache.get(true)
  await flush()
  ctx.calls[1].resolve('fresh')
  assert.equal(await refreshed, 'fresh')
  ctx.calls[0].resolve('stale')
  assert.equal(await old, 'fresh')
  assert.equal(await ctx.cache.get(), 'fresh')
})
test('identity change during a request protects even callers that started under the old identity', async () => {
  const ctx = setup()
  const old = ctx.cache.get()
  await flush()
  ctx.storage.set('user.oid', 'org-b')
  ctx.calls[0].resolve('old-org')
  await flush()
  assert.equal(ctx.calls.length, 2)
  ctx.calls[1].resolve('new-org')
  assert.equal(await old, 'new-org')
})
test('logout cleanup clears all configuration caches even when the same account signs back in', async () => {
  const ctx = setup()
  const a = ctx.cache.get()
  await flush()
  ctx.calls[0].resolve('old')
  await a
  ctx.clearConfigRequestCaches()
  const b = ctx.cache.get()
  await flush()
  ctx.calls[1].resolve('new')
  assert.equal(await b, 'new')
})

const loadApi = (file, cacheModule, request) => {
  const context = { exports: {}, require: name => name.includes('configRequestCache') ? cacheModule : { default: request } }
  vm.runInNewContext(compile(file), context)
  return context.exports
}
test('font mutations invalidate successful cached lists and raw list requests remain uncached', async () => {
  const ctx = setup()
  let lists = 0
  const request = { get: async () => ({ data: [] }), post: async ({ url }) => ({ data: url.endsWith('listFont') ? [{ id: String(++lists) }] : {} }) }
  const api = loadApi('src/api/font.ts', ctx, request)
  await api.getCachedFontList()
  await api.getCachedFontList()
  assert.equal(lists, 1)
  for (const mutate of [() => api.create({}), () => api.edit({}), () => api.deleteById('synthetic'), () => api.uploadFontFile({})]) {
    await mutate()
    const before = lists
    await api.getCachedFontList()
    assert.equal(lists, before + 1)
  }
  const before = lists
  await api.list()
  await api.list()
  assert.equal(lists, before + 2)
})
test('default settings support explicit invalidation, forced refresh and unchanged raw API reads', async () => {
  const ctx = setup()
  let calls = 0
  const api = loadApi('src/api/common.ts', ctx, { get: async () => ({ data: { 'basic.defaultSort': String(++calls) } }) })
  await api.getCachedDefaultSettings()
  await api.getCachedDefaultSettings()
  assert.equal(calls, 1)
  api.invalidateDefaultSettings()
  await api.getCachedDefaultSettings()
  await api.getCachedDefaultSettings(true)
  assert.equal(calls, 3)
  await api.getDefaultSettings()
  await api.getDefaultSettings()
  assert.equal(calls, 5)
})
test('font Store receives a copy so consumers cannot mutate the cached response', async () => {
  const source = fs.readFileSync('src/store/modules/appearance.ts', 'utf8')
  const ast = ts.createSourceFile('appearance.ts', source, ts.ScriptTarget.Latest, true)
  let method
  const visit = node => {
    if (ts.isMethodDeclaration(node) && node.name.getText(ast) === 'setFontList') method = node
    ts.forEachChild(node, visit)
  }
  visit(ast)
  assert.ok(method)
  const fonts = [{ id: 'synthetic', name: 'test font' }]
  let forced
  const context = { getCachedFontList: async force => { forced = force; return fonts }, cloneDeep: structuredClone }
  const action = vm.runInNewContext(ts.transpileModule(`({${method.getText(ast)}}).setFontList`, {
    compilerOptions: { target: ts.ScriptTarget.ES2020 }
  }).outputText, context)
  const store = {}
  await action.call(store, true)
  assert.equal(forced, true)
  store.fontList[0].name = 'changed'
  assert.equal(fonts[0].name, 'test font')
})
test('successful basic-settings save invalidates cached defaults, rejected save preserves them', () => {
  const { parse } = require('@vue/compiler-sfc')
  const source = parse(fs.readFileSync('src/views/system/parameter/basic/BasicEdit.vue', 'utf8')).descriptor.scriptSetup.content
  const ast = ts.createSourceFile('BasicEdit.ts', source, ts.ScriptTarget.Latest, true)
  let callback
  const visit = node => {
    if (ts.isCallExpression(node) && ts.isPropertyAccessExpression(node.expression) &&
      node.expression.name.text === 'then' && node.arguments[0]?.getText(ast).includes('invalidateDefaultSettings()')) {
      callback = node.arguments[0]
    }
    ts.forEachChild(node, visit)
  }
  visit(ast)
  assert.ok(callback)
  let invalidations = 0
  const context = { invalidateDefaultSettings: () => invalidations++,
    ElMessage: { success() {} }, t: value => value, emits() {}, reset() {}, closeLoading() {} }
  const apply = vm.runInNewContext(`(${callback.getText(ast)})`, context)
  apply({ msg: 'failed' })
  assert.equal(invalidations, 0)
  apply({})
  assert.equal(invalidations, 1)
})
test('concurrent explicit refreshes share one fresh request while replacing an older ordinary read', async () => {
  const ctx = setup()
  const old = ctx.cache.get()
  await flush()
  const firstRefresh = ctx.cache.get(true)
  const secondRefresh = ctx.cache.get(true)
  assert.equal(firstRefresh, secondRefresh)
  await flush()
  assert.equal(ctx.calls.length, 2)
  ctx.calls[0].resolve('old')
  await flush()
  ctx.calls[1].resolve('fresh')
  assert.equal(await old, 'fresh')
  assert.equal(await secondRefresh, 'fresh')
})
test('a mutation invalidates even an in-flight forced refresh', async () => {
  const ctx = setup()
  const beforeMutation = ctx.cache.get(true)
  await flush()
  ctx.cache.clear()
  const afterMutation = ctx.cache.get(true)
  await flush()
  assert.notEqual(beforeMutation, afterMutation)
  ctx.calls[0].resolve('pre-mutation')
  await flush()
  ctx.calls[1].resolve('post-mutation')
  assert.equal(await beforeMutation, 'post-mutation')
  assert.equal(await afterMutation, 'post-mutation')
})
