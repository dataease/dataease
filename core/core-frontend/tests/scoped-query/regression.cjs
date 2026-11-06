/* eslint-disable @typescript-eslint/no-var-requires -- Node CommonJS 回归入口。 */
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const vm = require('node:vm')
const { test } = require('node:test')
const ts = require('typescript')
const { parse } = require('@vue/compiler-sfc')
const axios = require('axios')
const root = path.resolve(__dirname, '../..')
const compile = source =>
  ts.transpileModule(source, {
    compilerOptions: { target: ts.ScriptTarget.ES2020, module: ts.ModuleKind.CommonJS }
  }).outputText
const deferred = () => {
  let resolve, reject
  const promise = new Promise((a, b) => {
    resolve = a
    reject = b
  })
  return { promise, resolve, reject }
}
const flush = async () => {
  for (let i = 0; i < 12; i++) await Promise.resolve()
}

function environment() {
  const modules = new Map()
  const messages = [],
    loading = [],
    signatures = []
  const permission = { getCurrentPath: '/page-a' }
  const mocks = {
    '@/utils/loading': {
      tryShowLoading: key => loading.push(['show', key]),
      tryHideLoading: key => loading.push(['hide', key])
    },
    '@/store/modules/permission': { usePermissionStoreWithOut: () => permission },
    '@/store/modules/embedded': { useEmbedded: () => ({}) },
    '@/store/modules/link': { useLinkStoreWithOut: () => ({}) },
    '@/utils/utils': { isMobile: () => false, getLocale: () => 'en', isLink: () => false },
    '@/hooks/web/useI18n': { useI18n: () => ({ t: key => key }) },
    '@/store/modules/request': {
      useRequestStoreWithOut: () => ({
        resetLoadingMap() {
          return undefined
        }
      })
    },
    '@/utils/cacheUtil': {
      clearCache() {
        return undefined
      }
    },
    '@/hooks/web/useCache': {
      useCache: () => ({
        wsCache: {
          get() {
            return undefined
          },
          set() {
            return undefined
          }
        }
      })
    },
    'element-plus-secondary': { ElMessage: value => messages.push(value), ElMessageBox: {} },
    '@/router': {
      default: {
        currentRoute: { value: { fullPath: '/page-a' } },
        push() {
          return undefined
        }
      }
    }
  }
  function load(file) {
    file = path.resolve(root, file)
    if (modules.has(file)) return modules.get(file)
    const exports = {}
    const context = {
      exports,
      AbortController,
      console,
      setTimeout,
      clearTimeout,
      window: {
        location: {
          reload() {
            return undefined
          }
        }
      },
      localStorage: {
        setItem() {
          return undefined
        }
      },
      XMLHttpRequest: class {
        open() {
          return undefined
        }
        send() {
          this.readyState = 4
          this.status = 200
          this.responseText = '{"code":0,"data":60}'
          this.onreadystatechange()
        }
      },
      require(name) {
        if (mocks[name]) return mocks[name]
        if (name === './refresh') return { configHandler: value => value }
        if (name === './hmac') return { securityConfig: async c => signatures.push(c.url) }
        if (name === './hmacError') return { getHmacFailure: () => null }
        if (name === './hmacMessage') return { getHmacMessage: async () => '' }
        if (name.startsWith('.')) return load(path.resolve(path.dirname(file), name + '.ts'))
        if (name.startsWith('@/config/axios/')) return load('src/' + name.slice(2) + '.ts')
        return require(name)
      }
    }
    const source = fs
      .readFileSync(file, 'utf8')
      .replaceAll('import.meta.env.VITE_API_BASEPATH', "'/api'")
    vm.runInNewContext(compile(source), context, { filename: file })
    modules.set(file, exports)
    return exports
  }
  return { load, messages, loading, signatures, permission }
}

test('scopes isolate equal resources and purposes; completion never removes a newer handle', () => {
  const { load } = environment()
  const { createQueryScope } = load('src/config/axios/queryScope.ts')
  const a = createQueryScope(),
    b = createQueryScope()
  const first = a.createQuery('chart-data'),
    second = a.createQuery('chart-data')
  const options = a.createQuery('options'),
    external = b.createQuery('chart-data')
  const old = first.start(),
    other = second.start(),
    option = options.start(),
    separate = external.start()
  const latest = first.start()
  assert.equal(old.signal.aborted, true)
  old.finish()
  assert.equal(a.pendingCount, 3)
  assert.equal(other.isCurrent(), true)
  assert.equal(option.isCurrent(), true)
  assert.equal(separate.isCurrent(), true)
  latest.finish()
  assert.equal(latest.isCurrent(), true, 'transport cleanup must not invalidate pending rendering')
  a.dispose()
  assert.equal(a.pendingCount, 0)
  assert.equal(latest.isCurrent(), false)
  assert.equal(other.signal.aborted, true)
  assert.equal(option.signal.aborted, true)
  assert.equal(separate.signal.aborted, false)
  first.start().finish()
  assert.equal(a.pendingCount, 0)
  b.dispose()
})

