/* eslint-disable @typescript-eslint/no-var-requires -- Node CommonJS 测试入口。 */
const { test, expect } = require(process.env.PLAYWRIGHT_TEST_MODULE || '@playwright/test')
const fs = require('node:fs/promises')
const path = require('node:path')
const os = require('node:os')
const { pathToFileURL } = require('node:url')
const ts = require('typescript')
const { parse } = require('@vue/compiler-sfc')
const frontend = path.resolve(__dirname, '../..')
let server, directory
const componentPaths = {
  G2: 'src/views/chart/components/views/components/ChartComponentG2Plot.vue',
  S2: 'src/views/chart/components/views/components/ChartComponentS2.vue'
}
async function statements(file, names) {
  const source = parse(await fs.readFile(path.join(frontend, file), 'utf8')).descriptor.scriptSetup
    .content
  const ast = ts.createSourceFile('source.ts', source, ts.ScriptTarget.Latest, true)
  return ast.statements
    .filter(
      n =>
        ts.isVariableStatement(n) &&
        n.declarationList.declarations.some(d => names.includes(d.name.getText(ast)))
    )
    .map(n => n.getText(ast))
    .join('\n')
}

test.beforeAll(async () => {
  directory = await fs.realpath(
    await fs.mkdtemp(path.join(os.tmpdir(), 'dataease-scoped-query-browser-'))
  )
  const actionMethods = await statements('src/components/visualization/ComponentEditBar.vue', [
    'exportAsExcel',
    'exportAsFormattedExcel'
  ])
  for (const [engine, filename] of Object.entries(componentPaths)) {
    const methods = await statements(filename, [
      'dataQuery',
      'invalidateQuery',
      'calcData',
      'pendingQueryCallback'
    ])
    await fs.writeFile(
      path.join(directory, `${engine}.vue`),
      `<script setup lang="ts">
import {ref, reactive, shallowRef} from 'vue'
import {useLatestQuery} from '@/hooks/web/useQueryScope'
import {provideChartRuntimeCache} from '@/hooks/web/useChartRuntimeCache'
const props=defineProps<{name:string}>()
const view={id:'same-resource',title:'synthetic-chart',tableId:1,customAttr:{basicStyle:{}},chartExtRequest:{},type:'bar'}
const chartRuntime=provideChartRuntimeCache(()=>view as Chart,()=> 'preview')
const chartData=shallowRef({}), state=reactive({pageInfo:{},currentPageSize:0,totalItems:0})
const isError=ref(false),errMsg=ref(''),loading=ref(false),exported=ref(''),detail=ref('')
const dynamicAreaId=ref(''), mockRefreshResponse=undefined
let scope=null,gadmName=null,chartRenderVersion=0,g2Timer=0,mapTimer=0,mapL7Timer=0
const finishG2Schedule=undefined, debounceRender={cancel:()=>undefined}, resolveRenderChart=()=>undefined
const emit=()=>undefined,emitter={emit:()=>undefined}, normalizeChartData=v=>v
const dvMainStore=new Proxy({}, {get:()=>{throw Error('preview accessed resource-global cache')}})
const getData=(input,query)=>fetch('/synthetic/chart?marker='+encodeURIComponent(input.marker)).then(r=>r.json()).finally(query.finish)
const renderChart=async (res,callback)=>{if('${engine}'==='G2'&&typeof callback==='function')callback()}
${methods}
const shareAllows=()=>true, dvInfo={value:{type:'dashboard',name:'synthetic'}}
const callbackExport=()=>undefined,openMessageLoading=()=>undefined
const exportExcelDownload=chart=>{exported.value=JSON.stringify({data:chart.data,request:chart.chartExtRequest})}
const exportPivotExcel=instance=>{exported.value=instance.marker}
${actionMethods}
const query=(marker,page=false)=>{
 loading.value=true
 chartRuntime.request={filter:[marker]}
 calcData({...view, marker},page?null:()=>{loading.value=false})
}
const save=()=>fetch('/synthetic/save',{method:'POST'}).then(()=>{exported.value='saved'})
const showDetails=()=>{detail.value=JSON.stringify(chartRuntime.snapshot()?.data)}
defineExpose({query})
</script>
<template><section :data-testid="name" :data-chart-runtime="chartRuntime.id">
<h2>{{name}} ${engine}</h2><button @click="query(name+'-old')">旧查询</button><button @click="query(name+'-new')">新查询</button><button @click="query(name+'-page',true)">下一页</button>
<button @click="showDetails">详情</button><button @click="exportAsExcel">导出数据</button><button @click="save">保存</button>
<output data-testid="loading">{{loading?'loading':'ready'}}</output><output data-testid="data">{{JSON.stringify(chartRuntime.data)}}</output>
<output data-testid="details">{{detail}}</output><output data-testid="export">{{exported}}</output></section></template>`
    )
  }
  await fs.writeFile(
    path.join(directory, 'App.vue'),
    `<script setup lang="ts">
import {ref} from 'vue';import G2 from './G2.vue';import S2 from './S2.vue';import {provideQueryScope} from '@/hooks/web/useQueryScope'
provideQueryScope();const visible=ref(true)
</script><template><button @click="visible=!visible">关闭或重开 A</button><template v-if="visible"><G2 name="A"/><S2 name="table-A"/></template><G2 name="B"/><S2 name="table-B"/></template>`
  )
  await fs.writeFile(
    path.join(directory, 'index.html'),
    '<div id="app"></div><script type="module" src="/main.ts"></script>'
  )
  await fs.writeFile(
    path.join(directory, 'main.ts'),
    "import {createApp} from 'vue';import App from './App.vue';createApp(App).mount('#app')"
  )
  const { createServer } = await import(pathToFileURL(require.resolve('vite')).href)
  const vue = (await import(pathToFileURL(require.resolve('@vitejs/plugin-vue')).href)).default
  server = await createServer({
    configFile: false,
    root: directory,
    plugins: [vue()],
    logLevel: 'error',
    resolve: {
      alias: [
        { find: '@', replacement: path.join(frontend, 'src') },
        { find: /^vue$/, replacement: require.resolve('vue/dist/vue.runtime.esm-bundler.js') },
        { find: 'lodash-es', replacement: path.join(frontend, 'node_modules/lodash-es/lodash.js') }
      ],
      dedupe: ['vue']
    },
    server: {
      host: '127.0.0.1',
      port: 7127,
      strictPort: true,
      fs: { allow: [frontend, directory] }
    },
    optimizeDeps: { include: ['vue', 'lodash-es'] }
  })
  await server.listen()
})
test.afterAll(async () => {
  await server?.close()
  if (directory) await fs.rm(directory, { recursive: true, force: true })
})

