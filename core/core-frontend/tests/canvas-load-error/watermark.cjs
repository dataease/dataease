/* eslint-disable @typescript-eslint/no-var-requires */
const { test } = require('node:test')
const assert = require('node:assert/strict')
const fs = require('node:fs')
const vm = require('node:vm')
const { transformSync } = require('esbuild')
const source = fs.readFileSync('src/utils/canvasWatermark.ts', 'utf8')
const moduleInfo = { exports: {} }
const warnings = []
vm.runInNewContext(transformSync(source, { loader: 'ts', format: 'cjs' }).code, {
  module: moduleInfo,
  console: { warn: text => warnings.push(text) }
})
const normalize = moduleInfo.exports.normalizeCanvasWatermark
for (const value of ['not-json', '{', 'null', '[]', 'true', '42', '"text"']) {
  test('非法水印安全跳过: ' + value, () => {
    const raw = { id: 'watermark', settingContent: value }
    assert.equal(normalize(raw), null)
    assert.equal(raw.settingContent, value)
    assert.ok(!warnings.at(-1).includes(value))
  })
}
test('正常水印保留元数据、配置对象和空配置兼容', () => {
  const raw = { id: 'watermark', settingContent: '{"enable":true}' }
  const result = normalize(raw)
  assert.equal(result.id, raw.id)
  assert.equal(result.settingContent.enable, true)
  assert.equal(raw.settingContent, '{"enable":true}')
  assert.equal(normalize({ settingContent: { enable: true } }).settingContent.enable, true)
  assert.equal(Object.keys(normalize({ settingContent: '' }).settingContent).length, 0)
  assert.equal(normalize(null), null)
})
test('实际画布初始化：坏水印仍回调，坏组件JSON继续拒绝', async () => {
  const canvasSource = fs.readFileSync('src/utils/canvasUtils.ts', 'utf8')
  const start = canvasSource.indexOf('export function initCanvasDataPrepare(')
  const code = transformSync(
    canvasSource.slice(start, canvasSource.indexOf('export async function initCanvasData(', start)),
    { loader: 'ts', format: 'cjs' }
  ).code
  const mod = { exports: {} }
  const response = {
    id: 'screen',
    type: 'dataV',
    componentData: '[]',
    canvasStyleData: '{}',
    watermarkInfo: { settingContent: 'not-json' }
  }
  vm.runInNewContext(code, {
    module: mod,
    normalizeCanvasWatermark: normalize,
    dvMainStore: {},
    findById: async () => ({ data: response }),
    findCopyResource: null,
    historyAdaptor: () => undefined,
    useAppearanceStoreWithOut: () => ({ setCurrentFont: () => undefined }),
    document: { documentElement: { style: { setProperty: () => undefined } } }
  })
  let callbackCount = 0
  await mod.exports.initCanvasDataPrepare('screen', {}, data => {
    callbackCount++
    assert.equal(data.dvInfo.watermarkInfo, null)
    assert.equal(data.canvasDataResult.length, 0)
  })
  assert.equal(callbackCount, 1)
  assert.equal(response.watermarkInfo.settingContent, 'not-json')
  response.componentData = 'not-json'
  await assert.rejects(mod.exports.initCanvasDataPrepare('screen', {}, () => callbackCount++))
  assert.equal(callbackCount, 1)
})
