/* eslint-disable @typescript-eslint/no-var-requires -- 完整生产配置弹窗的隔离浏览器回归。 */
const { test, expect } = require(process.env.PLAYWRIGHT_TEST_MODULE || '@playwright/test')
const fs = require('node:fs/promises')
const path = require('node:path')
const os = require('node:os')
const { pathToFileURL } = require('node:url')
const frontend = path.resolve(__dirname, '../..')
let server, directory

test.beforeAll(async () => {
  directory = await fs.realpath(
    await fs.mkdtemp(path.join(os.tmpdir(), 'dataease-query-full-dialog-'))
  )
  await fs.writeFile(
    path.join(directory, 'store.ts'),
    `import {createPinia,defineStore} from 'pinia';import {ref} from 'vue';
export const pinia=createPinia();export const snapshots=ref([]);
const useStore=defineStore('query-dialog',{state:()=>({componentData:[{id:'chart-a',component:'view',innerType:'view'},{id:'chart-b',component:'view',innerType:'view'}],canvasViewInfo:{'chart-a':{id:'chart-a',title:'图表甲',tableId:'ds',type:'bar'},'chart-b':{id:'chart-b',title:'图表乙',tableId:'ds',type:'bar'}},dvInfo:{type:'dashboard'},mobileInPc:false,editMode:'edit',firstLoadMap:[]})});export const dvMainStoreWithOut=()=>useStore(pinia);export const snapshotStoreWithOut=()=>({recordSnapshotCache:v=>snapshots.value.push(v)});`
  )
  await fs.writeFile(
    path.join(directory, 'bridge.ts'),
    `import messages from '${frontend}/src/locales/zh-CN';
export const t=k=>k.split('.').reduce((o,key)=>o?.[key],messages)??k;export const useI18n=()=>({t});
export const useCache=()=>({wsCache:{get(){return undefined},set(){}}});
export const isMobile=()=>false,isISOMobile=()=>false;let sequence=0;export const guid=()=>String(++sequence);
export const mixColor=()=>'',colorStringToHex=v=>v,getCSSVariable=()=> '#3370ff';
export const iconChartMap={},iconFieldMap={},Tree={};`
  )
  await fs.writeFile(
    path.join(directory, 'api.ts'),
    `import {cloneDeep} from 'lodash-es';
const f=(id,name,deType=0)=>({id,name,originName:name,deType,type:deType===1?'YYYY-MM-DD HH:mm:ss':deType===2?'INTEGER':'VARCHAR',groupType:deType===2?'q':'d',datasetGroupId:'ds',params:[]});
export const dimensions=[f('country','地区'),f('city','城市'),f('created','创建时间',1)],quotas=[f('amount','金额',2)];
export const parameters=[['num-start','数值开始',2,[2,'INTEGER']],['num-end','数值结束',2,[2,'INTEGER']],['date-start','日期开始',1,[1,'YYYY-MM-DD HH:mm:ss']],['date-end','日期结束',1,[1,'YYYY-MM-DD HH:mm:ss']]].map(([id,variableName,deType,type])=>({id,variableName,name:variableName,deType,type,datasetGroupId:'ds',params:[]}));
export const getDatasetTree=async()=>[{id:'0',name:'根目录',children:[{id:'ds',name:'数据集一',leaf:true},{id:'ds2',name:'数据集二',leaf:true}]}];
export const getDsDetailsWithPerm=async()=>[{id:'ds',name:'数据集一',fields:cloneDeep({dimensionList:dimensions,quotaList:quotas})}];
export const getSqlParams=async()=>cloneDeep(parameters);
export const getFieldByDQ=async()=>cloneDeep({dimensionList:dimensions,quotaList:quotas});
export const listFieldsWithPermissions=async()=>({data:cloneDeep([...dimensions,...quotas])});
const queued=(kind,data,signal,fallback)=>{if(!window.__holdCandidates)return fallback;return new Promise(resolve=>{const item={kind,data,aborted:signal?.aborted||false,resolve};signal?.addEventListener('abort',()=>{item.aborted=true},{once:true});(window.__candidateRequests ||= []).push(item)})};
export const enumValueObj=async(data,signal)=>{window.__fixtureOptionsLoaded=(window.__fixtureOptionsLoaded||0)+1;return queued('select',data,signal,[{country:'华东',city:'上海'},{country:'华北',city:'北京'}])};
export const getEnumValue=async()=>{window.__fixtureOptionsLoaded=(window.__fixtureOptionsLoaded||0)+1;return ['华东','华北']};export const getFieldTree=async(data,query)=>queued('tree',data,query?.signal,[{id:'华东',text:'华东',children:[{id:'上海',text:'上海'}]},{id:'华北',text:'华北',children:[{id:'北京',text:'北京'}]}]);
export const listByDsIds=async()=>({ds:cloneDeep([...dimensions,...quotas])});`
  )
  await fs.writeFile(
    path.join(directory, 'App.vue'),
    `<script setup lang="ts">
import {useEmitt} from '@/hooks/web/useEmitt';
window.__repeatCandidate=()=>useEmitt().emitter.emit('condition-a-select');
import {ref,computed,nextTick} from 'vue';import {cloneDeep} from 'lodash-es';
import Configuration from '@/custom-component/v-query/QueryConditionConfiguration.vue';import {infoFormat} from '@/custom-component/v-query/options';import {dimensions,parameters} from './api';import {snapshots} from './store';
const mode=new URLSearchParams(location.search).get('mode')||'text';
const make=(id,name)=>({...infoFormat({id:'country',name:'地区',deType:0,type:'VARCHAR',datasetId:'ds'}),id,name,checkedFields:['chart-a'],checkedFieldsMap:{'chart-a':'country','chart-b':'city'},auto:false,dataset:{id:'ds',fields:cloneDeep(dimensions)},field:cloneDeep(dimensions[0]),displayId:'country',sortId:'country',sort:'asc',optionValueSource:1,showError:false,defaultValueCheck:false,treeCheckedList:[],treeFieldList:[],historical:{zero:0,flag:false,text:'',value:null}});
const first=make('condition-a','主查询'),second=make('condition-b','级联查询');second.field=cloneDeep(dimensions[1]);second.displayId='city';second.checkedFieldsMap['chart-a']='city';
if(mode==='number'||mode==='time'){
const numeric=mode==='number',start=numeric?'num-start':'date-start',end=numeric?'num-end':'date-end';
first.displayType=numeric?'22':'7';first.field=cloneDeep(parameters.find(p=>p.id===start));first.parameters=cloneDeep(parameters.filter(p=>p.id===start||p.id===end));first.checkedFieldsMap['chart-a']=start;
Object.assign(first,numeric?{checkedFieldsMapArrNum:{'chart-a':[start,end]},checkedFieldsMapStartNum:{'chart-a':start},checkedFieldsMapEndNum:{'chart-a':end}}:{checkedFieldsMapArr:{'chart-a':[start,end]},checkedFieldsMapStart:{'chart-a':start},checkedFieldsMapEnd:{'chart-a':end},timeGranularity:'datetime',timeGranularityMultiple:'datetimerange'});
}
if(mode==='tree'){
first.displayType='9';first.treeDatasetId='ds';first.treeFieldList=cloneDeep(dimensions.slice(0,2));first.treeCheckedList=first.treeFieldList.map(f=>({checkedFields:['chart-a'],checkedFieldsMap:{'chart-a':f.id,'chart-b':'city'}}));
}
const initial={id:'query',propValue:[first,second],cascade:mode==='cascade'?[[{datasetId:'ds--condition-a--country',fieldId:'country',selectValue:[],currentSelectValue:[]},{datasetId:'ds--condition-b--city',fieldId:'city',selectValue:[],currentSelectValue:[]}]]:[]};
const element=ref(cloneDeep(initial)),config=ref(),events=ref([]),mounted=ref(true);
const open=()=>config.value.setCondition('condition-a');
const persist=async()=>{sessionStorage.setItem('saved-config',JSON.stringify(element.value));mounted.value=false;await nextTick();element.value=JSON.parse(sessionStorage.getItem('saved-config'));mounted.value=true;await nextTick();open()};
const saved=computed(()=>JSON.stringify(element.value));
</script>
<template><div><el-button @click="open">打开完整配置</el-button><el-button @click="persist">序列化并重新挂载</el-button><Configuration v-if="mounted" ref="config" :query-element="element" @query-data="events.push('query')" @re-render-all="events.push('render')"/><pre data-testid="saved">{{saved}}</pre><output data-testid="snapshots">{{snapshots.length}}</output><output data-testid="events">{{JSON.stringify(events)}}</output></div></template>`
  )
  await fs.writeFile(
    path.join(directory, 'main.ts'),
    `import {createApp} from 'vue';import ElementPlus from 'element-plus-secondary';import zhCn from 'element-plus-secondary/es/locale/lang/zh-cn';import 'element-plus-secondary/dist/index.css';import '${frontend}/src/style/index.less';import App from './App.vue';import {pinia} from './store';import {t} from './bridge';const app=createApp(App);app.config.globalProperties.$t=t;app.component('Icon',{template:'<span><slot/></span>'});app.component('HandleMore',{template:'<span/>'});app.use(pinia).use(ElementPlus,{namespace:'ed',locale:zhCn}).mount('#app');`
  )
  await fs.writeFile(
    path.join(directory, 'index.html'),
    '<div id="app"></div><script type="module" src="/main.ts"></script>'
  )
  const { createServer } = await import(
    pathToFileURL(path.join(frontend, 'node_modules/vite/dist/node/index.js'))
  )
  const { default: vue } = await import(
    pathToFileURL(require.resolve('@vitejs/plugin-vue', { paths: [frontend] }))
  )
  const bridge = path.join(directory, 'bridge.ts'),
    store = path.join(directory, 'store.ts'),
    api = path.join(directory, 'api.ts')
  server = await createServer({
    configFile: false,
    root: directory,
    cacheDir: path.join(directory, '.vite'),
    resolve: {
      alias: [
        ...[
          '@/hooks/web/useI18n',
          '@/hooks/web/useCache',
          '@/utils/utils',
          '@/utils/color',
          '@/views/visualized/data/dataset/form/util.js',
          '@/components/icon-group/chart-list',
          '@/components/icon-group/field-list',
          '@/views/visualized/data/dataset/form/CreatDsGroup.vue'
        ].map(find => ({ find, replacement: bridge })),
        ...[
          '@/store/modules/data-visualization/dvMain',
          '@/store/modules/data-visualization/snapshot'
        ].map(find => ({ find, replacement: store })),
        ...['@/api/dataset', '@/api/chart'].map(find => ({ find, replacement: api })),
        { find: '@', replacement: path.join(frontend, 'src') },
        { find: /^vue$/, replacement: require.resolve('vue/dist/vue.runtime.esm-bundler.js') },
        { find: /^pinia$/, replacement: require.resolve('pinia/dist/pinia.mjs') },
        {
          find: /^element-plus-secondary$/,
          replacement: path.join(frontend, 'node_modules/element-plus-secondary/es/index.mjs')
        },
        {
          find: 'element-plus-secondary/',
          replacement: path.join(frontend, 'node_modules/element-plus-secondary/')
        },
        { find: 'lodash-es', replacement: path.join(frontend, 'node_modules/lodash-es/lodash.js') }
      ],
      dedupe: ['vue', 'pinia', 'element-plus-secondary']
    },
    plugins: [
      vue(),
      {
        name: 'query-fixture-icons',
        enforce: 'pre',
        resolveId(id) {
          if (id.endsWith('.svg')) return '\0query-fixture-svg'
        },
        load(id) {
          if (id === '\0query-fixture-svg') return 'export default {render(){return null}}'
        }
      }
    ],
    css: {
      preprocessorOptions: {
        less: {
          javascriptEnabled: true,
          additionalData: '@import "' + path.join(frontend, 'src/style/variable.less') + '";'
        }
      }
    },
    server: {
      host: '127.0.0.1',
      port: 7132,
      strictPort: true,
      fs: { allow: [directory, frontend] }
    },
    logLevel: 'error'
  })
  await server.listen()
})
test.afterAll(async () => {
  await server?.close()
  if (directory) await fs.rm(directory, { recursive: true, force: true })
})
test.beforeEach(async ({ page }) => {
  page.setDefaultTimeout(10000)
  page.fixtureErrors = []
  page.on('pageerror', error => page.fixtureErrors.push(error.message))
})
test.afterEach(async ({ page }) => expect(page.fixtureErrors).toEqual([]))

