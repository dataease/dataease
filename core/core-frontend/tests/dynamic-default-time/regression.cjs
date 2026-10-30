/* eslint-disable @typescript-eslint/no-var-requires */
const { test, before, after } = require('node:test')
const assert = require('node:assert/strict')
const fs = require('node:fs')
const os = require('node:os')
const path = require('node:path')
const vm = require('node:vm')
const { buildSync, transformSync } = require('esbuild')
const root = path.resolve(__dirname, '../..')
const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'dataease-dynamic-default-'))
let evaluate, validate, searchQuery, runtime, DynamicTimeError, customClockDefault
const baseline = new Date(2026, 9, 30, 15, 25, 53)
const make = (extra = {}) => ({
  id: 'time',
  displayType: '1',
  timeType: 'dynamic',
  defaultValueCheck: true,
  timeGranularity: 'datetime',
  timeGranularityMultiple: 'datetimerange',
  relativeToCurrent: 'custom',
  relativeToCurrentRange: 'custom',
  timeNum: 0,
  relativeToCurrentType: 'date',
  around: 'f',
  timeNumRange: 0,
  relativeToCurrentTypeRange: 'date',
  aroundRange: 'f',
  arbitraryTime: new Date(2020, 0, 1, 9, 10, 11),
  arbitraryTimeRange: new Date(2020, 0, 1, 23, 59, 59),
  clockOffset: { mode: 'relative', hours: 1, minutes: 30, seconds: 0, around: 'f' },
  checkedFields: ['chart'],
  checkedFieldsMap: { chart: 'field' },
  ...extra
})
const local = date => [
  date.getFullYear(),
  date.getMonth() + 1,
  date.getDate(),
  date.getHours(),
  date.getMinutes(),
  date.getSeconds()
]
const bundle = (entry, name, alias = {}) => {
  const outfile = path.join(dir, name + '.cjs')
  buildSync({
    entryPoints: [path.join(root, entry)],
    outfile,
    bundle: true,
    platform: 'node',
    format: 'cjs',
    alias
  })
  return require(outfile)
}
before(() => {
  const mod = bundle('src/custom-component/v-query/dynamic-default-time.ts', 'time')
  runtime = mod.evaluateRuntimeDynamicDefault
  DynamicTimeError = mod.DynamicTimeError
  customClockDefault = mod.isCustomClockDefault
  evaluate = mod.evaluateDynamicDefault
  validate = mod.validateDynamicDefault
  const store = path.join(dir, 'store.js')
  fs.writeFileSync(store, 'exports.dvMainStoreWithOut=()=>({});exports.storeToRefs=()=>({});')
  searchQuery = bundle('src/hooks/web/useFilter.ts', 'filter', {
    '@': path.join(root, 'src'),
    '@/store/modules/data-visualization/dvMain': store,
    pinia: store
  }).searchQuery
})
after(() => fs.rmSync(dir, { recursive: true, force: true }))

for (const [name, clockOffset, expected] of [
  ['前1时30分', { hours: 1, minutes: 30, seconds: 0, around: 'f' }, [2026, 10, 30, 13, 55, 53]],
  ['后2分7秒', { hours: 0, minutes: 2, seconds: 7, around: 'b' }, [2026, 10, 30, 15, 28, 0]],
  ['零偏移', { hours: 0, minutes: 0, seconds: 0, around: 'f' }, [2026, 10, 30, 15, 25, 53]],
  ['25小时', { hours: 25, minutes: 0, seconds: 0, around: 'b' }, [2026, 10, 31, 16, 25, 53]]
])
  test(name, () =>
    assert.deepEqual(
      local(evaluate(make({ clockOffset: { mode: 'relative', ...clockOffset } }), baseline)),
      expected
    )
  )

test('日期先偏移且与时钟方向独立', () => {
  assert.deepEqual(local(evaluate(make({ timeNum: 1 }), baseline)), [2026, 10, 29, 13, 55, 53])
  assert.deepEqual(
    local(evaluate(make({ timeNum: 1, around: 'b' }), baseline)),
    [2026, 10, 31, 13, 55, 53]
  )
})
for (const [name, base, expected] of [
  ['跨日', new Date(2026, 9, 30, 0, 15), [2026, 10, 29, 23, 45, 0]],
  ['跨月', new Date(2026, 10, 1, 0, 15), [2026, 10, 31, 23, 45, 0]],
  ['跨年', new Date(2027, 0, 1, 0, 15), [2026, 12, 31, 23, 45, 0]],
  ['闰年', new Date(2024, 2, 1, 0, 15), [2024, 2, 29, 23, 45, 0]]
])
  test(name, () =>
    assert.deepEqual(
      local(
        evaluate(
          make({
            clockOffset: { mode: 'relative', hours: 0, minutes: 30, seconds: 0, around: 'f' }
          }),
          base
        )
      ),
      expected
    )
  )

