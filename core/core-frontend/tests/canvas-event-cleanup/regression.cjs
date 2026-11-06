/* eslint-disable @typescript-eslint/no-var-requires */
const { test } = require('node:test')
const assert = require('node:assert/strict')
const fs = require('node:fs')
const vm = require('node:vm')
const ts = require('typescript')
const { transformSync } = require('esbuild')
const { parse } = require('@vue/compiler-sfc')
const { createRenderer, defineComponent, onMounted, onBeforeUnmount, ref } = require('vue')
const mitt = require('mitt')

const source = fs.readFileSync('src/components/data-visualization/canvas/CanvasCore.vue', 'utf8')
const script = parse(source).descriptor.scriptSetup.content
const ast = ts.createSourceFile('CanvasCore.ts', script, ts.ScriptTarget.Latest, true)
const statements = ast.statements.filter(node => {
  if (ts.isVariableStatement(node)) {
    return node.declarationList.declarations.some(
      declaration => declaration.name.getText(ast) === 'canvasEventCleanups'
    )
  }
  return (
    ts.isExpressionStatement(node) &&
    ts.isCallExpression(node.expression) &&
    ['onMounted', 'onBeforeUnmount'].includes(node.expression.expression.getText(ast))
  )
})
assert.equal(statements.filter(ts.isExpressionStatement).length, 2)
const lifecycleCode = transformSync(statements.map(node => node.getText(ast)).join('\n'), {
  loader: 'ts'
}).code
const noop = () => undefined
const renderer = createRenderer({
  createElement: () => ({}),
  createText: () => ({}),
  createComment: () => ({}),
  insert: noop,
  remove: noop,
  setText: noop,
  setElementText: noop,
  parentNode: () => null,
  nextSibling: () => null,
  patchProp: noop
})
const eventNames = [
  'handleDragStartMoveIn',
  'handleDragEnd',
  'hideArea',
  'removeMatrixItem',
  'removeMatrixItemPosition',
  'removeMatrixItemById',
  'addDashboardItem',
  'snapshotChange',
  'doCanvasInit'
]
const countListeners = bus =>
  [...bus.all.values()].reduce((sum, handlers) => sum + handlers.length, 0)
const mount = (bus, id) => {
  const calls = { init: 0 }
  const canvasId = ref(id)
  const timer = { value: null }
  const cleared = []
  const component = defineComponent({
    setup() {
      vm.runInNewContext(lifecycleCode, {
        onMounted,
        onBeforeUnmount,
        eventBus: bus,
        canvasId,
        isMainCanvas: value => value === 'canvas-main',
        initSnapshotTimer: () => {
          timer.value = 123
        },
        initWatermark: noop,
        dvMainStore: { setEditMode: noop },
        composeStore: { getEditor: noop },
        snapshotTimer: timer,
        clearInterval: value => cleared.push(value),
        handleDragStartMoveIn: noop,
        handleDragEnd: noop,
        hideArea: noop,
        removeItem: noop,
        removeItemComponent: noop,
        removeItemById: noop,
        addItemBox: noop,
        canvasInit: () => {
          calls.init++
        }
      })
      return () => null
    }
  })
  const app = renderer.createApp(component)
  app.mount({})
  return { calls, canvasId, timer, cleared, unmount: () => app.unmount() }
}

test('20 次挂载卸载后监听恢复基线，重新挂载一次事件仅调用当前实例', () => {
  const bus = mitt()
  const baseline = () => undefined
  bus.on('doCanvasInit-canvas-main', baseline)
  const old = []
  for (let i = 0; i < 20; i++) {
    const instance = mount(bus, 'canvas-main')
    old.push(instance)
    assert.equal(countListeners(bus), 10)
    bus.emit('doCanvasInit-canvas-main')
    assert.equal(instance.calls.init, 1)
    instance.unmount()
    assert.equal(countListeners(bus), 1)
    assert.equal(instance.timer.value, null)
    assert.deepEqual(instance.cleared, [123])
  }
  const current = mount(bus, 'canvas-main')
  bus.emit('doCanvasInit-canvas-main')
  assert.equal(current.calls.init, 1)
  old.forEach(instance => assert.equal(instance.calls.init, 1))
  current.unmount()
  assert.deepEqual(bus.all.get('doCanvasInit-canvas-main'), [baseline])
})

test('不同画布及同 key 的其他订阅互不影响，卸载后旧事件不访问旧实例', () => {
  const bus = mitt()
  const first = mount(bus, 'tab-first')
  const second = mount(bus, 'tab-second')
  const sibling = mount(bus, 'tab-first')
  first.unmount()
  bus.emit('doCanvasInit-tab-first')
  bus.emit('snapshotChange-tab-second')
  assert.equal(first.calls.init, 0)
  assert.equal(sibling.calls.init, 1)
  assert.equal(second.calls.init, 1)
  second.unmount()
  sibling.unmount()
  for (const name of eventNames) {
    bus.emit(name + '-tab-first')
    bus.emit(name + '-tab-second')
  }
  assert.equal(countListeners(bus), 0)
  assert.equal(first.calls.init, 0)
  assert.equal(sibling.calls.init, 1)
  assert.equal(second.calls.init, 1)
})

test('卸载使用注册时的 key，即使 canvasId 已变化也释放全部旧监听', () => {
  const bus = mitt()
  const instance = mount(bus, 'group-original')
  instance.canvasId.value = 'group-replaced'
  instance.unmount()
  for (const name of eventNames) bus.emit(name + '-group-original')
  assert.equal(countListeners(bus), 0)
  assert.equal(instance.calls.init, 0)
})