test('real Axios preserves scoped metadata, signing and language; legacy cancellation excludes scoped reads and writes', async () => {
  const ctx = environment()
  const { service, cancelAllRequest, cancelMap } = ctx.load('src/config/axios/service.ts')
  const api = ctx.load('src/config/axios/index.ts').default
  const scope = ctx.load('src/config/axios/queryScope.ts').createQueryScope()
  const calls = []
  service.defaults.adapter = config => {
    const call = deferred()
    calls.push({ ...call, config })
    return call.promise
  }
  const query = scope.createQuery('chart-data').start()
  const read = api.post({
    url: '/chartData/getData',
    data: { id: 'same' },
    queryRequest: query,
    loading: true
  })
  const write = api.post({ url: '/chart/save', data: { id: 'same' } })
  await flush()
  assert.equal(calls.length, 2)
  assert.equal(calls[0].config.signal, query.signal)
  assert.equal(calls[0].config.headers['Accept-Language'], 'en-US')
  assert.deepEqual(ctx.signatures, ['/chartData/getData', '/chart/save'])
  assert.equal(Object.keys(cancelMap).length, 0)
  cancelAllRequest()
  assert.equal(query.signal.aborted, false)
  assert.equal(calls[1].config.cancelToken, undefined)
  ctx.permission.getCurrentPath = '/page-b'
  calls.forEach(call =>
    call.resolve({ config: call.config, headers: {}, data: { code: 0, data: 'ok' } })
  )
  await Promise.all([read, write])
  assert.equal(scope.pendingCount, 0)
  assert.deepEqual(ctx.loading, [
    ['show', '/page-a'],
    ['hide', '/page-a']
  ])
  assert.equal(ctx.messages.length, 0)
  scope.dispose()
})

for (const outcome of ['success', 'failure', 'cancel']) {
  test(`legacy ${outcome} releases its handle and loading; an older completion cannot delete the newer request`, async () => {
    const ctx = environment()
    const { service, cancelMap } = ctx.load('src/config/axios/service.ts')
    const calls = []
    service.defaults.adapter = config => {
      const call = deferred()
      calls.push({ ...call, config })
      return call.promise
    }
    const old = service
      .post('/datasetData/previewData', {}, { loading: true })
      .catch(error => error)
    await flush()
    const current = service
      .post('/datasetData/previewData', {}, { loading: true })
      .catch(error => error)
    await flush()
    const handle = cancelMap['/datasetData/previewData']
    calls[0].resolve({ config: calls[0].config, headers: {}, data: { code: 0 } })
    await old
    assert.equal(cancelMap['/datasetData/previewData'], handle)
    if (outcome === 'cancel') handle()
    if (outcome === 'failure') {
      calls[1].reject(new axios.AxiosError('offline', 'ERR_NETWORK', calls[1].config))
    } else {
      calls[1].resolve({ config: calls[1].config, headers: {}, data: { code: 0 } })
    }
    const result = await current
    if (outcome === 'cancel') assert.equal(axios.isCancel(result), true)
    assert.equal(Object.keys(cancelMap).length, 0)
    assert.equal(ctx.loading.filter(([kind]) => kind === 'hide').length, 2)
    assert.equal(ctx.messages.length, 0)
  })
}

test('stale scoped responses and business errors have no global side effects, even when adapter ignores cancellation', async () => {
  const ctx = environment()
  const { service } = ctx.load('src/config/axios/service.ts')
  const scope = ctx.load('src/config/axios/queryScope.ts').createQueryScope()
  const owner = scope.createQuery('chart-data')
  // Call the actual response handler to emulate a transport that delivers after cancellation.
  const onResponse = service.interceptors.response.handlers[0].fulfilled
  const old = owner.start()
  const latest = owner.start()
  const response = {
    config: { queryRequest: old, loading: false },
    headers: {},
    data: { code: 80001, msg: 'stale denied' }
  }
  await assert.rejects(onResponse(response), error => axios.isCancel(error))
  assert.equal(ctx.messages.length, 0)
  assert.equal(scope.pendingCount, 1)
  assert.equal(latest.isCurrent(), true)
  scope.dispose()
})

