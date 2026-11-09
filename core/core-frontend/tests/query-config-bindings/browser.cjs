/* eslint-disable @typescript-eslint/no-var-requires -- 隔离浏览器回归，使用真实配置操作和保存函数。 */
const { test, expect } = require(process.env.PLAYWRIGHT_TEST_MODULE || '@playwright/test')
const fs = require('node:fs/promises')
const path = require('node:path')
const os = require('node:os')
const { pathToFileURL } = require('node:url')
const ts = require('typescript')
const { parse } = require('@vue/compiler-sfc')
const frontend = path.resolve(__dirname, '../..')
let server, directory

test.beforeAll(async () => {
  directory = await fs.realpath(
    await fs.mkdtemp(path.join(os.tmpdir(), 'dataease-config-bindings-'))
  )
  const source = parse(
    await fs.readFile(
      path.join(frontend, 'src/custom-component/v-query/QueryConditionConfiguration.vue'),
      'utf8'
    )
  ).descriptor.scriptSetup.content
  const ast = ts.createSourceFile('Config.ts', source, ts.ScriptTarget.Latest, true)
  const names = [
    'setParametersArr',
    'setParametersArrNum',
    'timeTypeChange',
    'numTypeChange',
    'setParametersTimeType',
    'setParametersNumType',
    'duplicateRemoval',
    'typeTimeMap',
    'handleDatasetChange',
    'handleFieldChange',
    'handleValueSourceChange',
    'multipleChange',
    'setTypeChange',
    'setTreeDefault',
    'setRelationBack',
    'setTreeDefaultBatch',
    'validate',
    'setParams',
    'confirmClick',
    'cancelClick'
  ]
  const methods = names
    .map(name => {
      const statement = ast.statements.find(
        node =>
          ts.isVariableStatement(node) &&
          node.declarationList.declarations.some(d => d.name.getText(ast) === name)
      )
      if (!statement) throw new Error(`Missing production function ${name}`)
      return statement.getText(ast)
    })
    .join('\n')
  await fs.writeFile(
    path.join(directory, 'Editor.vue'),
    `<script setup lang="ts">
import {ref,computed,nextTick,unref} from 'vue'
import {cloneDeep} from 'lodash-es'
import * as operations from '@/custom-component/v-query/config-bindings'
const {createDatasetSourcePatch,createFieldSourcePatch,createOptionSourcePatch,createRangeParameterPatch,createRangeSelectionPatch,getSourceBindingError,hasQueryBindings,readRangeBinding}=operations
const fieldOptions=[{id:'field',name:'字段',deType:0},{id:'start',variableName:'开始参数',deType:2,type:['INTEGER']},{id:'end',variableName:'结束参数',deType:2,type:['INTEGER']}]
const make=()=>({id:'condition',auto:false,displayType:'0',multiple:false,required:false,defaultValueCheck:false,defaultValue:undefined,selectValue:undefined,mapValue:[],defaultMapValue:[],optionValueSource:1,valueSource:['手动值'],field:{id:'field',chartId:'owner',unknown:false},dataset:{id:'ds',fields:cloneDeep(fieldOptions)},displayId:'field',sortId:'field',parameters:[],checkedFields:['chart'],checkedFieldsMap:{chart:'field'},checkedFieldsMapArr:{chart:[]},checkedFieldsMapStart:{chart:''},checkedFieldsMapEnd:{chart:''},checkedFieldsMapArrNum:{chart:[]},checkedFieldsMapStartNum:{chart:''},checkedFieldsMapEndNum:{chart:''},parametersArr:{chart:[]},treeCheckedList:[],treeFieldList:[],timeType:'fixed',timeRange:{relativeToCurrentRange:'custom',historical:0},historical:{zero:0,flag:false,empty:'',value:null},defaultNumValueStart:0,defaultNumValueEnd:1})
const queryElement=ref({propValue:[make()],cascade:[]})
const curComponent=ref(cloneDeep(queryElement.value.propValue[0]))
const conditions=computed(()=>[curComponent.value])
const fields=ref([{componentId:'chart',id:'ds',fields:{dimensionList:[fieldOptions[0]],quotaList:[],parameterList:fieldOptions.slice(1)}}])
const datasetFieldList=ref([{id:'chart',tableId:'ds'}])
const relationshipChartIndex=ref(0),dialogVisible=ref(true),defaultConfigurationRef=ref(),snapshots=ref([]),events=ref([]),errors=ref([])
const timeParameterType=ref<0|1|2>(0),numParameterType=ref<0|1|2>(0),timeDialogShow=ref(false),numDialogShow=ref(false)
let currentComponentId='chart',currentParameterId='start',cascadeArr=[],oldDisplayType,newDatasetId='',oldDatasetId=''
const endpoint=ref<0|1|2>(1),parameterId=ref('start'),rangeKind=ref('number')
const t=k=>k,ElMessage={error:v=>errors.value.push(v)}
const snapshotStore={recordSnapshotCache:v=>snapshots.value.push(v)}
const emits=(...args)=>events.value.push(args)
const handleDialogClick=()=>undefined,getOptions=()=>undefined
// 日期求值由专项回归覆盖，本夹具不配置日期默认值；接口与子面板是测试边界。
const validateDynamicDefault=()=>undefined,hasUnresolvedFixedTimeValue=()=>false
${methods}
const assign=()=>{currentParameterId=parameterId.value;if(rangeKind.value==='number'){numParameterType.value=endpoint.value;numTypeChange()}else{timeParameterType.value=endpoint.value;timeTypeChange()}}
const clear=()=>rangeKind.value==='number'?setParametersArrNum([],'chart'):setParametersArr([],'chart')
const reopen=()=>{curComponent.value=cloneDeep(queryElement.value.propValue[0]);dialogVisible.value=true}
const clearBinding=()=>{curComponent.value.checkedFieldsMap.chart=''}
const sourceFieldChange=()=>{curComponent.value.field.chartId='owner';handleFieldChange()}
</script>
<template><main>
<section v-if="dialogVisible">
<el-radio-group aria-label="候选来源" v-model="curComponent.optionValueSource" @change="handleValueSourceChange"><el-radio :label="0">自动</el-radio><el-radio :label="1">字段来源</el-radio><el-radio :label="2">手动来源</el-radio></el-radio-group>
<el-select aria-label="数据集" v-model="curComponent.dataset.id" @change="handleDatasetChange"><el-option label="原数据集" value="ds"/><el-option label="新数据集" value="ds2"/></el-select>
<el-select aria-label="候选字段" v-model="curComponent.field.id" @change="sourceFieldChange"><el-option label="公共字段" value="field"/></el-select>
<el-radio-group aria-label="范围类型" v-model="rangeKind"><el-radio label="number">数字范围</el-radio><el-radio label="time">日期范围</el-radio></el-radio-group>
<el-select aria-label="参数" v-model="parameterId"><el-option label="开始参数" value="start"/><el-option label="结束参数" value="end"/></el-select>
<el-radio-group aria-label="绑定位置" v-model="endpoint"><el-radio :label="0">单值</el-radio><el-radio :label="1">开始</el-radio><el-radio :label="2">结束</el-radio></el-radio-group>
<el-button @click="assign">绑定参数</el-button><el-button @click="clear">清除范围</el-button><el-button @click="clearBinding">解除图表绑定</el-button>
<el-button @click="cancelClick">取消</el-button><el-button @click="confirmClick">确认</el-button>
</section>
<el-button v-else @click="reopen">重新打开</el-button>
<pre data-testid="draft">{{JSON.stringify(curComponent)}}</pre><pre data-testid="saved">{{JSON.stringify(queryElement.propValue[0])}}</pre>
<output data-testid="errors">{{JSON.stringify(errors)}}</output><output data-testid="snapshots">{{snapshots.length}}</output>
</main></template>`
  )
  await fs.writeFile(
    path.join(directory, 'index.html'),
    '<div id="app"></div><script type="module" src="/main.ts"></script>'
  )
  await fs.writeFile(
    path.join(directory, 'main.ts'),
    `import {createApp} from 'vue';import ElementPlus from 'element-plus-secondary';import 'element-plus-secondary/dist/index.css';import Editor from './Editor.vue';createApp(Editor).use(ElementPlus,{namespace:'ed'}).mount('#app')`
  )
  const { createServer } = await import(
    pathToFileURL(path.join(frontend, 'node_modules/vite/dist/node/index.js'))
  )
  const { default: vue } = await import(
    pathToFileURL(require.resolve('@vitejs/plugin-vue', { paths: [frontend] }))
  )
  server = await createServer({
    configFile: false,
    root: directory,
    cacheDir: path.join(directory, '.vite'),
    plugins: [vue()],
    resolve: {
      alias: [
        { find: '@', replacement: path.join(frontend, 'src') },
        { find: /^vue$/, replacement: require.resolve('vue/dist/vue.runtime.esm-bundler.js') },
        { find: 'lodash-es', replacement: path.join(frontend, 'node_modules/lodash-es/lodash.js') },
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
    server: {
      host: '127.0.0.1',
      port: 7131,
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
const read = async (page, name) => JSON.parse(await page.getByTestId(name).innerText())
const select = async (page, name, option) => {
  await page.getByRole('combobox', { name }).press('ArrowDown')
  await page.getByRole('option', { name: option, exact: true }).click()
}
test.beforeEach(async ({ page }) => {
  const errors = []
  page.setDefaultTimeout(10000)
  page.on('pageerror', error => errors.push(error.message))
  await page.goto('http://127.0.0.1:7131')
  await expect(page.getByTestId('draft')).toContainText('condition')
  page.testErrors = errors
})
test.afterEach(async ({ page }) => expect(page.testErrors).toEqual([]))

test('switch source and dataset, cancel discards draft, confirm persists legacy shape and reopens', async ({
  page
}) => {
  const initial = await read(page, 'saved')
  await page.getByText('手动来源', { exact: true }).click()
  await page.getByRole('button', { name: '取消', exact: true }).click()
  expect(await read(page, 'saved')).toEqual(initial)
  await page.getByRole('button', { name: '重新打开', exact: true }).click()
  await select(page, '数据集', '新数据集')
  const draft = await read(page, 'draft')
  expect(draft.field).toEqual({ id: '', unknown: false })
  expect(draft.displayId).toBe('')
  expect(draft.sortId).toBe('')
  await select(page, '候选字段', '公共字段')
  await page.getByRole('button', { name: '确认', exact: true }).click()
  await expect(page.getByRole('button', { name: '重新打开', exact: true })).toBeVisible()
  await page.getByRole('button', { name: '重新打开', exact: true }).click()
  const saved = await read(page, 'saved')
  expect(saved.dataset.id).toBe('ds2')
  expect(saved.field.chartId).toBeUndefined()
  expect(saved.historical).toEqual(initial.historical)
  expect(Object.keys(saved).sort()).toEqual(Object.keys(initial).sort())
  await expect(page.getByTestId('snapshots')).toHaveText('1')
})

test('number parameter range survives save and reopen, then single binding clears its endpoints', async ({
  page
}) => {
  await page.getByRole('button', { name: '绑定参数', exact: true }).click()
  await select(page, '参数', '结束参数')
  await page.getByText('结束', { exact: true }).click()
  await page.getByRole('button', { name: '绑定参数', exact: true }).click()
  await page.getByRole('button', { name: '确认', exact: true }).click()
  await expect(page.getByRole('button', { name: '重新打开', exact: true })).toBeVisible()
  await page.getByRole('button', { name: '重新打开', exact: true }).click()
  const saved = await read(page, 'saved')
  expect(saved.checkedFieldsMapArrNum.chart).toEqual(['start', 'end'])
  expect(saved.checkedFieldsMapStartNum.chart).toBe('start')
  expect(saved.checkedFieldsMapEndNum.chart).toBe('end')
  await page.getByText('单值', { exact: true }).click()
  await page.getByRole('button', { name: '绑定参数', exact: true }).click()
  const draft = await read(page, 'draft')
  expect(draft.checkedFieldsMapArrNum.chart).toEqual([])
  expect(draft.checkedFieldsMapStartNum.chart).toBe('')
  expect(draft.checkedFieldsMapEndNum.chart).toBe('')
  expect(draft.checkedFieldsMap.chart).toBe('end')
})

test('switching range kind and clearing range cannot leave opposite binding fields', async ({
  page
}) => {
  await page.getByRole('button', { name: '绑定参数', exact: true }).click()
  await page.getByText('日期范围', { exact: true }).click()
  await page.getByRole('button', { name: '绑定参数', exact: true }).click()
  let draft = await read(page, 'draft')
  expect(draft.checkedFieldsMapArr.chart).toEqual(['start'])
  expect(draft.checkedFieldsMapArrNum.chart).toEqual([])
  expect(draft.checkedFieldsMapStartNum.chart).toBe('')
  await page.getByRole('button', { name: '清除范围', exact: true }).click()
  draft = await read(page, 'draft')
  expect(draft.checkedFieldsMap.chart).toBe('')
  expect(draft.parametersArr.chart).toEqual([])
  expect(draft.checkedFieldsMapArr.chart).toEqual([])
  expect(draft.checkedFieldsMapStart.chart).toBe('')
  expect(draft.checkedFieldsMapEnd.chart).toBe('')
})

test('missing chart binding blocks save without a snapshot or persisted changes', async ({
  page
}) => {
  const before = await read(page, 'saved')
  await page.getByRole('button', { name: '解除图表绑定', exact: true }).click()
  await page.getByRole('button', { name: '确认', exact: true }).click()
  await expect(page.getByTestId('errors')).toContainText('v_query.be_linked_first')
  await expect(page.getByRole('button', { name: '确认', exact: true })).toBeVisible()
  expect(await read(page, 'saved')).toEqual(before)
  await expect(page.getByTestId('snapshots')).toHaveText('0')
})
