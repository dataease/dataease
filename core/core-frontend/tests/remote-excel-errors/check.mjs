import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import vm from 'node:vm'

const require = createRequire(new URL('../../package.json', import.meta.url))
const { parse: parseSfc } = require('@vue/compiler-sfc')
const { parse } = require('@babel/parser')
const { transformSync } = require('esbuild')
const { AxiosError, AxiosHeaders, CanceledError } = require('axios')
const root = new URL('../../', import.meta.url)
const component =
  process.env.REMOTE_EXCEL_COMPONENT_FILE ||
  fileURLToPath(new URL('src/views/visualized/data/datasource/form/ExcelRemoteDetail.vue', root))
const componentScript = parseSfc(readFileSync(component, 'utf8')).descriptor.scriptSetup.content
const componentAst = parse(componentScript, {
  sourceType: 'module',
  plugins: ['typescript', 'jsx']
})
const declaration = componentAst.program.body
  .filter(node => node.type === 'VariableDeclaration')
  .flatMap(node => node.declarations)
  .find(node => node.id.name === 'loadData')
assert.ok(declaration, 'Use the real component loadData handler')
const loadDataSource = componentScript.slice(declaration.init.start, declaration.init.end)
const serviceSource = readFileSync(new URL('src/config/axios/service.ts', root), 'utf8')
const serviceAst = parse(serviceSource, { sourceType: 'module', plugins: ['typescript'] })
let interceptors
function visit(node) {
  if (!node || typeof node !== 'object') return
  if (
    node.type === 'CallExpression' &&
    serviceSource.slice(node.callee.start, node.callee.end) === 'service.interceptors.response.use'
  ) {
    interceptors = node.arguments.map(arg => serviceSource.slice(arg.start, arg.end))
  }
  for (const value of Object.values(node)) {
    if (Array.isArray(value)) value.forEach(visit)
    else if (value && typeof value === 'object') visit(value)
  }
}
visit(serviceAst.program)
assert.equal(interceptors.length, 2, 'Use the real shared response interceptors')
const code = transformSync(
  `globalThis.loadData = ${loadDataSource};\nglobalThis.fulfilled = ${interceptors[0]};\nglobalThis.rejected = ${interceptors[1]}`,
  { loader: 'tsx', target: 'es2022' }
).code

function locale(name) {
  const { code } = transformSync(readFileSync(new URL(`src/locales/${name}.ts`, root), 'utf8'), {
    loader: 'ts',
    format: 'cjs',
    target: 'es2022'
  })
  const context = { module: { exports: {} }, exports: {} }
  vm.runInNewContext(code, context)
  return context.module.exports.default
}
const languages = ['zh-CN', 'en', 'tw'].map(name => [name, locale(name)])
const timeoutMessage = 'timeout of 1000ms exceeded'
const scenarios = [
  ['network disconnected', () => new AxiosError('Network Error', 'ERR_NETWORK'), 'network'],
  ['request interrupted', () => new AxiosError('Request aborted', 'ECONNABORTED'), 'network'],
  ['normal Axios timeout', () => new AxiosError(timeoutMessage, 'ECONNABORTED'), 'timeout'],
  ['clarified Axios timeout', () => new AxiosError(timeoutMessage, 'ETIMEDOUT'), 'timeout'],
  [
    'timeout without standard message',
    () => new AxiosError('Request timed out', 'ETIMEDOUT'),
    'timeout'
  ],
  ['intentional cancellation', () => new CanceledError(), null],
  [
    'HTTP failure already handled globally',
    () =>
      new AxiosError('Bad request', 'ERR_BAD_REQUEST', {}, null, {
        status: 400,
        data: { msg: 'fixture HTTP error' },
        headers: new AxiosHeaders()
      }),
    'fixture HTTP error'
  ],
  [
    'SMB business failure already handled globally',
    () => ({ business: true, msg: 'fixture SMB error' }),
    'fixture SMB error'
  ],
  ['successful remote load', () => ({ success: true }), 'success']
]
const selected = process.env.REMOTE_EXCEL_CASE
  ? scenarios.filter(([name]) => name === process.env.REMOTE_EXCEL_CASE)
  : scenarios
assert.ok(selected.length, 'Select an existing regression case')
let passed = 0
for (const [language, messages] of languages) {
  const t = key => key.split('.').reduce((item, part) => item?.[part], messages) ?? key
  assert.ok(
    messages.datasource.remote_file_load_interrupted,
    `${language} has a translated interruption message`
  )
  for (const [name, createError, expected] of selected) {
    const notifications = []
    const loading = { value: false }
    const state = { excelData: [{ old: true }] }
    const activeTab = { value: 'old' }
    const tabList = { value: ['old'] }
    const sheetObj = { checked: true }
    const defaultSheetObj = { checked: false }
    const context = {
      console,
      Promise,
      remoteExcelForm: { value: { validate: callback => callback(true) } },
      form: { value: { configuration: { userName: 'fixture', passwd: 'fixture' }, editType: 1 } },
      Base64: { encode: value => Buffer.from(value).toString('base64') },
      loading,
      state,
      activeTab,
      tabList,
      sheetObj,
      defaultSheetObj,
      cloneDeep: structuredClone,
      t,
      useI18n: () => ({ t }),
      ElMessage: options => notifications.push(options.message),
      uploadSuccess: () => {
        context.uploaded = true
      },
      executeVersionHandler: () => undefined,
      result_code: 0,
      linkStore: {},
      permissionStore: { getCurrentPath: '/data/datasource' },
      tryHideLoading: () => undefined,
      requestStore: { resetLoadingMap: () => undefined }
    }
    vm.createContext(context)
    vm.runInContext(code, context)
    context.loadRemoteFile = () => {
      const error = createError()
      const config = { url: '/datasource/loadRemoteFile', loading: false }
      if (error.success)
        return Promise.resolve({ config, headers: {}, data: { code: 0, data: {} } }).then(
          context.fulfilled
        )
      if (error.business)
        return Promise.resolve({ config, headers: {}, data: { code: 500, msg: error.msg } }).then(
          context.fulfilled
        )
      error.config = config
      return Promise.reject(error).catch(context.rejected)
    }
    context.loadData()
    await new Promise(resolve => setImmediate(resolve))
    assert.equal(loading.value, false, `${language}: ${name} releases loading`)
    if (expected === 'success') {
      assert.equal(context.uploaded, true, `${language}: successful load is accepted`)
      assert.deepEqual(notifications, [])
    } else {
      assert.equal(state.excelData.length, 0, `${language}: ${name} clears failed preview`)
      assert.equal(activeTab.value, '')
      assert.equal(tabList.value.length, 0)
      assert.equal(sheetObj.checked, false)
      assert.equal(context.uploaded, undefined)
      const message =
        expected === 'network'
          ? t('datasource.remote_file_load_interrupted')
          : expected === 'timeout'
          ? t('common.timeout_tips')
          : expected
      assert.deepEqual(
        notifications,
        message ? [message] : [],
        `${language}: ${name} shows exactly the expected messages`
      )
    }
    console.log(`PASS ${language}: ${name}`)
    passed++
  }
}
console.log(`PASS ${passed} component/interceptor cases; no server or credentials required`)