function chartHarness(file, scope) {
  const source = parse(
    fs.readFileSync(path.join(process.env.SCOPED_QUERY_BASELINE_DIR || root, file), 'utf8')
  ).descriptor.scriptSetup.content
  const ast = ts.createSourceFile('chart.ts', source, ts.ScriptTarget.Latest, true)
  const names = new Set(['dataQuery', 'invalidateQuery', 'calcData', 'pendingQueryCallback'])
  const body = ast.statements
    .filter(
      n =>
        ts.isVariableStatement(n) &&
        n.declarationList.declarations.some(d => names.has(d.name.getText(ast)))
    )
    .map(n => n.getText(ast))
    .join('\n')
  const calls = [],
    writes = [],
    renders = [],
    effects = []
  const context = {
    exports: {},
    AbortController,
    JSON,
    clearTimeout() {
      return undefined
    },
    chartRenderVersion: 0,
    chartData: { value: 'initial' },
    isError: { value: false },
    errMsg: { value: '' },
    dynamicAreaId: { value: '' },
    scope: null,
    gadmName: null,
    state: { pageInfo: {}, currentPageSize: 0 },
    canvasViewInfo: { value: { same: {} } },
    element: { value: { id: 'same' } },
    initReady: { value: false },
    initCurFields() {
      return undefined
    },
    initCarousel() {
      return undefined
    },
    withInit() {
      return undefined
    },
    updateEmptyValue() {
      return undefined
    },
    checkCompareCalc: () => true,
    parseJson: JSON.parse,
    debounceRender: {
      cancel() {
        return undefined
      }
    },
    resolveRenderChart() {
      return undefined
    },
    mapTimer: 0,
    mapL7Timer: 0,
    g2Timer: 0,
    finishG2Schedule: undefined,
    chartRuntime: { publishToStore: true, setDetails: () => undefined, view: { id: 'same' } },
    mockRefreshResponse: undefined,
    useLatestQuery: purpose => scope.createQuery(purpose),
    normalizeChartData: value => value,
    emit: (...args) => effects.push(args),
    emitter: { emit: (...args) => effects.push(args) },
    dvMainStore: new Proxy(
      {},
      {
        get:
          (_, name) =>
          (...args) =>
            writes.push([name, ...args])
      }
    ),
    getData: (view, query) => {
      const call = deferred()
      calls.push({ ...call, query, view })
      return query ? call.promise.finally(query.finish) : call.promise
    },
    nextTick: fn => Promise.resolve().then(fn),
    renderChart: (...args) => {
      const render = deferred()
      renders.push({ ...render, args })
      if (file.includes('G2Plot')) return render.promise.then(() => args[1]?.())
      if (file.includes('S2')) return render.promise
      return undefined
    }
  }
  vm.runInNewContext(
    compile(
      body +
        '\nexports.calcData = calcData; exports.invalidate = typeof invalidateQuery === "undefined" ? undefined : invalidateQuery'
    ),
    context
  )
  return { ...context, api: context.exports, calls, writes, renders, effects }
}
const chart = () => ({
  id: 'same',
  tableId: 'table',
  customAttr: { basicStyle: {} },
  chartExtRequest: {},
  senior: '{"threshold":{"enable":true}}'
})
const response = value => ({ data: { value, fields: [] }, drillFilters: [], totalItems: value })
for (const file of [
  'src/views/chart/components/views/components/ChartComponentG2Plot.vue',
  'src/views/chart/components/views/components/ChartComponentS2.vue',
  'src/custom-component/indicator/DeIndicator.vue',
  'src/custom-component/rich-text/DeRichTextView.vue',
  'src/custom-component/picture-group/Component.vue'
]) {
  test(`${path.basename(
    file
  )}: old success/failure cannot update data, cache, drill or loading`, async () => {
    const scope = environment().load('src/config/axios/queryScope.ts').createQueryScope()
    const a = chartHarness(file, scope),
      b = chartHarness(file, scope)
    let completed = 0
    const old = a.api.calcData(chart(), () => completed++)
    const other = b.api.calcData(chart(), () => undefined)
    const newest = a.api.calcData(chart(), () => completed++)
    a.calls[0].resolve(response('old'))
    await old
    assert.equal(a.writes.length, 0)
    assert.equal(a.renders.length, 0)
    assert.equal(a.effects.length, 0)
    assert.equal(completed, 0)
    assert.equal(b.calls[0].query.signal.aborted, false)
    a.calls[1].resolve(response('new'))
    await flush()
    a.renders[0]?.resolve()
    await newest
    assert.equal(completed, 1)
    assert.equal(a.writes[0][2].data.value, 'new')
    const obsolete = a.api.calcData(chart(), () => completed++)
    a.api.invalidate() // Filtering must invalidate before debounce executes the next request.
    a.calls[2].reject(new Error('late failure'))
    await obsolete
    assert.equal(completed, 1)
    b.calls[0].resolve(response('other'))
    await flush()
    b.renders[0]?.resolve()
    await other
    assert.equal(scope.pendingCount, 0)
    scope.dispose()
  })
}

test('S2 pagination supersedes pending filtering without losing its loading completion; old render completion is ignored', async () => {
  const scope = environment().load('src/config/axios/queryScope.ts').createQueryScope()
  const ctx = chartHarness(
    'src/views/chart/components/views/components/ChartComponentS2.vue',
    scope
  )
  let completed = 0
  const filtered = ctx.api.calcData(chart(), () => completed++)
  ctx.calls[0].resolve(response(100))
  await flush()
  assert.equal(ctx.renders.length, 1)
  const page = ctx.api.calcData(chart(), null, false)
  ctx.renders[0].resolve()
  await filtered
  assert.equal(completed, 0)
  ctx.calls[1].resolve(response(200))
  await flush()
  ctx.renders[1].resolve()
  await page
  assert.equal(ctx.state.totalItems, 200)
  assert.equal(completed, 1)
  scope.dispose()
})

