// Run: node --test tests/query-options-race/regression.cjs
const assert = require('node:assert/strict')
const fs = require('node:fs')
const vm = require('node:vm')
const { test } = require('node:test')
const ts = require('typescript')
const { parse, compileScript } = require('@vue/compiler-sfc')
const filename = 'src/custom-component/v-query/Select.vue'
const { descriptor } = parse(fs.readFileSync(filename, 'utf8'))
const ast = ts.createSourceFile('Select.ts', descriptor.scriptSetup.content, ts.ScriptTarget.Latest, true)
const names = new Set([
  'time', 'optionRequestId', 'optionRequestController', 'disposed',
  'invalidateOptionsRequest', 'startOptionsRequest', 'handleFieldIdDefaultChange',
  'handleFieldIdChange', 'pendingOptions', 'debounceOptions'
])
const statements = ast.statements.filter(node =>
  ts.isVariableStatement(node) && node.declarationList.declarations.some(d => names.has(d.name.getText(ast)))
)
const cleanup = ast.statements.find(node => ts.isExpressionStatement(node) &&
  node.expression.expression?.getText(ast) === 'onBeforeUnmount')
const code = ts.transpileModule([
  ...statements.map(node => node.getText(ast)),
  ast.statements.find(node => ts.isVariableStatement(node) && node.declarationList.declarations.some(d => d.name.getText(ast) === 'setOptions')).getText(ast).replace('const setOptions', 'const applyOptions'),
  cleanup.getText(ast),
  'globalThis.api = { handleFieldIdDefaultChange, handleFieldIdChange, debounceOptions, applyOptions }'
].join('\n'), { compilerOptions: { target: ts.ScriptTarget.ES2020 } }).outputText
const setup = () => {
  const calls = []
  const timers = new Map()
  const effects = []
  let timerId = 0
  let cleanupFn
  const fetch = (data, signal) => new Promise((resolve, reject) => calls.push({ data, signal, resolve, reject }))
  const context = {
    AbortController, cloneDeep: value => structuredClone(value),
    config: { value: { optionValueSource: 1, field: { id: 'id' }, defaultValueCheck: true,
      defaultValue: 'kept', optionFilter: [], multiple: false, mapValue: ['kept-id'] } },
    options: { value: [] }, selectValue: { value: 'kept' }, loading: { value: false },
    enumValueArr: [], disabledFirstItem: { value: true }, isFromRemote: { value: false },
    getEnumValue: fetch, enumValueObj: fetch,
    requiredComp: () => effects.push('required'), setEmptyData: () => effects.push('empty'),
    customSort() {}, setDefaultMapValue: () => [], setCascadeValueBack: () => effects.push('cascade'),
    setDefaultValueFirstItem: () => effects.push('first'),
    filterValueEquals: (a, b) => a === b,
    setOptions: value => effects.push(['load', value]),
    setTimeout: fn => { timers.set(++timerId, fn); return timerId },
    clearTimeout: id => timers.delete(id),
    debounce: fn => {
      let timer
      const debounced = (...args) => { context.clearTimeout(timer); timer = context.setTimeout(() => fn(...args)) }
      debounced.cancel = () => context.clearTimeout(timer)
      return debounced
    },
    onBeforeUnmount: fn => { cleanupFn = fn },
    eventBus: { off() {} }, componentClick() {}, console: { error: error => effects.push(error) }
  }
  vm.runInNewContext(code, context)
  return { ...context, calls, timers, effects, unmount: () => cleanupFn(),
    flush: () => { const pending = [...timers.values()]; timers.clear(); pending.forEach(fn => fn()) } }
}
const field = { queryId: 'id' }
for (const kind of ['handleFieldIdDefaultChange', 'handleFieldIdChange']) {
  test(`${kind}: old responses cannot replace latest options or clear its loading`, async () => {
    const ctx = setup()
    const old = ctx.api[kind](kind === 'handleFieldIdChange' ? field : ['old'])
    const latest = ctx.api[kind](kind === 'handleFieldIdChange' ? field : ['new'])
    assert.equal(ctx.calls[0].signal.aborted, true)
    ctx.calls[0].resolve(kind === 'handleFieldIdChange' ? [] : ['old'])
    await old
    assert.equal(ctx.loading.value, true)
    assert.equal(ctx.selectValue.value, 'kept')
    assert.equal(ctx.config.value.defaultValue, 'kept')
    assert.equal(ctx.effects.length, 0)
    ctx.calls[1].resolve(kind === 'handleFieldIdChange' ? [{ id: 'kept' }] : ['kept'])
    await latest
    assert.equal(ctx.options.value[0].value, 'kept')
    assert.equal(ctx.loading.value, false)
    // Even a late response after completion must remain harmless.
    ctx.flush()
    assert.ok(ctx.effects.length > 0)
  })
}
test('queueing during debounce invalidates in-flight work immediately', async () => {
  const ctx = setup()
  const old = ctx.api.handleFieldIdChange(field)
  ctx.api.debounceOptions(2)
  assert.equal(ctx.calls[0].signal.aborted, true)
  ctx.calls[0].resolve([])
  await old
  assert.equal(ctx.selectValue.value, 'kept')
  assert.equal(ctx.effects.length, 0)
  ctx.flush()
  assert.deepEqual(ctx.effects, [['load', 2]])
})
test('both enumeration sources share the same request generation', async () => {
  const ctx = setup()
  const old = ctx.api.handleFieldIdDefaultChange(['old'])
  const latest = ctx.api.handleFieldIdChange(field)
  ctx.calls[1].resolve([{ id: 'kept' }])
  await latest
  ctx.calls[0].resolve(['old'])
  await old
  assert.equal(ctx.options.value[0].value, 'kept')
})
test('unmount aborts requests, cancels queued loads and ignores late results', async () => {
  const ctx = setup()
  const pending = ctx.api.handleFieldIdChange(field)
  ctx.api.debounceOptions(1)
  ctx.unmount()
  ctx.calls[0].resolve([])
  await pending
  ctx.api.debounceOptions(1)
  ctx.flush()
  assert.equal(ctx.calls[0].signal.aborted, true)
  assert.equal(ctx.effects.length, 0)
  assert.equal(ctx.timers.size, 0)
})
test('superseding a completed request cancels its delayed default selection', async () => {
  const ctx = setup()
  const pending = ctx.api.handleFieldIdChange(field)
  ctx.calls[0].resolve([{ id: 'kept' }])
  await pending
  assert.equal(ctx.timers.size, 1)
  ctx.api.debounceOptions(2)
  ctx.flush()
  assert.deepEqual(ctx.effects, [['load', 2]])
})
for (const kind of ['handleFieldIdDefaultChange', 'handleFieldIdChange']) {
  test(`${kind}: failure releases loading without applying defaults or cascading`, async () => {
    const ctx = setup()
    const pending = ctx.api[kind](kind === 'handleFieldIdChange' ? field : ['old'])
    const error = new Error('network failure')
    ctx.calls[0].reject(error)
    await pending
    assert.equal(ctx.loading.value, false)
    assert.equal(ctx.selectValue.value, 'kept')
    assert.deepEqual(ctx.effects, [error])
    assert.equal(ctx.timers.size, 0)
  })
}
test('Select script compiles', () => {
  assert.ok(compileScript(descriptor, { id: 'query-options-race' }).content)
})