test('范围共用基准，可以混用两种模式，保留秒精度', () => {
  const c = make({ displayType: '7' })
  const [start, end] = evaluate(c, baseline)
  assert.deepEqual(local(start), [2026, 10, 30, 13, 55, 53])
  assert.deepEqual(local(end), [2026, 10, 30, 23, 59, 59])
  c.clockOffsetRange = { ...c.clockOffset, hours: 0, minutes: 0 }
  assert.equal(+evaluate(c, baseline)[1], +baseline)
})
test('起点晚于终点报错，不交换', () => {
  const c = make({
    displayType: '7',
    clockOffsetRange: { mode: 'relative', hours: 2, minutes: 0, seconds: 0, around: 'f' }
  })
  assert.throws(() => evaluate(c, baseline), /v_query.the_start_time/)
  assert.equal(validate(c), 'v_query.the_start_time')
})
for (const bad of ['', ' ', null, undefined, -1, 0.5, NaN, Infinity, 'abc', 1e20]) {
  test(`非法小时 ${String(bad)}`, () =>
    assert.throws(
      () =>
        evaluate(
          make({
            clockOffset: { mode: 'relative', hours: bad, minutes: 0, seconds: 0, around: 'f' }
          }),
          baseline
        ),
      /invalid_offset/
    ))
}
for (const unit of ['minutes', 'seconds'])
  test(`${unit} 不接受60`, () => {
    const c = make()
    c.clockOffset[unit] = 60
    assert.equal(validate(c), 'dynamic_time.invalid_offset')
  })
test('日期为空、小数、负值和溢出均报错', () => {
  for (const timeNum of [undefined, null, -1, 0.1, 1e15])
    assert.throws(() => evaluate(make({ timeNum }), baseline), /invalid_offset/)
})
test('旧配置默认指定时刻，非活动模式的非法值不参与计算', () => {
  const c = make({ clockOffset: undefined })
  assert.deepEqual(local(evaluate(c, baseline)), [2026, 10, 30, 9, 10, 11])
  c.clockOffset = { mode: 'fixed', hours: -1, minutes: 90, seconds: '', around: 'f' }
  assert.deepEqual(local(evaluate(c, baseline)), [2026, 10, 30, 9, 10, 11])
})
test('保存/复制/模板使用JSON往返保留规则，新基准重新计算', () => {
  const c = JSON.parse(JSON.stringify(make()))
  c.defaultValue = new Date(2000, 0, 1).toISOString()
  assert.deepEqual(local(evaluate(c, baseline)), [2026, 10, 30, 13, 55, 53])
  assert.deepEqual(
    local(evaluate(c, new Date(2026, 9, 31, 15, 25, 53))),
    [2026, 10, 31, 13, 55, 53]
  )
})
test('首次加载实际过滤器重新计算，普通查询保留手动输入', () => {
  const c = make()
  const before = Date.now() - 5400000
  const filters = []
  searchQuery([{ id: 'query', propValue: [c] }], filters, 'chart', true)
  assert.ok(Math.abs(filters[0].value[0] - before) < 1500)
  assert.equal(filters[0].value[0], filters[0].value[1])
  c.selectValue = new Date(2025, 0, 1, 12, 34, 56)
  const manual = []
  searchQuery([{ id: 'query', propValue: [c] }], manual, 'chart', false)
  assert.deepEqual(manual[0].value, [+c.selectValue, +c.selectValue])
})
test('实际重置入口使用规则而非旧预览，恢复为最新默认值', () => {
  const source = fs.readFileSync(
    path.join(root, 'src/custom-component/v-query/Component.vue'),
    'utf8'
  )
  const start = source.indexOf('const resetData = () =>')
  const code = source.slice(start, source.indexOf('const clearData =', start))
  const item = make({ defaultValue: new Date(2000, 0, 1) })
  const element = { value: { propValue: [] } }
  vm.runInNewContext(code + ';resetData()', {
    element,
    props: { element: { cascade: [] } },
    nextTick: fn => fn(),
    cloneDeep: structuredClone,
    oldList: [item],
    cascadeResetState: { value: false },
    componentWithSure: { value: true },
    getKeyList: () => [],
    evaluateRuntimeDynamicDefault: runtime,
    DynamicTimeError
  })
  assert.ok(Math.abs(+element.value.propValue[0].selectValue - (Date.now() - 5400000)) < 1500)
})

