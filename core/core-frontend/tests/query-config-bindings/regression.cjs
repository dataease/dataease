/* eslint-disable @typescript-eslint/no-var-requires -- Node 回归入口。 */
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const vm = require('node:vm')
const { test } = require('node:test')
const { cloneDeep } = require('lodash')
const { nextTick, unref } = require('vue')
const ts = require('typescript')
const { parse } = require('@vue/compiler-sfc')
const bindings = require('./load.cjs')
const {
  readRangeBinding,
  createRangeParameterPatch,
  createRangeSelectionPatch,
  hasQueryBindings,
  getSourceBindingError,
  createDatasetSourcePatch,
  createFieldSourcePatch,
  createOptionSourcePatch
} = bindings
const filename = path.resolve(
  __dirname,
  '../../src/custom-component/v-query/QueryConditionConfiguration.vue'
)

const freeze = value => {
  if (value && typeof value === 'object') {
    Object.values(value).forEach(freeze)
    Object.freeze(value)
  }
  return value
}
const config = extra => ({
  id: 'condition',
  checkedFields: ['chart-a', 'chart-b'],
  checkedFieldsMap: { 'chart-a': 'date-a', 'chart-b': 'date-b' },
  checkedFieldsMapArr: { 'chart-a': ['date-a', 'date-end'], 'chart-b': ['b-start'] },
  checkedFieldsMapStart: { 'chart-a': 'date-a', 'chart-b': 'b-start' },
  checkedFieldsMapEnd: { 'chart-a': 'date-end', 'chart-b': '' },
  parametersArr: { 'chart-a': [{ id: 'date-a', variableName: 'start' }] },
  field: { id: 'source-field', chartId: 'owner', historicalFlag: false },
  dataset: { id: 'dataset', fields: [] },
  displayId: 'name-field',
  sortId: 'sort-field',
  optionValueSource: 1,
  valueSource: ['手动值'],
  multiple: false,
  defaultValueCheck: true,
  defaultValue: 0,
  selectValue: 0,
  mapValue: [0],
  defaultMapValue: [0],
  timeRange: { relativeToCurrentRange: 'custom', historicalFlag: '' },
  historicalConfig: { zero: 0, bool: false, empty: '', nullable: null },
  ...extra
})

const extract = (names, sourceFile = filename) => {
  const source = parse(fs.readFileSync(sourceFile, 'utf8')).descriptor.scriptSetup.content
  const ast = ts.createSourceFile(sourceFile, source, ts.ScriptTarget.Latest, true)
  return names
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
}
const run = (names, context, expression, sourceFile) =>
  vm.runInNewContext(
    ts.transpileModule(extract(names, sourceFile) + '\n' + expression, {
      compilerOptions: { target: ts.ScriptTarget.ES2020, module: ts.ModuleKind.CommonJS }
    }).outputText,
    { ...bindings, ...context }
  )
const normalized = value => JSON.parse(JSON.stringify(value))

test('binding projection and validation do not initialize or mutate historical storage', () => {
  const input = freeze({
    checkedFields: ['chart'],
    historical: { zero: 0, flag: false, value: null }
  })
  const before = cloneDeep(input)
  assert.deepEqual(readRangeBinding(input, 'chart', 'time'), {
    fieldIds: [],
    start: undefined,
    end: undefined
  })
  assert.equal(hasQueryBindings(input, 'some'), false)
  assert.equal(hasQueryBindings(input, 'every'), false)
  assert.equal(getSourceBindingError(input), undefined)
  assert.deepEqual(input, before)
  assert.equal(Object.hasOwn(input, 'checkedFieldsMapArr'), false)
})

test('range projection is detached and preserves a single endpoint without normalizing it', () => {
  const input = freeze(config())
  const range = readRangeBinding(input, 'chart-b', 'time')
  assert.equal(range.start, 'b-start')
  assert.equal(range.end, '')
  range.fieldIds.push('local-only')
  assert.deepEqual(input.checkedFieldsMapArr['chart-b'], ['b-start'])
})