async function fixture(page) {
  const errors = [],
    pending = new Map(),
    calls = []
  page.on('pageerror', error => errors.push(error.message))
  await page.route('**/synthetic/chart?*', route => {
    const marker = new URL(route.request().url()).searchParams.get('marker')
    calls.push(marker)
    pending.set(marker, route)
  })
  await page.route('**/synthetic/save', route => route.fulfill({ json: { ok: true } }))
  await page.goto('http://127.0.0.1:7127')
  await expect(page.getByTestId('B')).toBeVisible()
  const reply = async marker => {
    await expect.poll(() => pending.has(marker)).toBe(true)
    const pendingRoute = pending.get(marker)
    await pendingRoute.fulfill({
      json: { data: { data: [marker], fields: [] }, drillFilters: [], totalItems: 100 }
    })
  }
  return { errors, calls, pending, reply }
}

test('same chart instances retain their own latest data, details and export while saving continues', async ({
  page
}) => {
  const { reply, errors } = await fixture(page),
    a = page.getByTestId('A'),
    b = page.getByTestId('B')
  await a.getByRole('button', { name: '旧查询', exact: true }).click()
  await b.getByRole('button', { name: '新查询', exact: true }).click()
  await a.getByRole('button', { name: '新查询', exact: true }).click()
  await a.getByRole('button', { name: '保存', exact: true }).click()
  await expect(a.getByTestId('export')).toHaveText('saved')
  await reply('A-new')
  await reply('B-new')
  await reply('A-old')
  await expect(a.getByTestId('data')).toContainText('A-new')
  await expect(b.getByTestId('data')).toContainText('B-new')
  await expect(a.getByTestId('loading')).toHaveText('ready')
  await a.getByRole('button', { name: '详情', exact: true }).click()
  await a.getByRole('button', { name: '导出数据', exact: true }).click()
  await expect(a.getByTestId('details')).toContainText('A-new')
  await expect(a.getByTestId('export')).toContainText('A-new')
  await expect(a.getByTestId('export')).not.toContainText('B-new')
  expect(errors).toEqual([])
})

test('S2 pagination and filtering share one generation and obsolete completions never reset current loading', async ({
  page
}) => {
  const { reply, errors } = await fixture(page),
    a = page.getByTestId('table-A'),
    b = page.getByTestId('table-B')
  await a.getByRole('button', { name: '旧查询', exact: true }).click()
  await a.getByRole('button', { name: '下一页', exact: true }).click()
  await b.getByRole('button', { name: '新查询', exact: true }).click()
  await reply('table-A-old')
  await expect(a.getByTestId('loading')).toHaveText('loading')
  await a.getByRole('button', { name: '新查询', exact: true }).click()
  await reply('table-A-new')
  await reply('table-A-page')
  await reply('table-B-new')
  await expect(a.getByTestId('data')).toContainText('table-A-new')
  await expect(a.getByTestId('loading')).toHaveText('ready')
  await expect(b.getByTestId('data')).toContainText('table-B-new')
  expect(errors).toEqual([])
})

test('closing one container and reopening it cannot reuse its old cache or interrupt another container', async ({
  page
}) => {
  const { reply, errors } = await fixture(page)
  await page.getByTestId('A').getByRole('button', { name: '旧查询', exact: true }).click()
  await page.getByTestId('B').getByRole('button', { name: '新查询', exact: true }).click()
  await page.getByRole('button', { name: '关闭或重开 A', exact: true }).click()
  await reply('A-old')
  await reply('B-new')
  await expect(page.getByTestId('B').getByTestId('data')).toContainText('B-new')
  await page.getByRole('button', { name: '关闭或重开 A', exact: true }).click()
  await expect(page.getByTestId('A').getByTestId('data')).not.toContainText('A-old')
  await expect(page.getByTestId('B').getByTestId('data')).toContainText('B-new')
  expect(errors).toEqual([])
})
