const { test, expect } = require(process.env.PLAYWRIGHT_TEST_MODULE || '@playwright/test')
const fs = require('node:fs/promises')
const path = require('node:path')
const os = require('node:os')
const { pathToFileURL } = require('node:url')
const frontend = path.resolve(__dirname, '../..')
let server, dir

test.beforeAll(async () => {
  dir = await fs.realpath(await fs.mkdtemp(path.join(os.tmpdir(), 'dataease-viewport-')))
  await fs.writeFile(path.join(dir, 'index.html'), '<div id="app"></div><script type="module" src="/main.ts"></script>')
  await fs.writeFile(path.join(dir, 'main.ts'), `
import {createApp,ref,h,onMounted,onBeforeUnmount,nextTick} from 'vue'
import {useChartViewportLoad,prepareViewportChartsForExport} from '${frontend}/src/hooks/web/useChartViewportLoad.ts'
const params=new URLSearchParams(location.search)
if(params.has('fallback')) window.IntersectionObserver=undefined
const enabled=ref(!params.has('eager')), shown=ref(true), input=ref('default'), confirmed=ref('default')
const requests=ref([]), exports=ref(''), events=ref([])
const Chart={props:['name','top'],setup(props){
  const target=ref(null),loading=ref(false),text=ref('empty'),renderer=ref(null)
  const RenderSurface={setup(){return()=>h('span',text.value)}}
  let disposed=false
  const gate=useChartViewportLoad(target,()=>enabled.value,loading,()=>{})
  const query=value=>gate.run(()=>{
    if(!renderer.value)throw new Error('Renderer was not mounted before query')
    loading.value=true
    requests.value.push(props.name+':'+value)
    fetch('/fixture/data?value='+encodeURIComponent(value)).then(r=>r.text()).then(value=>{if(!disposed)text.value=value}).finally(()=>loading.value=false)
  })
  const listener=()=>query(confirmed.value)
  onMounted(()=>{window.addEventListener('query',listener);query(confirmed.value)})
  onBeforeUnmount(()=>{disposed=true;window.removeEventListener('query',listener)})
  return()=>h('div',{ref:target,'data-chart':props.name,style:{position:'absolute',top:props.top+'px',width:'300px',height:'150px',border:'1px solid'}},!enabled.value||gate.activated.value?h(RenderSurface,{ref:renderer}):'empty')
}}
createApp({setup(){
 const submit=()=>{confirmed.value=input.value;window.dispatchEvent(new Event('query'))}
 const change=e=>{input.value=e.target.value;if(!params.has('button'))submit()}
 const exportRoot=async()=>{try{await prepareViewportChartsForExport(document.querySelector('#board'));exports.value=document.querySelector('#board').innerText}catch(e){exports.value='failed'}}
 return()=>h('main',[
  h('nav',{style:{position:'fixed',top:0,right:0,zIndex:10,background:'white'}},[
   h('input',{'aria-label':'filter',value:input.value,onInput:change}),h('button',{onClick:submit},'query'),
   h('button',{onClick:exportRoot},'export'),h('button',{onClick:()=>enabled.value=false},'eager'),
   h('button',{onClick:()=>shown.value=false},'remove'),
   h('pre',{'data-testid':'requests'},JSON.stringify(requests.value)),h('output',{'data-testid':'export'},exports.value)]),
  h('div',{id:'board',style:{position:'relative',height:params.has('nested')?'500px':'2500px',overflow:params.has('nested')?'auto':'visible'}},[
   h('div',{style:{position:'relative',height:'2500px'}},[h(Chart,{name:'first',top:100}),shown.value?h(Chart,{name:'last',top:1800}):null])]),
  h('aside',{style:{position:'relative',height:'2000px'}},[h(Chart,{name:'other-board',top:1000})])])
}}).mount('#app')
`)
  const { createServer } = await import(pathToFileURL(path.join(frontend, 'node_modules/vite/dist/node/index.js')))
  server = await createServer({
    configFile: false, root: dir, cacheDir: path.join(dir, '.vite'),
    resolve: { alias: { vue: path.join(frontend, 'node_modules/vue/dist/vue.runtime.esm-bundler.js') } },
    server: { host: '127.0.0.1', port: 7099, strictPort: true, fs: { allow: [frontend, dir] } }
  })
  await server.listen()
})
test.afterAll(async () => {
  await server?.close()
  if (dir) await fs.rm(dir, { recursive: true, force: true })
})

