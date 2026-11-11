/* eslint-disable @typescript-eslint/no-var-requires -- 独立 Node 回归入口。 */
const { test } = require('node:test')
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const { pathToFileURL } = require('node:url')
const ts = require('typescript')
const Vue = require('vue')
const { parse } = require('@vue/compiler-sfc')
const { baseParse, compile } = require('@vue/compiler-dom')
const { renderToString } = require('@vue/server-renderer')
const { cloneDeep } = require('lodash')

const root = path.resolve(__dirname, '../..')
const componentPath = path.join(root, 'src/views/system/parameter/map/Geometry.vue')
const { descriptor } = parse(fs.readFileSync(componentPath, 'utf8'))
const readDeclarations = (source, names) => {
  const ast = ts.createSourceFile('fixture.ts', source, ts.ScriptTarget.Latest, true)
  return names
    .map(name => {
      const statement = ast.statements.find(
        item =>
          ts.isVariableStatement(item) &&
          item.declarationList.declarations.some(
            declaration => ts.isIdentifier(declaration.name) && declaration.name.text === name
          )
      )
      assert.ok(statement, `生产函数 ${name} 存在`)
      return statement.getText(ast).replace(/^export\s+/, '')
    })
    .join('\n')
}
const execute = (source, names, context = {}) => {
  const body = ts.transpileModule(source, {
    compilerOptions: { target: ts.ScriptTarget.ES2020, module: ts.ModuleKind.CommonJS }
  }).outputText
  return new Function(...Object.keys(context), `${body}\nreturn { ${names.join(',')} }`)(
    ...Object.values(context)
  )
}
const filters = execute(
  readDeclarations(descriptor.scriptSetup.content, ['filterResourceNode']),
  ['filterResourceNode'],
  {
    // 旧实现在真实 Vue 树上写属性；保留这个替身使回归能明确捕获深监听风暴。
    setColorName: (data, keyword) => {
      data.colorName = data.name.includes(keyword) ? `<span>${data.name}</span>` : null
    }
  }
)
const TreeStoreModule = import(
  pathToFileURL(
    path.join(
      path.dirname(require.resolve('element-plus-secondary/package.json')),
      'es/components/tree/src/model/tree-store.mjs'
    )
  ).href
)
const flush = async () => {
  await new Promise(resolve => setImmediate(resolve))
  await Vue.nextTick()
}
const flatten = nodes => nodes.flatMap(node => [node, ...flatten(node.children || [])])
const areaData = count => {
  const nodes = [
    {
      id: '000',
      name: '世界村',
      children: [
        {
          id: '156',
          name: '中国',
          children: [
            { id: 'beijing', name: '北京市', children: [{ id: 'dongcheng', name: '东城区' }] },
            {
              id: 'shanxi',
              name: '山西省',
              children: [{ id: 'taiyuan', pid: 'shanxi', name: '太原市' }]
            }
          ]
        },
        { id: '826', name: 'United Kingdom', children: [{ id: 'london', name: 'London' }] }
      ]
    }
  ]
  const china = nodes[0].children[0]
  for (let i = flatten(nodes).length; i < count; i++) {
    china.children.push({ id: `area-${i}`, name: `其他区域${i}` })
  }
  return nodes
}