const readSaved = async page => JSON.parse(await page.getByTestId('saved').innerText())
const openFull = async (page, mode = 'text') => {
  await page.goto('http://127.0.0.1:7132/?mode=' + mode)
  await page.getByRole('button', { name: '打开完整配置', exact: true }).click()
  const dialog = page.locator('.query-condition-configuration')
  await expect(dialog.locator('.chart-field')).toContainText('图表甲')
  return dialog
}
const confirmFull = async (page, dialog) => {
  await dialog
    .locator('.ed-dialog__footer')
    .getByRole('button', { name: '确认', exact: true })
    .click()
  await expect(dialog).toBeHidden()
  return (await readSaved(page)).propValue[0]
}
const reopenSerialized = async page => {
  await page.getByRole('button', { name: '序列化并重新挂载', exact: true }).click()
  const dialog = page.locator('.query-condition-configuration')
  await expect(dialog).toBeVisible()
  await expect(dialog.locator('.chart-field')).toContainText('图表甲')
  return dialog
}
const choose = async (page, control, name) => {
  await control.press('ArrowDown')
  await page.getByRole('option', { name, exact: true }).filter({ visible: true }).click()
}
const editRangeEndpoint = async (page, dialog, parameter, position) => {
  await dialog.locator('.chart-field .ed-select').getByRole('combobox').press('ArrowDown')
  const dropdown = page.locator('.field-select--dqp:visible')
  await dropdown
    .getByRole('option')
    .filter({ hasText: parameter })
    .locator('.range-time_setting')
    .click()
  const parameterDialog = page.getByRole('dialog', { name: parameter, exact: true })
  await expect(parameterDialog).toBeVisible()
  await parameterDialog.getByText(position, { exact: true }).click()
  await parameterDialog.getByRole('button', { name: /^(确认|确定)$/, exact: true }).click()
  await expect(parameterDialog).toBeHidden()
}

