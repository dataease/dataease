// Run: node --test tests/column-permission-init/regression.cjs
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const vm = require('node:vm')
const { test } = require('node:test')
const ts = require('typescript')
const vue = require('vue')
const { parse, compileScript, compileTemplate } = require('@vue/compiler-sfc')
const { cloneDeep } = require('lodash')
const file = path.resolve(
  __dirname,
  '../../src/views/component/row-col-permission/dataset-column-permissions-auth/index.vue'
)
const descriptor = parse(fs.readFileSync(file, 'utf8')).descriptor
const script = compileScript(descriptor, { id: 'column-permission-test' })
const flush = async () => {
  for (let i = 0; i < 15; i++) await Promise.resolve()
  await vue.nextTick()
}
const detail = id => ({
  datasetId: id,
  authTargetId: 'user-' + id,
  authTargetType: 'user',
  name: id
})
const fields = [{ id: 'field-1', name: 'Amount', deType: 2 }]
const existing = id => ({
  id: 'permission-' + id,
  datasetId: id,
  authTargetId: 'user-' + id,
  authTargetType: 'user',
  whiteListUser: '["allow-user"]',
  permissions: JSON.stringify({
    enable: false,
    columns: [
      {
        id: 'field-1',
        selected: true,
        opt: 'Desensitization',
        desensitizationRule: { builtInRule: 'CompleteDesensitization' }
      }
    ]
  })
})
const mount = () => {
  const requests = [],
    events = [],
    errors = []
  const exports = {}
  const requireMock = id => {
    if (id === 'vue') return vue
    if (id === 'lodash-es') return { cloneDeep }
    if (id === '@/hooks/web/useI18n') return { useI18n: () => ({ t: key => key }) }
    if (id === 'element-plus-secondary')
      return { ElMessage: Object.assign(() => {}, { error: text => errors.push(text) }) }
    if (id === '@/config/axios')
      return {
        default: {
          post: config =>
            new Promise((resolve, reject) => requests.push({ ...config, resolve, reject }))
        }
      }
    if (id.endsWith('.svg')) return { default: {} }
    if (id.endsWith('options.js')) return { fieldEnums: {} }
    if (id.endsWith('RowAuth.vue')) return { default: {} }
    throw new Error(id)
  }
  const code = ts.transpileModule(script.content, {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2020 }
  }).outputText
  vm.runInNewContext(code, { exports, require: requireMock })
  const component = exports.default
  component.render = () => null
  const renderer = vue.createRenderer({
    createComment: () => ({}),
    insert() {},
    remove() {},
    parentNode: () => null,
    nextSibling: () => null
  })
  const vnode = vue.h(component, { onSavePermissions: (...args) => events.push(args) })
  const container = {}
  renderer.render(vnode, container)
  const state = vnode.component.setupState
  state.rowAuth = {
    init() {},
    submit: () => state.save({ logic: 'or', items: [] }),
    relationList: [],
    logic: 'or'
  }
  state.tableDesensitization = { toggleRowSelection() {} }
  const request = (datasetId, part) =>
    requests.find(
      item =>
        item.url.includes(part) &&
        (item.data?.datasetId === datasetId || item.url.endsWith('/' + datasetId))
    )
  const load = async (id, data = existing(id)) => {
    request(id, 'dataSetRowPermissionInfo').resolve({ data: null })
    request(id, 'listByDatasetGroup').resolve({ data: fields })
    await flush()
    request(id, 'columnPermissions/info').resolve({ data })
    await flush()
  }
  return {
    state,
    requests,
    events,
    errors,
    request,
    load,
    unmount: () => renderer.render(null, container)
  }
}

test('component template compiles and save is disabled until initialization', () => {
  const result = compileTemplate({
    source: descriptor.template.content,
    filename: file,
    id: 'column-permission-test',
    compilerOptions: { bindingMetadata: script.bindings }
  })
  assert.equal(result.errors.length, 0)
  assert.match(descriptor.template.content, /:disabled="!permissionsReady" @click="confirm"/)
})

