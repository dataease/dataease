const { test } = require('node:test')
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const vm = require('node:vm')
const source = fs.readFileSync(
  path.join(__dirname, '../../src/views/chart/components/views/index.vue'),
  'utf8'
)
const methods = source.slice(
  source.indexOf('const calcData = '),
  source.indexOf('const showChartView = ')
)

const setup = (plugin = false) => {
  const calls = [],
    saved = [],
    tasks = []
  const context = {
    dvMainStore: { setLastViewRequestInfo: (...args) => saved.push(args) },
    cloneDeep: value => structuredClone(value),
    viewportLoad: { run: task => tasks.splice(0, tasks.length, task) },
    viewportLoadEnabled: () => true,
    view: { value: { isPlugin: plugin } },
    loadPlugin: { value: false },
    loading: { value: false },
    isAutoRefreshing: { value: false },
    chartComponent: {
      value: { calcData: (...args) => calls.push(args), invokeMethod: data => calls.push(data) }
    }
  }
  vm.runInNewContext(methods + ';this.calcData=calcData', context)
  return { ...context, calls, saved, activate: () => tasks.pop()?.() }
}

test('屏外保留已确认的参数快照，连续查询仅执行最后一次', () => {
  const state = setup()
  const request = { id: 'view', chartExtRequest: { value: 'A' } }
  state.calcData(request)
  request.chartExtRequest.value = 'B'
  state.calcData(request)
  request.chartExtRequest.value = 'not-confirmed'
  assert.equal(state.calls.length, 0)
  state.activate()
  assert.equal(state.calls.length, 1)
  assert.equal(state.calls[0][0].chartExtRequest.value, 'B')
  assert.equal(state.loading.value, true)
  state.calls[0][1]()
  assert.equal(state.loading.value, false)
})
test('插件后续查询沿用原有方法和完成回调契约', () => {
  const state = setup(true)
  state.calcData({ id: 'plugin', chartExtRequest: { value: 'B' } })
  state.activate()
  assert.equal(state.calls[0].methodName, 'calcData')
  assert.equal(state.calls[0].args[0].chartExtRequest.value, 'B')
  state.calls[0].args[1]()
  assert.equal(state.loading.value, false)
})
test('定时刷新保留刷新来源和原生回调', () => {
  const state = setup()
  state.calcData({ id: 'view', chartExtRequest: {} }, true)
  state.activate()
  assert.equal(state.isAutoRefreshing.value, true)
  assert.equal(state.calls[0][2], true)
})

test('插件、移动端、大屏和编辑态不启用桌面原生图表懒加载', () => {
  const enabledSource = source.slice(
    source.indexOf('const viewportLoadEnabled ='),
    source.indexOf('const viewportLoad =')
  )
  const state = {
    isMobile: () => false,
    dvMainStore: { inMobile: false, mobileInPc: false },
    dvInfo: { value: { type: 'dashboard' } },
    view: { value: {} },
    showPosition: { value: 'preview' }
  }
  vm.runInNewContext(enabledSource + ';this.enabled=viewportLoadEnabled', state)
  assert.equal(state.enabled(), true)
  state.view.value.isPlugin = true
  assert.equal(state.enabled(), false)
  state.view.value = { plugin: { isPlugin: true } }
  assert.equal(state.enabled(), false)
  state.view.value = {}
  state.dvInfo.value.type = 'dataV'
  assert.equal(state.enabled(), false)
  state.dvInfo.value.type = 'dashboard'
  state.showPosition.value = 'canvas'
  assert.equal(state.enabled(), false)
  state.showPosition.value = 'preview'
  state.dvMainStore.inMobile = true
  assert.equal(state.enabled(), false)
})

const templateExportSource = fs.readFileSync(
  path.join(__dirname, '../../src/utils/imgUtils.ts'),
  'utf8'
)
const templateExportMethod = templateExportSource.slice(
  templateExportSource.indexOf('export async function download2AppTemplate'),
  templateExportSource.indexOf('export function downloadCanvas(')
)
const ts = require('typescript')
const templateExportJS = ts.transpileModule(templateExportMethod.replace('export async', 'async'), {
  compilerOptions: { target: ts.ScriptTarget.ES2022 }
}).outputText

test('模板导出先补齐屏外数据，再校验并生成快照和数据包', async () => {
  let saved,
    completed = 0
  const state = {
    prepareViewportChartsForExport: async () => {
      state.canvasViewDataInfo.value.view = { rows: ['loaded'] }
    },
    canvasViewInfo: { value: { view: { tableId: 'dataset', type: 'bar' } } },
    canvasViewDataInfo: { value: {} },
    deepCopy: value => structuredClone(value),
    prePareTemplateBaseData: (_, callback) =>
      callback({ canvasDataResult: '[]', canvasStyleResult: '{}' }),
    dvInfo: { value: { id: 'dashboard', type: 'dashboard' } },
    useAppStoreWithOut: () => ({ getXpackValid: false }),
    findStaticSource: callback => {
      callback({})
      return Promise.resolve()
    },
    withMapPictures: (_, render) => render(),
    waitForResourceImages: async () => {},
    domToPng: async () => 'snapshot',
    window: { devicePixelRatio: 1 },
    remapMaterialDocument: value => value,
    Blob,
    FileSaver: {
      saveAs: blob => {
        saved = blob
      }
    },
    useI18n: () => ({ t: key => key }),
    ElMessage: { error: value => assert.fail(value), warning: value => assert.fail(value) },
    console
  }
  vm.runInNewContext(templateExportJS + ';this.exportTemplate=download2AppTemplate', state)
  await state.exportTemplate(
    'template',
    { offsetWidth: 100, offsetHeight: 100 },
    'name',
    null,
    () => completed++
  )
  const result = JSON.parse(await saved.text())
  assert.deepEqual(JSON.parse(result.dynamicData).view.data, { rows: ['loaded'] })
  assert.equal(result.snapshot, 'snapshot')
  assert.equal(completed, 1)
})

test('补齐数据失败时模板导出明确提示并结束加载', async () => {
  const errors = []
  let completed = 0
  const state = {
    prepareViewportChartsForExport: async () => {
      throw new Error('load failed')
    },
    useI18n: () => ({ t: key => key }),
    ElMessage: { error: value => errors.push(value) },
    console: { error: () => {} }
  }
  vm.runInNewContext(templateExportJS + ';this.exportTemplate=download2AppTemplate', state)
  await state.exportTemplate('template', {}, 'name', null, () => completed++)
  assert.deepEqual(errors, ['material.export_failed'])
  assert.equal(completed, 1)
})