test('Vue page unmount disposes inherited queries while a second mounted page continues', async () => {
  const { createRenderer, defineComponent, h, ref, nextTick } = require('vue')
  const ctx = environment()
  const { provideQueryScope, useLatestQuery } = ctx.load('src/hooks/web/useQueryScope.ts')
  const scopes = [],
    queries = [],
    visible = ref(true)
  const Child = defineComponent({
    setup() {
      const options = useLatestQuery('options').start()
      const preview = useLatestQuery('preview').start()
      assert.equal(options.instanceId, preview.instanceId)
      queries.push(options, preview)
      return () => null
    }
  })
  const Page = defineComponent({
    setup() {
      scopes.push(provideQueryScope())
      return () => h(Child)
    }
  })
  const renderer = createRenderer({
    createElement: () => ({}),
    createText: () => ({}),
    createComment: () => ({}),
    insert: () => undefined,
    remove: () => undefined,
    setText: () => undefined,
    setElementText: () => undefined,
    patchProp: () => undefined,
    parentNode: () => null,
    nextSibling: () => null
  })
  const app = renderer.createApp({
    render: () => h('div', [visible.value ? h(Page, { key: 'a' }) : null, h(Page, { key: 'b' })])
  })
  app.mount({})
  assert.equal(scopes[0].pendingCount, 2)
  assert.equal(scopes[1].pendingCount, 2)
  visible.value = false
  await nextTick()
  assert.equal(queries[0].isCurrent(), false)
  assert.equal(queries[1].isCurrent(), false)
  assert.equal(scopes[0].pendingCount, 0)
  assert.equal(queries[2].isCurrent(), true)
  assert.equal(scopes[1].pendingCount, 2)
  app.unmount()
  assert.equal(scopes[1].pendingCount, 0)
})

test('ten rapid filters commit only the latest response when all obsolete responses arrive afterward', async () => {
  const scope = environment().load('src/config/axios/queryScope.ts').createQueryScope()
  const ctx = chartHarness(
    'src/views/chart/components/views/components/ChartComponentG2Plot.vue',
    scope
  )
  let completed = 0
  const promises = Array.from({ length: 10 }, () => ctx.api.calcData(chart(), () => completed++))
  ctx.calls[9].resolve(response(9))
  await flush()
  ctx.renders[0].resolve()
  await flush()
  for (let i = 0; i < 9; i++) ctx.calls[i].resolve(response(i))
  await flush()
  ctx.renders.slice(1).forEach(render => render.resolve())
  await Promise.all(promises)
  await flush()
  const detailWrites = ctx.writes.filter(([name]) => name === 'setViewDataDetails')
  assert.equal(detailWrites.length, 1)
  assert.equal(detailWrites[0][2].data.value, 9)
  assert.equal(completed, 1)
  assert.equal(scope.pendingCount, 0)
  scope.dispose()
})

const queryComponents = [
  'src/views/chart/components/views/components/ChartComponentG2Plot.vue',
  'src/views/chart/components/views/components/ChartComponentS2.vue',
  'src/custom-component/indicator/DeIndicator.vue',
  'src/custom-component/rich-text/DeRichTextView.vue',
  'src/custom-component/picture-group/Component.vue'
]
for (const file of queryComponents) {
  test(`${path.basename(
    file
  )}: synchronous serialization failure releases only its registration and remains observable`, async () => {
    const scope = environment().load('src/config/axios/queryScope.ts').createQueryScope()
    const other = scope.createQuery('preview').start()
    const ctx = chartHarness(file, scope)
    const input = chart()
    input.circular = input
    await assert.rejects(async () => ctx.api.calcData(input, () => undefined), /circular/i)
    assert.equal(scope.pendingCount, 1, 'failed preparation must release its own registration')
    assert.equal(other.isCurrent(), true)
    assert.equal(ctx.calls.length, 0)
    assert.equal(ctx.writes.length, 0)
    const retry = ctx.api.calcData(chart(), () => undefined)
    ctx.calls[0].resolve(response('retry'))
    await flush()
    ctx.renders[0]?.resolve()
    await retry
    assert.equal(scope.pendingCount, 1)
    assert.equal(ctx.writes[0][2].data.value, 'retry')
    other.finish()
    assert.equal(scope.pendingCount, 0)
    scope.dispose()
  })
}

for (const [file, corrupt] of [
  [
    queryComponents[1],
    input => {
      delete input.customAttr
    }
  ],
  [
    queryComponents[4],
    input => {
      input.senior = 'not-json'
    }
  ]
]) {
  test(`${path.basename(
    file
  )}: invalid configuration releases registration before making any request`, async () => {
    const scope = environment().load('src/config/axios/queryScope.ts').createQueryScope()
    const ctx = chartHarness(file, scope)
    const input = chart()
    corrupt(input)
    await assert.rejects(async () => ctx.api.calcData(input, () => undefined))
    assert.equal(scope.pendingCount, 0)
    assert.equal(ctx.calls.length, 0)
    assert.equal(ctx.writes.length, 0)
    scope.dispose()
  })
}