test('实际保存校验拒绝非法规则和颠倒范围，包括自动字段', () => {
  const source = fs.readFileSync(
    path.join(root, 'src/custom-component/v-query/QueryConditionConfiguration.vue'),
    'utf8'
  )
  const start = source.indexOf('const validate = () =>')
  const code = transformSync(
    source.slice(start, source.indexOf('const handleBeforeClose =', start)),
    { loader: 'ts' }
  ).code
  for (const item of [
    make({ auto: true, timeNum: -1 }),
    make({
      displayType: '7',
      clockOffsetRange: { mode: 'relative', hours: 2, minutes: 0, seconds: 0, around: 'f' }
    })
  ]) {
    const messages = []
    const context = {
      conditions: { value: [item] },
      validateDynamicDefault: validate,
      ElMessage: { error: key => messages.push(key) },
      t: key => key
    }
    assert.equal(vm.runInNewContext(code + ';validate()', context), true)
    assert.equal(messages.length, 1)
  }
})
for (const timeGranularity of ['year', 'month', 'date'])
  test(`${timeGranularity} 首次查询仍包含完整周期`, () => {
    const c = make({
      timeGranularity,
      relativeToCurrentType: timeGranularity,
      clockOffset: undefined
    })
    const filters = []
    searchQuery([{ id: 'query', propValue: [c] }], filters, 'chart', true)
    assert.ok(filters[0].value[1] - filters[0].value[0] >= 24 * 3600000 - 1000)
  })

