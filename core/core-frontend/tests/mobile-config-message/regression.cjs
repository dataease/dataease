/* eslint-disable @typescript-eslint/no-var-requires -- Node CommonJS 测试入口使用 require。 */
const assert = require('node:assert/strict')
const vm = require('node:vm')
const { test } = require('node:test')
const { compileScript, compileTemplate } = require('@vue/compiler-sfc')
const { fixtureScript, filename, descriptor } = require('./harness.cjs')

const setup = (baseUrl = '') => {
  const replies = []
  const listeners = new Map()
  const source = { postMessage: (...args) => replies.push(args) }
  const context = {
    URL,
    window: {
      innerHeight: 1200,
      addEventListener: (name, fn) => listeners.set(name, fn),
      removeEventListener: (name, fn) => {
        if (listeners.get(name) === fn) listeners.delete(name)
      }
    },
    document: {
      baseURI: 'https://editor.example/app/',
      querySelector: () => assert.fail('must use panel iframe ref')
    }
  }
  vm.runInNewContext(fixtureScript(baseUrl), context)
  const fixture = context.fixture
  fixture.newWindow.value = { contentWindow: source }
  fixture.mounted.forEach(fn => fn())
  const event = data => ({ source, origin: fixture.origin.value, data })
  return { ...fixture, event, replies, listeners, source }
}

test('同源和跨域嵌入的真实面板 iframe 可初始化并接收精确 origin 的消息', () => {
  for (const baseUrl of ['', 'https://bi.example:8443/app/']) {
    const f = setup(baseUrl)
    f.handler(f.event({ type: 'panelInit' }))
    assert.equal(f.replies[0][0].type, 'panelInit')
    assert.equal(f.replies[0][1], baseUrl ? 'https://bi.example:8443' : 'https://editor.example')
    f.handler(f.event({ msgOrigin: 'de-fit2cloud' }))
    assert.equal(f.replies.length, baseUrl ? 2 : 1)
    f.mobileStatusChange('mobileSave', undefined)
    assert.equal(f.replies.at(-1)[1], f.origin.value)
  }
})

test('所有配置操作和握手在 origin 或 source 不匹配时均无副作用', () => {
  const f = setup('https://bi.example:8443/app/')
  const payloads = [
    { msgOrigin: 'de-fit2cloud' },
    { type: 'panelInit' },
    ...[
      'curComponentChange',
      'delFromMobile',
      'syncPcDesign',
      'mobileSaveFromMobile',
      'mobilePatchFromMobile'
    ].map(type => ({ type, value: 'chart' }))
  ]
  for (const data of payloads) {
    for (const change of [
      { origin: 'https://evil.example' },
      { origin: 'null' },
      { origin: '' },
      { origin: 'https://bi.example.evil:8443' },
      { origin: 'http://bi.example:8443' },
      { origin: 'https://bi.example' },
      { source: {} },
      { source: null }
    ])
      f.handler({ ...f.event(data), ...change })
  }
  assert.equal(f.replies.length, 0)
  assert.equal(f.events.length, 0)
  assert.equal(f.state.componentData.value[0].inMobile, true)
})

test('缺失 iframe、非法 URL、opaque origin 和非对象消息被拒绝', () => {
  for (const base of ['data:text/html,', 'javascript:', 'file:///tmp/', 'http://[']) {
    const f = setup(base)
    f.handler({ source: f.source, origin: 'null', data: { msgOrigin: 'de-fit2cloud' } })
    f.mobileStatusChange('mobileSave', undefined)
    assert.equal(f.replies.length, 0)
  }
  const f = setup()
  for (const value of [null, undefined, '', 'text', 42, [], true]) f.handler(f.event(value))
  f.newWindow.value = null
  f.handler(f.event({ type: 'panelInit' }))
  assert.equal(f.replies.length, 0)
})

test('合法窗口仍能删除组件、同步设计、保存布局及切回 PC，卸载移除监听器', () => {
  const f = setup()
  f.handler(f.event({ type: 'panelInit' }))
  f.handler(f.event({ type: 'curComponentChange', value: { id: 'chart' } }))
  f.handler(f.event({ type: 'syncPcDesign', value: 'chart' }))
  f.handler(f.event({ type: 'delFromMobile', value: 'chart' }))
  assert.equal(f.state.componentData.value[0].inMobile, false)
  const value = { chart: { x: 2, y: 3, sizeX: 4, sizeY: 5, style: {} } }
  f.handler(f.event({ type: 'mobileSaveFromMobile', value }))
  f.handler(f.event({ type: 'mobilePatchFromMobile', value }))
  assert.equal(f.state.componentData.value[0].mx, 2)
  assert.deepEqual(Array.from(f.events), ['select', 'sync', 'save', 'pcMode'])
  f.unmounted.forEach(fn => fn())
  assert.equal(f.listeners.size, 0)
})

test('生产 SFC 脚本和模板可编译', () => {
  const compiled = compileScript(descriptor, { id: 'mobile-config-message' })
  const result = compileTemplate({
    source: descriptor.template.content,
    filename,
    id: 'mobile-config-message',
    compilerOptions: { bindingMetadata: compiled.bindings }
  })
  assert.deepEqual(result.errors, [])
})