test('real select shares repeated pending cascade loads and aborts on manual source switch', async ({
  page
}) => {
  const dialog = await openFull(page)
  await page.evaluate(() => {
    window.__holdCandidates = true
  })
  const panel = dialog.locator('.condition-configuration')
  await panel.getByText('设置默认值', { exact: true }).click()
  await expect.poll(() => page.evaluate(() => window.__candidateRequests?.length || 0)).toBe(1)
  await page.clock.install()
  await page.evaluate(() => {
    window.__repeatCandidate()
    window.__repeatCandidate()
  })
  await page.clock.runFor(400)
  expect(await page.evaluate(() => window.__candidateRequests.length)).toBe(1)
  expect(await page.evaluate(() => window.__candidateRequests[0].aborted)).toBe(false)
  await panel.getByText('手动输入', { exact: true }).click()
  await expect.poll(() => page.evaluate(() => window.__candidateRequests[0].aborted)).toBe(true)
  await page.evaluate(() => window.__candidateRequests[0].resolve([{ country: '过期候选' }]))
  await expect(panel.locator('.parameters')).not.toContainText('过期候选')
  await expect(panel.locator('.ed-loading-mask')).toBeHidden()
})

test('real select ignores late old-field responses and keeps loading for the latest field', async ({
  page
}) => {
  const dialog = await openFull(page)
  await page.evaluate(() => {
    window.__holdCandidates = true
  })
  const panel = dialog.locator('.condition-configuration')
  await panel.getByText('设置默认值', { exact: true }).click()
  await expect.poll(() => page.evaluate(() => window.__candidateRequests?.length || 0)).toBe(1)
  await choose(page, panel.locator('.search-field').first().getByRole('combobox'), '城市')
  await expect.poll(() => page.evaluate(() => window.__candidateRequests[0].aborted)).toBe(true)
  // 切换字段沿用原逻辑清空默认值设置，再显式启用新字段的预览。
  const defaultCheckbox = panel.getByRole('checkbox', { name: '设置默认值', exact: true })
  if (!(await defaultCheckbox.isChecked())) await defaultCheckbox.check()
  await expect.poll(() => page.evaluate(() => window.__candidateRequests?.length || 0)).toBe(2)
  await page.evaluate(() => window.__candidateRequests[0].resolve([{ country: '过期候选' }]))
  await expect(panel.locator('.parameters .ed-loading-mask')).toBeVisible()
  await page.evaluate(() =>
    window.__candidateRequests[1].resolve([{ country: '新城市', city: 'city-id' }])
  )
  await expect(panel.locator('.parameters .ed-loading-mask')).toBeHidden()
  await panel.locator('.parameters .ed-select__wrapper').click()
  const options = page.locator('.filter-select-popper_class:visible')
  await expect(options.getByText('新城市', { exact: true })).toBeVisible()
  await expect(options.getByText('过期候选', { exact: true })).toHaveCount(0)
})

