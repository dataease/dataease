const assert = require('node:assert/strict')
const fs = require('node:fs')
const vm = require('node:vm')
const { test } = require('node:test')
const ts = require('typescript')
const { parse } = require('@vue/compiler-sfc')
const { reactive, watch, nextTick, effectScope } = require('vue')
const source = parse(
  fs.readFileSync(
    process.env.GRID_TABLE_SOURCE || 'src/components/grid-table/src/GridTable.vue',
    'utf8'
  )
).descriptor.scriptSetup.content
const ast = ts.createSourceFile('GridTable.ts', source, ts.ScriptTarget.Latest, true)
const code = ast.statements
  .filter(
    node =>
      ts.isExpressionStatement(node) &&
      ts.isCallExpression(node.expression) &&
      node.expression.expression.getText(ast) === 'watch'
  )
  .map(node => node.getText(ast))
  .join('\n')

test('optional pagination and replacement props remain valid watch sources', async () => {
  const props = reactive({
    pagination: undefined,
    tableData: [],
    isRememberSelected: false,
    multipleSelection: []
  })
  const state = reactive({ paginationDefault: { currentPage: 1, pageSize: 10, total: 0 } })
  let layouts = 0
  const warnings = []
  const originalWarn = console.warn
  const scope = effectScope()
  console.warn = (...args) => warnings.push(args)
  try {
    scope.run(() =>
      vm.runInNewContext(code, {
        props,
        state,
        watch,
        nextTick,
        table: { value: { doLayout: () => layouts++ } },
        handlerSelected() {}
      })
    )
    assert.equal(warnings.length, 0, 'omitting pagination must not create an invalid watch source')
    props.pagination = { currentPage: 2, total: 50 }
    await nextTick()
    assert.equal(state.paginationDefault.currentPage, 2)
    assert.equal(state.paginationDefault.pageSize, 10)
    assert.equal(state.paginationDefault.total, 50)
    props.pagination.total = 60
    await nextTick()
    assert.equal(state.paginationDefault.total, 60)
    props.tableData = [{ id: 'new-row' }]
    await nextTick()
    await nextTick()
    assert.equal(layouts, 1, 'replacing the row array triggers layout')
    props.tableData[0].id = 'edited-row'
    await nextTick()
    await nextTick()
    assert.equal(layouts, 2, 'deep row mutations still trigger layout')
    assert.equal(warnings.length, 0)
  } finally {
    scope.stop()
    console.warn = originalWarn
  }
})