for (const file of queryComponents) {
  test(`${path.basename(
    file
  )}: a throwing no-query completion callback also releases registration without a second invocation`, async () => {
    const scope = environment().load('src/config/axios/queryScope.ts').createQueryScope()
    const ctx = chartHarness(file, scope)
    const input = chart()
    delete input.tableId
    input.senior = '{"threshold":{"enable":false}}'
    const error = new Error('completion failure')
    let completed = 0
    await assert.rejects(
      async () =>
        ctx.api.calcData(input, () => {
          completed++
          throw error
        }),
      failure => failure === error
    )
    assert.equal(completed, 1)
    assert.equal(scope.pendingCount, 0)
    assert.equal(ctx.calls.length, 0)
    scope.dispose()
  })
}

test('G2 map without a data query releases registration when rendering rejects', async () => {
  const scope = environment().load('src/config/axios/queryScope.ts').createQueryScope()
  const ctx = chartHarness(queryComponents[0], scope)
  const input = { ...chart(), tableId: undefined, type: 'map' }
  const error = new Error('render failure')
  const pending = ctx.api.calcData(input, () => undefined)
  const rejected = assert.rejects(pending, failure => failure === error)
  ctx.renders[0].reject(error)
  await rejected
  assert.equal(scope.pendingCount, 0)
  assert.equal(ctx.calls.length, 0)
  scope.dispose()
})

test('canvas workflow forks have distinct request IDs and are all invalidated together', () => {
  const scope = environment().load('src/config/axios/queryScope.ts').createQueryScope()
  const owner = scope.createQuery('canvas-load')
  const first = owner.start()
  const links = first.fork('linkage')
  const jumps = first.fork('jump')
  first.finish()
  assert.equal(scope.pendingCount, 2)
  assert.notEqual(links.requestId, jumps.requestId)
  assert.equal(links.instanceId, first.instanceId)
  const latest = owner.start()
  assert.equal(links.isCurrent(), false)
  assert.equal(jumps.signal.aborted, true)
  links.finish()
  jumps.finish()
  assert.equal(scope.pendingCount, 1)
  const obsoleteChild = first.fork('late')
  assert.equal(obsoleteChild.isCurrent(), false)
  assert.equal(scope.pendingCount, 1)
  latest.finish()
  assert.equal(scope.pendingCount, 0)
  scope.dispose()
})

test('runtime caches and frozen detail/export snapshots isolate equal resource IDs and clean up on unmount', async () => {
  const { createRenderer, defineComponent, h, ref, nextTick } = require('vue')
  const ctx = environment()
  const { provideChartRuntimeCache, useChartRuntimeCache, collectChartRuntimeCaches } = ctx.load(
    'src/hooks/web/useChartRuntimeCache.ts'
  )
  const caches = [],
    actionBars = [],
    show = ref(true)
  const Bar = defineComponent({
    setup() {
      actionBars.push(useChartRuntimeCache())
      return () => null
    }
  })
  const Chart = defineComponent({
    props: ['position'],
    setup(props) {
      const view = { id: 'same', calParams: [{ id: 'param', value: 0 }] }
      const runtime = provideChartRuntimeCache(
        () => view,
        () => props.position,
        () => ({ id: props.position, type: 'dashboard', name: props.position })
      )
      caches.push(runtime)
      return () => h('div', [h(Bar)])
    }
  })
  const renderer = createRenderer({
    createElement: () => ({}),
    createText: () => ({}),
    createComment: () => ({}),
    insert: () => undefined,
    remove: () => undefined,
    setText: () => undefined,
    setElementText: () => undefined,
    patchProp: () => undefined,
    parentNode: () => null,
    nextSibling: () => null
  })
  const app = renderer.createApp({
    render: () =>
      h('div', [
        show.value ? h(Chart, { key: 'a', position: 'preview' }) : null,
        h(Chart, { key: 'b', position: 'canvas' })
      ])
  })
  app.mount({})
  const [a, b] = caches
  assert.equal(actionBars[0], a)
  assert.equal(actionBars[1], b)
  assert.notEqual(a.id, b.id)
  a.setDetails({ data: { data: ['A'] }, calParams: [{ id: 'param', value: 42 }] })
  a.request = { filter: ['A'] }
  b.setDetails({ data: { data: ['B'] } })
  b.request = { filter: ['B'] }
  assert.equal(a.view.calParams[0].value, 0)
  const snapshot = a.snapshot()
  a.setDetails({ data: { data: ['new-A'] } })
  a.request.filter.push('new-A')
  assert.deepEqual([...snapshot.data.data], ['A'])
  assert.deepEqual([...snapshot.request.filter], ['A'])
  assert.deepEqual([...b.data.data], ['B'])
  assert.equal(snapshot.canvasInfo.id, 'preview')
  assert.equal(b.canvasInfo.id, 'canvas')
  assert.equal(a.publishToStore, false)
  assert.equal(b.publishToStore, true)
  const dom = id => ({ dataset: { chartRuntime: id }, querySelectorAll: () => [] })
  assert.equal(collectChartRuntimeCaches(dom(a.id))[0], a)
  show.value = false
  await nextTick()
  assert.equal(collectChartRuntimeCaches(dom(a.id)).length, 0)
  assert.equal(collectChartRuntimeCaches(dom(b.id))[0], b)
  app.unmount()
  assert.equal(collectChartRuntimeCaches(dom(b.id)).length, 0)
})

