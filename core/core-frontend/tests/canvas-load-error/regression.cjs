/* eslint-disable @typescript-eslint/no-var-requires */
const { test } = require('node:test')
const assert = require('node:assert/strict')
const fs = require('node:fs')
const vm = require('node:vm')
const { transformSync } = require('esbuild')
for (const file of ['data-visualization/PreviewShow.vue', 'dashboard/DashboardPreviewShow.vue']) {
  const source = fs.readFileSync('src/views/' + file, 'utf8')
  const start = source.indexOf('const loadCanvasData =')
  const code = transformSync(source.slice(start, source.indexOf('\nconst ', start + 10)), {
    loader: 'ts'
  }).code
  const fixture = () => {
    const pending = []
    const init = (id, params, callback) => {
      assert.equal(params.awaitLoad, true)
      return new Promise((resolve, reject) =>
        pending.push({
          resolve: () => {
            callback({ dvInfo: {}, canvasStyleResult: {}, canvasDataResult: [] })
            resolve()
          },
          reject
        })
      )
    }
    const context = {
      props: { showPosition: 'preview' },
      showPosition: { value: 'preview' },
      dataInitState: { value: true },
      loadError: { value: false },
      state: {},
      dvMainStore: { updateCurDvInfo: () => undefined },
      initCanvasData: init,
      initCanvasDataPrepare: init,
      nextTick: () => undefined
    }
    vm.runInNewContext('let loadSequence = 0;' + code + ';this.load = loadCanvasData', context)
    return { context, pending }
  }
  const flush = () => new Promise(resolve => setImmediate(resolve))
  test(file + ' 非法JSON结束加载且显示错误，重开成功恢复', async () => {
    const { context: c, pending } = fixture()
    c.load('a')
    assert.equal(c.dataInitState.value, false)
    pending[0].reject(new SyntaxError('not-json'))
    await flush()
    assert.equal(c.loadError.value, true)
    assert.equal(c.dataInitState.value, true)
    c.load('a')
    assert.equal(c.loadError.value, false)
    pending[1].resolve()
    await flush()
    assert.equal(c.loadError.value, false)
    assert.equal(c.dataInitState.value, true)
  })
  test(file + ' 旧请求失败不覆盖新资源成功状态', async () => {
    const { context: c, pending } = fixture()
    c.load('a')
    c.load('b')
    pending[1].resolve()
    await flush()
    pending[0].reject(new Error('old'))
    await flush()
    assert.equal(c.loadError.value, false)
    assert.equal(c.dataInitState.value, true)
  })
  test(file + ' 请求失败不提前结束新请求的加载', async () => {
    const { context: c, pending } = fixture()
    c.load('a')
    c.load('b')
    pending[0].reject(new Error('network'))
    await flush()
    assert.equal(c.dataInitState.value, false)
    pending[1].resolve()
    await flush()
    assert.equal(c.dataInitState.value, true)
  })
}
