const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const vm = require('node:vm')
const { test } = require('node:test')
const ts = require('typescript')
const { parse, compileScript, compileTemplate } = require('@vue/compiler-sfc')
const { cloneDeep } = require('lodash')
const root = path.resolve(__dirname, '../..')
const configFile = 'src/custom-component/v-query/QueryConditionConfiguration.vue'
const componentFile = 'src/custom-component/v-query/Component.vue'
const read = file => fs.readFileSync(path.join(process.env.QUERY_SOURCE_ROOT || root, file), 'utf8')
const extract = (file, names) => {
  const source = file.endsWith('.vue')
    ? parse(read(file)).descriptor.scriptSetup.content
    : read(file)
  const ast = ts.createSourceFile(file + '.ts', source, ts.ScriptTarget.Latest, true)
  return names
    .map(name => {
      const statement = ast.statements.find(
        node =>
          ts.isVariableStatement(node) &&
          node.declarationList.declarations.some(
            declaration => declaration.name.getText(ast) === name
          )
      )
      if (!statement) return ''
      return statement.getText(ast)
    })
    .join('\n')
}
const run = (file, names, context, expression) =>
  vm.runInNewContext(
    ts.transpileModule('{\n' + extract(file, names) + '\n' + expression + '\n}', {
      compilerOptions: { target: ts.ScriptTarget.ES2020, module: ts.ModuleKind.CommonJS }
    }).outputText,
    context
  )
const chartA = '3103035270000000001',
  chartB = '3103035270000000002',
  dataset = '3103035270000000003'
const original = { id: '3103035270000000004', name: 'name', deType: 0, groupType: 'd' }
const copy = {
  ...original,
  id: '3103035270000000005',
  name: 'name_copy',
  chartId: chartA,
  extField: 2
}
const charts = [
  { id: chartA, tableId: dataset, type: 'bar' },
  { id: chartB, tableId: dataset, type: 'bar' }
]
const condition = () => ({
  auto: true,
  field: { id: copy.id },
  dataset: { id: dataset, fields: [] },
  checkedFields: [chartA, chartB],
  checkedFieldsMap: { [chartA]: copy.id, [chartB]: copy.id }
})
const flush = () => new Promise(resolve => setImmediate(resolve))
const load = async () => {
  const query = condition()
  const calls = []
  const context = {
    conditions: { value: [query] },
    datasetFieldList: { value: charts },
    datasetMap: {},
    fields: { value: [] },
    relationshipChartIndex: { value: 0 },
    renameInput: { value: [] },
    curComponent: { value: query },
    dialogVisible: { value: false },
    queryElement: { value: {} },
    cascadeArr: [],
    cloneDeep,
    initDataset() {},
    handleCondition() {},
    nextTick() {},
    setTreeDefault() {},
    handleCheckedFieldsChange() {},
    getDsDetailsWithPerm: async () => [
      { id: dataset, fields: { dimensionList: [original], quotaList: [] } }
    ],
    getSqlParams: async () => [{ id: 'parameter', datasetGroupId: dataset }],
    getFieldByDQ: async (ds, owner) => {
      calls.push([ds, owner])
      return {
        dimensionList: [
          original,
          ...(owner === chartA ? [copy] : []),
          { id: '-1' },
          { id: 'parameterized', params: [{}] },
          { id: 'aggregate', agg: true },
          { id: 'masked', desensitized: true }
        ],
        quotaList: []
      }
    }
  }
  run(configFile, ['setActiveSelectTab', 'isQueryField', 'init'], context, "init('query')")
  await flush()
  return { context, query, calls }
}

test('association options contain only the owning chart copy and preserve field restrictions', async () => {
  const { context, calls } = await load()
  const [a, b] = context.fields.value
  assert.equal(a.fields.dimensionList.find(field => field.id === copy.id)?.name, 'name_copy')
  assert.equal(
    b.fields.dimensionList.some(field => field.id === copy.id),
    false
  )
  assert.deepEqual(calls, [
    [dataset, chartA],
    [dataset, chartB]
  ])
  for (const chart of [a, b]) {
    assert.equal(
      chart.fields.dimensionList.some(field =>
        ['-1', 'parameterized', 'aggregate'].includes(field.id)
      ),
      false
    )
    assert.equal(chart.fields.dimensionList.find(field => field.id === 'masked').desensitized, true)
    assert.equal(chart.fields.parameterList[0].id, 'parameter')
  }
})

