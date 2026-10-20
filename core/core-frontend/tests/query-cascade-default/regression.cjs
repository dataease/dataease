// Run: node --test tests/query-cascade-default/regression.cjs
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const vm = require('node:vm')
const { test } = require('node:test')
const ts = require('typescript')
const { parse } = require('@vue/compiler-sfc')
const { cloneDeep } = require('lodash')
const { nextTick, unref } = require('vue')

const filename = path.resolve(
  __dirname,
  '../../src/custom-component/v-query/QueryConditionConfiguration.vue'
)
const source = parse(
  fs.readFileSync(process.env.QUERY_CONFIGURATION_SOURCE || filename, 'utf8')
).descriptor.scriptSetup.content
const ast = ts.createSourceFile(filename + '.ts', source, ts.ScriptTarget.Latest, true)
const functions = ['validate', 'confirmClick', 'multipleChange'].map(name => {
  const statement = ast.statements.find(
    node =>
      ts.isVariableStatement(node) &&
      node.declarationList.declarations.some(declaration => declaration.name.getText(ast) === name)
  )
  assert.ok(statement, `Production function ${name} exists`)
  return statement.getText(ast)
})
const code = ts.transpileModule(functions.join('\n'), {
  compilerOptions: { target: ts.ScriptTarget.ES2020, module: ts.ModuleKind.CommonJS }
}).outputText
const condition = (id, extra = {}) => ({
  id,
  displayType: '0',
  multiple: false,
  defaultValueCheck: false,
  optionValueSource: 1,
  checkedFields: ['chart'],
  checkedFieldsMap: { chart: 'field' },
  field: { id: 'field' },
  dataset: { id: 'dataset' },
  defaultValue: 'old-label',
  selectValue: 'old-label',
  mapValue: ['old-id'],
  defaultMapValue: ['old-id'],
  ...extra
})
const item = id => ({
  datasetId: `dataset--${id}--field`,
  selectValue: ['old-id'],
  currentSelectValue: ['old-id']
})
const save = async (conditions, cascade, invalid = false) => {
  const element = { propValue: cloneDeep(conditions), cascade: cloneDeep(cascade) }
  const events = []
  const snapshots = []
  const context = {
    conditions: { value: cloneDeep(conditions) },
    cascadeArr: cloneDeep(cascade),
    queryElement: { value: element },
    curComponent: { value: {} },
    defaultConfigurationRef: { value: undefined },
    relationshipChartIndex: { value: 0 },
    dialogVisible: { value: true },
    fields: { value: [] },
    duplicateRemoval: values => [...new Set(values)],
    t: key => key,
    ElMessage: { error() {} },
    handleDialogClick() {},
    cloneDeep,
    unref,
    nextTick,
    snapshotStore: { recordSnapshotCache: name => snapshots.push(name) },
    emits: (...args) => events.push(args)
  }
  if (invalid) context.conditions.value[0].checkedFields = []
  vm.runInNewContext(code + '\nconfirmClick()', context)
  await nextTick()
  await nextTick()
  // Normalize VM arrays for strict assertions; retain undefined values.
  return { element: cloneDeep(element), events, snapshots, context }
}

test('saving disabled defaults clears mapped filters in every cascade occurrence', async () => {
  const configs = [condition('parent'), condition('child')]
  const { element, events, snapshots } = await save(configs, [
    [item('parent'), item('child')],
    [item('parent')]
  ])
  element.propValue.forEach(config => {
    assert.equal(config.defaultValue, undefined)
    assert.equal(config.selectValue, undefined)
    assert.equal(config.mapValue.length, 0)
    assert.equal(config.defaultMapValue.length, 0)
  })
  element.cascade.flat().forEach(node => {
    assert.equal(node.selectValue.length, 0)
    assert.equal(node.currentSelectValue.length, 0)
  })
  assert.deepEqual(events.map(event => event[0]), ['reRenderAll', 'queryData'])
  assert.deepEqual(snapshots, ['confirmClick'])
  assert.equal(configs[0].defaultValue, 'old-label')
})

test('multiple and automatic conditions also clear their disabled defaults', async () => {
  for (const extra of [{ multiple: true }, { auto: true }, { displayType: '7' }]) {
    const { element } = await save([condition('query', extra)], [[item('query')]])
    const config = element.propValue[0]
    assert.ok(config.selectValue === undefined || config.selectValue.length === 0)
    assert.ok(config.defaultValue === undefined || config.defaultValue.length === 0)
    assert.equal(config.mapValue.length, 0)
    assert.equal(element.cascade[0][0].currentSelectValue.length, 0)
  }
})

test('enabled defaults retain their labels and mapped IDs including zero', async () => {
  const config = condition('enabled', {
    displayType: '2',
    defaultValueCheck: true,
    defaultValue: 0,
    mapValue: [0],
    defaultMapValue: [0]
  })
  const { element } = await save([config], [[item('enabled')]])
  assert.equal(element.propValue[0].selectValue, 0)
  assert.equal(element.propValue[0].defaultMapValue[0], 0)
  assert.equal(element.cascade[0][0].selectValue[0], 0)
  assert.equal(element.cascade[0][0].currentSelectValue[0], 0)
  assert.notEqual(element.cascade[0][0].selectValue, element.cascade[0][0].currentSelectValue)
})

test('unrelated enabled defaults and cascade structure are preserved', async () => {
  const enabled = condition('other', { defaultValueCheck: true })
  const { element } = await save(
    [condition('disabled'), enabled],
    [[item('disabled')], [item('other')]]
  )
  assert.equal(element.cascade.length, 2)
  assert.equal(element.cascade[1][0].datasetId, 'dataset--other--field')
  assert.equal(element.cascade[1][0].selectValue[0], 'old-id')
  assert.equal(element.propValue[1].defaultValue, 'old-label')
})

test('validation failure leaves persisted conditions and cascade values unchanged', async () => {
  const configs = [condition('parent')]
  const cascade = [[item('parent')]]
  const { element, events, snapshots, context } = await save(configs, cascade, true)
  assert.deepEqual(element.propValue, configs)
  assert.deepEqual(element.cascade, cascade)
  assert.equal(context.dialogVisible.value, true)
  assert.equal(events.length, 0)
  assert.equal(snapshots.length, 0)
})

test('saved empty cascade values remain empty after serialization and reopening', async () => {
  const { element } = await save([condition('parent')], [[item('parent')]])
  const reopened = JSON.parse(JSON.stringify(element))
  assert.equal(reopened.propValue[0].mapValue.length, 0)
  assert.equal(reopened.propValue[0].defaultMapValue.length, 0)
  const second = await save(reopened.propValue, reopened.cascade)
  assert.equal(second.element.cascade[0][0].selectValue.length, 0)
  assert.equal(second.element.cascade[0][0].currentSelectValue.length, 0)
})
