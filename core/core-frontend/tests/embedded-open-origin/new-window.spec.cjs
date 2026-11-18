/* eslint-disable @typescript-eslint/no-var-requires -- CommonJS test entry */
const { test, expect } = require('../../../../tests/browser/node_modules/@playwright/test')
const fs = require('node:fs')
const path = require('node:path')
const ts = require('typescript')
const { parse, compileScript, compileTemplate } = require('@vue/compiler-sfc')
const filename = path.resolve(
  __dirname,
  '../../src/views/component/embedded-iframe/NewWindowHandler.vue'
)
const { descriptor } = parse(fs.readFileSync(filename, 'utf8'), { filename })
const ast = ts.createSourceFile(
  filename + '.ts',
  descriptor.scriptSetup.content,
  ts.ScriptTarget.Latest
)
const code = ts.transpileModule(
  ast.statements
    .filter(node => !ts.isImportDeclaration(node))
    .map(node => node.getText(ast))
    .join('\n'),
  { compilerOptions: { target: ts.ScriptTarget.ES2020, module: ts.ModuleKind.CommonJS } }
).outputText

// Real windows/postMessage; mock only Vue lifecycle, stores and the validation service.
const fixture = `
const mounted = [], unmounted = [];
window.result = { writes: [], modes: [], loaded: 0, errors: [], checks: [] };
const useEmbedded = () => ({
  setIframeData: data => result.writes.push(data),
  setBaseUrl: url => result.baseUrl = url
});
const useAppStoreWithOut = () => ({
  setIsDataEaseBi: value => result.modes.push('div'),
  setIsIframe: value => result.modes.push('iframe')
});
const useI18n = () => ({ t: key => key });
const ElMessage = { error: message => result.errors.push(message) };
const config = { result_code: 0 };
const ensureDekey = async () => {};
const rsaEncryp = value => value;
const initApi = async (token, origin) => {
  result.checks.push({ token, origin: atob(origin), writesBefore: result.writes.length, baseBefore: result.baseUrl });
  if (window.delayCheck) await new Promise(resolve => window.releaseCheck = resolve);
  if (token === 'network-error') throw new Error('network');
  return { code: token === 'valid' && atob(origin) === 'https://host.test' ? 0 : 50001 };
};
const onMounted = callback => mounted.push(callback);
const onUnmounted = callback => unmounted.push(callback);
const defineEmits = () => () => result.loaded++;
window.dispose = () => unmounted.forEach(callback => callback());
${code}
mounted.forEach(callback => callback());
`

async function openFixture(page, { host = 'https://host.test', div = true } = {}) {
  await page.context().route('**/*', route =>
    route.fulfill({
      contentType: 'text/html',
      body: route.request().url().includes('/child')
        ? '<!doctype html><script>' + fixture + '</script>'
        : '<!doctype html><body>isolated host</body>'
    })
  )
  await page.goto(host)
  const popupPromise = page.waitForEvent('popup')
  await page.evaluate(
    ({ div }) => {
      window.ready = null
      window.addEventListener('message', event => {
        window.ready = event.data
      })
      const params = btoa(
        encodeURIComponent(JSON.stringify({ uuid: 123, name: 'de-new-resource-window' }))
      )
      window.child = window.open(
        'https://bi.test/child' + (div ? '?de-embedded-window-args=' + params : '')
      )
      if (!div) {
        window.child.name = 'de-new-resource-window'
        window.child.uuid = 123
      }
    },
    { div }
  )
  const popup = await popupPromise
  await expect.poll(() => page.evaluate(() => window.ready?.ready)).toBe(true)
  return popup
}
const data = {
  'de-inner-embedded': true,
  uuid: 123,
  embeddedToken: 'valid',
  resourceId: 'resource-1',
  baseUrl: 'https://bi.test/'
}
async function send(page, payload = data) {
  await page.evaluate(payload => window.child.postMessage(payload, 'https://bi.test'), payload)
}

