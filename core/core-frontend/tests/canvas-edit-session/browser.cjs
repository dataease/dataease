/* eslint-disable @typescript-eslint/no-var-requires -- 独立 Playwright CLI 回归。 */
const { test, expect } = require(process.env.PLAYWRIGHT_TEST_MODULE || '@playwright/test')
const fs = require('node:fs/promises')
const path = require('node:path')
const os = require('node:os')
const { pathToFileURL } = require('node:url')
const ts = require('typescript')
const { parse } = require('@vue/compiler-sfc')
const frontend = path.resolve(__dirname, '../..')
let server, directory
const statements = async (file, names, includeWatch = false) => {
  const source = parse(await fs.readFile(path.join(frontend, file), 'utf8')).descriptor.scriptSetup
    .content
  const ast = ts.createSourceFile('source.ts', source, ts.ScriptTarget.Latest, true)
  return ast.statements
    .filter(
      n =>
        (ts.isVariableStatement(n) &&
          n.declarationList.declarations.some(d => names.includes(d.name.getText(ast)))) ||
        (includeWatch &&
          ts.isExpressionStatement(n) &&
          n.getText(ast).startsWith('watch(') &&
          n.getText(ast).includes('editorLocation'))
    )
    .map(n => n.getText(ast))
    .join('\n')
}
test.beforeAll(async () => {
  directory = await fs.realpath(
    await fs.mkdtemp(path.join(os.tmpdir(), 'dataease-edit-session-browser-'))
  )
  const source = await fs.readFile(path.join(frontend, 'src/utils/canvasUtils.ts'), 'utf8')
  const saveMethods = source
    .slice(
      source.indexOf('export function checkCanvasChangePre'),
      source.indexOf('\nconst httpUrlReg')
    )
    .replaceAll('export ', '')
  for (const [name, type, key] of [
    ['Dashboard', 'dashboard', 'resourceId'],
    ['Screen', 'dataV', 'dvId']
  ]) {
    const entry = await statements(
      `src/views/${name === 'Dashboard' ? 'dashboard/index' : 'data-visualization/indexV3'}.vue`,
      [
        'editorRoutePath',
        'editorLocation',
        'editorSession',
        'initializeEditor',
        'initLocalCanvasData',
        'doUseCache'
      ],
      true
    )
    const toolbar = await statements(
      `src/components/${
        name === 'Dashboard' ? 'dashboard/DbToolbar' : 'data-visualization/DvToolbar'
      }.vue`,
      ['saveResource']
    )
    await fs.writeFile(
      path.join(directory, name + '.vue'),
      `<script setup lang="ts">
import {ref,reactive,watch,onMounted,onUnmounted,nextTick} from 'vue'
import {cloneDeep} from 'lodash-es'
import {useCanvasEditSession} from '@/hooks/web/useCanvasEditSession'
import {useLatestQuery} from '@/hooks/web/useQueryScope'
import {captureCanvasSave,failCanvasEditSession,isCanvasDraft} from '@/utils/canvasEditSession'
const canvasQuery=useLatestQuery('canvas-load'), embeddedStore={baseUrl:''}
const router={currentRoute:ref({path:'/editor',query:{${key}:'a'}}),replace:async ({query})=>{router.currentRoute.value={path:'/editor',query}}}
const dvInfo=ref({id:'a',type:'${type}',name:'synthetic-a',contentId:'0',optType:null})
const curDvInfo=dvInfo, componentData=ref([]),canvasStyleData=ref({component:{chartTitle:{}}}),canvasViewInfo=ref({}),appData=ref(null)
const state=reactive({resourceId:null,opt:null,sourcePid:null,canvasInitStatus:false})
const dataInitState=ref(false),templateLoadError=ref(false),canvasCacheOutRefShow=ref(false), canvasCacheOutRef={value:{close:()=>{},dialogInit:()=>{}}}
const styleChangeTimes=ref(1),messages=ref([]),loading=ref(0),persisted=ref(''),drafts=new Map(), timers=[]
const wsCache={get:key=>drafts.get(key),set:(key,v)=>drafts.set(key,v),delete:key=>drafts.delete(key)}
const dvMainStore={removeGroupArea:()=>{},matrixSizeAdaptor:()=>{},setDataPrepareState:()=>{},setAppDataInfo:value=>appData.value=value,setCurComponent:()=>{},
setComponentData:v=>componentData.value=v,setCanvasStyle:v=>canvasStyleData.value=v,setCanvasViewInfo:v=>canvasViewInfo.value=v,getAppDataInfo:()=>appData.value,
createInit:()=>{dvInfo.value={id:null,type:'${type}',name:'synthetic-new',contentId:'0',optType:null};componentData.value=[];canvasStyleData.value={component:{chartTitle:{}}}},
updateDvInfoCall:(status,newId,contentId)=>{if(newId)dvInfo.value.id=newId;dvInfo.value.status=status;dvInfo.value.contentId=contentId;dvInfo.value.optType=null}}
const snapshotStore={initSnapShot:()=>{styleChangeTimes.value=1},recordSnapshotCache:()=>{},resetSnapshot:()=>{},resetStyleChangeTimes:()=>styleChangeTimes.value=0,
snapshotPublish:draft=>{dvInfo.value=draft.dvInfo;componentData.value=draft.componentData;canvasStyleData.value=draft.canvasStyleData;canvasViewInfo.value=draft.canvasViewInfo}}
const permissionStore={getCurrentPath:'/editor'},queryList={value:[]},eventBus={emit:()=>{}},useEmitt=()=>({emitter:eventBus})
const ElMessage={success:text=>messages.value.push(text),error:text=>messages.value.push(text)},ElMessageBox={confirm:async()=>{}},t=k=>k, isDesktop=()=>false
const checkCanvasChange=async()=>({data:'ok'}), appCanvasNameCheck=async()=>({data:'success'}),dvNameCheck=async()=>{},nameTrim=d=>d.name=d.name.trim(),guid=()=>String(Date.now())
const clearMaterialPreviews=()=>{}, tryShowLoading=()=>loading.value++,tryHideLoading=()=>loading.value--
const resourceAppOptShow=ref(false),resourceAppOpt={value:{close:()=>{}}}
const request=async(url,init)=>{const r=await fetch(url,init);if(!r.ok)throw new Error('synthetic '+r.status);return r.json()}
const updateCanvas=async payload=>{const res=await request('/synthetic/save',{method:'POST',body:JSON.stringify(payload)});persisted.value=payload.name;return res}
const saveCanvas=payload=>updateCanvas(payload)
const initCanvasData=async(id,params,callback)=>{const data=await request('/synthetic/load?id='+id);if(!params.isCurrent())return;dvInfo.value={...data,id:data.id??id,type:'${type}'};componentData.value=[];canvasViewInfo.value={};callback()}
const publishStatusChange=async status=>request('/synthetic/publish',{method:'POST',body:JSON.stringify({id:dvInfo.value.id,status})})
const checkPer=async()=>true,initDataset=()=>{},initScroll=()=>{},onInitReady=()=>{},deepCopy=cloneDeep
const watermarkFind=async()=>({data:{settingContent:'{}'}}), Base64={decode:value=>atob(value)}
const decompressionPre=async(_params,callback,isCurrent)=>{const result=await request('/synthetic/template');if(isCurrent())callback(result)}
let editorMounted=false
${saveMethods}
${entry}
${toolbar}
onMounted(async()=>{editorMounted=true;await initializeEditor()})
const navigate=(id,opt)=>router.currentRoute.value={path:'/editor',query:{${key}:id,opt}}
const createTemplate=()=>router.currentRoute.value={path:'/editor',query:{opt:'create',createType:'template',templateParams:btoa('{}')}}
const saveApp=publish=>{appData.value={synthetic:true};styleChangeTimes.value=1;return saveResource({withPublish:publish,status:1})}
</script>
<template><div><button @click="navigate('a')">A</button><button @click="navigate('b')">B</button><button @click="navigate(null,'create')">新建</button><button @click="navigate('a','copy')">复制</button><button @click="createTemplate">模板</button><button @click="saveResource({withPublish:false,status:1})">保存</button><button @click="saveApp(false)">应用保存</button><button @click="saveApp(true)">应用保存并发布</button><button @click="initializeEditor">重新打开</button><button @click="navigate('b')">切换</button><output data-testid="id">{{dvInfo.id}}</output><output data-testid="ready">{{(dataInitState || state.canvasInitStatus) && !!editorSession.capture()}}</output><output data-testid="messages">{{JSON.stringify(messages)}}</output><output data-testid="loading">{{loading}}</output><output data-testid="persisted">{{persisted}}</output></div></template>`
    )
  }
  await fs.writeFile(
    path.join(directory, 'App.vue'),
    `<script setup>import {ref} from 'vue';import Dashboard from './Dashboard.vue';import Screen from './Screen.vue';const show=ref(true);const type=new URLSearchParams(location.search).get('type')</script><template><button @click="show=false">卸载</button><component v-if="show" :is="type==='screen'?Screen:Dashboard" /></template>`
  )
  await fs.writeFile(
    path.join(directory, 'main.ts'),
    "import {createApp} from 'vue';import App from './App.vue';createApp(App).mount('#app')"
  )
  await fs.writeFile(
    path.join(directory, 'index.html'),
    '<div id="app"></div><script type="module" src="/main.ts"></script>'
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
      port: 7131,
      strictPort: true,
      fs: { allow: [directory, frontend] }
    },
    optimizeDeps: { include: ['vue', 'lodash-es'] }
  })
  await server.listen()
})
test.afterAll(async () => {
  await server?.close()
  if (directory) await fs.rm(directory, { recursive: true, force: true })
})
async function fixture(page, type) {
  const pending = [],
    writes = [],
    publishes = [],
    errors = []
  page.on('pageerror', e => errors.push(e.message))
  await page.route('**/synthetic/load?*', route => pending.push(route))
  await page.route('**/synthetic/save', route => {
    writes.push(route.request().postDataJSON())
    return route.fulfill({ json: { data: 'saved-id' } })
  })
  await page.route('**/synthetic/publish', route => {
    publishes.push(route.request().postDataJSON())
    return route.fulfill({ json: { data: {} } })
  })
  await page.route('**/synthetic/template', route =>
    route.fulfill({
      json: {
        baseInfo: {},
        componentData: [],
        canvasStyleData: { component: { chartTitle: {} } },
        canvasViewInfo: {}
      }
    })
  )
  await page.goto('http://127.0.0.1:7131/?type=' + type)
  const reply = async (index, id, status = 200) => {
    await expect.poll(() => pending.length).toBeGreaterThan(index)
    await pending[index].fulfill({
      status,
      json: { id, name: 'synthetic-' + id, contentId: 'old' }
    })
  }
  await reply(0, 'a')
  await expect(page.getByTestId('ready')).toHaveText('true')
  return { pending, writes, publishes, errors, reply }
}
for (const type of ['dashboard', 'screen']) {
  test(type + ': 快速切换后旧加载不能回写', async ({ page }) => {
    const f = await fixture(page, type)
    await page.getByRole('button', { name: 'B', exact: true }).click()
    await expect(page.getByTestId('ready')).toHaveText('false')
    await page.getByRole('button', { name: 'A', exact: true }).click()
    await f.reply(2, 'a')
    await f.reply(1, 'b')
    await expect(page.getByTestId('id')).toHaveText('a')
    await expect(page.getByTestId('ready')).toHaveText('true')
    expect(f.errors).toEqual([])
  })
  for (const mode of ['新建', '复制', '模板'])
    test(type + ': ' + mode + '首次保存路由替换后继续编辑', async ({ page }) => {
      const f = await fixture(page, type)
      await page.getByRole('button', { name: mode, exact: true }).click()
      if (mode === '复制') await f.reply(1, 'a')
      if (mode === '模板') await expect(page.getByTestId('id')).toHaveText('')
      await expect(page.getByTestId('ready')).toHaveText('true')
      await page.getByRole('button', { name: '保存', exact: true }).click()
      await expect(page.getByTestId('id')).toHaveText('saved-id')
      await expect(page.getByTestId('ready')).toHaveText('true')
      expect(f.writes).toHaveLength(1)
      expect(f.errors).toEqual([])
    })
  for (const publish of [false, true])
    test(type + ': 应用重载失败保留保存结果并阻止再次写入 publish=' + publish, async ({ page }) => {
      const f = await fixture(page, type)
      await page
        .getByRole('button', { name: publish ? '应用保存并发布' : '应用保存', exact: true })
        .click()
      await f.reply(1, 'a', 500)
      await expect(page.getByTestId('messages')).toContainText('visualization.saved_reload_failed')
      await expect(page.getByTestId('persisted')).toHaveText('synthetic-a')
      await expect(page.getByTestId('ready')).toHaveText('false')
      await expect(page.getByTestId('loading')).toHaveText('0')
      await page.getByRole('button', { name: '应用保存并发布', exact: true }).click()
      expect(f.writes).toHaveLength(1)
      expect(f.publishes).toHaveLength(0)
      await page.getByRole('button', { name: '重新打开', exact: true }).click()
      await f.reply(2, 'a')
      await expect(page.getByTestId('ready')).toHaveText('true')
      expect(f.errors).toEqual([])
    })
  test(type + ': 重载成功后才发布', async ({ page }) => {
    const f = await fixture(page, type)
    await page.getByRole('button', { name: '应用保存并发布', exact: true }).click()
    await expect.poll(() => f.pending.length).toBe(2)
    expect(f.publishes).toHaveLength(0)
    await f.reply(1, 'a')
    await expect.poll(() => f.publishes.length).toBe(1)
    await expect(page.getByTestId('loading')).toHaveText('0')
    expect(f.errors).toEqual([])
  })
  test(type + ': 卸载期间旧响应不执行保存', async ({ page }) => {
    const f = await fixture(page, type)
    await page.getByRole('button', { name: 'B', exact: true }).click()
    await page.getByRole('button', { name: '卸载', exact: true }).click()
    await f.reply(1, 'b')
    expect(f.writes).toHaveLength(0)
    expect(f.publishes).toHaveLength(0)
    expect(f.errors).toEqual([])
  })
}
