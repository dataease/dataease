// Run: node --test tests/visualization-layer-lookup/regression.cjs
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const vm = require('node:vm')
const { test } = require('node:test')
const ts = require('typescript')
const { ref } = require('vue')

const rootDir = path.resolve(__dirname, '../..')
const read = file => fs.readFileSync(path.join(rootDir, file), 'utf8')

const evaluate = (file, requireMock) => {
  const exports = {}
  const code = ts.transpileModule(read(file), {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2020 }
  }).outputText
  vm.runInNewContext(code, { exports, require: requireMock })
  return exports
}

const createScene = () => {
  const image = { id: 'image', component: 'Image', isShow: true }
  const text = { id: 'text', component: 'Text', isShow: true }
  const group = { id: 'group', component: 'Group', propValue: [image, text] }
  const tabs = {
    id: 'tabs',
    component: 'DeTabs',
    propValue: [{ name: 'tab-1', componentData: [group] }]
  }
  const userView = { id: 'user-view', component: 'UserView' }
  const root = [tabs, userView]
  const componentData = ref(root)
  const curComponent = ref(image)
  const curComponentIndex = ref(0)

  const common = evaluate('src/store/modules/data-visualization/common.ts', id => {
    if (id === 'pinia') {
      return { storeToRefs: () => ({ curComponent, componentData }) }
    }
    if (id === './dvMain') {
      return { dvMainStoreWithOut: () => ({}) }
    }
    throw new Error(`Unexpected import: ${id}`)
  })

  const layer = evaluate('src/store/modules/data-visualization/layer.ts', id => {
    if (id === 'pinia') {
      return {
        defineStore: (_name, options) => options.actions,
        storeToRefs: () => ({ curComponentIndex, curComponent })
      }
    }
    if (id === '../../index') {
      return { store: {} }
    }
    if (id === './dvMain') {
      return { dvMainStoreWithOut: () => ({ curComponentIndex, curComponent }) }
    }
    if (id === '@/utils/utils') {
      return {
        swap: (items, from, to) => {
          const value = items[from]
          items[from] = items[to]
          items[to] = value
        }
      }
    }
    if (id === '@/hooks/web/useEmitt') {
      return { useEmitt: () => ({ emitter: { emit() {} } }) }
    }
    if (id === '@/store/modules/data-visualization/common') {
      return common
    }
    if (id === '@/views/chart/components/js/G2TooltipCarousel') {
      return { default: { paused() {}, resume() {} } }
    }
    throw new Error(`Unexpected import: ${id}`)
  })

  return { common, layer, root, group, tabs, image, text, userView }
}

test('finds a component nested in Group inside DeTabs', () => {
  const { common, group, image } = createScene()
  const info = common.getCurInfoById('image')
  assert.equal(info.index, 0)
  assert.equal(info.tabIndex, 0)
  assert.equal(info.targetComponent.id, image.id)
  assert.deepEqual(
    info.componentData.map(component => component.id),
    ['image', 'text']
  )
  assert.equal(common.getCurInfoById('missing'), null)
})

test('topComponent moves a nested component without corrupting the root array', () => {
  const { layer, root, group } = createScene()
  layer.layerStore.topComponent('image')
  assert.deepEqual(
    group.propValue.map(component => component.id),
    ['text', 'image']
  )
  assert.deepEqual(
    root.map(component => component?.id),
    ['tabs', 'user-view']
  )
  assert.equal(root.includes(null), false)
})

test('layer actions ignore components they cannot resolve', () => {
  const { layer, root } = createScene()
  layer.layerStore.topComponent('missing')
  layer.layerStore.bottomComponent('missing')
  layer.layerStore.upComponent('missing')
  layer.layerStore.downComponent('missing')
  assert.deepEqual(
    root.map(component => component?.id),
    ['tabs', 'user-view']
  )
  assert.equal(root.includes(null), false)
})
