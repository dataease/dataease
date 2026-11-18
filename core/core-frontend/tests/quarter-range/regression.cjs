/* eslint-disable @typescript-eslint/no-var-requires */
const { test, after } = require('node:test')
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const os = require('node:os')
const vm = require('node:vm')
const { buildSync, transformSync } = require('esbuild')
const root = path.resolve(__dirname, '../..')
const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'dataease-quarter-'))
const bundle = (file, name, alias = {}) => {
  const outfile = path.join(dir, name + '.cjs')
  buildSync({
    entryPoints: [path.join(root, file)],
    outfile,
    bundle: true,
    platform: 'node',
    alias
  })
  return require(outfile)
}
const { getCustomRange } = bundle('src/custom-component/v-query/time-format-dayjs.ts', 'range')
const { evaluateRuntimeDynamicDefault } = bundle(
  'src/custom-component/v-query/dynamic-default-time.ts',
  'dynamic'
)
const local = date => require('dayjs')(date).format('YYYY-MM-DD HH:mm:ss')
after(() => fs.rmSync(dir, { recursive: true, force: true }))

for (const [date, current, previous] of [
  ['2026-10-30', ['2026-10-01', '2026-12-31'], ['2026-07-01', '2026-09-30']],
  ['2027-01-01', ['2027-01-01', '2027-03-31'], ['2026-10-01', '2026-12-31']],
  ['2028-02-29', ['2028-01-01', '2028-03-31'], ['2027-10-01', '2027-12-31']],
  ['2028-04-01', ['2028-04-01', '2028-06-30'], ['2028-01-01', '2028-03-31']],
  ['2026-06-30', ['2026-04-01', '2026-06-30'], ['2026-01-01', '2026-03-31']]
]) {
  for (const [rule, expected] of [
    ['thisQuarter', current],
    ['lastQuarter', previous]
  ]) {
    test(`${date} ${rule} 完整自然季度，精确到秒`, () => {
      const baseline = new Date(date + 'T23:59:59.999')
      const range = getCustomRange(rule, baseline)
      assert.deepEqual(range.map(local), [expected[0] + ' 00:00:00', expected[1] + ' 23:59:59'])
      assert.equal(range[1].getMilliseconds(), 0)
      for (const timeGranularityMultiple of ['monthrange', 'daterange', 'datetimerange']) {
        const config = JSON.parse(
          JSON.stringify({
            displayType: '7',
            timeType: 'dynamic',
            defaultValueCheck: true,
            relativeToCurrentRange: rule,
            timeGranularityMultiple
          })
        )
        assert.deepEqual(
          evaluateRuntimeDynamicDefault(config, baseline).map(local),
          range.map(local)
        )
      }
    })
  }
}
for (const file of [
  'ConditionDefaultConfiguration.vue',
  'QueryConditionConfiguration.vue',
  'RangeFilterTime.vue',
  'FilterTime.vue'
]) {
  test(`${file} 三种范围提供季度、年范围排除季度、原有选项保留`, () => {
    const source = fs.readFileSync(path.join(root, 'src/custom-component/v-query', file), 'utf8')
    const start = source.indexOf('const relativeToCurrentListRange = computed(')
    const expression = transformSync(source.slice(start, source.indexOf('\n})', start) + 3), {
      loader: 'ts'
    }).code
    for (const granularity of ['year', 'month', 'date', 'datetime']) {
      const value = { timeGranularityMultiple: granularity + 'range' }
      const options = vm
        .runInNewContext(expression + ';relativeToCurrentListRange', {
          computed: fn => fn(),
          curComponent: { value },
          timeRange: { value: {} },
          props: { timeGranularity: granularity, timeGranularityMultiple: granularity + 'range' },
          t: key => key
        })
        .map(item => item.value)
      assert.equal(options.includes('thisQuarter'), granularity !== 'year')
      assert.equal(options.includes('lastQuarter'), granularity !== 'year')
      assert.equal(options.filter(v => v === 'thisQuarter').length, granularity === 'year' ? 0 : 1)
      assert.ok(options.includes('custom'))
      if (granularity === 'month') assert.ok(options.includes('LastThreeMonths'))
    }
  })
}
test('报告摘要显示季度名称，规则持久化后仍可计算', () => {
  const source = fs.readFileSync(
    path.join(root, 'src/views/menu/system/report/form/ConditionDefaultSet.vue'),
    'utf8'
  )
  const start = source.indexOf('const toDisplayText =')
  const code = transformSync(source.slice(start, source.indexOf('const getValue =', start)), {
    loader: 'ts'
  }).code
  for (const rule of ['thisQuarter', 'lastQuarter']) {
    const lines = vm.runInNewContext(code + ';getDisplayLines(item)', {
      item: { displayType: '7', timeType: 'dynamic', relativeToCurrentRange: rule },
      getValue: () => 'nonempty',
      isCustomClockDefault: () => false,
      t: key => key
    })
    assert.equal(
      lines[1].text,
      rule === 'thisQuarter' ? 'dynamic_time.tquarter' : 'dynamic_time.last_quarter'
    )
  }
})

