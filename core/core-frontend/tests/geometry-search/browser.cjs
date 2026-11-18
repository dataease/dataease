/* eslint-disable @typescript-eslint/no-var-requires -- 隔离 Chrome 回归，挂载真实地理信息组件。 */
const { test, expect } = require(process.env.PLAYWRIGHT_TEST_MODULE || '@playwright/test')
const fs = require('node:fs/promises')
const path = require('node:path')
const os = require('node:os')
const { pathToFileURL } = require('node:url')
const ts = require('typescript')
const frontend = path.resolve(__dirname, '../..')
const url = 'http://127.0.0.1:7134'
let server, directory

test.beforeAll(async () => {
  directory = await fs.realpath(
    await fs.mkdtemp(path.join(os.tmpdir(), 'dataease-geometry-search-'))
  )
  const write = (name, content) => fs.writeFile(path.join(directory, name), content)
  const utils = ts.createSourceFile(
    'utils.ts',
    await fs.readFile(path.join(frontend, 'src/utils/utils.ts'), 'utf8'),
    ts.ScriptTarget.Latest,
    true
  )
  const highlight = utils.statements.find(
    node =>
      ts.isVariableStatement(node) &&
      node.declarationList.declarations.some(d => d.name.getText(utils) === 'getHighlightSegments')
  )
  if (!highlight) throw new Error('Missing production getHighlightSegments')
  await write('utils.ts', highlight.getText(utils))
  await write('Empty.vue', '<template><span /></template>')
  await write('index.html', '<div id="app"></div><script type="module" src="/main.ts"></script>')
  await write(
    'i18n.ts',
    `const messages={'commons.search':'搜索','online_map.geometry':'地理信息','system.coordinate_file':'坐标文件','system.custom_area':'自定义区域'}; export const useI18n=()=>({t:key=>messages[key]||key});`
  )
  await write(
    'api.ts',
    `
export const fixtureTree={id:'000',name:'世界村',children:[{id:'156',name:'中国',children:[
 {id:'156110000',pid:'156',name:'北京市',children:[{id:'156110100',pid:'156110000',name:'朝阳区'}]},
 {id:'156140000',pid:'156',name:'山西省',children:[{id:'156140100',pid:'156140000',name:'太原市'}]},
 {id:'unsafe',pid:'156',name:'<img src=x onerror="window.geometryXss=1"> &amp; 北京'},
 {id:'english',pid:'156',name:'Beijing'},
 ...Array.from({length:1000},(_,i)=>({id:'leaf-'+i,pid:'156',name:'其他区域'+i}))
]}]};
export const getWorldTree=async()=>({data:fixtureTree});
export const listCustomGeoArea=async()=>({data:[]});
export const listSubAreaOptions=async()=>({data:[]});
export const saveCustomGeoArea=async()=>({code:0});
export const saveCustomGeoSubArea=async()=>({code:0});
export const deleteCustomGeoArea=async()=>({code:0});
export const getCustomGeoArea=async()=>({data:[]});
export const deleteCustomGeoSubArea=async()=>({code:0});
export const savePlaceNameMapping=async()=>({code:0});
`
  )
  await write(
    'geo.ts',
    `export const getGeoJsonFile=async id=>({type:'FeatureCollection',area:id,features:[{type:'Feature',properties:{name:'太原市'},geometry:{type:'Polygon',coordinates:[Array.from({length:12000},(_,i)=>[112+i/100000,37+i/100000])]}}]});`
  )
  await write('cache.ts', 'export const useCache=()=>({wsCache:{get:()=>null,set:()=>undefined}});')
  await write('request.ts', 'export default {post:async()=>({code:0})};')
  await write('map-store.ts', 'export const useMapStoreWithOut=()=>({mapCache:{}});')
  await write('plot.ts', 'export class Choropleth{};export class TextLayer{};')
  await write(
    'main.ts',
    `import {createApp,h} from 'vue';import ElementPlus from 'element-plus-secondary';import 'element-plus-secondary/dist/index.css';import Geometry from '${frontend}/src/views/system/parameter/map/Geometry.vue';createApp({render:()=>h(Geometry)}).use(ElementPlus,{namespace:'ed'}).component('Icon',{render:()=>h('span')}).mount('#app');`
  )
  const { createServer } = await import(
    pathToFileURL(path.join(frontend, 'node_modules/vite/dist/node/index.js'))
  )
  const { default: vue } = await import(
    pathToFileURL(require.resolve('@vitejs/plugin-vue', { paths: [frontend] }))
  )
  const empty = path.join(directory, 'Empty.vue')
  server = await createServer({
    configFile: false,
    root: directory,
    cacheDir: path.join(directory, 'cache'),
    resolve: {
      alias: [
        ...[
          ['@/hooks/web/useI18n', 'i18n.ts'],
          ['@/api/map', 'api.ts'],
          ['@/views/chart/components/js/util', 'geo.ts'],
          ['@/utils/utils', 'utils.ts'],
          ['@/hooks/web/useCache', 'cache.ts'],
          ['@/config/axios', 'request.ts'],
          ['@/store/modules/map', 'map-store.ts']
        ].map(([find, file]) => ({ find, replacement: path.join(directory, file) })),
        { find: './GeometryEdit.vue', replacement: empty },
        { find: '@/views/system/parameter/map/PlaceNameMapping.vue', replacement: empty },
        { find: '@/components/empty-background/src/EmptyBackground.vue', replacement: empty },
        { find: /^@antv\/l7plot(?:\/.*)?$/, replacement: path.join(directory, 'plot.ts') },
        { find: '@', replacement: path.join(frontend, 'src') },
        {
          find: /^vue$/,
          replacement: require.resolve('vue/dist/vue.runtime.esm-bundler.js', { paths: [frontend] })
        },
        {
          find: /^element-plus-secondary$/,
          replacement: path.join(frontend, 'node_modules/element-plus-secondary/es/index.mjs')
        },
        {
          find: 'element-plus-secondary/',
          replacement: path.join(frontend, 'node_modules/element-plus-secondary/')
        }
      ],
      dedupe: ['vue']
    },
    plugins: [
      vue(),
      {
        name: 'fixture-svg',
        enforce: 'pre',
        resolveId(id) {
          if (id.endsWith('.svg')) return '\0fixture-svg'
        },
        load(id) {
          if (id === '\0fixture-svg') return 'export default {render(){return null}}'
        }
      }
    ],
    server: {
      host: '127.0.0.1',
      port: 7134,
      strictPort: true,
      fs: { allow: [directory, frontend] }
    }
  })
  await server.listen()
})

