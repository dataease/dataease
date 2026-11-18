/* eslint-disable @typescript-eslint/no-var-requires */
const { test, expect } = require(process.env.PLAYWRIGHT_TEST_MODULE || '@playwright/test')
const fs = require('node:fs/promises')
const path = require('node:path')
const os = require('node:os')
const { pathToFileURL } = require('node:url')
const { execFileSync } = require('node:child_process')
const frontend = path.resolve(__dirname, '../..')
let dir, server, component

test.beforeAll(async () => {
  const beforeRef = process.env.I18N_BEFORE_REF
  if (process.env.I18N_HMR_VARIANT === 'before' && !beforeRef) {
    throw new Error('Set I18N_BEFORE_REF to a commit before the i18n HMR fix')
  }
  dir = await fs.realpath(await fs.mkdtemp(path.join(os.tmpdir(), 'dataease-i18n-hmr-')))
  const plugin = path.join(dir, 'plugins/vue-i18n')
  const hooks = path.join(dir, 'hooks')
  await fs.mkdir(plugin, { recursive: true })
  await fs.mkdir(hooks)
  await fs.mkdir(path.join(dir, 'locales'))
  for (const name of ['index.ts', 'instance.ts', 'loader.ts', 'helper.ts']) {
    const relative = 'src/plugins/vue-i18n/' + name
    const text =
      process.env.I18N_HMR_VARIANT === 'before' && name === 'index.ts'
        ? execFileSync('git', ['show', beforeRef + ':core/core-frontend/' + relative], {
            cwd: frontend,
            encoding: 'utf8'
          })
        : await fs.readFile(path.join(frontend, relative), 'utf8')
    await fs.writeFile(path.join(plugin, name), text)
  }
  const hookSource =
    process.env.I18N_HMR_VARIANT === 'before'
      ? execFileSync('git', ['show', beforeRef + ':core/core-frontend/src/hooks/web/useI18n.ts'], {
          cwd: frontend,
          encoding: 'utf8'
        })
      : await fs.readFile(path.join(frontend, 'src/hooks/web/useI18n.ts'), 'utf8')
  await fs.writeFile(path.join(hooks, 'useI18n.ts'), hookSource)
  const messages = { 'zh-CN': ['确认', '你好'], en: ['Confirm', 'Hello'], tw: ['確認', '你好'] }
  for (const [locale, [confirm, hello]] of Object.entries(messages)) {
    await fs.writeFile(
      path.join(dir, 'locales', locale + '.ts'),
      `export default {chart:{confirm:'${confirm}'},common:{greet:'${hello} {name}'}}`
    )
  }
  await fs.writeFile(
    path.join(dir, 'bridge.ts'),
    `
export const getLocale=()=> 'zh-CN';export const PATH_URL='/fixture';export const useCache=()=>({wsCache:{set(){}}});
const state={getLocaleMap:Promise.resolve(['zh-CN','en','tw'].map(lang=>({lang,name:lang}))),setCurrentLocale(){}};
export const useLocaleStoreWithOut=()=>state;
`
  )
  component = path.join(dir, 'Probe.vue')
  await fs.writeFile(
    component,
    `<script setup lang="ts">
import {useI18n} from '@/hooks/web/useI18n';
const {t}=useI18n();</script>
<template><main><output data-testid="translated">{{t('chart.confirm')}}</output><input placeholder="draft" /><span>revision-0</span></main></template>`
  )
  await fs.writeFile(
    path.join(dir, 'main.ts'),
    `
import {createApp} from 'vue';import Probe from './Probe.vue';
import {setupI18n,i18n} from '@/plugins/vue-i18n';import {useI18n} from '@/hooks/web/useI18n';
import {useLocale} from '${frontend}/src/hooks/web/useLocale';
const early=useI18n();const namespaced=useI18n('chart');const before=early.t('chart.confirm');
await setupI18n(createApp({}));const app=createApp(Probe);app.use(i18n);app.mount('#app');
window.fixture={early,namespaced,before,original:i18n,changeLocale:useLocale().changeLocale};
`
  )
  await fs.writeFile(
    path.join(dir, 'index.html'),
    '<div id="app"></div><script type="module" src="/main.ts"></script>'
  )
  const { createServer } = await import(
    pathToFileURL(path.join(frontend, 'node_modules/vite/dist/node/index.js'))
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
        { find: '@/plugins/vue-i18n', replacement: plugin },
        { find: '@/hooks/web/useI18n', replacement: path.join(hooks, 'useI18n.ts') },
        ...[
          '@/utils/utils',
          '@/hooks/web/useCache',
          '@/store/modules/locale',
          '@/config/axios/service'
        ].map(find => ({ find, replacement: bridge })),
        { find: '@', replacement: path.join(frontend, 'src') },
        {
          find: /^vue$/,
          replacement: require.resolve('vue/dist/vue.runtime.esm-bundler.js', { paths: [frontend] })
        },
        {
          find: /^vue-i18n$/,
          replacement: require.resolve('vue-i18n/dist/vue-i18n.esm-bundler.js', {
            paths: [frontend]
          })
        }
      ],
      dedupe: ['vue', 'vue-i18n']
    },
    plugins: [vue()],
    server: { host: '127.0.0.1', port: 7095, strictPort: true, fs: { allow: [dir, frontend] } }
  })
  await server.listen()
})
test.afterAll(async () => {
  await server?.close()
  if (dir) await fs.rm(dir, { recursive: true, force: true })
})
const open = async page => {
  const errors = []
  page.on('pageerror', e => errors.push(e.message))
  await page.goto('http://127.0.0.1:7095/')
  await expect(page.getByTestId('translated')).toHaveText('确认')
  return errors
}