test('DIV accepts configured host, preserves resource/base URL, initializes once', async ({
  page
}) => {
  const child = await openFixture(page)
  await child.evaluate(() => (window.delayCheck = true))
  await send(page)
  await expect.poll(() => child.evaluate(() => result.checks.length)).toBe(1)
  await send(page)
  await child.evaluate(() => window.releaseCheck())
  await expect.poll(() => child.evaluate(() => result.loaded)).toBe(1)
  await send(page, { ...data, resourceId: 'overwrite' })
  const actual = await child.evaluate(() => result)
  expect(actual.checks).toEqual([{ token: 'valid', origin: 'https://host.test', writesBefore: 0 }])
  expect(actual.writes).toEqual([data])
  expect(actual.modes).toEqual(['div'])
  expect(actual.baseUrl).toBe(data.baseUrl)
})

test('iframe same-origin handshake needs no extra server validation', async ({ page }) => {
  const child = await openFixture(page, { host: 'https://bi.test', div: false })
  await send(page)
  await expect.poll(() => child.evaluate(() => result.loaded)).toBe(1)
  expect(await child.evaluate(() => result.checks)).toEqual([])
  expect(await child.evaluate(() => result.modes)).toEqual(['iframe'])
})

test('unrelated window, malformed payload and wrong UUID do not finish handshake', async ({
  page
}) => {
  const child = await openFixture(page)
  for (const payload of [
    null,
    'invalid',
    [],
    { ...data, uuid: 321 },
    { ...data, embeddedToken: '' },
    { ...data, 'de-inner-embedded': 'true' }
  ]) {
    await send(page, payload)
  }
  // Self has a real Window reference and matching UUID, but is not the opener.
  await child.evaluate(payload => window.postMessage(payload, '*'), data)
  expect(await child.evaluate(() => result.loaded)).toBe(0)
  expect(await child.evaluate(() => result.writes)).toEqual([])
  await send(page)
  await expect.poll(() => child.evaluate(() => result.loaded)).toBe(1)
})

for (const scenario of [
  { title: 'unconfigured host', host: 'https://evil.test', token: 'valid' },
  { title: 'invalid token', host: 'https://host.test', token: 'invalid' },
  { title: 'network failure', host: 'https://host.test', token: 'network-error' }
]) {
  test(`${scenario.title} never writes state or signals loaded`, async ({ page }) => {
    const child = await openFixture(page, { host: scenario.host })
    await send(page, { ...data, embeddedToken: scenario.token })
    await expect.poll(() => child.evaluate(() => result.errors.length)).toBe(1)
    expect(await child.evaluate(() => result.loaded)).toBe(0)
    expect(await child.evaluate(() => result.writes)).toEqual([])
    expect(await child.evaluate(() => result.baseUrl)).toBeUndefined()
  })
}

test('unmount during validation prevents late state writes', async ({ page }) => {
  const child = await openFixture(page)
  await child.evaluate(() => (window.delayCheck = true))
  await send(page)
  await expect.poll(() => child.evaluate(() => result.checks.length)).toBe(1)
  await child.evaluate(() => {
    window.dispose()
    window.releaseCheck()
  })
  expect(await child.evaluate(() => result.writes)).toEqual([])
  expect(await child.evaluate(() => result.loaded)).toBe(0)
})

test('standalone page without opener loads normally; malformed arguments fail closed', async ({
  page
}) => {
  await page
    .context()
    .route('**/*', route =>
      route.fulfill({ contentType: 'text/html', body: '<script>' + fixture + '</script>' })
    )
  await page.goto('https://bi.test/child')
  expect(await page.evaluate(() => result.loaded)).toBe(1)
  await page.goto('https://bi.test/child?de-embedded-window-args=invalid')
  expect(await page.evaluate(() => result.loaded)).toBe(0)
  expect(await page.evaluate(() => result.errors.length)).toBe(1)
})

test('production SFC compiles', () => {
  const script = compileScript(descriptor, { id: 'new-window-origin' })
  const template = compileTemplate({
    source: descriptor.template.content,
    filename,
    id: 'new-window-origin',
    compilerOptions: { bindingMetadata: script.bindings }
  })
  expect(template.errors).toEqual([])
})