test('real tree shares repeated pending cascade loads and aborts when dialog closes', async ({
  page
}) => {
  await page.goto('http://127.0.0.1:7132/?mode=tree')
  await page.evaluate(() => {
    window.__holdCandidates = true
  })
  await page.getByRole('button', { name: '打开完整配置', exact: true }).click()
  const dialog = page.locator('.query-condition-configuration')
  await expect(dialog.locator('.chart-field')).toContainText('图表甲')
  await dialog.getByText('设置默认值', { exact: true }).click()
  await expect.poll(() => page.evaluate(() => window.__candidateRequests?.length || 0)).toBe(1)
  await page.clock.install()
  await page.evaluate(() => {
    window.__repeatCandidate()
    window.__repeatCandidate()
  })
  await page.clock.runFor(400)
  expect(await page.evaluate(() => window.__candidateRequests.length)).toBe(1)
  await dialog
    .locator('.ed-dialog__footer')
    .getByRole('button', { name: '取消', exact: true })
    .click()
  await expect(dialog).toBeHidden()
  await expect.poll(() => page.evaluate(() => window.__candidateRequests[0].aborted)).toBe(true)
  await page.evaluate(() => window.__candidateRequests[0].resolve([]))
})
test('complete production dialog opens with real default, binding and cascade panels', async ({
  page
}) => {
  const errors = []
  page.on('pageerror', e => errors.push(e.message))
  await page.goto('http://127.0.0.1:7132')
  await page.getByRole('button', { name: '打开完整配置', exact: true }).click()
  await expect(page.locator('.query-condition-configuration')).toBeVisible()
  await expect(page.locator('.chart-field')).toContainText('图表甲')
  await expect(page.locator('.condition-configuration')).toBeVisible()
  await page.screenshot({ path: test.info().outputPath('full-dialog.png') })
  expect(errors).toEqual([])
})

