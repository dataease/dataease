// Run: node --test tests/embedded-open-origin/regression.cjs
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const vm = require('node:vm')
const { test } = require('node:test')
const ts = require('typescript')
const { parse, compileScript, compileTemplate } = require('@vue/compiler-sfc')

const filename = path.resolve(
  __dirname,
  '../../src/views/component/embedded-iframe/OpenHandler.vue'
)
const { descriptor } = parse(fs.readFileSync(filename, 'utf8'), { filename })
const ast = ts.createSourceFile(filename + '.ts', descriptor.scriptSetup.content, ts.ScriptTarget.Latest)
const code = ts.transpileModule(
  ast.statements.filter(node => !ts.isImportDeclaration(node)).map(node => node.getText(ast)).join('\n'),
  { compilerOptions: { target: ts.ScriptTarget.ES2020, module: ts.ModuleKind.CommonJS } }
).outputText

const setup = ({ iframe = false, baseUrl = 'https://bi.example/app/', blocked = false } = {}) => {
  const responses = []
  const opened = []
  const listeners = new Map()
  const mounted = []
  const unmounted = []
  const store = {
    getToken: 'test-only-token',
    getBaseUrl: baseUrl,
    getIframeData: { embeddedToken: 'test-only-token' },
    getIsIframe: iframe
  }
  const app = { getIsIframe: iframe, getIsDataEaseBi: !iframe, getIsApp: true }
  const window = {
    location: { origin: 'https://host.example', href: 'https://host.example/app/' },
    open: url => {
      const result = blocked ? null : { postMessage: (...args) => responses.push(args) }
      opened.push({ url, result })
      return result
    },
    btoa: value => Buffer.from(value).toString('base64'),
    addEventListener: (name, callback) => listeners.set(name, callback),
    removeEventListener: (name, callback) => {
      if (listeners.get(name) === callback) listeners.delete(name)
    }
  }
  let exposed
  const context = {
    window,
    URL,
    useEmbedded: () => store,
    useAppStoreWithOut: () => app,
    shallowRef: value => ({ value }),
    onMounted: callback => mounted.push(callback),
    onUnmounted: callback => unmounted.push(callback),
    defineExpose: methods => (exposed = methods),
    interactive() {}
  }
  vm.runInNewContext(code, context)
  mounted.forEach(callback => callback())
  const register = () => {
    const target = window.open(iframe ? '/preview' : 'https://bi.example/app/#/preview', '_blank')
    exposed.initOpenHandler(target)
    return {
      source: iframe ? target : target.proxyWindow,
      origin: iframe ? window.location.origin : 'https://bi.example',
      data: { msgOrigin: 'de-inner-fit2cloud', ready: true, uuid: target?.uuid }
    }
  }
  return { register, responses, opened, listeners, store, window, exposed, unmounted }
}

test('real iframe window handshake sends credentials only to its exact origin', () => {
  const fixture = setup({ iframe: true })
  const event = fixture.register()
  fixture.listeners.get('message')(event)
  assert.equal(fixture.responses.length, 1)
  assert.equal(fixture.responses[0][1], 'https://host.example')
  assert.equal(fixture.responses[0][0].uuid, event.data.uuid)
})

test('DIV embed handshake validates the DataEase base origin rather than host origin', () => {
  const fixture = setup()
  const event = fixture.register()
  fixture.listeners.get('message')(event)
  assert.equal(fixture.responses.length, 1)
  assert.equal(fixture.responses[0][1], 'https://bi.example')
  assert.equal(fixture.responses[0][0].baseUrl, fixture.store.getBaseUrl)
})

test('rejects forged source, UUID, origin and malformed messages in both modes', () => {
  for (const iframe of [true, false]) {
    const fixture = setup({ iframe })
    const event = fixture.register()
    for (const change of [
      { source: { postMessage() { throw new Error('must not send') } } },
      { source: null },
      { origin: 'https://evil.example' },
      { origin: 'null' },
      { origin: '' },
      { data: null },
      { data: 'invalid' },
      { data: { ...event.data, uuid: -1 } },
      { data: { ...event.data, ready: 'true' } },
      { data: { ...event.data, msgOrigin: 'other' } }
    ]) {
      fixture.listeners.get('message')({ ...event, ...change })
    }
    assert.equal(fixture.responses.length, 0)
    assert.equal(fixture.store.getIframeData['de-inner-embedded'], undefined)
  }
})

test('redirecting a registered window to another origin cannot receive credentials', () => {
  const fixture = setup()
  const event = fixture.register()
  fixture.listeners.get('message')({ ...event, origin: 'https://bi.example.evil.example' })
  assert.equal(fixture.responses.length, 0)
})

test('URL matching does not trust a host contained inside the base URL path', () => {
  const fixture = setup({ baseUrl: 'https://bi.example/path/https://evil.example' })
  const target = fixture.window.open('https://evil.example/preview', '_blank')
  fixture.exposed.initOpenHandler(target)
  assert.equal(new URL(fixture.opened[0].url).searchParams.has('de-embedded-window-args'), false)
  fixture.listeners.get('message')({
    source: target.proxyWindow,
    origin: 'https://evil.example',
    data: { msgOrigin: 'de-inner-fit2cloud', ready: true, uuid: target.uuid }
  })
  assert.equal(fixture.responses.length, 0)
})

test('missing, malformed or opaque base URL fails closed in DIV mode', () => {
  for (const baseUrl of ['', 'http://[', 'data:text/html,example']) {
    const fixture = setup({ baseUrl })
    const event = fixture.register()
    fixture.listeners.get('message')(event)
    assert.equal(fixture.responses.length, 0)
  }
})

test('blocked popups are ignored and revoked tokens do not produce responses', () => {
  const blocked = setup({ blocked: true })
  assert.doesNotThrow(() => blocked.register())
  blocked.exposed.initOpenHandler(null)
  const fixture = setup()
  const event = fixture.register()
  fixture.store.getToken = ''
  fixture.listeners.get('message')(event)
  assert.equal(fixture.responses.length, 0)
  fixture.unmounted.forEach(callback => callback())
  assert.equal(fixture.listeners.has('message'), false)
})

test('production SFC script and template compile', () => {
  const script = compileScript(descriptor, { id: 'open-origin' })
  const template = compileTemplate({
    source: descriptor.template.content,
    filename,
    id: 'open-origin',
    compilerOptions: { bindingMetadata: script.bindings }
  })
  assert.deepEqual(template.errors, [])
})