for (const kind of ['time', 'number']) {
  test(`${kind}: parameter assignment preserves range direction, then switches to single binding`, () => {
    let input = config({
      checkedFieldsMapArr: {},
      checkedFieldsMapStart: {},
      checkedFieldsMapEnd: {}
    })
    input = { ...input, ...createRangeParameterPatch(freeze(input), 'chart-a', kind, 'start', 1) }
    assert.deepEqual(readRangeBinding(input, 'chart-a', kind), {
      fieldIds: ['start'],
      start: 'start',
      end: ''
    })
    input = { ...input, ...createRangeParameterPatch(freeze(input), 'chart-a', kind, 'end', 2) }
    assert.deepEqual(readRangeBinding(input, 'chart-a', kind), {
      fieldIds: ['start', 'end'],
      start: 'start',
      end: 'end'
    })
    input = { ...input, ...createRangeParameterPatch(freeze(input), 'chart-a', kind, 'end', 1) }
    assert.equal(readRangeBinding(input, 'chart-a', kind).start, 'end')
    assert.equal(readRangeBinding(input, 'chart-a', kind).end, 'start')
    input = { ...input, ...createRangeParameterPatch(freeze(input), 'chart-a', kind, 'single', 0) }
    assert.equal(input.checkedFieldsMap['chart-a'], 'single')
    assert.deepEqual(readRangeBinding(input, 'chart-a', kind), { fieldIds: [], start: '', end: '' })
    assert.equal(input.defaultValue, 0)
    assert.deepEqual(input.historicalConfig, config().historicalConfig)
  })

  test(`${kind}: clearing range removes only this chart binding and parameter list`, () => {
    const input = freeze(config())
    const patched = { ...input, ...createRangeSelectionPatch(input, 'chart-a', kind, []) }
    assert.equal(patched.checkedFieldsMap['chart-a'], '')
    assert.deepEqual(patched.parametersArr['chart-a'], [])
    assert.equal(patched.checkedFieldsMap['chart-b'], input.checkedFieldsMap['chart-b'])
    assert.deepEqual(patched.checkedFieldsMapArr['chart-b'], ['b-start'])
    assert.deepEqual(readRangeBinding(patched, 'chart-a', 'time').fieldIds, [])
    assert.equal(input.checkedFieldsMap['chart-a'], 'date-a')
  })
}

test('switching range kind clears stale opposite endpoints even if its list is already empty', () => {
  const input = freeze(config({ checkedFieldsMapArr: { 'chart-a': [], 'chart-b': ['b-start'] } }))
  const patched = {
    ...input,
    ...createRangeParameterPatch(input, 'chart-a', 'number', 'number-start', 1)
  }
  assert.deepEqual(readRangeBinding(patched, 'chart-a', 'time'), {
    fieldIds: [],
    start: '',
    end: ''
  })
  assert.deepEqual(readRangeBinding(patched, 'chart-a', 'number'), {
    fieldIds: ['number-start'],
    start: 'number-start',
    end: ''
  })
  assert.deepEqual(
    readRangeBinding(patched, 'chart-b', 'time'),
    readRangeBinding(input, 'chart-b', 'time')
  )
})

test('removing one endpoint reconciles direction without clearing the other chart or defaults', () => {
  const input = freeze(config())
  const patched = { ...input, ...createRangeSelectionPatch(input, 'chart-a', 'time', ['date-end']) }
  assert.deepEqual(readRangeBinding(patched, 'chart-a', 'time'), {
    fieldIds: ['date-end'],
    start: undefined,
    end: 'date-end'
  })
  assert.equal(patched.parametersArr, input.parametersArr)
  assert.equal(patched.defaultValue, input.defaultValue)
})

test('field IDs stay strings and edits do not initialize absent unrelated maps', () => {
  const id = '9223372036854775807'
  const input = freeze({ checkedFieldsMap: { other: '0' }, historical: false })
  const patch = createRangeParameterPatch(input, 'chart', 'number', id, 0)
  assert.equal(patch.checkedFieldsMap.chart, id)
  assert.equal(patch.checkedFieldsMap.other, '0')
  assert.equal(Object.hasOwn(patch, 'checkedFieldsMapArr'), false)
})