test('complete dialog cancels source edits without writing persisted configuration', async ({
  page
}) => {
  const dialog = await openFull(page)
  const before = await readSaved(page)
  await dialog.locator('.condition-configuration').getByText('手动输入', { exact: true }).click()
  await dialog
    .locator('.ed-dialog__footer')
    .getByRole('button', { name: '取消', exact: true })
    .click()
  await expect(dialog).toBeHidden()
  expect(await readSaved(page)).toEqual(before)
  await expect(page.getByTestId('snapshots')).toHaveText('0')
  await page.getByRole('button', { name: '打开完整配置', exact: true }).click()
  await expect(
    dialog.locator('.condition-configuration').getByText('选择数据集', { exact: true })
  ).toBeVisible()
})

test('complete dialog dataset and source field changes preserve other chart binding after serialization', async ({
  page
}) => {
  const dialog = await openFull(page)
  const panel = dialog.locator('.condition-configuration')
  const dataset = panel.getByRole('combobox').nth(1)
  await dataset.press('ArrowDown')
  await page.locator('.ed-select-dropdown:visible').getByText('数据集二', { exact: true }).click()
  const fieldControl = panel
    .getByText('查询字段', { exact: true })
    .locator('..')
    .getByRole('combobox')
  await choose(page, fieldControl, '城市')
  const saved = await confirmFull(page, dialog)
  expect(saved.dataset.id).toBe('ds2')
  expect(saved.field.id).toBe('city')
  expect(saved.checkedFieldsMap['chart-a']).toBe('country')
  expect(saved.checkedFieldsMap['chart-b']).toBe('city')
  expect(saved.historical).toEqual({ zero: 0, flag: false, text: '', value: null })
  const reopened = await reopenSerialized(page)
  await expect(reopened.locator('.condition-configuration')).toContainText('数据集二')
  const resaved = await confirmFull(page, reopened)
  expect(resaved.checkedFieldsMap).toEqual(saved.checkedFieldsMap)
  expect(resaved.field).toEqual(saved.field)
  expect(resaved.historical).toEqual(saved.historical)
  await expect(page.getByTestId('snapshots')).toHaveText('2')
})

test('complete numeric parameter dialog changes range to single and can bind a date parameter', async ({
  page
}) => {
  let dialog = await openFull(page, 'number')
  await editRangeEndpoint(page, dialog, '数值开始', '数值')
  await choose(
    page,
    dialog.locator('.condition-configuration .search-field').first().getByRole('combobox'),
    '金额'
  )
  let saved = await confirmFull(page, dialog)
  expect(saved.checkedFieldsMapArrNum['chart-a']).toEqual([])
  expect(saved.checkedFieldsMapStartNum['chart-a']).toBe('')
  expect(saved.checkedFieldsMapEndNum['chart-a']).toBe('')
  expect(saved.checkedFieldsMap['chart-a']).toBe('num-start')
  expect(saved.displayType).toBe('2')
  dialog = await reopenSerialized(page)
  const select = dialog.locator('.chart-field .ed-select').getByRole('combobox')
  await select.press('ArrowDown')
  await page
    .locator('.field-select--dqp:visible')
    .getByRole('option', { name: '日期开始', exact: true })
    .click()
  await expect(dialog.locator('.condition-configuration')).toContainText('时间粒度')
  saved = await confirmFull(page, dialog)
  expect(saved.displayType).toBe('1')
  expect(saved.checkedFieldsMap['chart-a']).toBe('date-start')
  expect(saved.checkedFieldsMapArrNum['chart-a']).toEqual([])
  expect(saved.checkedFieldsMap['chart-b']).toBe('city')
})