test('初始化前取得的翻译函数，初始化后可以正常翻译和插值', async ({ page }) => {
  const errors = await open(page)
  const result = await page.evaluate(() => ({
    before: window.fixture.before,
    after: window.fixture.early.t('chart.confirm'),
    namespaced: window.fixture.namespaced.t('confirm'),
    named: window.fixture.early.t('common.greet', { name: 'Ada' }),
    literal: window.fixture.early.t('literal')
  }))
  expect(result).toEqual({
    before: 'chart.confirm',
    after: '确认',
    namespaced: '确认',
    named: '你好 Ada',
    literal: 'literal'
  })
  expect(errors).toEqual([])
})
test('国际化入口带新时间戳重新求值，仍使用已安装实例', async ({ page }) => {
  const errors = await open(page)
  const result = await page.evaluate(async () => {
    const values = []
    for (let version = 1; version <= 3; version++) {
      const module = await import('/plugins/vue-i18n/index.ts?t=' + version)
      const hook = await import('/hooks/useI18n.ts?t=' + version)
      values.push({
        same: module.i18n === window.fixture.original,
        text: hook.useI18n().t('chart.confirm')
      })
    }
    return values
  })
  expect(result).toEqual(Array(3).fill({ same: true, text: '确认' }))
  expect(errors).toEqual([])
})
test('Vue 文件真实热更新后，保持翻译和未保存输入', async ({ page }) => {
  const errors = await open(page)
  await page.getByPlaceholder('draft').fill('unsaved draft')
  let navigations = 0
  page.on('framenavigated', frame => {
    if (frame === page.mainFrame()) navigations++
  })
  for (let revision = 1; revision <= 2; revision++) {
    const source = await fs.readFile(component, 'utf8')
    await fs.writeFile(
      component,
      source.replace('revision-' + (revision - 1), 'revision-' + revision)
    )
    await expect(page.locator('main span')).toHaveText('revision-' + revision)
    await expect(page.getByTestId('translated')).toHaveText('确认')
    await expect(page.getByPlaceholder('draft')).toHaveValue('unsaved draft')
  }
  expect(navigations).toBe(0)
  expect(errors).toEqual([])
})
test('切换语言后，早期函数、全局翻译及新组件保持一致', async ({ page }) => {
  const errors = await open(page)
  for (const [locale, text] of [
    ['en', 'Confirm'],
    ['tw', '確認'],
    ['zh-CN', '确认']
  ]) {
    const translated = await page.evaluate(async locale => {
      await window.fixture.changeLocale(locale)
      return {
        hook: window.fixture.early.t('chart.confirm'),
        global: window.fixture.original.global.t('chart.confirm')
      }
    }, locale)
    expect(translated).toEqual({ hook: text, global: text })
    await expect(page.getByTestId('translated')).toHaveText(text)
  }
  expect(errors).toEqual([])
})