const open = async (page, query = '') => {
  await page.route('**/fixture/data?*', async route => {
    await new Promise(resolve => setTimeout(resolve, 120))
    await route.fulfill({ body: new URL(route.request().url()).searchParams.get('value') })
  })
  await page.goto('http://127.0.0.1:7099/' + query)
  await expect(page.locator('[data-chart="first"]')).toHaveText('default')
}
const requests = page => page.getByTestId('requests')

test('首屏只请求附近图表，滚动加载一次并保留结果', async ({ page }) => {
  await open(page)
  await expect(requests(page)).toHaveText('["first:default"]')
  await page.locator('[data-chart="last"]').scrollIntoViewIfNeeded()
  await expect(page.locator('[data-chart="last"]')).toHaveText('default')
  await page.evaluate(() => window.scrollTo(0, 0))
  await page.locator('[data-chart="last"]').scrollIntoViewIfNeeded()
  await expect(requests(page)).toHaveText('["first:default","last:default"]')
})
test('屏外多次筛选合并为最后一次确认值', async ({ page }) => {
  await open(page)
  await page.getByRole('textbox').fill('A')
  await page.getByRole('textbox').fill('B')
  await expect(requests(page)).not.toContainText('last:')
  await page.locator('[data-chart="last"]').scrollIntoViewIfNeeded()
  await expect(page.locator('[data-chart="last"]')).toHaveText('B')
  await expect(requests(page)).not.toContainText('last:A')
})
test('查询按钮模式首次滚动不使用尚未提交的输入值', async ({ page }) => {
  await open(page, '?button')
  await page.getByRole('textbox').fill('draft')
  await page.locator('[data-chart="last"]').scrollIntoViewIfNeeded()
  await expect(page.locator('[data-chart="last"]')).toHaveText('default')
  await page.getByRole('button', { name: 'query', exact: true }).click()
  await expect(page.locator('[data-chart="last"]')).toHaveText('draft')
})
test('屏外点击查询后再滚动使用已确认的值', async ({ page }) => {
  await open(page, '?button')
  await page.getByRole('textbox').fill('confirmed')
  await page.getByRole('button', { name: 'query', exact: true }).click()
  await page.getByRole('textbox').fill('draft')
  await page.locator('[data-chart="last"]').scrollIntoViewIfNeeded()
  await expect(page.locator('[data-chart="last"]')).toHaveText('confirmed')
})
test('整页导出补齐并等待屏外请求，限定当前容器', async ({ page }) => {
  await open(page)
  await page.getByRole('button', { name: 'export', exact: true }).click()
  await expect(page.getByTestId('export')).toHaveText('default\ndefault')
  await expect(requests(page)).toHaveText('["first:default","last:default"]')
})
test('不支持 IntersectionObserver 时立即加载', async ({ page }) => {
  await open(page, '?fallback')
  await expect(page.locator('[data-chart="last"]')).toHaveText('default')
})
test('非预览模式立即加载，切换离开预览补齐待执行查询', async ({ page }) => {
  await open(page, '?eager')
  await expect(page.locator('[data-chart="last"]')).toHaveText('default')
  await page.goto('http://127.0.0.1:7099/')
  await expect(page.locator('[data-chart="first"]')).toHaveText('default')
  await page.getByRole('button', { name: 'eager', exact: true }).click()
  await expect(page.locator('[data-chart="last"]')).toHaveText('default')
})
test('卸载后不请求也不遗留导出任务', async ({ page }) => {
  await open(page)
  await page.getByRole('button', { name: 'remove', exact: true }).click()
  await page.getByRole('button', { name: 'export', exact: true }).click()
  await expect(page.getByTestId('export')).toHaveText('default')
  await expect(requests(page)).toHaveText('["first:default"]')
})
test('仪表板内部滚动容器裁剪范围也触发按需查询', async ({ page }) => {
  await open(page, '?nested')
  await expect(requests(page)).toHaveText('["first:default"]')
  await page.locator('[data-chart="last"]').scrollIntoViewIfNeeded()
  await expect(page.locator('[data-chart="last"]')).toHaveText('default')
})