test('complete date parameter dialog swaps endpoint direction and persists after reopening', async ({
  page
}) => {
  const dialog = await openFull(page, 'time')
  await editRangeEndpoint(page, dialog, '日期开始', '结束时间')
  const saved = await confirmFull(page, dialog)
  expect(saved.checkedFieldsMapArr['chart-a']).toEqual(['date-start', 'date-end'])
  expect(saved.checkedFieldsMapStart['chart-a']).toBe('date-end')
  expect(saved.checkedFieldsMapEnd['chart-a']).toBe('date-start')
  expect(saved.displayType).toBe('7')
  await reopenSerialized(page)
  expect((await readSaved(page)).propValue[0].checkedFieldsMapStart['chart-a']).toBe('date-end')
})

test('complete default-value panel blocks missing required defaults then saves a selected default', async ({
  page
}) => {
  const dialog = await openFull(page)
  await dialog.getByText('必填项', { exact: true }).click()
  await dialog
    .locator('.ed-dialog__footer')
    .getByRole('button', { name: '确认', exact: true })
    .click()
  await expect(dialog).toBeVisible()
  await expect(page.getByText('查询条件为必填项,默认值不能为空', { exact: true })).toBeVisible()
  const panel = dialog.locator('.condition-configuration')
  await panel.getByText('设置默认值', { exact: true }).click()
  await expect
    .poll(() => page.evaluate(() => window.__fixtureOptionsLoaded || 0))
    .toBeGreaterThan(0)
  await expect(panel.locator('.parameters .ed-loading-mask')).toBeHidden()
  await panel.locator('.parameters .ed-select__wrapper').click()
  await page
    .locator('.filter-select-popper_class:visible')
    .getByText('华北', { exact: true })
    .click()
  const saved = await confirmFull(page, dialog)
  expect(saved.required).toBe(true)
  expect(saved.defaultValueCheck).toBe(true)
  expect(saved.defaultValue).toBe('华北')
  expect(saved.defaultMapValue).toContain('华北')
  const reopened = await reopenSerialized(page)
  await expect(reopened.locator('.condition-configuration .parameters')).toContainText('华北')
})

test('complete cascade panel confirms its own draft and retains relations after save and reopen', async ({
  page
}) => {
  const dialog = await openFull(page, 'cascade')
  await dialog.getByRole('button', { name: '查询组件级联配置', exact: true }).click()
  const cascade = page.locator('.query-condition-cascade')
  await expect(cascade).toBeVisible()
  await expect(cascade).toContainText('主查询')
  await expect(cascade).toContainText('级联查询')
  await cascade.getByRole('button', { name: /^(确认|确定)$/, exact: true }).click()
  await expect(cascade).toBeHidden()
  await confirmFull(page, dialog)
  const saved = await readSaved(page)
  expect(saved.cascade[0].map(item => item.datasetId)).toEqual([
    'ds--condition-a--country',
    'ds--condition-b--city'
  ])
  const reopened = await reopenSerialized(page)
  await reopened.getByRole('button', { name: '查询组件级联配置', exact: true }).click()
  await expect(cascade).toBeVisible()
  await expect(cascade).toContainText('级联查询')
})

test('complete tree panel switches levels and preserves level bindings after save and reopen', async ({
  page
}) => {
  const dialog = await openFull(page, 'tree')
  await expect(dialog.locator('.condition-configuration')).toContainText('地区')
  await dialog.locator('.list-tree_primary').getByText('城市', { exact: true }).click()
  await expect(dialog.locator('.chart-field')).toContainText('城市')
  const saved = await confirmFull(page, dialog)
  expect(saved.treeCheckedList[0].checkedFieldsMap['chart-a']).toBe('country')
  expect(saved.treeCheckedList[1].checkedFieldsMap['chart-a']).toBe('city')
  expect(saved.treeFieldList.map(field => field.id)).toEqual(['country', 'city'])
  const reopened = await reopenSerialized(page)
  await expect(reopened.locator('.list-tree_primary')).toContainText('城市')
})
