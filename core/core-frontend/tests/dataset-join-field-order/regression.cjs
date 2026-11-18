// Run: node --test tests/dataset-join-field-order/regression.cjs
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const vm = require('node:vm')
const { test } = require('node:test')
const ts = require('typescript')
const { parse } = require('@vue/compiler-sfc')
const { cloneDeep } = require('lodash')

const filename = path.resolve(
  __dirname,
  '../../src/views/pages/visualized/data/dataset/form/index.vue'
)
const source = parse(fs.readFileSync(process.env.DATASET_FORM_SOURCE || filename, 'utf8'))
  .descriptor.scriptSetup.content
const ast = ts.createSourceFile(
  filename + '.tsx',
  source,
  ts.ScriptTarget.Latest,
  true,
  ts.ScriptKind.TSX
)
const functions = ['dfsFields', 'diffArr'].map(name => {
  const statement = ast.statements.find(
    node =>
      ts.isVariableStatement(node) &&
      node.declarationList.declarations.some(declaration => declaration.name.getText(ast) === name)
  )
  assert.ok(statement, `Production function ${name} exists`)
  return statement.getText(ast)
})
const code = ts.transpileModule(functions.join('\n'), {
  compilerOptions: { target: ts.ScriptTarget.ES2020, module: ts.ModuleKind.CommonJS }
}).outputText
const { collect, merge } = vm.runInNewContext(`${code}\n({ collect: dfsFields, merge: diffArr })`, {
  cloneDeep
})
const field = (id, extra = {}) => ({ id, name: id, extField: 0, ...extra })
const node = (ids, children = []) => ({ currentDsFields: ids.map(id => field(id)), children })
const collectFields = nodes => {
  const fields = []
  collect(fields, nodes)
  return fields
}
const ids = fields => Array.from(fields, field => field.id)

test('首次连表预览按左表、右表顺序排列', () => {
  const fields = collectFields([node(['company', 'department'], [node(['total', 'average'])])])
  assert.deepEqual(ids(merge(fields, [])), ['company', 'department', 'total', 'average'])
})
test('已有左表增加右表时，新增字段不能插到主表前', () => {
  const fields = collectFields([node(['company', 'department'], [node(['total', 'average'])])])
  assert.deepEqual(ids(merge(fields, [field('company'), field('department')])), [
    'company',
    'department',
    'total',
    'average'
  ])
})
test('多级关联和兄弟表按父表优先遍历，保留各表字段顺序', () => {
  const fields = collectFields([
    node(['a1', 'a2'], [node(['b'], [node(['c'])]), node(['d'])]),
    node(['e'])
  ])
  assert.deepEqual(ids(merge(fields, [])), ['a1', 'a2', 'b', 'c', 'd', 'e'])
})
test('旧数据顺序颠倒时再次确认可恢复，并保留字段编辑配置', () => {
  const old = [field('right', { name: '自定义名称', deType: 3 }), field('left', { checked: false })]
  const result = merge([field('left'), field('right')], old)
  assert.deepEqual(ids(result), ['left', 'right'])
  assert.equal(result[1].name, '自定义名称')
  assert.equal(result[1].deType, 3)
  assert.equal(result[0].checked, false)
  result[1].name = '修改副本'
  assert.equal(old[0].name, '自定义名称')
})
test('取消勾选字段会移除，重新勾选字段按表顺序归位', () => {
  const old = [field('left1'), field('left2'), field('right')]
  const unchecked = merge([field('left1'), field('right')], old)
  assert.deepEqual(ids(unchecked), ['left1', 'right'])
  assert.deepEqual(ids(merge([field('left1'), field('left2'), field('right')], unchecked)), [
    'left1',
    'left2',
    'right'
  ])
})
test('计算、复制和分组字段沿用已有保留规则，保持引用配置', () => {
  const derived = [
    field('copy', { extField: 2, originName: '[left]' }),
    field('group', { extField: 3, originName: 'left' })
  ]
  const result = merge([field('left'), field('right')], [field('left'), ...derived])
  assert.deepEqual(ids(result), ['copy', 'group', 'left', 'right'])
  assert.equal(result[0].originName, '[left]')
  assert.equal(result[1].originName, 'left')
})
test('自关联同名字段以 ID 匹配，不能串用重命名', () => {
  const old = [
    field('table-b-name', { name: '右侧别名' }),
    field('table-a-name', { name: '左侧别名' })
  ]
  const result = merge([field('table-a-name'), field('table-b-name')], old)
  assert.deepEqual(
    Array.from(result, item => item.name),
    ['左侧别名', '右侧别名']
  )
})
test('反复确认不重复字段；空表和空字段集合可处理', () => {
  const fields = collectFields([node(['left'], [node(['right'])]), node([])])
  const once = merge(fields, [])
  assert.deepEqual(ids(merge(fields, once)), ['left', 'right'])
  assert.deepEqual(ids(merge([], once)), [])
  assert.deepEqual(collectFields([]), [])
})
