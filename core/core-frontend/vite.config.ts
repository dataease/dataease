import Vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'
import electron from 'vite-plugin-electron'
import eslintPlugin from 'vite-plugin-eslint'
import VueJsx from '@vitejs/plugin-vue-jsx'

import viteStylelint from 'vite-plugin-stylelint'
import { resolve } from 'path'
import {
  createStyleImportPlugin,
  ElementPlusSecondaryResolve
} from 'vite-plugin-style-import-secondary'
import VueI18nPlugin from '@intlify/unplugin-vue-i18n/vite'

import electronRenderer from 'vite-plugin-electron-renderer'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'

import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components-secondary/vite'
import { ElementPlusResolver } from 'unplugin-vue-components-secondary/resolvers'

import path from 'path'
import { readdirSync } from 'fs'

export interface Page {
  name: string
  path?: string // 如'pages/dev'
  template?: string
}

function readPages(srcDir: string): Page[] {
  const pagesDir = path.resolve(srcDir, 'pages')
  let pages: Page[] = readdirSync(pagesDir, { withFileTypes: true })
    .filter(o => o.isDirectory() && !/^[._]/.test(o.name))
    .map(o => ({ name: o.name, path: path.join('pages', o.name) }))
  if (!pages.length) {
    pages = [
      {
        name: 'index',
        path: ''
      }
    ]
  }

  return pages
}

export const ROOT_DIR = path.resolve(__dirname, '.')

export const PAGES = readPages(path.resolve(ROOT_DIR, 'src'))

// https://vitejs.dev/config/
const root = process.cwd()

function pathResolve(dir: string) {
  return resolve(root, '.', dir)
}

const isDesktop = process.argv[3] === 'desktop'


// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    Vue(),
    VueJsx(),
    isDesktop && electron({
      entry: 'electron-main/index.ts' // 主进程文件
    }),
    createStyleImportPlugin({
      resolves: [ElementPlusSecondaryResolve()],
      libs: [
        {
          libraryName: 'element-plus-secondary',
          esModule: true,
          resolveStyle: name => {
            return `element-plus-secondary/es/components/${name.substring(3)}/style/css`
          }
        }
      ]
    }),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
    createSvgIconsPlugin({
      iconDirs: [pathResolve('src/assets/svg')],
      symbolId: 'icon-[dir]-[name]'
    }),
    VueI18nPlugin({
      runtimeOnly: false,
      compositionOnly: true,
      include: [resolve(__dirname, 'src/locales/**')]
    }),
    eslintPlugin({
      cache: false,
      include: ['src/**/*.ts','src/**/*.tsx', 'src/**/*.js', 'src/**/*.vue', 'src/*.ts', 'src/*.js', 'src/*.vue']
    }),
    viteStylelint(),
    isDesktop && electronRenderer()
  ],
  resolve: {
    extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.less', '.css'],
    alias: [
      {
        find: '@',
        replacement: `${pathResolve('src')}`
      }
    ]
  },
  server: {
    proxy: {
      '/api/f': {
        target: 'http://192.168.31.42:8100',
        // target: 'http://localhost:8100',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api\/f/, '')
      },
      // 使用 proxy 实例
      '/api': {
        target: 'https://de2.fit2cloud.com',
        // target: 'http://localhost:8100',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api/, '')
      }
    }
  },
  build: {
    sourcemap: true,
    emptyOutDir: false, // 默认情况下，若 outDir 在 root 目录下，则 Vite 会在构建时清空该目录
    rollupOptions: {
      // 多页支持
      input: PAGES.reduce((map, { name }) => {
        // @ts-ignore
        map[name] = path.resolve(ROOT_DIR, `${name}.html`)
        return map
      }, {})
    }
  },
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'vue-types',
      'element-plus-secondary/es/locale/lang/zh-cn',
      'element-plus-secondary/es/locale/lang/en',
      '@vueuse/core',
      'axios',
    ]
  }
})
