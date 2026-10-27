// Run: node --test tests/route-config-parallel/regression.cjs
const assert = require('node:assert/strict')
const fs = require('node:fs')
const vm = require('node:vm')
const ts = require('typescript')
const { test } = require('node:test')
const source = fs.readFileSync(process.env.PERMISSION_SOURCE || 'src/permission.ts', 'utf8')
const ast = ts.createSourceFile('permission.ts', source, ts.ScriptTarget.Latest, true)
const guard = ast.statements.find(node => ts.isExpressionStatement(node) &&
  ts.isCallExpression(node.expression) && node.expression.expression.getText(ast) === 'router.beforeEach')
const statements = guard.expression.arguments[0].body.statements
const start = statements.findIndex(node => node.getText(ast).includes('appearanceStore.setAppearance()'))
const end = statements.findIndex((node, i) => i > start && ts.isIfStatement(node))
assert.ok(start >= 0 && end > start)
// Execute the actual initialization block, including its cache writes.
const code = ts.transpileModule(`(async () => { ${statements.slice(start, end).map(node => node.getText(ast)).join('\n')} })()`, {
  compilerOptions: { target: ts.ScriptTarget.ES2020 }
}).outputText
const setup = () => {
  const calls = []
  const pending = []
  const writes = []
  const request = name => () => {
    calls.push(name)
    return new Promise((resolve, reject) => pending.push({ name, resolve, reject }))
  }
  const result = vm.runInNewContext(code, {
    appearanceStore: { setAppearance: request('appearance'), setFontList: request('fonts') },
    appStore: { setXpackValid: request('xpack') },
    getDefaultSettings: request('defaults'),
    getCachedDefaultSettings: request('defaults'),
    wsCache: { set: (...args) => writes.push(args) }
  })
  return { result, calls, pending, writes }
}
test('all configuration requests start before any response and navigation waits for all', async () => {
  const ctx = setup()
  assert.deepEqual(ctx.calls, ['appearance', 'fonts', 'xpack', 'defaults'])
  const settings = { 'basic.defaultSort': 'custom-sort', 'basic.defaultOpen': 'custom-open', 'basic.embeddedExportMode': 'async' }
  let complete = false
  ctx.result.then(() => { complete = true })
  ctx.pending[3].resolve(settings)
  ctx.pending[1].resolve()
  ctx.pending[2].resolve()
  await Promise.resolve()
  assert.equal(complete, false)
  assert.deepEqual(ctx.writes, [])
  ctx.pending[0].resolve()
  await ctx.result
  assert.deepEqual(ctx.writes, [
    ['TreeSort-backend', 'custom-sort'], ['open-backend', 'custom-open'], ['embeddedExportMode-backend', 'async']
  ])
})
test('missing settings retain existing fallback values', async () => {
  const ctx = setup()
  ctx.pending.forEach(item => item.resolve(item.name === 'defaults' ? {} : undefined))
  await ctx.result
  assert.deepEqual(ctx.writes, [
    ['TreeSort-backend', '1'], ['open-backend', '0'], ['embeddedExportMode-backend', 'sync']
  ])
})
for (const failing of ['appearance', 'fonts', 'xpack', 'defaults']) {
  test(`${failing} failure rejects initialization without applying partial defaults`, async () => {
    const ctx = setup()
    const error = new Error(`${failing} failed`)
    const rejected = assert.rejects(ctx.result, error)
    ctx.pending.forEach(item => item.name === failing ? item.reject(error) : item.resolve({}))
    await rejected
    assert.deepEqual(ctx.writes, [])
  })
}