function selectedMethods(file, names, context) {
  const source = parse(fs.readFileSync(path.join(root, file), 'utf8')).descriptor.scriptSetup
    .content
  const ast = ts.createSourceFile(
    'fixture.tsx',
    source,
    ts.ScriptTarget.Latest,
    true,
    ts.ScriptKind.TSX
  )
  const body = ast.statements
    .filter(
      node =>
        ts.isVariableStatement(node) &&
        node.declarationList.declarations.some(d => names.includes(d.name.getText(ast)))
    )
    .map(node => node.getText(ast))
    .join('\n')
  context.exports = {}
  vm.runInNewContext(compile(body + '\nexports.api = {' + names.join(',') + '}'), context)
  return context.exports.api
}

test('API debugging cancels only its own instance and ignores responses after close/reopen', async () => {
  const scope = environment().load('src/config/axios/queryScope.ts').createQueryScope()
  const setup = () => {
    const calls = [],
      warnings = []
    const context = {
      useLatestQuery: purpose => scope.createQuery(purpose),
      loading: { value: false },
      formLoading: { value: false },
      disabledNext: { value: false },
      apiItemBasicInfo: { value: { validate: fn => fn(true) } },
      apiItem: { name: 'synthetic' },
      paramsList: [],
      Base64: { encode: value => value },
      JSON,
      dsType: { value: 'api' },
      originFieldItem: {},
      checkApiItem: (data, query) => {
        const call = deferred()
        calls.push({ ...call, query })
        return call.promise.finally(query.finish)
      },
      console: { warn: value => warnings.push(value) },
      isPlugin: { value: false },
      edit_api_item: { value: true }
    }
    const api = selectedMethods(
      'src/views/visualized/data/datasource/form/ApiHttpRequestDraw.vue',
      ['apiQuery', 'invalidateApiQuery', 'showApiData', 'closeEditItem'],
      context
    )
    return { context, calls, api, warnings }
  }
  const a = setup(),
    b = setup()
  a.api.showApiData()
  b.api.showApiData()
  a.api.showApiData()
  assert.equal(a.calls[0].query.signal.aborted, true)
  assert.equal(b.calls[0].query.signal.aborted, false)
  a.calls[0].resolve({ data: { jsonFields: ['old'] } })
  await flush()
  assert.equal(a.context.originFieldItem.jsonFields, undefined)
  assert.equal(a.context.loading.value, true)
  a.api.closeEditItem()
  a.calls[1].reject(new Error('late canceled'))
  b.calls[0].resolve({ data: { jsonFields: ['B'] } })
  await flush()
  assert.equal(a.warnings.length, 0)
  assert.equal(a.context.loading.value, false)
  assert.deepEqual(b.context.originFieldItem.jsonFields, ['B'])
  assert.equal(scope.pendingCount, 0)
  scope.dispose()
})

test('dataset preview is invalidated by canvas edits and an old response cannot replace the new preview or loading', async () => {
  const scope = environment().load('src/config/axios/queryScope.ts').createQueryScope()
  const owner = scope.createQuery('dataset-preview'),
    calls = []
  const context = {
    ref: value => ({ value }),
    previewQuery: owner,
    datasetDrag: { value: { nodeNameList: [], getNodeList: () => [] } },
    state: {},
    changeUpdate: () => undefined,
    reGetName: () => undefined,
    columns: { value: [] },
    tableData: { value: [] },
    allfields: { value: [] },
    isCross: { value: false },
    dfsNodeList: () => undefined,
    generateColumns: value => value,
    getPreviewData: (data, query) => {
      const call = deferred()
      calls.push({ ...call, query })
      return call.promise.finally(query.finish)
    }
  }
  const api = selectedMethods(
    'src/views/pages/visualized/data/dataset/form/index.vue',
    ['datasetPreviewLoading', 'datasetPreviewRequest', 'datasetPreview', 'addComplete'],
    context
  )
  api.datasetPreview()
  api.addComplete()
  api.datasetPreview()
  calls[0].resolve({ data: { fields: ['old'], data: ['old'] } })
  await flush()
  assert.equal(calls[0].query.signal.aborted, true)
  assert.deepEqual([...context.tableData.value], [])
  assert.equal(api.datasetPreviewLoading.value, true)
  calls[1].resolve({ data: { fields: ['new'], data: ['new'] } })
  await flush()
  assert.deepEqual(context.tableData.value, ['new'])
  assert.equal(api.datasetPreviewLoading.value, false)
  assert.equal(scope.pendingCount, 0)
  scope.dispose()
})