for (const count of [500, 1000]) {
  test(`${count} 个节点搜索不写树数据、不触发全树深监听，保留祖先与清空行为`, async () => {
    const { default: TreeStore } = await TreeStoreModule
    const treeData = Vue.ref(areaData(count))
    let visits = 0
    const store = Vue.ref(
      new TreeStore({
        key: 'id',
        data: treeData.value,
        props: { label: 'name', children: 'children' },
        defaultExpandedKeys: [],
        defaultCheckedKeys: [],
        filterNodeMethod: (value, data) => {
          visits++
          return filters.filterResourceNode(value, data)
        }
      })
    )
    store.value.initialize()
    const before = JSON.stringify(treeData.value)
    let updates = 0
    const stop = Vue.watch(
      () => treeData.value,
      value => {
        updates++
        store.value.setData(value)
      },
      { deep: true }
    )
    try {
      const search = async keyword => {
        visits = 0
        store.value.filter(keyword)
        await flush()
        assert.equal(visits, count, '每次搜索只访问每个节点一次')
        assert.equal(updates, 0, '过滤不得触发树数据的深监听')
        assert.equal(JSON.stringify(treeData.value), before, '节点及详情属性保持不变')
      }
      await search('北京')
      assert.equal(store.value.getNode('beijing').visible, true)
      assert.equal(store.value.getNode('156').visible, true)
      assert.equal(store.value.getNode('000').visible, true)
      assert.equal(store.value.getNode('shanxi').visible, false)
      assert.equal(store.value.getNode('taiyuan').visible, false)
      assert.equal(store.value.getNode('beijing').expanded, true)
      await search('lOnDoN')
      assert.equal(store.value.getNode('london').visible, true)
      assert.equal(store.value.getNode('826').visible, true)
      assert.equal(store.value.getNode('156').visible, false)
      await search('不存在的地区')
      assert.equal(store.value.getNode('000').visible, false)
      await search('')
      assert.equal(
        flatten(treeData.value).every(node => store.value.getNode(node.id).visible),
        true
      )
      await search('北京')
    } finally {
      stop()
    }
  })
}

test('输入提交及清空只调用主地理树过滤，未挂载时安全返回', () => {
  const values = []
  const areaTreeRef = Vue.ref({ filter: value => values.push(value) })
  const { filterResource } = execute(
    readDeclarations(descriptor.scriptSetup.content, ['filterResource']),
    ['filterResource'],
    { areaTreeRef }
  )
  filterResource('北京')
  filterResource('')
  assert.deepEqual(values, ['北京', ''])
  areaTreeRef.value = null
  assert.doesNotThrow(() => filterResource('北京'))
})

const findNameSpan = node => {
  if (
    node.type === 1 &&
    node.tag === 'span' &&
    node.props.some(prop => prop.name === 'class' && prop.value?.content.includes('geo-name-span'))
  ) {
    return node
  }
  for (const child of node.children || []) {
    const result = findNameSpan(child)
    if (result) return result
  }
}
test('真实名称模板保持高亮并以文本渲染 HTML 与实体，不依赖节点 colorName', async () => {
  const helperNames = ['getHighlightSegments']
  const helpers = execute(
    readDeclarations(fs.readFileSync(path.join(root, 'src/utils/utils.ts'), 'utf8'), helperNames),
    helperNames
  )
  const span = findNameSpan(baseParse(descriptor.template.content))
  assert.ok(span, '真实地名模板存在')
  const { code } = compile(span.loc.source, { mode: 'function', prefixIdentifiers: true })
  const render = new Function('Vue', code)(Vue)
  const renderName = (name, keyword) =>
    renderToString(
      Vue.createSSRApp({
        data: () => ({ data: { name, colorName: '<b>错误的旧高亮</b>' }, keyword }),
        methods: helpers,
        render
      })
    )
  const highlighted = await renderName('北京 & <img src=x>', '北京')
  assert.match(highlighted, /class="search-key-span">北京<\/span>/)
  assert.match(highlighted, /&amp; &lt;img src=x&gt;/)
  assert.doesNotMatch(highlighted, /<img|错误的旧高亮/)
  const plain = await renderName('&lt;北京&gt;', '')
  assert.match(plain, /&amp;lt;北京&amp;gt;/)
  assert.doesNotMatch(plain, /search-key-span/)
})