test.afterAll(async () => {
  await server?.close()
  if (directory) await fs.rm(directory, { recursive: true, force: true })
})

const row = (page, name) =>
  page.locator('.ed-tree-node__content').filter({ has: page.getByText(name, { exact: true }) })
const expand = (page, name) => row(page, name).locator('.ed-tree-node__expand-icon').click()
const filter = async (page, value) => {
  const input = page.getByPlaceholder('搜索')
  await input.fill(value)
  await input.press('Enter')
}

test('选太原后回车搜索北京、清空与重复过滤保持响应和详情', async ({ page }) => {
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  await page.goto(url)
  await expand(page, '世界村')
  await expand(page, '中国')
  await expand(page, '山西省')
  await row(page, '太原市').click()
  await expect(page.locator('.area-content-geo')).toContainText('FeatureCollection')
  await expect(page.locator('.area-content-geo')).toContainText('156140100')
  await filter(page, '北京')
  await expect(row(page, '北京市')).toBeVisible()
  await expect(row(page, '太原市')).toBeHidden()
  await expect(
    page
      .locator('.search-key-span')
      .filter({ hasText: /^北京$/ })
      .first()
  ).toBeVisible()
  await expect(page.locator('.area-content-geo')).toContainText('156140100')
  for (const query of ['无此区域', '', '北京', 'beIJING', '']) {
    await filter(page, query)
    if (query === '无此区域') await expect(row(page, '中国')).toBeHidden()
    if (query === 'beIJING') await expect(row(page, 'Beijing')).toBeVisible()
    if (query === '') await expect(row(page, '山西省')).toBeVisible()
  }
  await expect(page.locator('.area-content-geo')).toContainText('156140100')
  expect(errors).toEqual([])
})

test('搜索高亮只渲染文本，不解析节点名中的 HTML 或实体', async ({ page }) => {
  const name = '<img src=x onerror="window.geometryXss=1"> &amp; 北京'
  await page.goto(url)
  await filter(page, '北京')
  const label = page.locator('.geo-name-span').filter({ hasText: name })
  await expect(label).toBeVisible()
  await expect(label).toHaveText(name)
  await expect(label.locator('img')).toHaveCount(0)
  await filter(page, '')
  await expect(label).toHaveText(name)
  expect(await page.evaluate(() => window.geometryXss)).toBeUndefined()
})
