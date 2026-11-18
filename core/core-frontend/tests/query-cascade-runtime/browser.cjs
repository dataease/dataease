const { test, expect } = require(process.env.PLAYWRIGHT_TEST_MODULE || '@playwright/test')
const fs = require('node:fs/promises')
const path = require('node:path')
const os = require('node:os')
const { pathToFileURL } = require('node:url')
const frontend = path.resolve(__dirname, '../..')
let server, dir

test.beforeAll(async () => {
  dir = await fs.realpath(await fs.mkdtemp(path.join(os.tmpdir(), 'dataease-cascade-browser-')))
  // Freeze sources for the entire run so concurrent workspace edits cannot change the tested variant.
  const snapshots = new Map()
  for (const name of ['Component.vue', 'Select.vue', 'cascade-utils.ts']) {
    const filename = path.join(frontend, 'src/custom-component/v-query', name)
    let source = await fs.readFile(filename, 'utf8')
    if (process.env.CASCADE_VARIANT === 'combined' && !source.includes('cascadeResetState')) {
      if (name === 'Component.vue') {
        source = source.replace(
          "import { getKeyList, reRenderAll } from '@/custom-component/v-query/QueryUtils'",
          "import { getKeyList, reRenderAll } from '@/custom-component/v-query/QueryUtils'\nimport { cascadeResetState } from '@/custom-component/v-query/cascade-utils'"
        )
        source = source.replace(
          'element.value.propValue = cloneDeep(oldList)',
          'element.value.propValue = cloneDeep(oldList)\n    cascadeResetState.value = true'
        )
        source = source.replace(
          '    !componentWithSure.value && queryData()\n  })\n}\n\nconst clearData',
          '    cascadeResetState.value = false\n    !componentWithSure.value && queryData()\n  })\n}\n\nconst clearData'
        )
      } else if (name === 'Select.vue') {
        source = source.replace(
          "isEmptyCascadeValue } from './cascade-utils'",
          "isEmptyCascadeValue, cascadeResetState } from './cascade-utils'"
        )
        source = source.replace(
          '  if (isCascadeParentCleared(cascade.value, config.value.id, props.isConfig)) {',
          '  const parentCleared = isCascadeParentCleared(cascade.value, config.value.id, props.isConfig)\n  if (!cascadeResetState.value || parentCleared) {'
        )
      } else {
        source += '\nexport const cascadeResetState = { value: false }\n'
      }
    }
    snapshots.set(filename, source)
    await fs.writeFile(path.join(dir, name + '.snapshot'), source)
  }
  const files = {
    'index.html': '<div id="app"></div><script type="module" src="/main.ts"></script>',
    'Empty.vue': '<template><span /></template>',
    'store.ts': `import { createPinia, defineStore } from 'pinia';
export const pinia=createPinia(); const useStore=defineStore('fixture',{state:()=>({curComponent:{id:''},canvasViewInfo:{},mobileInPc:false,firstLoadMap:[],editMode:'preview',dvInfo:{type:'dataV'},componentData:[],componentDataMultiply:[],canvasStyleData:{popupAvailable:true}}),actions:{setFirstLoadMap(v){this.firstLoadMap=v}}}); export const dvMainStoreWithOut=()=>useStore(pinia)`,
    'bridge.ts': `import {ref} from 'vue'; export const useI18n=()=>({t:k=>k}); export const snapshotStoreWithOut=()=>({recordSnapshotCache(){}});export const isMobile=()=>false;export const isISOMobile=()=>false;export const mixColor=()=>'';export const colorStringToHex=v=>v;export const getCSSVariable=()=> '#3370ff';export const comInfo=()=>({datasetFieldList:ref([{id:'rich',tableId:'ds',title:'rich'},{id:'parent-chart',tableId:'ds',title:'parent-chart'}])});export const infoFormat=v=>v;export {splitConditionValues} from '@/custom-component/v-query/condition-utils';`,
    'api.ts': `export const enumValueObj=async(data,signal)=>{const r=await fetch('/fixture/enum',{method:'POST',headers:{'content-type':'application/json'},body:JSON.stringify(data),signal});return r.json()};export const getEnumValue=async()=>[];`,
    'main.ts': `import {createApp,reactive,ref,h,onBeforeUnmount} from 'vue';
import ElementPlus from 'element-plus-secondary'; import 'element-plus-secondary/dist/index.css';
import Query from '@/custom-component/v-query/Component.vue';
import {pinia,dvMainStoreWithOut} from './store'; import {useEmitt} from '@/hooks/web/useEmitt';import {useFilter} from '@/hooks/web/useFilter';
const params=new URLSearchParams(location.search); const multiple=params.has('multiple'),withButton=params.has('button'),defaults=params.has('defaults');
const make=(id,name)=>({id,name,dataset:{id:'ds'},field:{id},displayId:id,displayType:'0',displayFormat:params.has('dropdown')?0:1,visible:true,optionValueSource:1,multiple:id==='person'&&multiple,defaultValueCheck:defaults,defaultValue:defaults?(id==='dept'?'部门A':id==='person'?(multiple?['人员甲']:'人员甲'):'项目一'):undefined,defaultMapValue:defaults?[id==='dept'?'部门A':id==='person'?'人员甲':'项目一']:[],mapValue:[],checkedFields:['rich'],checkedFieldsMap:{rich:id},parameters:[],queryConditionWidth:230,sort:'asc'});
const conditions=[make('dept','部门'),make('person','人员'),make('project','项目')];
if(params.has('split')) for(const c of conditions.filter(c=>c.id!=='person')){c.checkedFields=['parent-chart'];c.checkedFieldsMap={'parent-chart':c.id}}
const element=reactive({id:'query',component:'VQuery',innerType:'VQuery',propValue:conditions,cascade:[conditions.map(c=>({datasetId:'ds--'+c.id+'--'+c.id,selectValue:c.defaultMapValue}))]});
const view={customStyle:{component:{btnList:withButton?['reset','clear','sure']:['reset','clear'],queryConditionWidth:230,labelShow:true,layout:'vertical',queryConditionHeight:32}}};
const store=dvMainStoreWithOut();store.componentData=[element];
createApp({setup(){const result=ref('等待查询'),count=ref(0),last=ref([]);let generation=0;const emitter=useEmitt().emitter;const query=async()=>{const version=++generation;const {filter}=useFilter('rich',false,'preview');last.value=filter;count.value++;const r=await fetch('/fixture/chart',{method:'POST',headers:{'content-type':'application/json'},body:JSON.stringify({customFilter:filter})});const data=await r.json();if(version===generation)result.value=data.text};emitter.on('query-data-rich',query);onBeforeUnmount(()=>emitter.off('query-data-rich',query));return()=>h('main',[h(Query,{element,view,showPosition:'preview'}),h('output',{'data-testid':'richtext'},result.value),h('pre',{'data-testid':'filters'},JSON.stringify(last.value)),h('pre',{'data-testid':'state'},JSON.stringify(element.propValue.map(c=>({id:c.id,value:c.selectValue,map:c.mapValue})))),h('span',{'data-testid':'query-count'},count.value)])}}).use(pinia).use(ElementPlus,{namespace:'ed'}).component('Icon',{render:()=>h('span')}).mount('#app');`
  }
  await Promise.all(
    Object.entries(files).map(([name, content]) => fs.writeFile(path.join(dir, name), content))
  )
  const { createServer } = await import(
    pathToFileURL(path.join(frontend, 'node_modules/vite/dist/node/index.js'))
  )
  const { default: vue } = await import(
    pathToFileURL(require.resolve('@vitejs/plugin-vue', { paths: [frontend] }))
  )
  const bridge = path.join(dir, 'bridge.ts'),
    empty = path.join(dir, 'Empty.vue')
  const aliases = [
    ...[
      '@/hooks/web/useI18n',
      '@/store/modules/data-visualization/snapshot',
      '@/utils/utils',
      '@/utils/color',
      './com-info',
      './options'
    ].map(find => ({ find, replacement: bridge })),
    { find: '@/store/modules/data-visualization/dvMain', replacement: path.join(dir, 'store.ts') },
    { find: '@/api/dataset', replacement: path.join(dir, 'api.ts') },
    ...[
      './QueryConditionConfiguration.vue',
      './Time.vue',
      './TextSearch.vue',
      './NumberInput.vue',
      './Tree.vue'
    ].map(find => ({ find, replacement: empty })),
    { find: '@', replacement: path.join(frontend, 'src') },
    {
      find: /^vue$/,
      replacement: require.resolve('vue/dist/vue.runtime.esm-bundler.js', { paths: [frontend] })
    },
    {
      find: /^pinia$/,
      replacement: require.resolve('pinia/dist/pinia.mjs', { paths: [frontend] })
    },
    {
      find: /^element-plus-secondary$/,
      replacement: path.join(frontend, 'node_modules/element-plus-secondary/es/index.mjs')
    },
    {
      find: 'element-plus-secondary/',
      replacement: path.join(frontend, 'node_modules/element-plus-secondary/')
    }
  ]
  server = await createServer({
    configFile: false,
    root: dir,
    cacheDir: path.join(dir, 'cache'),
    resolve: { alias: aliases, dedupe: ['vue', 'pinia'] },
    plugins: [
      {
        name: 'frozen-query-sources',
        enforce: 'pre',
        load(id) {
          return snapshots.get(id)
        }
      },
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
    server: { host: '127.0.0.1', port: 7098, strictPort: true, fs: { allow: [dir, frontend] } }
  })
  await server.listen()
})
test.afterAll(async () => {
  await server?.close()
  if (dir) await fs.rm(dir, { recursive: true, force: true })
})
const records = [
  { dept: '部门A', person: '人员甲', project: '项目一' },
  { dept: '部门A', person: '人员乙', project: '项目二' },
  { dept: '部门B', person: '人员丙', project: '项目三' }
]
const open = async (page, query = '') => {
  const errors = []
  page.on('pageerror', e => errors.push(e.message))
  await page.route('**/fixture/enum', async route => {
    const data = route.request().postDataJSON()
    let filtered = records.filter(r =>
      (data.filter || []).every(f => !f.value?.length || f.value.includes(r[f.fieldId]))
    )
    if (
      query.includes('empty') &&
      data.queryId === 'person' &&
      (data.filter || []).some(f => f.value?.includes('部门B'))
    )
      filtered = []
    if ((data.filter || []).some(f => f.value?.includes('部门A')))
      await new Promise(r => setTimeout(r, 180))
    await route.fulfill({ json: filtered })
  })
  await page.route('**/fixture/chart', async route => {
    const data = route.request().postDataJSON()
    const filtered = records.filter(r =>
      (data.customFilter || []).every(f => !f.value?.length || f.value.includes(r[f.fieldId]))
    )
    await route.fulfill({ json: { text: filtered.map(r => r.person).join('、') || '---' } })
  })
  await page.goto('http://127.0.0.1:7098/' + query)
  if (query.includes('dropdown'))
    await expect(page.locator('.query-item').first().getByRole('combobox')).toBeVisible()
  else
    await expect(
      page.locator('.query-item').first().getByText('部门A', { exact: true })
    ).toBeVisible()
  return errors
}
const choose = (page, index, label) =>
  page.locator('.query-item').nth(index).getByText(label, { exact: true }).click()
const state = page => page.getByTestId('state').textContent().then(JSON.parse)

test('连续三级级联：变更部门后无旧人员、旧项目，富文本查询按有效条件恢复', async ({ page }) => {
  const errors = await open(page)
  await choose(page, 0, '部门A')
  await choose(page, 1, '人员甲')
  await choose(page, 2, '项目一')
  await expect(page.getByTestId('richtext')).toHaveText('人员甲')
  await choose(page, 0, '部门B')
  await expect(page.getByTestId('richtext')).toHaveText('人员丙')
  await expect
    .poll(async () => {
      const s = await state(page)
      return s.slice(1).every(c => !c.value?.length && !c.map?.length)
    })
    .toBe(true)
  await choose(page, 1, '人员丙')
  await choose(page, 2, '项目三')
  await expect(page.getByTestId('richtext')).toHaveText('人员丙')
  expect(errors).toEqual([])
})
test('默认值重置：改选后反复重置均恢复完整三级默认值', async ({ page }) => {
  const errors = await open(page, '?defaults')
  await choose(page, 0, '部门B')
  await choose(page, 1, '人员丙')
  await choose(page, 2, '项目三')
  for (let i = 0; i < 2; i++) {
    await page.getByRole('button', { name: 'chart.reset', exact: true }).click()
    await expect
      .poll(async () => {
        const s = await state(page)
        return s.map(c => c.value)
      })
      .toEqual(['部门A', '人员甲', '项目一'])
    await expect(page.getByTestId('richtext')).toHaveText('人员甲')
    await page.waitForTimeout(800)
    expect((await state(page)).map(c => c.value)).toEqual(['部门A', '人员甲', '项目一'])
  }
  expect(errors).toEqual([])
})
test('查询按钮：级联改选与重置只更新条件，点击查询后才更新结果', async ({ page }) => {
  const errors = await open(page, '?defaults&button')
  await page.waitForTimeout(900)
  const count = await page.getByTestId('query-count').textContent()
  await choose(page, 0, '部门B')
  await choose(page, 1, '人员丙')
  await page.waitForTimeout(500)
  expect(await page.getByTestId('query-count').textContent()).toBe(count)
  await page.getByRole('button', { name: 'commons.adv_search.search', exact: true }).click()
  await expect(page.getByTestId('richtext')).toHaveText('人员丙')
  const after = await page.getByTestId('query-count').textContent()
  await page.getByRole('button', { name: 'chart.reset', exact: true }).click()
  await page.waitForTimeout(900)
  expect(await page.getByTestId('query-count').textContent()).toBe(after)
  await page.getByRole('button', { name: 'commons.adv_search.search', exact: true }).click()
  await expect(page.getByTestId('richtext')).toHaveText('人员甲')
  expect(errors).toEqual([])
})
test('快速改选：延迟的旧枚举响应不能覆盖新部门选项', async ({ page }) => {
  const errors = await open(page)
  await choose(page, 0, '部门A')
  await page.waitForTimeout(70)
  await choose(page, 0, '部门B')
  await expect(
    page.locator('.query-item').nth(1).getByText('人员丙', { exact: true })
  ).toBeVisible()
  await page.waitForTimeout(600)
  await expect(page.locator('.query-item').nth(1).getByText('人员甲', { exact: true })).toHaveCount(
    0
  )
  await expect(page.getByTestId('richtext')).toHaveText('人员丙')
  expect(errors).toEqual([])
})

test('多选子级：保留多项筛选，改选部门后清空并可继续选择', async ({ page }) => {
  const errors = await open(page, '?multiple')
  await choose(page, 0, '部门A')
  await choose(page, 1, '人员甲')
  await choose(page, 1, '人员乙')
  await expect(page.getByTestId('richtext')).toHaveText('人员甲、人员乙')
  await expect
    .poll(async () => Array.from((await state(page))[1].value))
    .toEqual(['人员甲', '人员乙'])
  await choose(page, 0, '部门B')
  await expect(page.getByTestId('richtext')).toHaveText('人员丙')
  expect((await state(page))[1].value).toEqual([])
  await choose(page, 1, '人员丙')
  await expect.poll(async () => Array.from((await state(page))[1].value)).toEqual(['人员丙'])
  expect(errors).toEqual([])
})
test('清空上级和全部清空：下级条件消失，恢复查询全集', async ({ page }) => {
  const errors = await open(page)
  await choose(page, 0, '部门A')
  await choose(page, 1, '人员甲')
  await choose(page, 2, '项目一')
  await expect(page.getByTestId('richtext')).toHaveText('人员甲')
  await choose(page, 0, '部门A')
  await expect(page.getByTestId('richtext')).toHaveText('人员甲、人员乙、人员丙')
  await expect
    .poll(async () => (await state(page)).every(c => !c.value?.length && !c.map?.length))
    .toBe(true)
  await choose(page, 0, '部门B')
  await choose(page, 1, '人员丙')
  await expect(page.getByTestId('richtext')).toHaveText('人员丙')
  await page.getByRole('button', { name: 'commons.clear', exact: true }).click()
  await expect(page.getByTestId('richtext')).toHaveText('人员甲、人员乙、人员丙')
  expect(errors).toEqual([])
})
test('实际单选下拉框：改选部门后富文本请求只携带新部门', async ({ page }) => {
  const errors = await open(page, '?dropdown')
  const pick = async (index, label) => {
    await page.locator('.query-item').nth(index).getByRole('combobox').click()
    await page.getByText(label, { exact: true }).last().click()
  }
  await pick(0, '部门A')
  await pick(1, '人员甲')
  await pick(2, '项目一')
  await expect(page.getByTestId('richtext')).toHaveText('人员甲')
  await pick(0, '部门B')
  await expect(page.getByTestId('richtext')).toHaveText('人员丙')
  expect((await state(page)).slice(1).every(c => !c.value?.length && !c.map?.length)).toBe(true)
  expect(errors).toEqual([])
})

test('下级枚举为空：页面清空失效子级，查询不残留旧人员和旧项目', async ({ page }) => {
  const errors = await open(page, '?empty')
  await choose(page, 0, '部门A')
  await choose(page, 1, '人员甲')
  await choose(page, 2, '项目一')
  await expect(page.getByTestId('richtext')).toHaveText('人员甲')
  await choose(page, 0, '部门B')
  await expect(page.getByTestId('richtext')).toHaveText('人员丙')
  await page.waitForTimeout(500)
  await expect(page.locator('.query-item').nth(1).locator('.select-item')).toHaveCount(0)
  expect((await state(page)).slice(1).every(c => !c.value?.length && !c.map?.length)).toBe(true)
  expect(errors).toEqual([])
})

for (const button of [false, true]) {
  test(`上下级绑定不同图表：清空下级后通知其图表，并遵循查询按钮（${button}）`, async ({
    page
  }) => {
    const errors = await open(page, '?split' + (button ? '&button' : ''))
    await choose(page, 0, '部门A')
    await choose(page, 1, '人员甲')
    if (button)
      await page.getByRole('button', { name: 'commons.adv_search.search', exact: true }).click()
    await expect(page.getByTestId('richtext')).toHaveText('人员甲')
    const count = await page.getByTestId('query-count').textContent()
    await choose(page, 0, '部门B')
    await expect.poll(async () => (await state(page))[1].value ?? '').toBe('')
    if (button) {
      await page.waitForTimeout(600)
      expect(await page.getByTestId('query-count').textContent()).toBe(count)
      await expect(page.getByTestId('richtext')).toHaveText('人员甲')
      await page.getByRole('button', { name: 'commons.adv_search.search', exact: true }).click()
    }
    await expect(page.getByTestId('richtext')).toHaveText('人员甲、人员乙、人员丙')
    expect(errors).toEqual([])
  })
}