test('resource tree instances read their own returned snapshot and reject stale refreshes', async () => {
  const scope = environment().load('src/config/axios/queryScope.ts').createQueryScope()
  const setup = () => {
    const calls = [],
      accepted = []
    const context = {
      treeQuery: scope.createQuery('tree'),
      curCanvasType: { value: 'dashboard' },
      props: { resourceTable: 'core' },
      interactiveStore: {
        setInteractive: (params, unused, isCurrent, query) => {
          const call = deferred()
          calls.push({ ...call, query })
          return call.promise.finally(query.finish)
        }
      },
      state: {},
      rootManage: { value: false },
      anyManage: { value: false },
      dvInfo: { value: null },
      showPosition: { value: 'preview' },
      wsCache: { get: key => (key === 'TreeSort-backend' ? '1' : undefined) },
      sortList: [{ value: 'default' }, { value: 'default' }],
      JSON,
      sortTypeChange: () => undefined,
      afterTreeInit: () => accepted.push(context.state.originResourceTree)
    }
    const api = selectedMethods('src/views/common/DeResourceTree.vue', ['getTree'], context)
    return { api, context, calls, accepted }
  }
  const a = setup(),
    b = setup()
  const old = a.api.getTree(),
    other = b.api.getTree(),
    latest = a.api.getTree()
  a.calls[0].resolve({ rootManage: true, anyManage: true, treeNodes: [{ id: 'old' }] })
  b.calls[0].resolve({ rootManage: false, anyManage: false, treeNodes: [{ id: 'B' }] })
  a.calls[1].resolve({ rootManage: true, anyManage: true, treeNodes: [{ id: 'A' }] })
  await Promise.all([old, other, latest])
  assert.equal(a.accepted.length, 1)
  assert.equal(a.context.state.originResourceTree[0].id, 'A')
  assert.equal(b.context.state.originResourceTree[0].id, 'B')
  assert.equal(b.calls[0].query.signal.aborted, false)
  assert.equal(scope.pendingCount, 0)
  scope.dispose()
})

test('canvas metadata and linkage/jump follow-ups cannot write after another resource becomes current', async () => {
  const scope = environment().load('src/config/axios/queryScope.ts').createQueryScope()
  const owner = scope.createQuery('canvas-load'),
    calls = [],
    links = [],
    jumps = [],
    writes = []
  const read = (id, flag, attach, query) => {
    const call = deferred()
    calls.push({ ...call, id, query })
    return call.promise
  }
  const follow = list => (id, table, query) => {
    const call = deferred()
    list.push({ ...call, id, query })
    return call.promise.finally(query.finish)
  }
  const context = {
    exports: {},
    JSON,
    findById: read,
    findCopyResource: read,
    normalizeCanvasWatermark: value => value,
    historyAdaptor: () => undefined,
    useAppearanceStoreWithOut: () => ({ setCurrentFont: () => undefined }),
    document: { documentElement: { style: { setProperty: () => undefined } } },
    getPanelAllLinkageInfo: follow(links),
    queryVisualizationJumpInfo: follow(jumps),
    dvMainStore: new Proxy(
      { canvasAttachInfo: null },
      {
        get: (target, key) =>
          key in target ? target[key] : (...args) => writes.push([key, ...args])
      }
    ),
    console: { error: () => undefined }
  }
  const source = fs.readFileSync(path.join(root, 'src/utils/canvasUtils.ts'), 'utf8')
  const ast = ts.createSourceFile('canvas.ts', source, ts.ScriptTarget.Latest, true)
  const names = ['initCanvasDataPrepare', 'initCanvasData']
  const body = ast.statements
    .filter(n => ts.isFunctionDeclaration(n) && names.includes(n.name.text))
    .map(n => n.getText(ast))
    .join('\n')
  vm.runInNewContext(compile(body + '\nexports.api={initCanvasData}'), context)
  const api = context.exports.api
  const qA = owner.start(),
    a = api.initCanvasData('A', { busiFlag: 'dashboard', awaitLoad: true, queryRequest: qA }, () =>
      writes.push(['callback', 'A'])
    )
  const qB = owner.start(),
    b = api.initCanvasData('B', { busiFlag: 'dashboard', awaitLoad: true, queryRequest: qB }, () =>
      writes.push(['callback', 'B'])
    )
  calls[0].resolve({ data: { componentData: 'not-json' } })
  await a
  assert.equal(writes.length, 0)
  calls[1].resolve({
    data: {
      id: 'B',
      type: 'dashboard',
      componentData: '[]',
      canvasStyleData: '{"dashboard":{"gap":"no"},"fontFamily":"synthetic"}',
      canvasViewInfo: {}
    }
  })
  await b
  assert.equal(writes.find(([name]) => name === 'updateCurDvInfo')[1].id, 'B')
  assert.equal(links.length, 1)
  assert.notEqual(links[0].query.requestId, jumps[0].query.requestId)
  const qC = owner.start()
  const c = api.initCanvasData(
    'C',
    { busiFlag: 'dashboard', awaitLoad: true, queryRequest: qC },
    () => undefined
  )
  const failed = assert.rejects(c, /load failed/)
  links[0].resolve({ data: { old: true } })
  jumps[0].resolve({ data: { old: true } })
  await flush()
  assert.equal(
    writes.some(([name]) => name === 'setNowPanelTrackInfo' || name === 'setNowPanelJumpInfo'),
    false
  )
  calls[2].reject(new Error('load failed'))
  await failed
  assert.equal(scope.pendingCount, 0)
  scope.dispose()
})