test('opening historical automatic conditions repairs foreign-chart copy bindings', async () => {
  const { query } = await load()
  assert.equal(query.field.chartId, chartA)
  assert.deepEqual(Array.from(query.checkedFields), [chartA])
  assert.equal(query.checkedFieldsMap[chartA], copy.id)
  assert.equal(query.checkedFieldsMap[chartB], undefined)
  assert.equal(query.dataset.fields.find(field => field.id === copy.id).name, 'name_copy')
})

test('same-dataset auto-fill propagates public fields but never foreign chart copies', async () => {
  const { context } = await load()
  for (const [id, expected] of [
    [copy.id, undefined],
    [original.id, original.id]
  ]) {
    context.curComponent.value = {
      checkedFields: [chartA, chartB],
      checkedFieldsMap: { [chartA]: id, [chartB]: '' }
    }
    run(configFile, ['setSameId'], context, 'setSameId()')
    assert.equal(context.curComponent.value.checkedFieldsMap[chartB] || undefined, expected)
  }
})

test('dragging and refreshing a copied field only links its owner; public fields link both charts', () => {
  for (const [field, expected] of [
    [copy, [chartA]],
    [original, [chartA, chartB]]
  ]) {
    const context = {
      datasetFieldList: { value: charts },
      list: { value: [] },
      element: { value: {} },
      snapshotStore: { recordSnapshotCache() {} },
      infoFormat: field => ({ field }),
      dvMainStore: { mobileInPc: false },
      isMobile: () => false
    }
    run(
      componentFile,
      ['drop', 'updateQueryCriteria'],
      context,
      `drop({ dataTransfer: { getData: () => ${JSON.stringify(
        JSON.stringify([{ ...field, datasetId: dataset }])
      )} } })`
    )
    const query = context.element.value.propValue[0]
    assert.deepEqual(Array.from(query.checkedFields), expected)
    query.dataset = { id: dataset }
    run(componentFile, ['updateQueryCriteria'], context, 'updateQueryCriteria()')
    assert.deepEqual(Array.from(query.checkedFields), expected)
    assert.deepEqual(Object.keys(query.checkedFieldsMap), expected)
  }
})

test('persisted condition field retains owner ID without number conversion', () => {
  const result = run(
    'src/custom-component/v-query/options.ts',
    ['infoFormat'],
    { guid: () => 'query' },
    `infoFormat(${JSON.stringify({ ...copy, datasetId: dataset })})`
  )
  assert.equal(result.field.chartId, chartA)
  assert.equal(result.field.id, copy.id)
})

test('value-source field options use the copied field owner and retain its name', async () => {
  const calls = []
  const component = { field: { ...copy }, dataset: { id: dataset } }
  const context = {
    getFieldByDQ: async (...args) => {
      calls.push(args)
      return { dimensionList: [copy], quotaList: [] }
    },
    listFieldsWithPermissions() {
      throw Error('copy must use its owning chart fields')
    },
    component,
    dataset
  }
  run(configFile, ['isQueryField', 'getOptions'], context, 'getOptions(dataset, component)')
  await flush()
  assert.deepEqual(calls[0].slice(0, 2), [dataset, chartA])
  assert.equal(component.dataset.fields[0].name, 'name_copy')
})

test('changing back to a public field clears its private owner even without a default value', () => {
  const query = {
    field: { id: original.id, chartId: chartA },
    dataset: { fields: [original, copy] },
    defaultValueCheck: false
  }
  run(configFile, ['handleFieldChange'], { curComponent: { value: query } }, 'handleFieldChange()')
  assert.equal(query.field.chartId, undefined)
  query.field.id = copy.id
  run(configFile, ['handleFieldChange'], { curComponent: { value: query } }, 'handleFieldChange()')
  assert.equal(query.field.chartId, chartA)
})

test('a delayed public field response cannot overwrite repaired copy options', async () => {
  let resolve
  const component = { field: { id: copy.id }, dataset: { id: dataset, fields: [copy] } }
  const context = {
    component,
    dataset,
    listFieldsWithPermissions: () => new Promise(done => (resolve = done))
  }
  run(configFile, ['isQueryField', 'getOptions'], context, 'getOptions(dataset, component)')
  component.field.chartId = chartA
  resolve({ data: [original] })
  await flush()
  assert.equal(component.dataset.fields[0].name, 'name_copy')
})

test('edited query components compile', () => {
  for (const file of [configFile, componentFile]) {
    const { descriptor, errors } = parse(read(file))
    assert.deepEqual(errors, [])
    const script = compileScript(descriptor, { id: 'copied-field' })
    const template = compileTemplate({
      source: descriptor.template.content,
      filename: file,
      id: 'copied-field',
      compilerOptions: { bindingMetadata: script.bindings }
    })
    assert.deepEqual(template.errors, [])
  }
})
