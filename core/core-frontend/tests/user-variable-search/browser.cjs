/* eslint-disable @typescript-eslint/no-var-requires */
const { test, expect } = require(process.env.PLAYWRIGHT_TEST_MODULE || '@playwright/test')
const fs = require('node:fs/promises')
const path = require('node:path')
const os = require('node:os')
const { pathToFileURL } = require('node:url')
const frontend = path.resolve(__dirname, '../..')
let server, dir

test.beforeAll(async () => {
  dir = await fs.realpath(await fs.mkdtemp(path.join(os.tmpdir(), 'user-variable-search-')))
  await fs.writeFile(
    path.join(dir, 'index.html'),
    '<div id="app"></div><script type="module" src="/main.ts"></script>'
  )
  await fs.writeFile(
    path.join(dir, 'bridge.ts'),
    `
import zh from '${frontend}/src/locales/zh-CN';
export const t=key=>key.split('.').reduce((value,part)=>value?.[part],zh)||key;
export const useI18n=()=>({t});export const useUserStoreWithOut=()=>({getUid:'administrator'});
export const EMAIL_REGEX=/^[^@]+@[^@]+$/;
export const useClipboard=()=>({toClipboard:async()=>undefined});
export const Icon={render:()=>null};
const variable={id:'unit',name:'单位代码',type:'text'};
const options=Array.from({length:2005},(_,i)=>({id:'value-'+i,value:'UNIT-'+String(i).padStart(4,'0'),valueDesc:i===2004?'特殊研发单位':'普通单位'}));
export const searchVariableApi=async()=>({data:[variable]});
export const valueForVariable=async()=>{window.valueRequests=(window.valueRequests||0)+1;return {data:options}};
export const queryFormApi=async()=>({data:{id:'synthetic-user',account:'sample',name:'示例用户',email:'sample@example.invalid',enable:true,roleIds:[],defaultOid:null,variables:[{variableId:'unit',variableType:'text',sysVariableDto:variable,variableValueIds:['value-7'],valueList:[]}]}});
export const searchApi=async()=>({data:[]});export const roleOptionForUserApi=async()=>({data:[]});export const defaultPwdApi=async()=>({data:'synthetic-only'});
export const userCreateApi=async data=>{window.saved=JSON.parse(JSON.stringify(data));return {}};export const userEditApi=userCreateApi;
`
  )
  await fs.writeFile(
    path.join(dir, 'main.ts'),
    `
import {createApp,h,ref} from 'vue';import ElementPlus from 'element-plus-secondary';import 'element-plus-secondary/dist/index.css';
import UserForm from '${frontend}/src/views/menu/system/sysuser/UserForm.vue';import {t} from './bridge';
const app=createApp({setup(){const form=ref();return()=>h('main',[h('button',{onClick:()=>form.value.init()},'新增合成用户'),h('button',{onClick:()=>form.value.edit('synthetic-user')},'编辑合成用户'),h(UserForm,{ref:form})])}});
app.config.globalProperties.$t=t;app.use(ElementPlus,{namespace:'ed'});app.mount('#app');
`
  )
  const { createServer } = await import(
    pathToFileURL(require.resolve('vite', { paths: [frontend] }))
  )
  const { default: vue } = await import(
    pathToFileURL(require.resolve('@vitejs/plugin-vue', { paths: [frontend] }))
  )
  const bridge = path.join(dir, 'bridge.ts')
  server = await createServer({
    configFile: false,
    root: dir,
    cacheDir: path.join(dir, 'cache'),
    resolve: {
      alias: [
        ...[
          '@/hooks/web/useI18n',
          '@/store/modules/user',
          '@/utils/validate',
          '@/api/user',
          '@/api/variable',
          '@/api/org',
          '@/components/icon-custom'
        ].map(find => ({ find, replacement: bridge })),
        { find: 'vue-clipboard3', replacement: path.join(dir, 'clipboard.ts') },
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
    server: { host: '127.0.0.1', port: 7116, strictPort: true, fs: { allow: [dir, frontend] } }
  })
  await fs.writeFile(
    path.join(dir, 'clipboard.ts'),
    "export {useClipboard as default} from './bridge'"
  )
  await server.listen()
})
test.afterAll(async () => {
  await server?.close()
  if (dir) await fs.rm(dir, { recursive: true, force: true })
})

for (const edit of [false, true]) {
  test(`${edit ? '编辑' : '新增'}用户可检索 2005 个变量候选并保留多选`, async ({ page }) => {
    const errors = []
    page.on('pageerror', error => errors.push(error.message))
    await page.goto('http://127.0.0.1:7116')
    await page
      .getByRole('button', { name: edit ? '编辑合成用户' : '新增合成用户', exact: true })
      .click()
    if (!edit) {
      await page.getByRole('button', { name: '添加参数', exact: true }).click()
      await page.locator('.system-table_variable .ed-select').first().click()
      await page.getByRole('option', { name: '单位代码', exact: true }).click()
    }
    const select = page.locator('.system-table_variable .ed-select').nth(1)
    const input = select.locator('input')
    await expect(input).toBeEditable()
    await select.click()
    await input.fill('unit-2004')
    await expect(
      page.getByRole('option', { name: 'UNIT-2004(特殊研发单位)', exact: true })
    ).toBeVisible()
    await expect(
      page.getByRole('option', { name: 'UNIT-0008(普通单位)', exact: true })
    ).toBeHidden()
    await page.getByRole('option', { name: 'UNIT-2004(特殊研发单位)', exact: true }).click()
    await input.fill('特殊研发')
    await expect(
      page.getByRole('option', { name: 'UNIT-2004(特殊研发单位)', exact: true })
    ).toBeVisible()
    await input.fill('not-a-unit')
    await expect(
      page.getByRole('option', { name: 'UNIT-2004(特殊研发单位)', exact: true })
    ).toBeHidden()
    await input.fill('UNIT-0008')
    await page.getByRole('option', { name: 'UNIT-0008(普通单位)', exact: true }).click()
    await input.fill('')
    await page
      .getByRole('option', { name: 'UNIT-2004(特殊研发单位)', exact: true })
      .scrollIntoViewIfNeeded()
    await expect(
      page.getByRole('option', { name: 'UNIT-2004(特殊研发单位)', exact: true })
    ).toHaveAttribute('aria-selected', 'true')
    await expect(
      page.getByRole('option', { name: 'UNIT-0008(普通单位)', exact: true })
    ).toHaveAttribute('aria-selected', 'true')
    if (edit)
      await expect(
        page.getByRole('option', { name: 'UNIT-0007(普通单位)', exact: true })
      ).toHaveAttribute('aria-selected', 'true')
    expect(await page.evaluate(() => window.valueRequests)).toBe(1)
    await input.press('Escape')
    if (!edit) {
      const formInputs = page.locator('.user-edit-form input')
      await formInputs.nth(0).fill('synthetic_sample')
      await formInputs.nth(1).fill('示例用户')
      await formInputs.nth(2).fill('sample@example.invalid')
    }
    await page
      .locator('.ed-dialog__footer')
      .getByRole('button', { name: '确定', exact: true })
      .click()
    await expect
      .poll(() => page.evaluate(() => window.saved?.variables[0]?.variableValueIds))
      .toEqual(edit ? ['value-7', 'value-2004', 'value-8'] : ['value-2004', 'value-8'])
    expect(errors).toEqual([])
  })
}
