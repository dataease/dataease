/* eslint-disable @typescript-eslint/no-var-requires */
const { test, expect } = require(process.env.PLAYWRIGHT_TEST_MODULE || '@playwright/test')
const fs = require('node:fs/promises')
const path = require('node:path')
const os = require('node:os')
const { pathToFileURL } = require('node:url')
const frontend = path.resolve(__dirname, '../..')
let server, dir

test.beforeAll(async () => {
  dir = await fs.realpath(await fs.mkdtemp(path.join(os.tmpdir(), 'dataease-clock-ui-')))
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
import Configuration from '${frontend}/src/custom-component/v-query/ConditionDefaultConfiguration.vue';
import {validateDynamicDefault,evaluateDynamicDefault} from '${frontend}/src/custom-component/v-query/dynamic-default-time';
import {t} from './i18n';
const params=new URLSearchParams(location.search);
const config=reactive({id:'time',displayType:params.has('range')?'7':'1',field:{deType:1},timeType:'dynamic',defaultValueCheck:true,
timeGranularity:params.has('date')?'date':'datetime',timeGranularityMultiple:params.has('date')?'daterange':'datetimerange',relativeToCurrent:'custom',relativeToCurrentRange:'custom',
timeNum:0,relativeToCurrentType:'date',around:'f',timeNumRange:0,relativeToCurrentTypeRange:'date',aroundRange:'f',arbitraryTime:new Date(2020,0,1,9,10,11),arbitraryTimeRange:new Date(2020,0,1,23,59,59)});
window.fixture={config,evaluateDynamicDefault,validateDynamicDefault};
createApp({setup(){const saved=ref('');return()=>h('main',{style:'width:460px;padding:16px'},[h(Configuration,{curComponent:config}),h('button',{onClick:()=>{const error=validateDynamicDefault(config);saved.value=error?t(error):'saved'}},'保存'),h('output',saved.value)])}}).use(ElementPlus,{namespace:'ed'}).component('Icon',{render:()=>h('span')}).mount('#app');
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
        ...['Time', 'Select', 'Tree', 'RangeFilterTime', 'FilterTime'].map(name => ({
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
    server: { host: '127.0.0.1', port: 7096, strictPort: true, fs: { allow: [dir, frontend] } }
  })
  await server.listen()
})
test.afterAll(async () => {
  await server?.close()
  if (dir) await fs.rm(dir, { recursive: true, force: true })
})
const open = async (page, query = '') => {
  const errors = []
  page.on('pageerror', e => errors.push(e.message))
  await page.clock.install({ time: new Date(2026, 9, 30, 15, 25, 53) })
  await page.clock.pauseAt(new Date(2026, 9, 30, 15, 25, 53))
  await page.goto('http://127.0.0.1:7096/?' + query)
  await expect(page.locator('.dynamic-default-preview input').first()).toBeVisible()
  return errors
}
const relative = async (page, index = 0) =>
  page.locator('.clock-offset-editor').nth(index).getByText('相对当前', { exact: true }).click()

test('旧配置指定时刻、切换保留值、预览基准一致，实际UI无溢出', async ({ page }) => {
  const errors = await open(page)
  await expect(page.locator('.dynamic-default-preview input')).toHaveValue('2026-10-30 09:10:11')
  await relative(page)
  await page.getByRole('spinbutton', { name: '时', exact: true }).fill('1')
  await page.getByRole('spinbutton', { name: '分', exact: true }).fill('30')
  await expect(page.locator('.dynamic-default-preview input')).toHaveValue('2026-10-30 13:55:53')
  await expect(page.locator('.baseline')).toHaveText('当前时刻：2026-10-30 15:25:53')
  await page.locator('.clock-offset-editor').getByText('指定时刻', { exact: true }).click()
  await expect(page.locator('.dynamic-default-preview input')).toHaveValue('2026-10-30 09:10:11')
  await relative(page)
  await expect(page.getByRole('spinbutton', { name: '分', exact: true })).toHaveValue('30')
  const fits = await page
    .locator('.clock-offset-editor')
    .evaluate(el => el.scrollWidth <= el.clientWidth)
  expect(fits).toBe(true)
  await page.screenshot({ path: '/tmp/dataease-clock-offset-single.png' })
  expect(errors).toEqual([])
})
for (const query of ['', 'range'])
  test(`数字组件及整行宽度 ${query || 'single'}`, async ({ page }) => {
    const errors = await open(page, query)
    const layouts = await page.locator('.date-offset-only').evaluateAll(elements =>
      elements.map(el => {
        const bounds = el.getBoundingClientRect()
        const children = [...el.children].map(child => child.getBoundingClientRect())
        return {
          padding: getComputedStyle(el).paddingLeft,
          left: bounds.left,
          right: bounds.right,
          children: children.map(child => ({
            left: child.left,
            right: child.right,
            width: child.width
          }))
        }
      })
    )
    for (const layout of layouts) {
      expect(layout.padding).toBe('0px')
      expect(layout.children).toHaveLength(3)
      expect(Math.abs(layout.children[0].left - layout.left)).toBeLessThan(1)
      expect(Math.abs(layout.children[2].right - layout.right)).toBeLessThan(1)
      expect(
        Math.max(...layout.children.map(c => c.width)) -
          Math.min(...layout.children.map(c => c.width))
      ).toBeLessThan(1)
    }
    const widths = await page.locator('.fixed-clock').evaluateAll(elements =>
      elements.map(el => {
        const bounds = el.getBoundingClientRect(),
          row = el.parentElement.getBoundingClientRect()
        const label = el.previousElementSibling.getBoundingClientRect()
        return {
          left: bounds.left,
          right: bounds.right,
          rowRight: row.right,
          labelRight: label.right
        }
      })
    )
    for (const width of widths) {
      expect(Math.abs(width.right - width.rowRight)).toBeLessThan(1)
      expect(Math.abs(width.left - width.labelRight - 8)).toBeLessThan(1)
    }
    await page.screenshot({ path: `/tmp/dataease-clock-fixed-${query || 'single'}.png` })
    await relative(page)
    const editor = page.locator('.clock-offset-editor').first()
    await expect(editor.locator('.clock-inputs .ed-input-number')).toHaveCount(3)
    const digitsFit = await editor.locator('.clock-inputs input').evaluateAll(inputs => {
      const context = document.createElement('canvas').getContext('2d')
      return inputs.every(input => {
        context.font = getComputedStyle(input).font
        return input.clientWidth >= Math.ceil(context.measureText('59').width) + 2
      })
    })
    expect(digitsFit).toBe(true)
    await editor.locator('.ed-input-number__increase').first().click()
    await expect(editor.getByRole('spinbutton', { name: '时', exact: true })).toHaveValue('1')
    expect(errors).toEqual([])
  })
test('非法数字不修正，预览错误并阻止保存；日期负值同样拒绝', async ({ page }) => {
  const errors = await open(page)
  await relative(page)
  for (const value of ['60', '-1', '0.5', '']) {
    await page.getByRole('spinbutton', { name: '分', exact: true }).fill(value)
    await page.getByRole('button', { name: '保存', exact: true }).click()
    await expect(page.locator('output')).toContainText('请输入有效的非负整数')
    await expect(page.getByRole('spinbutton', { name: '分', exact: true })).toHaveValue(value)
    await expect(page.getByRole('alert')).toBeVisible()
  }
  await page.getByRole('spinbutton', { name: '分', exact: true }).fill('0')
  await page.locator('.setting-input').first().getByRole('spinbutton').fill('-1')
  await page.getByRole('button', { name: '保存', exact: true }).click()
  await expect(page.locator('output')).toContainText('请输入有效的非负整数')
  expect(errors).toEqual([])
})
test('时间范围两端独立模式，同一基准；起止颠倒阻止保存', async ({ page }) => {
  const errors = await open(page, 'range')
  await relative(page, 0)
  await relative(page, 1)
  const editors = page.locator('.clock-offset-editor')
  await editors.nth(0).getByRole('spinbutton', { name: '时', exact: true }).fill('1')
  await editors.nth(1).getByRole('spinbutton', { name: '时', exact: true }).fill('2')
  await page.getByRole('button', { name: '保存', exact: true }).click()
  await expect(page.locator('output')).toContainText('开始时间')
  await editors.nth(0).getByRole('spinbutton', { name: '时', exact: true }).fill('25')
  await page.getByRole('button', { name: '保存', exact: true }).click()
  await expect(page.locator('output')).toHaveText('saved')
  const values = await page
    .locator('.dynamic-default-preview input')
    .evaluateAll(els => els.map(el => el.value))
  expect(values).toEqual(['2026-10-29 14:25:53', '2026-10-30 13:25:53'])
  await page.screenshot({ path: '/tmp/dataease-clock-offset-range.png' })
  expect(errors).toEqual([])
})
test('非时分秒粒度保持原有配置界面', async ({ page }) => {
  const errors = await open(page, 'date')
  await expect(page.locator('.clock-offset-editor')).toHaveCount(0)
  expect(errors).toEqual([])
})
for (const lang of ['en', 'tw'])
  test(`${lang} 文案不显示国际化key`, async ({ page }) => {
    const errors = await open(page, 'lang=' + lang)
    await expect(page.locator('main')).not.toContainText('dynamic_time.')
    expect(errors).toEqual([])
  })