test('preview requires any binding while save requires every selected chart to be bound', () => {
  const input = freeze({ checkedFields: ['a', 'b'], checkedFieldsMap: { a: 'field', b: '' } })
  assert.equal(hasQueryBindings(input, 'some'), true)
  assert.equal(hasQueryBindings(input, 'every'), false)
  assert.equal(hasQueryBindings({ checkedFields: [] }, 'every'), false)
  assert.equal(
    hasQueryBindings({ checkedFields: ['a'], checkedFieldsMap: { a: '0' } }, 'every'),
    true
  )
})

test('source validation keeps existing raw versus nonempty manual-value rules without changing values', () => {
  const input = freeze(config({ optionValueSource: 2, valueSource: ['', 0, false, null] }))
  assert.equal(getSourceBindingError(input), undefined)
  assert.equal(getSourceBindingError(input, 'nonempty'), 'missing-manual-values')
  assert.deepEqual(input.valueSource, ['', 0, false, null])
  assert.equal(getSourceBindingError({ optionValueSource: 1 }), 'missing-field')
  assert.equal(getSourceBindingError({ optionValueSource: 2 }), 'missing-manual-values')
  assert.equal(getSourceBindingError({ optionValueSource: 0 }), undefined)
})

test('dataset source reset clears source owner, label and sort while retaining chart bindings and metadata', () => {
  const input = freeze(config())
  const patched = { ...input, ...createDatasetSourcePatch(input) }
  assert.deepEqual(patched.field, { id: '', chartId: undefined, historicalFlag: false })
  assert.equal(patched.displayId, '')
  assert.equal(patched.sortId, '')
  assert.equal(patched.checkedFieldsMap, input.checkedFieldsMap)
  assert.equal(patched.timeRange, input.timeRange)
  assert.deepEqual(patched.defaultMapValue, [0])
})

test('changing source field updates private ownership and only resets enabled defaults', () => {
  const input = freeze(config({ defaultValueCheck: false }))
  const patch = createFieldSourcePatch(input, undefined)
  assert.equal(patch.field.chartId, undefined)
  assert.equal(Object.hasOwn(patch, 'defaultValue'), false)
  assert.equal(Object.hasOwn(patch, 'displayId'), false)
  const mapped = createFieldSourcePatch(
    freeze(config({ displayId: '', multiple: true })),
    'new-owner'
  )
  assert.equal(mapped.field.chartId, 'new-owner')
  assert.equal(mapped.displayId, 'source-field')
  assert.deepEqual(mapped.defaultValue, [])
})

test('switching candidate source resets selections without rewriting bindings, mappings, operators or dates', () => {
  for (const multiple of [true, false]) {
    const input = freeze(
      config({ multiple, conditionValueOperatorF: 'eq', conditionValueF: false })
    )
    const patch = createOptionSourcePatch(input)
    const patched = { ...input, ...patch }
    assert.deepEqual(patched.defaultValue, multiple ? [] : undefined)
    assert.equal(patched.defaultValue, patched.selectValue)
    for (const key of Object.keys(input).filter(
      key => !['defaultValue', 'selectValue'].includes(key)
    )) {
      assert.equal(patched[key], input[key], key)
    }
  }
})

test('production configuration handlers use binding patches and retain display/callback behavior', () => {
  const input = config()
  let updated = 0
  const context = {
    curComponent: { value: input },
    currentComponentId: 'chart-a',
    currentParameterId: 'number-start',
    numParameterType: { value: 1 },
    numDialogShow: { value: true },
    setParametersNumType: () => updated++,
    setTypeChange: () => updated++,
    console
  }
  run(['numTypeChange'], context, 'numTypeChange()')
  assert.equal(input.displayType, '22')
  assert.equal(context.numDialogShow.value, false)
  assert.equal(updated, 2)
  assert.equal(readRangeBinding(input, 'chart-a', 'time').fieldIds.length, 0)
  const calls = []
  run(
    ['handleDatasetChange'],
    {
      curComponent: { value: input },
      newDatasetId: '',
      oldDatasetId: '',
      getOptions: (...args) => calls.push(args)
    },
    'handleDatasetChange()'
  )
  assert.equal(calls[0][0], 'dataset')
  assert.equal(input.field.id, '')
  assert.equal(input.field.historicalFlag, false)
})