test('实际查询参数覆盖季度边界、跨季度首次打开重算，普通查询保留用户值', t => {
  const store = path.join(dir, 'store.js')
  fs.writeFileSync(store, 'exports.dvMainStoreWithOut=()=>({});exports.storeToRefs=()=>({});')
  const { searchQuery } = bundle('src/hooks/web/useFilter.ts', 'filter', {
    '@': path.join(root, 'src'),
    '@/store/modules/data-visualization/dvMain': store,
    pinia: store
  })
  t.mock.timers.enable({ apis: ['Date'], now: new Date(2026, 9, 30, 15, 25, 53) })
  for (const grain of ['monthrange', 'daterange', 'datetimerange']) {
    const item = {
      id: 'time',
      displayType: '7',
      timeType: 'dynamic',
      defaultValueCheck: true,
      timeGranularity: 'datetime',
      timeGranularityMultiple: grain,
      relativeToCurrentRange: 'lastQuarter',
      checkedFields: ['chart'],
      checkedFieldsMap: { chart: 'field' }
    }
    const query = first => {
      const filters = []
      searchQuery([{ id: 'query', propValue: [item] }], filters, 'chart', first)
      return filters[0].value.map(value => Number(value))
    }
    const values = query(true)
    assert.deepEqual(
      values.map(value => local(new Date(value))),
      ['2026-07-01 00:00:00', '2026-09-30 23:59:59']
    )
    const within = value => +new Date(value) >= values[0] && +new Date(value) <= values[1]
    assert.ok(within('2026-07-01T00:00:00'))
    assert.ok(within('2026-09-30T23:59:59'))
    assert.equal(within('2026-06-30T23:59:59'), false)
    assert.equal(within('2026-10-01T00:00:00'), false)
    item.selectValue = [new Date(2026, 7, 1), new Date(2026, 7, 2)]
    assert.notDeepEqual(query(false), values)
    t.mock.timers.setTime(+new Date(2027, 0, 1, 1))
    assert.deepEqual(
      query(true).map(value => local(new Date(value))),
      ['2026-10-01 00:00:00', '2026-12-31 23:59:59']
    )
    t.mock.timers.setTime(+new Date(2026, 9, 30, 15, 25, 53))
  }
})

test('实际保存区间校验和日期禁用使用同一季度规则，固定限制仍生效', () => {
  const source = fs.readFileSync(
    path.join(root, 'src/custom-component/v-query/QueryConditionConfiguration.vue'),
    'utf8'
  )
  const a = source.indexOf('const isInRange =')
  const code = transformSync(source.slice(a, source.indexOf('const CascadeDialog', a)), {
    loader: 'ts'
  }).code
  const config = {
    timeGranularityMultiple: 'datetimerange',
    timeRange: {
      intervalType: 'timeInterval',
      regularOrTrends: 'dynamic',
      relativeToCurrentRange: 'lastQuarter'
    }
  }
  const context = {
    getCustomRange: rule => getCustomRange(rule, new Date(2026, 9, 30)),
    dayjs: require('dayjs')
  }
  const invalid = (start, end) =>
    vm.runInNewContext(code + ';isInRange(config,start,end)', {
      ...context,
      config,
      start: +new Date(start),
      end: +new Date(end)
    })
  assert.equal(invalid('2026-07-01', '2026-09-30T23:59:59'), false)
  assert.equal(invalid('2026-06-30', '2026-09-30'), true)
  assert.equal(invalid('2026-07-01', '2026-10-01'), true)
  config.timeRange = {
    intervalType: 'end',
    regularOrTrends: 'fixed',
    regularOrTrendsValue: new Date(2026, 8, 1)
  }
  assert.equal(invalid('2026-07-01', '2026-09-30'), true)
  const time = fs.readFileSync(path.join(root, 'src/custom-component/v-query/Time.vue'), 'utf8')
  const b = time.indexOf('const disabledDate =')
  const disabledCode = transformSync(time.slice(b, time.indexOf('const showPopup =', b)), {
    loader: 'ts'
  }).code
  config.setTimeRange = true
  config.timeRange = {
    intervalType: 'timeInterval',
    regularOrTrends: 'dynamic',
    relativeToCurrentRange: 'lastQuarter'
  }
  for (const [date, expected] of [
    ['2026-06-30', true],
    ['2026-07-01', false],
    ['2026-09-30', false],
    ['2026-10-01', true]
  ]) {
    assert.equal(
      vm.runInNewContext(disabledCode + ';disabledDate(date)', {
        ...context,
        config: { value: config },
        date: new Date(date),
        startWindowTime: { value: 0 }
      }),
      expected
    )
  }
})

test('跨季度重置按当前时刻恢复规则；最近三个月仍是滚动口径', t => {
  t.mock.timers.enable({ apis: ['Date'], now: new Date(2027, 0, 1, 12) })
  const source = fs.readFileSync(
    path.join(root, 'src/custom-component/v-query/Component.vue'),
    'utf8'
  )
  const start = source.indexOf('const resetData = () =>')
  const code = source.slice(start, source.indexOf('const clearData =', start))
  const item = {
    displayType: '7',
    timeType: 'dynamic',
    defaultValueCheck: true,
    timeGranularityMultiple: 'datetimerange',
    relativeToCurrentRange: 'lastQuarter',
    defaultValue: [new Date(2000, 0, 1), new Date(2000, 0, 2)]
  }
  const element = { value: { propValue: [] } }
  vm.runInNewContext(code + ';resetData()', {
    Date,
    element,
    props: { element: { cascade: [] } },
    nextTick: fn => fn(),
    cloneDeep: structuredClone,
    oldList: [item],
    cascadeResetState: { value: false },
    componentWithSure: { value: true },
    getKeyList: () => [],
    evaluateRuntimeDynamicDefault
  })
  assert.deepEqual(Array.from(element.value.propValue[0].selectValue, local), [
    '2026-10-01 00:00:00',
    '2026-12-31 23:59:59'
  ])
  t.mock.timers.setTime(+new Date(2026, 9, 30, 12))
  assert.deepEqual(getCustomRange('LastThreeMonths').map(local), [
    '2026-08-01 00:00:00',
    '2026-10-30 23:59:59'
  ])
})