test('saving without opening column tab preserves loaded permissions and whitelist', async () => {
  const ctx = mount()
  try {
    ctx.state.init(detail('A'))
    ctx.state.confirm()
    ctx.state.save({ logic: 'or', items: [] })
    assert.equal(ctx.events.length, 0)
    await ctx.load('A')
    assert.equal(ctx.state.permissionsReady, true)
    assert.equal(ctx.state.datasetPermissionsTabActive, 'RowPermissions')
    ctx.state.confirm()
    assert.equal(ctx.events.length, 1)
    const column = ctx.events[0][1]
    assert.equal(column.datasetId, 'A')
    assert.equal(column.whiteListUser, '["allow-user"]')
    assert.equal(JSON.parse(column.permissions).enable, false)
    assert.equal(JSON.parse(column.permissions).columns[0].selected, true)
    assert.equal(JSON.parse(column.permissions).columns[0].opt, 'Desensitization')
    const count = ctx.requests.length
    ctx.state.handleClickTabs({ paneName: 'ColumnPermissions' })
    ctx.state.handleSelectionChange([])
    ctx.state.handleClickTabs({ paneName: 'RowPermissions' })
    ctx.state.handleClickTabs({ paneName: 'ColumnPermissions' })
    assert.equal(ctx.requests.length, count)
    assert.equal(ctx.state.columnPermissionForm.permissions.columns[0].selected, false)
  } finally {
    ctx.unmount()
  }
})

test('loading failure blocks saving and reopening without permissions resets previous data', async () => {
  const ctx = mount()
  try {
    ctx.state.init(detail('A'))
    await ctx.load('A')
    ctx.state.init(detail('B'))
    ctx.request('B', 'dataSetRowPermissionInfo').resolve({ data: null })
    ctx.request('B', 'listByDatasetGroup').resolve({ data: fields })
    await flush()
    ctx.request('B', 'columnPermissions/info').reject(new Error('network failure'))
    await flush()
    ctx.state.confirm()
    assert.equal(ctx.events.length, 0)
    assert.equal(ctx.state.permissionsReady, false)
    assert.equal(ctx.errors[0], 'auth.permission_load_failed')
    ctx.state.init(detail('C'))
    await ctx.load('C', null)
    ctx.state.confirm()
    const saved = ctx.events[0][1]
    assert.equal(saved.datasetId, 'C')
    assert.equal(saved.id, '')
    assert.equal(saved.whiteListUser, '[]')
    assert.equal(JSON.parse(saved.permissions).enable, true)
    assert.equal(JSON.parse(saved.permissions).columns[0].selected, undefined)
  } finally {
    ctx.unmount()
  }
})

test('stale requests after switching targets or closing cannot overwrite current state', async () => {
  const ctx = mount()
  try {
    ctx.state.init(detail('A'))
    ctx.request('A', 'listByDatasetGroup').resolve({ data: fields })
    await flush()
    ctx.state.init(detail('B'))
    await ctx.load('B')
    ctx.request('A', 'dataSetRowPermissionInfo').resolve({ data: { id: 'old-row' } })
    ctx.request('A', 'columnPermissions/info').resolve({ data: existing('A') })
    await flush()
    assert.equal(ctx.state.columnPermissionForm.datasetId, 'B')
    assert.equal(ctx.state.defaultForm.datasetId, 'B')
    assert.equal(ctx.state.permissionsReady, true)
    ctx.state.init(detail('C'))
    ctx.state.clearData()
    ctx.request('C', 'dataSetRowPermissionInfo').resolve({ data: { id: 'closed-row' } })
    ctx.request('C', 'listByDatasetGroup').resolve({ data: fields })
    await flush()
    assert.equal(ctx.state.showDrawer, false)
    assert.equal(ctx.state.permissionsReady, false)
    assert.equal(ctx.request('C', 'columnPermissions/info'), undefined)
  } finally {
    ctx.unmount()
  }
})
