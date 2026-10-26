// Run: node --test tests/nested-group-border-lookup/regression.cjs
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

// 与生产代码 src/utils/translate.ts 保持一致的三角函数换算
const toRadian = angle => (angle * Math.PI) / 180
const translateMock = {
  sin: rotate => Math.abs(Math.sin(toRadian(rotate))),
  cos: rotate => Math.abs(Math.cos(toRadian(rotate))),
  toPercent: val => val * 100 + '%'
}

const deepCopy = target => {
  if (target === null || typeof target !== 'object') {
    return target
  }
  if (Array.isArray(target)) {
    return target.map(deepCopy)
  }
  const result = {}
  Object.keys(target).forEach(key => {
    result[key] = deepCopy(target[key])
  })
  return result
}

const createScene = () => {
  const componentData = ref([])
  const curComponent = ref(null)
  const curOriginThemes = ref('light')
  const dvMainStore = { componentData, curComponent, curOriginThemes }

  const piniaMock = {
    defineStore: (_name, options) => options.actions,
    storeToRefs: () => ({ componentData, curComponent, curOriginThemes })
  }

  const style = evaluate('src/utils/style.ts', id => {
    if (id === '@/utils/translate') {
      return translateMock
    }
    if (id === '@/utils/imgUtils') {
      return { imgUrlTrans: url => url }
    }
    if (id === '@/views/chart/components/js/util') {
      return { hexColorToRGBA: () => '' }
    }
    if (id === '@/store/modules/data-visualization/dvMain') {
      return { dvMainStoreWithOut: () => dvMainStore }
    }
    if (id === '@/utils/canvasUtils') {
      return {
        isMainCanvas: canvasId => canvasId === 'canvas-main',
        isTabCanvas: canvasId =>
          !!canvasId && !canvasId.includes('Group') && canvasId !== 'canvas-main'
      }
    }
    throw new Error(`Unexpected import: ${id}`)
  })

  const decompose = evaluate('src/utils/decomposeComponent.ts', id => {
    throw new Error(`Unexpected import: ${id}`)
  })

  const common = evaluate('src/store/modules/data-visualization/common.ts', id => {
    if (id === 'pinia') {
      return piniaMock
    }
    if (id === './dvMain') {
      return { dvMainStoreWithOut: () => dvMainStore }
    }
    throw new Error(`Unexpected import: ${id}`)
  })

  const compose = evaluate('src/store/modules/data-visualization/compose.ts', id => {
    if (id === 'pinia') {
      return piniaMock
    }
    if (id === '../../index') {
      return { store: {} }
    }
    if (id === './dvMain') {
      return { dvMainStoreWithOut: () => dvMainStore }
    }
    if (id === '@/utils/utils') {
      return { $: () => null, deepCopy }
    }
    if (id === '@/utils/decomposeComponent') {
      return { default: decompose.default }
    }
    if (id === '@/utils/generateID') {
      return { generateID: () => 'generated-id' }
    }
    if (id === '@/custom-component/component-list') {
      return {
        commonStyle: { width: 0, height: 0, top: 0, left: 0, rotate: 0 },
        commonAttr: {},
        COMMON_COMPONENT_BACKGROUND_MAP: { light: {}, dark: {} }
      }
    }
    if (id === '@/utils/style') {
      return style
    }
    if (id === '@/utils/eventBus') {
      return { default: { emit: () => {}, on: () => {} } }
    }
    if (id === '@/utils/canvasUtils') {
      return {
        canvasIdMapCheck: () => {},
        checkJoinGroup: () => true,
        isTabCanvas: canvasId =>
          !!canvasId && !canvasId.includes('Group') && canvasId !== 'canvas-main'
      }
    }
    if (id === '@/hooks/web/useI18n') {
      return { useI18n: () => ({ t: key => key }) }
    }
    if (id === '@/store/modules/data-visualization/common') {
      return common
    }
    throw new Error(`Unexpected import: ${id}`)
  })

  return { compose, common, componentData, curComponent }
}

// 画布结构：Tab 页内的分组，分组内还有一个子组件
const buildTabWithGroup = () => {
  const child = {
    id: 'child-1',
    component: 'UserView',
    canvasId: 'Group-group-1',
    style: { left: 10, top: 20, width: 100, height: 50, rotate: 0 },
    groupStyle: { left: 0, top: 0, width: 0, height: 0 }
  }
  const group = {
    id: 'group-1',
    component: 'Group',
    canvasId: 'tabs-1--tab-1',
    style: { left: 100, top: 200, width: 300, height: 400, rotate: 0 },
    propValue: [child]
  }
  const tabs = {
    id: 'tabs-1',
    component: 'DeTabs',
    canvasId: 'canvas-main',
    style: { left: 0, top: 0, width: 800, height: 600, rotate: 0 },
    propValue: [{ name: 'tab-1', componentData: [group] }]
  }
  return { tabs, group, child }
}

test('updateGroupBorder recalculates the border of a group nested in a tab', () => {
  const { compose, componentData } = createScene()
  const { tabs, group, child } = buildTabWithGroup()
  componentData.value = [tabs]

  compose.composeStore.updateGroupBorder('Group-group-1')

  // 分组边界按照内部组件重新计算
  assert.deepEqual(
    { left: group.style.left, top: group.style.top },
    { left: 110, top: 220 }
  )
  assert.equal(group.style.width, 100)
  assert.equal(group.style.height, 50)
  // 子组件仍然留在分组画布内，且相对分组定位
  assert.equal(child.canvasId, 'Group-group-1')
  assert.equal(child.style.left, 0)
  assert.equal(child.style.top, 0)
  assert.deepEqual(
    { left: child.groupStyle.left, top: child.groupStyle.top },
    { left: 0, top: 0 }
  )
  assert.equal(child.groupStyle.width, 1)
  assert.equal(child.groupStyle.height, 1)
})

test('updateGroupBorder keeps working for a group on the main canvas', () => {
  const { compose, componentData } = createScene()
  const { group, child } = buildTabWithGroup()
  group.canvasId = 'canvas-main'
  componentData.value = [group]

  compose.composeStore.updateGroupBorder('Group-group-1')

  assert.equal(group.style.left, 110)
  assert.equal(group.style.top, 220)
  assert.equal(group.style.width, 100)
  assert.equal(group.style.height, 50)
  assert.equal(child.canvasId, 'Group-group-1')
  assert.equal(child.style.left, 0)
  assert.equal(child.style.top, 0)
})

test('updateGroupBorder ignores a canvas id whose group no longer exists', () => {
  const { compose, componentData } = createScene()
  const { tabs } = buildTabWithGroup()
  componentData.value = [tabs]

  assert.doesNotThrow(() => {
    compose.composeStore.updateGroupBorder('Group-missing')
  })
})