test('旧配置清空的日期数量和时刻保持旧运行语义，保存仍严格校验', () => {
  const c = make({ clockOffset: undefined, timeNum: null, arbitraryTime: null })
  assert.deepEqual(local(runtime(c, baseline)), local(baseline))
  assert.equal(c.timeNum, null)
  assert.equal(c.arbitraryTime, null)
  assert.equal(validate(c), 'dynamic_time.invalid_offset')
  const range = { ...c, displayType: '7', timeNumRange: null, arbitraryTimeRange: null }
  assert.deepEqual(runtime(range, baseline).map(local), [local(baseline), local(baseline)])
})
test('新相对规则的非法输入不被旧配置兼容逻辑归零', () => {
  const c = make()
  c.clockOffset.minutes = 60
  assert.throws(() => runtime(c, baseline), /invalid_offset/)
})
test('报告独立保存入口拒绝非法时钟和颠倒范围，自动字段也校验', () => {
  const source = fs.readFileSync(
    path.join(root, 'src/views/menu/system/report/form/ConditionDefaultSet.vue'),
    'utf8'
  )
  const start = source.indexOf('const validate = () =>')
  const code = transformSync(source.slice(start, source.indexOf('const confirm =', start)), {
    loader: 'ts'
  }).code
  const invalid = make({ auto: true })
  invalid.clockOffset.minutes = 60
  for (const item of [
    invalid,
    make({
      displayType: '7',
      clockOffsetRange: { mode: 'relative', hours: 2, minutes: 0, seconds: 0, around: 'f' }
    })
  ]) {
    const messages = []
    assert.equal(
      vm.runInNewContext(code + ';validate()', {
        filterInfo: { value: { defaultFilter: [item] } },
        validateDynamicDefault: validate,
        ElMessage: { error: key => messages.push(key) },
        t: key => key
      }),
      true
    )
    assert.equal(messages.length, 1)
  }
})
test('报告摘要展示单值、范围混合模式的完整时钟规则', () => {
  const source = fs.readFileSync(
    path.join(root, 'src/views/menu/system/report/form/ConditionDefaultSet.vue'),
    'utf8'
  )
  const start = source.indexOf('const toDisplayText =')
  const code = transformSync(source.slice(start, source.indexOf('const getValue =', start)), {
    loader: 'ts'
  }).code
  const context = {
    isCustomClockDefault: customClockDefault,
    getValue: () => 'nonempty',
    toDisplayString: value => String(value ?? ''),
    dayjs: require('dayjs'),
    relativeToCurrentList: { date: '日', f: '前', b: '后' },
    t: key =>
      ({ 'dynamic_time.hours': '时', 'dynamic_time.minutes': '分', 'dynamic_time.seconds': '秒' }[
        key
      ] || key)
  }
  const single = vm.runInNewContext(code + ';getDisplayLines(item)', { ...context, item: make() })
  assert.match(single[0].text, /1时 30分 0秒 前/)
  const range = vm.runInNewContext(code + ';getDisplayLines(item)', {
    ...context,
    item: make({ displayType: '7' })
  })
  assert.match(range[1].text, /1时 30分 0秒 前/)
  assert.match(range[2].text, /23:59:59/)
})
test('重置非法配置时恢复原筛选、清理联动标记，不触发未过滤查询', () => {
  const source = fs.readFileSync(
    path.join(root, 'src/custom-component/v-query/Component.vue'),
    'utf8'
  )
  const start = source.indexOf('const resetData = () =>')
  const code = source.slice(start, source.indexOf('const clearData =', start))
  const item = make()
  item.clockOffset.minutes = 60
  const previous = make({ selectValue: new Date(2025, 0, 1) })
  const element = { value: { propValue: [previous] } },
    cascadeResetState = { value: false },
    messages = []
  let queries = 0
  vm.runInNewContext(code + ';resetData()', {
    element,
    props: { element: { cascade: [] } },
    nextTick: fn => fn(),
    cloneDeep: structuredClone,
    oldList: [item],
    cascadeResetState,
    componentWithSure: { value: false },
    getKeyList: () => [],
    evaluateRuntimeDynamicDefault: runtime,
    DynamicTimeError,
    ElMessage: { error: value => messages.push(value) },
    t: key => key,
    queryData: () => queries++
  })
  assert.deepEqual(element.value.propValue, [previous])
  assert.equal(cascadeResetState.value, false)
  assert.equal(messages.length, 1)
  assert.equal(queries, 0)
})
test('原生图表首次加载的非法默认规则显示错误并停止取数', () => {
  const source = fs.readFileSync(
    path.join(root, 'src/views/chart/components/views/index.vue'),
    'utf8'
  )
  const start = source.indexOf('const queryData = debounce(')
  const code = source.slice(start, source.indexOf('const calcData =', start))
  let calls = 0
  const messages = []
  vm.runInNewContext(code + ';queryData(true)', {
    debounce: fn => fn,
    loading: { value: false },
    dvMainStore: { firstLoadMap: [] },
    element: { value: { id: 'chart' } },
    showPosition: { value: 'preview' },
    filter: () => {
      throw new DynamicTimeError('dynamic_time.invalid_offset')
    },
    DynamicTimeError,
    ElMessage: { error: value => messages.push(value) },
    t: key => key,
    calcData: () => calls++
  })
  assert.equal(calls, 0)
  assert.equal(messages[0].message, 'dynamic_time.invalid_offset')
})
test('插件初次过滤失败时提示并阻止带空过滤参数的加载', () => {
  const source = fs.readFileSync(
    path.join(root, 'src/views/chart/components/views/index.vue'),
    'utf8'
  )
  const start = source.indexOf('onMounted(() => {\n  if (!view.value.isPlugin)')
  const code = source.slice(start, source.indexOf('  if (!listenerEnable.value)', start)) + '})'
  const messages = [],
    loadPlugin = { value: false }
  vm.runInNewContext(code, {
    onMounted: fn => fn(),
    view: { value: { isPlugin: true } },
    dvMainStore: { firstLoadMap: [] },
    element: { value: { id: 'chart' } },
    filter: () => {
      throw new DynamicTimeError('dynamic_time.invalid_offset')
    },
    DynamicTimeError,
    ElMessage: { error: value => messages.push(value) },
    t: key => key,
    loadPlugin
  })
  assert.equal(loadPlugin.value, false)
  assert.equal(messages.length, 1)
})

test('后续条件非法时，重置不提前改写或通知前面条件的联动状态', () => {
  const source = fs.readFileSync(
    path.join(root, 'src/custom-component/v-query/Component.vue'),
    'utf8'
  )
  const start = source.indexOf('const resetData = () =>')
  const code = source.slice(start, source.indexOf('const clearData =', start))
  const invalid = make()
  invalid.clockOffset.minutes = 60
  const cascade = [[{ datasetId: 'ds--valid--field', currentSelectValue: ['manual'] }]]
  let emitted = 0
  const messages = []
  vm.runInNewContext(code + ';resetData()', {
    element: { value: { propValue: [] } },
    props: { element: { cascade } },
    nextTick: fn => fn(),
    cloneDeep: structuredClone,
    oldList: [make({ id: 'valid' }), invalid],
    cascadeResetState: { value: false },
    componentWithSure: { value: false },
    getKeyList: () => [],
    evaluateRuntimeDynamicDefault: runtime,
    DynamicTimeError,
    ElMessage: { error: value => messages.push(value) },
    t: key => key,
    useEmitt: () => ({ emitter: { emit: () => emitted++ } })
  })
  assert.deepEqual(cascade[0][0].currentSelectValue, ['manual'])
  assert.equal(emitted, 0)
  assert.equal(messages.length, 1)
})