const deferred = () => {
  let resolve
  const promise = new Promise(yes => (resolve = yes))
  return { promise, resolve }
}
const detailFixture = () => {
  const selectedData = Vue.ref(null)
  const showGeoJson = Vue.ref(false)
  const curCustomGeoArea = Vue.reactive({ id: 'previous-custom' })
  const requests = []
  const cleared = []
  let destroys = 0
  const context = {
    selectedData,
    showGeoJson,
    curCustomGeoArea,
    customAreaTreeRef: Vue.ref({ setCurrentKey: key => cleared.push(key) }),
    mapInstance: { destroy: () => destroys++ },
    cloneDeep,
    getGeoJsonFile: id => {
      const result = deferred()
      requests.push({ id, ...result })
      return result.promise
    },
    areaTreeRef: Vue.ref({
      getNode: id => ({ data: { name: id === 'shanxi' ? '山西省' : '北京市' } })
    })
  }
  const { handleNodeClick } = execute(
    readDeclarations(descriptor.scriptSetup.content, ['handleNodeClick']),
    ['handleNodeClick'],
    context
  )
  return {
    selectedData,
    showGeoJson,
    curCustomGeoArea,
    handleNodeClick,
    requests,
    cleared,
    destroys: () => destroys
  }
}

test('详情使用独立的实际 Vue ref 对象，长 GeoJSON 不污染树或触发树深监听', async () => {
  const f = detailFixture()
  const tree = Vue.ref(areaData(12))
  const node = flatten(tree.value).find(item => item.id === 'taiyuan')
  const original = JSON.stringify(tree.value)
  let treeUpdates = 0
  const stop = Vue.watch(tree, () => treeUpdates++, { deep: true })
  try {
    const loading = f.handleNodeClick(node)
    assert.notEqual(f.selectedData.value, node)
    const geoJson = {
      type: 'FeatureCollection',
      features: [
        {
          type: 'Feature',
          properties: { name: '太原市' },
          geometry: {
            type: 'Polygon',
            coordinates: [Array.from({ length: 20000 }, (_, i) => [112 + i / 100000, 37.8])]
          }
        }
      ]
    }
    f.requests[0].resolve(geoJson)
    await loading
    await Vue.nextTick()
    assert.equal(f.selectedData.value.id, 'taiyuan')
    assert.equal(f.selectedData.value.parentName, '山西省')
    assert.deepEqual(JSON.parse(f.selectedData.value.geoJson), geoJson)
    assert.equal(f.showGeoJson.value, true)
    assert.equal(f.curCustomGeoArea.id, '')
    assert.deepEqual(f.cleared, [null])
    assert.equal(f.destroys(), 1)
    assert.equal(JSON.stringify(tree.value), original)
    assert.equal(treeUpdates, 0)
    assert.equal(Object.hasOwn(node, 'geoJson'), false)
    assert.equal(Object.hasOwn(node, 'parentName'), false)
  } finally {
    stop()
  }
})

test('太原→北京→太原快速切换后，旧同 ID 请求也不能覆盖最新详情', async () => {
  const f = detailFixture()
  const taiyuan = Vue.reactive({ id: 'taiyuan', pid: 'shanxi', name: '太原市' })
  const beijing = Vue.reactive({ id: 'beijing', pid: '156', name: '北京市' })
  const first = f.handleNodeClick(taiyuan)
  const firstDetail = f.selectedData.value
  const second = f.handleNodeClick(beijing)
  const secondDetail = f.selectedData.value
  const third = f.handleNodeClick(taiyuan)
  const latestDetail = f.selectedData.value
  f.requests[2].resolve({ version: 'latest-taiyuan' })
  await third
  f.requests[1].resolve({ version: 'late-beijing' })
  await second
  f.requests[0].resolve({ version: 'old-taiyuan' })
  await first
  assert.equal(f.selectedData.value, latestDetail)
  assert.deepEqual(JSON.parse(latestDetail.geoJson), { version: 'latest-taiyuan' })
  assert.equal(latestDetail.parentName, '山西省')
  assert.equal(firstDetail.geoJson, undefined)
  assert.equal(secondDetail.geoJson, undefined)
  assert.equal(taiyuan.geoJson, undefined)
  assert.equal(beijing.geoJson, undefined)
})