test('switching to manual options prevents late network results from replacing them', async () => {
  const ctx = setup()
  const old = ctx.api.handleFieldIdChange(field)
  ctx.config.value.optionValueSource = 2
  ctx.config.value.valueSource = ['manual']
  ctx.api.applyOptions(2)
  ctx.calls[0].resolve([])
  await old
  assert.equal(ctx.calls[0].signal.aborted, true)
  assert.equal(ctx.options.value[0].value, 'manual')
  assert.equal(ctx.selectValue.value, 'kept')
})
test('both API wrappers forward optional cancellation without changing existing calls', async () => {
  const source = fs.readFileSync('src/api/dataset.ts', 'utf8')
  const apiAst = ts.createSourceFile('dataset.ts', source, ts.ScriptTarget.Latest, true)
  const declarations = apiAst.statements.filter(node => ts.isVariableStatement(node) &&
    node.declarationList.declarations.some(d => ['enumValueObj', 'getEnumValue'].includes(d.name.getText(apiAst))))
  const calls = []
  const context = { exports: {}, request: { post: async options => { calls.push(options); return { data: ['result'] } } } }
  vm.runInNewContext(ts.transpileModule(declarations.map(node => node.getText(apiAst)).join('\n'), {
    compilerOptions: { target: ts.ScriptTarget.ES2020, module: ts.ModuleKind.CommonJS }
  }).outputText, context)
  for (const fn of Object.values(context.exports)) {
    const data = { queryId: 'field' }
    const { signal } = new AbortController()
    assert.deepEqual(await fn(data, signal), ['result'])
    assert.equal(calls.at(-1).signal, signal)
    assert.equal(calls.at(-1).data, data)
    await fn(data)
    assert.equal(calls.at(-1).signal, undefined)
  }
  assert.equal(calls.length, 4)
})
