/* eslint-disable @typescript-eslint/no-var-requires */
const { test, before } = require('node:test')
const assert = require('node:assert/strict')
const fs = require('node:fs')
const vm = require('node:vm')
const { transformSync } = require('esbuild')
let debounce
before(async () => {
  debounce = (await import('lodash-es')).debounce
})
const source = fs.readFileSync('src/views/pages/visualized/data/dataset/index.vue', 'utf8')
const selection = source.slice(
  source.indexOf('const selectionLoading ='),
  source.indexOf('const exportDataset =')
)
const tabs = source.slice(
  source.indexOf('const handleClick ='),
  source.indexOf('const relationChartRef =')
)
const code = transformSync(
  selection +
    tabs +
    ';this.click = handleNodeClick; this.tab = handleClick;this.loading = selectionLoading',
  { loader: 'tsx' }
).code
const settle = () => new Promise(resolve => setImmediate(resolve))
const wait = () => new Promise(resolve => setTimeout(resolve, 180))
const fixture = () => {
  const requests = { info: [], preview: [], total: [] }
  const request = name => (id, signal) =>
    new Promise((resolve, reject) => requests[name].push({ id, signal, resolve, reject }))
  let dispose
  const c = {
    ref: value => ({ value }),
    defaultNode: { id: '', name: '', weight: 0 },
    debounce,
    AbortController,
    nodeInfo: {},
    activeName: { value: 'dataPreview' },
    dataPreviewLoading: { value: false },
    total: { value: null },
    columns: { value: [] },
    tableData: { value: [] },
    datasetTableFiled: { value: [] },
    allFields: [],
    columnsPreview: [],
    dataPreview: [],
    allFieldsColumns: ['structure'],
    generateColumns: fields => fields,
    barInfoApi: request('info'),
    getDatasetPreview: request('preview'),
    getDatasetTotal: request('total'),
    onBeforeUnmount: fn => {
      dispose = fn
    },
    datasetListTree: { value: { setCurrentKey: () => undefined } }
  }
  vm.runInNewContext(code, c)
  return { c, requests, dispose: () => dispose() }
}
const leaf = id => ({ id, leaf: true, weight: 7 })
const response = id => ({
  allFields: [id + '-field'],
  data: { fields: [id + '-column'], data: [id + '-row'] }
})
test('连续快速点击只加载最后一项详情和预览', async () => {
  const { c, requests: r, dispose } = fixture()
  for (const id of ['a', 'b', 'c']) c.click(leaf(id))
  await wait()
  assert.deepEqual(
    r.info.map(v => v.id),
    ['c']
  )
  r.info[0].resolve({ id: 'c' })
  await settle()
  assert.deepEqual(
    r.preview.map(v => v.id),
    ['c']
  )
  assert.deepEqual(
    r.total.map(v => v.id),
    ['c']
  )
  dispose()
})
test('旧详情晚到不发起过期预览', async () => {
  const { c, requests: r, dispose } = fixture()
  c.click(leaf('a'))
  await wait()
  c.click(leaf('b'))
  await wait()
  assert.equal(r.info[0].signal.aborted, true)
  r.info[1].resolve({ id: 'b' })
  await settle()
  r.info[0].resolve({ id: 'a' })
  await settle()
  assert.equal(c.nodeInfo.id, 'b')
  assert.deepEqual(
    r.preview.map(v => v.id),
    ['b']
  )
  dispose()
})
test('旧预览、总数和finally不能覆盖当前数据或结束当前加载', async () => {
  const { c, requests: r, dispose } = fixture()
  c.click(leaf('a'))
  await wait()
  r.info[0].resolve({ id: 'a' })
  await settle()
  c.click(leaf('b'))
  await wait()
  r.info[1].resolve({ id: 'b' })
  await settle()
  assert.equal(r.preview[0].signal.aborted, true)
  r.preview[0].resolve(response('a'))
  r.total[0].resolve(100)
  await settle()
  assert.equal(c.dataPreviewLoading.value, true)
  assert.equal(c.total.value, null)
  r.preview[1].resolve(response('b'))
  r.total[1].resolve(20)
  await settle()
  assert.deepEqual(c.tableData.value, ['b-row'])
  assert.equal(c.total.value, 20)
  dispose()
})
test('切换结构页签不被晚到预览覆盖；空字段预览也缓存', async () => {
  const { c, requests: r, dispose } = fixture()
  c.click(leaf('a'))
  await wait()
  r.info[0].resolve({ id: 'a' })
  await settle()
  c.tab('dataPreview')
  assert.equal(r.preview.length, 1)
  c.activeName.value = 'structPreview'
  c.tab('structPreview')
  r.preview[0].resolve(response('a'))
  await settle()
  assert.deepEqual(c.columns.value, ['structure'])
  assert.deepEqual(c.tableData.value, ['a-field'])
  c.activeName.value = 'dataPreview'
  c.tab('dataPreview')
  assert.equal(r.preview.length, 1)
  dispose()
})
test('卸载取消定时加载和已有请求', async () => {
  const { c, requests: r, dispose } = fixture()
  c.click(leaf('a'))
  dispose()
  await wait()
  assert.equal(r.info.length, 0)
})

