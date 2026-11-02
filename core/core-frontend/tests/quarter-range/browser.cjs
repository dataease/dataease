/* eslint-disable @typescript-eslint/no-var-requires */
const { test, expect } = require(process.env.PLAYWRIGHT_TEST_MODULE || '@playwright/test')
const fs = require('node:fs/promises')
const path = require('node:path')
const os = require('node:os')
const { pathToFileURL } = require('node:url')
const frontend = path.resolve(__dirname, '../..')
let server, dir

test.beforeAll(async () => {
  dir = await fs.realpath(await fs.mkdtemp(path.join(os.tmpdir(), 'dataease-quarter-ui-')))
  await fs.writeFile(
    path.join(dir, 'index.html'),
    '<div id="app"></div><script type="module" src="/main.ts"></script>'
  )
  await fs.writeFile(path.join(dir, 'Empty.vue'), '<template><span /></template>')
  await fs.writeFile(
    path.join(dir, 'i18n.ts'),
    `import zh from '${frontend}/src/locales/zh-CN'; import en from '${frontend}/src/locales/en'; import tw from '${frontend}/src/locales/tw'; export const t=(key)=>key.split('.').reduce((v,k)=>v?.[k], {zh,en,tw}[new URLSearchParams(location.search).get('lang')||'zh'])||key;export const useI18n=()=>({t});`
  )
  await fs.writeFile(
    path.join(dir, 'main.ts'),
    `
import {createApp,reactive,h,ref} from 'vue';
import ElementPlus from 'element-plus-secondary'; import 'element-plus-secondary/dist/index.css';
import RangeFilter from '${frontend}/src/custom-component/v-query/RangeFilterTime.vue';
import Configuration from '${frontend}/src/custom-component/v-query/ConditionDefaultConfiguration.vue';
import {validateDynamicDefault,evaluateDynamicDefault} from '${frontend}/src/custom-component/v-query/dynamic-default-time';
import {t} from './i18n';
const params=new URLSearchParams(location.search);
const config=reactive({id:'time',displayType:params.has('range')?'7':'1',field:{deType:1},timeType:'dynamic',defaultValueCheck:true,
timeGranularity:params.has('date')?'date':'datetime',timeGranularityMultiple:params.has('date')?'daterange':'datetimerange',relativeToCurrent:'custom',relativeToCurrentRange:'custom',
timeNum:0,relativeToCurrentType:'date',around:'f',timeNumRange:0,relativeToCurrentTypeRange:'date',aroundRange:'f',arbitraryTime:new Date(2020,0,1,9,10,11),arbitraryTimeRange:new Date(2020,0,1,23,59,59)});
const allowed=reactive({intervalType:'timeInterval',regularOrTrends:'dynamic',regularOrTrendsValue:[],relativeToCurrentRange:'lastQuarter',relativeToCurrent:'today',timeNum:0,relativeToCurrentType:'date',around:'f',timeNumRange:0,relativeToCurrentTypeRange:'date',aroundRange:'f'});
config.displayType='7';config.timeGranularityMultiple=params.get('grain')||'datetimerange';config.relativeToCurrentRange='lastQuarter';
if(params.has('saved'))Object.assign(config,JSON.parse(params.get('saved')));
window.fixture={config,allowed,evaluateDynamicDefault,validateDynamicDefault};
createApp({setup(){const saved=ref('');return()=>h('main',{style:'width:460px;padding:16px'},[h(Configuration,{curComponent:config}),h('section',{id:'allowed'},[h(RangeFilter,{timeRange:allowed,timeGranularityMultiple:config.timeGranularityMultiple})]),h('button',{onClick:()=>{const error=validateDynamicDefault(config);saved.value=error?t(error):'saved'}},'保存'),h('output',saved.value)])}}).use(ElementPlus,{namespace:'ed'}).mixin({methods:{$t:t}}).component('Icon',{render:()=>h('span')}).mount('#app');
`
  )
  const { createServer } = await import(
    pathToFileURL(path.join(frontend, 'node_modules/vite/dist/node/index.js'))
  )
  const { default: vue } = await import(
    pathToFileURL(require.resolve('@vitejs/plugin-vue', { paths: [frontend] }))
  )
  const empty = path.join(dir, 'Empty.vue')
  server = await createServer({
    configFile: false,
    root: dir,
    cacheDir: path.join(dir, 'cache'),
    resolve: {
      alias: [
        { find: '@/hooks/web/useI18n', replacement: path.join(dir, 'i18n.ts') },
        ...['Time', 'Select', 'Tree', 'FilterTime'].map(name => ({
          find: '@/custom-component/v-query/' + name + '.vue',
          replacement: empty
        })),
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
    server: { host: '127.0.0.1', port: 7097, strictPort: true, fs: { allow: [dir, frontend] } }
  })
  await server.listen()
})
test.afterAll(async () => {
  await server?.close()
  if (dir) await fs.rm(dir, { recursive: true, force: true })
})

for (const [grain, expected] of [
  ['monthrange', ['2026-07', '2026-09']],
  ['daterange', ['2026-07-01', '2026-09-30']],
  ['datetimerange', ['2026-07-01 00:00:00', '2026-09-30 23:59:59']]
]) {
  test(`${grain} 季度默认值、允许区间实际预览、保存重开`, async ({ page }) => {
    const errors = []
    page.on('pageerror', e => errors.push(e.message))
    await page.clock.install({ time: new Date(2026, 9, 30, 15, 25, 53) })
    await page.clock.pauseAt(new Date(2026, 9, 30, 15, 25, 53))
    await page.goto('http://127.0.0.1:7097/?grain=' + grain)
    const preview = page.locator('.dynamic-default-preview input')
    await expect(preview.nth(0)).toHaveValue(expected[0])
    await expect(preview.nth(1)).toHaveValue(expected[1])
    const allowed = page.locator('#allowed .ed-range-input')
    // Allowed interval follows the same preset, but its picker intentionally shows dates.
    await expect(allowed.nth(0)).toHaveValue(expected[0].slice(0, grain === 'monthrange' ? 7 : 10))
    const saved = await page.evaluate(() => JSON.stringify(window.fixture.config))
    await page.goto('http://127.0.0.1:7097/?grain=' + grain + '&saved=' + encodeURIComponent(saved))
    await expect(preview.nth(0)).toHaveValue(expected[0])
    await expect(preview.nth(1)).toHaveValue(expected[1])
    await page
      .locator('.ed-select__selected-item')
      .filter({ hasText: /^上季度$/ })
      .first()
      .click()
    await page.clock.runFor(500)
    await page.getByRole('option', { name: '本季度', exact: true }).click()
    await page.clock.runFor(500)
    await page
      .locator('#allowed .ed-select__selected-item')
      .filter({ hasText: /^上季度$/ })
      .click()
    await page.clock.runFor(500)
    await page.getByRole('option', { name: '本季度', exact: true }).click()
    await page.clock.runFor(500)
    await expect(preview.nth(0)).toHaveValue(
      grain === 'monthrange'
        ? '2026-10'
        : grain === 'daterange'
        ? '2026-10-01'
        : '2026-10-01 00:00:00'
    )
    await expect(preview.nth(1)).toHaveValue(
      grain === 'monthrange'
        ? '2026-12'
        : grain === 'daterange'
        ? '2026-12-31'
        : '2026-12-31 23:59:59'
    )
    await expect(allowed.nth(0)).toHaveValue(grain === 'monthrange' ? '2026-10' : '2026-10-01')
    expect(errors).toEqual([])
  })
}
