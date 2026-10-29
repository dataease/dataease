// Run: node --test tests/query-cascade-runtime/regression.cjs
const assert = require('node:assert/strict')
const fs = require('node:fs')
const vm = require('node:vm')
const { test } = require('node:test')
const ts = require('typescript')
const { parse } = require('@vue/compiler-sfc')
const { cloneDeep, isEqual } = require('lodash')
const { nextTick } = require('vue')
const filename = 'src/custom-component/v-query/Select.vue'
const descriptor = parse(fs.readFileSync(process.env.SELECT_SOURCE || filename, 'utf8')).descriptor
const ast = ts.createSourceFile(
  'Select.ts',
  descriptor.scriptSetup.content,
  ts.ScriptTarget.Latest,
  true
)
const names = [
  'setDefaultMapValue',
  'setCascadeValueBack',
  'emitCascade',
  'handleValueChange',
  'handleFieldIdChange'
]
const source = names
  .map(name => {
    const node = ast.statements.find(
      node =>
        ts.isVariableStatement(node) &&
        node.declarationList.declarations.some(d => d.name.getText(ast) === name)
    )
    assert.ok(node, `Production function ${name} exists`)
    return node.getText(ast)
  })
  .join('\n')
const code = ts.transpileModule(source + '\nglobalThis.reload = handleFieldIdChange', {
  compilerOptions: { target: ts.ScriptTarget.ES2020 }
}).outputText
const setup = (extra = {}, { button = false, isConfig = false } = {}) => {
  const queries = [],
    events = [],
    errors = [],
    configEvents = []
  let records = []
  const child = {
    datasetId: 'ds--child--id',
    currentSelectValue: ['old-id'],
    selectValue: ['old-id']
  }
  const context = {
    config: {
      value: {
        id: 'child',
        field: { id: 'id' },
        displayId: 'label',
        multiple: false,
        selectValue: '旧人员',
        defaultValue: '旧人员',
        mapValue: ['old-id'],
        defaultMapValue: ['old-id'],
        optionValueSource: 1,
        defaultValueCheck: false,
        ...extra
      }
    },
    props: { isConfig },
    selectValue: { value: cloneDeep(extra.selectValue ?? '旧人员') },
    options: { value: [] },
    cascade: { value: [[child, { datasetId: 'ds--grandchild--id' }]] },
    enumValueArr: [],
    loading: { value: true },
    disabledFirstItem: { value: false },
    isFromRemote: { value: false },
    unMountSelect: { value: [] },
    cloneDeep,
    isEqual,
    nextTick,
    filterValueEquals: (a, b) => a === b,
    isEmptyCascadeValue: value =>
      Array.isArray(value) ? value.length === 0 : value == null || value === '',
    startOptionsRequest: () => ({ signal: new AbortController().signal, isCurrent: () => true }),
    enumValueObj: async () => records,
    customSort() {},
    releaseSelect() {},
    setDefaultValueFirstItem() {},
    setCascadeDefault: value => configEvents.push(value),
    emitCascadeConfig: () => [],
    useEmitt: () => ({ emitter: { emit: name => events.push(name) } }),
    isConfirmSearch: () => {
      if (!button) queries.push(cloneDeep(context.config.value))
    },
    queryDataForId: () => queries.push(cloneDeep(context.config.value)),
    console: { error: error => errors.push(error) },
    ElMessage() {},
    t: key => key
  }
  vm.runInNewContext(code, context)
  return {
    context,
    child,
    queries,
    events,
    configEvents,
    async reload(next) {
      records = next
      await context.reload({ queryId: 'id', displayId: 'label' })
      await nextTick()
      assert.deepEqual(errors, [])
    }
  }
}
for (const multiple of [false, true]) {
  test(`${multiple ? '多选' : '单选'}失效值清空后不残留查询条件，继续通知下一级`, async () => {
    const ctx = setup({ multiple, selectValue: multiple ? ['旧人员'] : '旧人员' })
    await ctx.reload([{ id: 'new-id', label: '新人员' }])
    assert.equal(ctx.context.config.value.selectValue?.length || 0, 0)
    assert.equal(ctx.context.config.value.mapValue.length, 0)
    assert.equal(ctx.child.currentSelectValue.length, 0)
    assert.deepEqual(ctx.events, ['grandchild-select'])
    assert.equal(ctx.queries.length, 1)
    assert.equal(ctx.queries[0].selectValue?.length || 0, 0)
  })
  test(`${multiple ? '多选' : '单选'}枚举为空也会同步实际查询值`, async () => {
    const ctx = setup({ multiple, selectValue: multiple ? ['旧人员'] : '旧人员' })
    await ctx.reload([])
    assert.equal(ctx.context.config.value.selectValue?.length || 0, 0)
    assert.equal(ctx.context.config.value.mapValue.length, 0)
    assert.equal(ctx.queries.length, 1)
  })
}
test('多选只移除失效项，保留有效筛选', async () => {
  const ctx = setup({
    multiple: true,
    selectValue: ['旧人员', '保留人员'],
    mapValue: ['old-id', 'kept-id']
  })
  await ctx.reload([{ id: 'kept-id', label: '保留人员' }])
  assert.deepEqual(Array.from(ctx.context.config.value.selectValue), ['保留人员'])
  assert.deepEqual(Array.from(ctx.context.config.value.mapValue), ['kept-id'])
  assert.equal(ctx.queries.length, 1)
})
test('显示名称没变但实际 ID 改变时更新查询映射', async () => {
  const ctx = setup()
  await ctx.reload([{ id: 'new-id', label: '旧人员' }])
  assert.deepEqual(Array.from(ctx.context.config.value.mapValue), ['new-id'])
  assert.deepEqual(Array.from(ctx.child.currentSelectValue), ['new-id'])
  assert.equal(ctx.queries.length, 1)
})
for (const defaultValueCheck of [false, true]) {
  test(`带查询按钮时仅同步条件，不自动查询（默认值${defaultValueCheck}）`, async () => {
    const ctx = setup({ defaultValueCheck }, { button: true })
    await ctx.reload([{ id: 'new-id', label: '新人员' }])
    assert.equal(ctx.context.config.value.selectValue, undefined)
    assert.equal(ctx.queries.length, 0)
  })
}
test('启用默认值时清空只查询一次', async () => {
  const ctx = setup({ defaultValueCheck: true })
  await ctx.reload([])
  assert.equal(ctx.queries.length, 1)
})
test('选项与 ID 均未变化时，不触发查询或下级刷新', async () => {
  const ctx = setup()
  await ctx.reload([{ id: 'old-id', label: '旧人员' }])
  assert.equal(ctx.queries.length, 0)
  assert.equal(ctx.events.length, 0)
})
test('配置态更新默认值，不发送运行态查询', async () => {
  const ctx = setup({}, { isConfig: true })
  await ctx.reload([{ id: 'new-id', label: '新人员' }])
  assert.equal(ctx.context.config.value.defaultValue, undefined)
  assert.equal(ctx.context.config.value.defaultMapValue.length, 0)
  assert.equal(ctx.configEvents.length, 1)
  assert.equal(ctx.queries.length, 0)
})