test('chart query listener ignores another canvas scope and retains legacy broadcast compatibility', () => {
  const source = parse(
    fs.readFileSync(path.join(root, 'src/views/chart/components/views/index.vue'), 'utf8')
  ).descriptor.scriptSetup.content
  const ast = ts.createSourceFile('view.ts', source, ts.ScriptTarget.Latest, true)
  let callback
  const visit = node => {
    if (
      ts.isCallExpression(node) &&
      node.expression.getText(ast) === 'useEmitt' &&
      node.arguments[0] &&
      ts.isObjectLiteralExpression(node.arguments[0])
    ) {
      const props = node.arguments[0].properties
      if (
        props.some(p => p.name?.getText(ast) === 'name' && p.getText(ast).includes('query-data-'))
      ) {
        callback = props.find(p => p.name?.getText(ast) === 'callback').initializer
      }
    }
    ts.forEachChild(node, visit)
  }
  visit(ast)
  assert.ok(callback)
  let invoked = 0
  const context = { exports: {}, queryScope: { scopeId: 7 }, queryDataFromSelect: () => invoked++ }
  vm.runInNewContext(compile('exports.accept=' + callback.getText(ast)), context)
  context.exports.accept({ scopeId: 8 })
  assert.equal(invoked, 0)
  context.exports.accept({ scopeId: 7 })
  assert.equal(invoked, 1)
  context.exports.accept()
  assert.equal(invoked, 2)
})

test('tree options invalidate immediately during debounce; stale responses cannot reset loading or options', async () => {
  const scope = environment().load('src/config/axios/queryScope.ts').createQueryScope()
  const owner = scope.createQuery('tree-options'),
    calls = []
  let queued
  const config = { treeFieldList: [{ id: 'field' }], resultMode: 0, required: false }
  const context = {
    useLatestQuery: () => owner,
    debounce: fn => {
      const schedule = (...args) => {
        queued = () => fn(...args)
      }
      schedule.cancel = () => {
        queued = undefined
      }
      return schedule
    },
    props: { config },
    config: { value: config },
    loading: { value: false },
    showWholePath: { value: false },
    treeOptionList: { value: [] },
    getCascadeFieldId: () => [],
    dfs: value => value,
    filterTree: value => value,
    fromSelect: false,
    console: { error: () => undefined },
    getFieldTree: (data, query) => {
      const call = deferred()
      calls.push({ ...call, query })
      return call.promise.finally(query.finish)
    }
  }
  const api = selectedMethods(
    'src/custom-component/v-query/Tree.vue',
    ['treeQuery', 'loadTreeOptions', 'getTreeOption'],
    context
  )
  api.getTreeOption()
  const old = queued()
  api.getTreeOption()
  assert.equal(calls[0].query.signal.aborted, true)
  calls[0].resolve(['old'])
  await old
  assert.equal(context.treeOptionList.value.length, 0)
  assert.equal(context.loading.value, true)
  const latest = queued()
  calls[1].resolve(['new'])
  await latest
  assert.deepEqual(context.treeOptionList.value, ['new'])
  assert.equal(context.loading.value, false)
  assert.equal(scope.pendingCount, 0)
  api.getTreeOption()
  owner.dispose()
  await queued()
  assert.equal(calls.length, 2)
  assert.equal(scope.pendingCount, 0)
  scope.dispose()
})

test('nested canvas keeps root query context while DOM exports retain nested canvas data', () => {
  const { createRenderer, defineComponent, h } = require('vue')
  const { registerCanvasRuntime, useCanvasRuntime, getCanvasRuntime } = environment().load(
    'src/hooks/web/useChartRuntimeCache.ts'
  )
  const rootCanvas = { componentData: ['outside-query'], dvInfo: { id: 'root' } }
  const nestedCanvas = { componentData: ['tab-chart'], dvInfo: { id: 'tab' } }
  let inherited, nestedId
  const Chart = defineComponent({
    setup() {
      inherited = useCanvasRuntime()()
      return () => null
    }
  })
  const Tab = defineComponent({
    setup() {
      nestedId = registerCanvasRuntime(() => nestedCanvas, true)
      return () => h(Chart)
    }
  })
  const renderer = createRenderer({
    createElement: () => ({}),
    createText: () => ({}),
    createComment: () => ({}),
    insert: () => undefined,
    remove: () => undefined,
    setText: () => undefined,
    setElementText: () => undefined,
    patchProp: () => undefined,
    parentNode: () => null,
    nextSibling: () => null
  })
  const app = renderer.createApp({
    setup() {
      registerCanvasRuntime(() => rootCanvas)
      return () => h(Tab)
    }
  })
  app.mount({})
  assert.equal(inherited, rootCanvas)
  assert.equal(getCanvasRuntime({ dataset: { canvasRuntime: nestedId } }), nestedCanvas)
  app.unmount()
  assert.equal(getCanvasRuntime({ dataset: { canvasRuntime: nestedId } }), undefined)
})