test('source switches retain configured dataset fallback and source-specific defaults', () => {
  const input = config({ dataset: { id: '', historical: false }, optionValueSource: 1 })
  const fields = [
    {
      componentId: 'chart-b',
      id: 'fallback-dataset',
      fields: { dimensions: [{ id: 'date-b', chartId: 'chart-b' }] }
    }
  ]
  const calls = []
  run(
    ['handleValueSourceChange'],
    {
      curComponent: { value: input },
      fields: { value: fields },
      getOptions: (...args) => calls.push(args)
    },
    'handleValueSourceChange()'
  )
  assert.equal(input.dataset.id, 'fallback-dataset')
  assert.equal(input.dataset.historical, false)
  assert.equal(input.field.id, 'date-b')
  assert.equal(input.field.chartId, 'chart-b')
  assert.equal(input.displayId, 'date-b')
  assert.equal(input.sortId, 'date-b')
  assert.equal(calls.length, 1)
  assert.equal(input.defaultValue, undefined)
})

test('save, serialize and reopen retain untouched legacy fields and commit exactly one snapshot', async () => {
  const original = config({
    displayType: '7',
    timeType: 'dynamic',
    multiple: true,
    defaultValue: ['2026-11-01', '2026-11-06'],
    selectValue: ['2026-11-01', '2026-11-06'],
    relativeToCurrentRange: 'lastQuarter',
    clockOffset: { mode: 'relative', hours: 0, minutes: 0, seconds: 0 },
    parameters: [{ id: 'p', value: 0, historical: false }]
  })
  const draft = cloneDeep(original)
  const element = { propValue: [cloneDeep(original)] }
  const snapshots = [],
    events = []
  run(
    ['multipleChange', 'confirmClick'],
    {
      curComponent: { value: draft },
      conditions: { value: [draft] },
      queryElement: { value: element },
      validate: () => false,
      defaultConfigurationRef: { value: undefined },
      handleDialogClick: () => undefined,
      dialogVisible: { value: true },
      relationshipChartIndex: { value: 0 },
      cascadeArr: [],
      cloneDeep,
      unref,
      nextTick,
      snapshotStore: { recordSnapshotCache: value => snapshots.push(value) },
      emits: (...args) => events.push(args)
    },
    'confirmClick()'
  )
  await nextTick()
  await nextTick()
  assert.deepEqual(normalized(element.propValue[0]), normalized(original))
  assert.deepEqual(snapshots, ['confirmClick'])
  assert.deepEqual(
    events.map(([name]) => name),
    ['reRenderAll', 'queryData']
  )
})

test(
  'optional baseline compares production configuration transitions with pre-refactor handlers',
  {
    skip: !process.env.QUERY_CONFIGURATION_BASELINE
  },
  () => {
    for (const prefix of ['time', 'num']) {
      for (const endpoint of [0, 1, 2]) {
        const input = config(
          prefix === 'num'
            ? {
                checkedFieldsMapArr: {},
                checkedFieldsMapStart: {},
                checkedFieldsMapEnd: {},
                checkedFieldsMapArrNum: { 'chart-a': ['number-a', 'number-end'] },
                checkedFieldsMapStartNum: { 'chart-a': 'number-a' },
                checkedFieldsMapEndNum: { 'chart-a': 'number-end' }
              }
            : {}
        )
        const execute = sourceFile => {
          const draft = cloneDeep(input)
          run(
            [`${prefix}TypeChange`],
            {
              curComponent: { value: draft },
              currentComponentId: 'chart-a',
              currentParameterId: 'field',
              [`${prefix}ParameterType`]: { value: endpoint },
              [`${prefix}DialogShow`]: { value: true },
              setParametersTimeType: () => undefined,
              setParametersNumType: () => undefined,
              setTypeChange: () => undefined
            },
            `${prefix}TypeChange()`,
            sourceFile
          )
          return normalized(draft)
        }
        assert.deepEqual(execute(filename), execute(process.env.QUERY_CONFIGURATION_BASELINE))
      }
    }
  }
)
