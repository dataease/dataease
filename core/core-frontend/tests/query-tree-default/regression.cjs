// Run: node --test tests/query-tree-default/regression.cjs
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
const functions = ['setTreeDefaultBatch', 'validate', 'confirmClick', 'multipleChange'].map(name => {
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
const save = async (conditions, cascade, extra = {}) => {
  const element = { propValue: cloneDeep(conditions), cascade: cloneDeep(cascade) }
  const errors = []
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
    fields: { value: [
      { componentId: 'unrelated', fields: {} },
      { componentId: 'chart-a', fields: {} },
      { componentId: 'chart-b', fields: {} }
    ] },
    datasetFieldList: { value: [
      { id: 'unrelated', tableId: 'dataset-other' },
      { id: 'chart-a', tableId: 'dataset-a' },
      { id: 'chart-b', tableId: 'dataset-b' }
    ] },
    duplicateRemoval: values => [...new Set(values)],
    t: key => key,
    ElMessage: { error: message => errors.push(message) },
    handleDialogClick() {},
    cloneDeep,
    unref,
    nextTick,
    snapshotStore: { recordSnapshotCache: name => snapshots.push(name) },
    emits: (...args) => events.push(args)
  }
  Object.assign(context, extra)
  vm.runInNewContext(code + '\nconfirmClick()', context)
  await nextTick()
  await nextTick()
  // Normalize VM arrays for strict assertions; retain undefined values.
  return { element: cloneDeep(element), events, snapshots, context, errors }
}

const tree = (id, chart, extra = {}) => condition(id, {
  displayType: '9',
  checkedFields: [chart],
  checkedFieldsMap: { [chart]: 'field' },
  treeFieldList: ['level'],
  treeCheckedList: [{ checkedFields: [chart], checkedFieldsMap: { [chart]: 'field' } }],
  ...extra
})
test('multiple tree conditions infer their own dataset and save on the first attempt', async () => {
  const input = [tree('first', 'chart-a'), tree('second', 'chart-b')]
  const { element, errors, events } = await save(input, [[item('first'), item('second')]])
  assert.deepEqual(errors, [])
  assert.deepEqual(element.propValue.map(value => value.treeDatasetId), ['dataset-a', 'dataset-b'])
  assert.equal(element.propValue.length, 2)
  assert.deepEqual(events.map(event => event[0]), ['reRenderAll', 'queryData'])
  assert.equal(input[0].treeDatasetId, undefined)
})
test('existing explicit tree dataset is preserved', async () => {
  const { element, errors } = await save([tree('first', 'chart-a', { treeDatasetId: 'explicit' })], [])
  assert.deepEqual(errors, [])
  assert.equal(element.propValue[0].treeDatasetId, 'explicit')
})
const rejected = async (input, error, extra) => {
  const cascade = [[item('first')]]
  const { element, errors, events, snapshots, context } = await save(input, cascade, extra)
  assert.deepEqual(element.propValue, input)
  assert.deepEqual(element.cascade, cascade)
  assert.deepEqual(errors, [error])
  assert.equal(events.length, 0)
  assert.equal(snapshots.length, 0)
  assert.equal(context.dialogVisible.value, true)
}
test('unresolvable dataset rejects save without touching persisted filters', async () => {
  await rejected([tree('first', 'missing')], 'data_set.dataset_cannot_be')
})
test('missing tree structure is checked after successfully inferring dataset', async () => {
  await rejected([tree('first', 'chart-a', { treeFieldList: [] })], 'common.tree_structure')
})
test('invalid inner tree binding is not hidden by a valid final binding', async () => {
  await rejected([tree('first', 'chart-a', {
    treeFieldList: ['level-1', 'level-2'],
    treeCheckedList: [
      { checkedFields: ['chart-a'], checkedFieldsMap: {} },
      { checkedFields: ['chart-a'], checkedFieldsMap: { 'chart-a': 'field' } }
    ]
  })], 'v_query.be_linked_first')
})
test('a valid later condition cannot allow an invalid earlier condition to be persisted', async () => {
  await rejected([
    tree('first', 'chart-a', { treeFieldList: [] }),
    tree('second', 'chart-b')
  ], 'common.tree_structure')
})
test('a valid earlier condition cannot allow an invalid later condition to be persisted', async () => {
  await rejected([
    tree('first', 'chart-a'),
    tree('second', 'chart-b', { treeCheckedList: [{ checkedFields: [], checkedFieldsMap: {} }] })
  ], 'v_query.be_linked_first')
})
test('an unavailable field list rejects save without a null access', async () => {
  await rejected([tree('first', 'chart-a')], 'data_set.dataset_cannot_be', { fields: { value: [] } })
})
