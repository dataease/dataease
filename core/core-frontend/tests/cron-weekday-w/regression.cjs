// Run from core/core-frontend: node --test tests/cron-weekday-w/regression.cjs
const assert = require('node:assert/strict')
const { readFileSync } = require('node:fs')
const path = require('node:path')
const { test } = require('node:test')
const vm = require('node:vm')
const ts = require('typescript')
const vue = require('vue')
const { parse, compileScript } = require('@vue/compiler-sfc')
const Schema = require('async-validator').default

const root = path.resolve(__dirname, '../..')
const read = file => readFileSync(path.join(root, file), 'utf8')
const evaluate = (source, requireModule = require) => {
  const exports = {}
  const code = ts.transpileModule(source, {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2020 }
  }).outputText
  vm.runInNewContext(code, { exports, require: requireModule })
  return exports
}
const validation = evaluate(read('src/components/cron/src/validation.ts'))

const renderer = vue.createRenderer({
  createComment: () => ({}),
  insert() {},
  remove() {},
  parentNode: () => null,
  nextSibling: () => null
})

const mountEditor = name => {
  const { descriptor } = parse(read(`src/components/cron/src/${name}.vue`))
  const script = compileScript(descriptor, { id: name }).content
  const component = evaluate(script, id => {
    if (id === 'vue') return vue
    if (id === './validation') return validation
    if (id === '@/utils/propTypes')
      return { propTypes: { string: { def: value => ({ default: value }) } } }
    if (id === '@/hooks/web/useI18n') return { useI18n: () => ({ t: key => key }) }
    throw new Error(`Unexpected import: ${id}`)
  }).default
  component.render = () => null
  const events = []
  const container = {}
  const update = async modelValue => {
    const vnode = vue.h(component, {
      modelValue,
      'onUpdate:modelValue': value => events.push(value)
    })
    renderer.render(vnode, container)
    await vue.nextTick()
    return vnode.component.setupState
  }
  return { update, events, unmount: () => renderer.render(null, container) }
}

test('week editor preserves invalid W input and recovers after a valid selection', async () => {
  const editor = mountEditor('Week')
  try {
    for (const value of [
      '15W',
      '1w',
      'LW',
      '1W-5',
      '?',
      '*',
      '1,3,5',
      '1-5',
      '1/2',
      '6L',
      '6#3',
      'WED'
    ]) {
      const state = await editor.update(value)
      assert.equal(state.resultValue, value)
      assert.equal(editor.events.at(-1), value)
    }
    const state = await editor.update('15W')
    assert.equal(state.type, '8')
    state.type = '5'
    await vue.nextTick()
    assert.equal(editor.events.at(-1), '?')
  } finally {
    editor.unmount()
  }
})

test('day editor still supports nearest weekday expressions', async () => {
  const editor = mountEditor('Day')
  try {
    for (const value of ['1W', '15W', '31W', '?']) {
      const state = await editor.update(value)
      assert.equal(state.resultValue, value)
      assert.equal(editor.events.at(-1), value)
    }
  } finally {
    editor.unmount()
  }
})

test('both datasource form rules reject illegal W on save, but allow day W and WED', async () => {
  for (const name of ['EditorDetail', 'ExcelRemoteDetail']) {
    const { descriptor } = parse(read(`src/views/visualized/data/datasource/form/${name}.vue`))
    const source = ts.createSourceFile(
      'form.ts',
      descriptor.scriptSetup.content,
      ts.ScriptTarget.Latest
    )
    let initializer
    const visit = node => {
      if (ts.isVariableDeclaration(node) && node.name.getText(source) === 'apiRule')
        initializer = node.initializer.getText(source)
      ts.forEachChild(node, visit)
    }
    visit(source)
    const rules = vm.runInNewContext(`(${initializer})`, { ...validation, t: key => key })
    const schema = new Schema({ cron: rules['syncSetting.cron'] })
    for (const value of ['0 0 12 ? * 15W *', '0 0 12 ? * LW *', '0 0 12 ? * 1w *']) {
      await assert.rejects(
        schema.validate({ cron: value }),
        error => error.errors[0].message === 'cron.invalid_weekday_w'
      )
    }
    for (const value of [
      '0 0 12 15W * ? *',
      '0 0 12 ? * WED *',
      '0 0 12 ? * MON-WED *',
      '0 0 12 ? * WED#2 *'
    ]) {
      await schema.validate({ cron: value })
    }
  }
})