test('空字段结果可缓存，预览失败后允许重新加载', async () => {
  const { c, requests: r, dispose } = fixture()
  c.click(leaf('a'))
  await wait()
  r.info[0].resolve({ id: 'a' })
  await settle()
  r.preview[0].reject(new Error('request failed'))
  await settle()
  assert.equal(c.dataPreviewLoading.value, false)
  assert.equal(c.nodeInfo.id, 'a')
  c.tab('dataPreview')
  assert.equal(r.preview.length, 2)
  r.preview[1].resolve({ allFields: [], data: { fields: [], data: [] } })
  await settle()
  c.tab('structPreview')
  c.tab('dataPreview')
  assert.equal(r.preview.length, 2)
  dispose()
})

test('点击瞬间清除旧ID、字段、表格和总数，等待详情时不允许页签取旧数据', async () => {
  const { c, requests: r, dispose } = fixture()
  c.click(leaf('a'))
  await wait()
  r.info[0].resolve({ id: 'a', name: 'A' })
  await settle()
  r.preview[0].resolve(response('a'))
  r.total[0].resolve(100)
  await settle()
  c.click(leaf('b'))
  assert.equal(c.nodeInfo.id, '')
  assert.equal(c.nodeInfo.name, '')
  assert.equal(c.nodeInfo.weight, 0)
  assert.equal(c.loading.value, true)
  assert.equal(c.total.value, null)
  assert.equal(c.tableData.value.length, 0)
  assert.equal(c.columns.value.length, 0)
  assert.equal(c.datasetTableFiled.value.length, 0)
  c.tab('structPreview')
  c.tab('dataPreview')
  assert.equal(c.tableData.value.length, 0)
  assert.equal(r.preview.length, 1)
  dispose()
})

test('最新详情失败结束加载并保留原有未选择状态，重新选择后可恢复', async () => {
  const { c, requests: r, dispose } = fixture()
  c.click(leaf('a'))
  await wait()
  r.info[0].reject(new Error('denied'))
  await settle()
  assert.equal(c.loading.value, false)
  assert.equal(c.dataPreviewLoading.value, false)
  assert.equal(c.nodeInfo.id, '')
  c.click(leaf('b'))
  await wait()
  r.info[1].resolve({ id: 'b' })
  await settle()
  assert.equal(c.loading.value, false)
  assert.equal(c.nodeInfo.id, 'b')
  assert.equal(r.preview[0].id, 'b')
  dispose()
})

test('旧详情失败不能结束最新选择的加载', async () => {
  const { c, requests: r, dispose } = fixture()
  c.click(leaf('a'))
  await wait()
  c.click(leaf('b'))
  r.info[0].reject(new Error('aborted'))
  await settle()
  assert.equal(c.loading.value, true)
  assert.equal(c.dataPreviewLoading.value, true)
  dispose()
})
