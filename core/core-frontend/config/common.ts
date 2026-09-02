import path from 'path'
import { resolve } from 'path'
import Vue from '@vitejs/plugin-vue'
import eslintPlugin from 'vite-plugin-eslint'
import VueJsx from '@vitejs/plugin-vue-jsx'
import viteStylelint from 'vite-plugin-stylelint'
import {
  createStyleImportPlugin,
  ElementPlusSecondaryResolve
} from 'vite-plugin-style-import-secondary'
import VueI18nPlugin from '@intlify/unplugin-vue-i18n/vite'
import svgLoader from 'vite-svg-loader'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components-secondary/vite'
import { ElementPlusResolver } from 'unplugin-vue-components-secondary/resolvers'
import { createG2LayoutOptimizerPlugin, createG2LayoutPlugin } from './g2-layout'
const root = process.cwd()

export function pathResolve(dir: string) {
  return resolve(root, '.', dir)
}
export default {
  base: './',
  plugins: [
    // 在 G2 模块解析前接入 DataEase 布局校正，升级 G2 时需验证内部 layout 入口
    createG2LayoutPlugin(root),
    Vue(),
    svgLoader({
      svgo: false,
      defaultImport: 'component' // or 'raw'
    }),
    VueJsx(),
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
      resolvers: [ElementPlusResolver()]
    }),
    Components({
      resolvers: [ElementPlusResolver()]
    }),
    VueI18nPlugin({
      runtimeOnly: false,
      compositionOnly: true,
      include: [resolve(__dirname, 'src/locales/**')]
    }),
    eslintPlugin({
      cache: true,
      include: [
        'src/**/*.ts',
        'src/**/*.tsx',
        'src/**/*.js',
        'src/**/*.vue',
        'src/*.ts',
        'src/*.js',
        'src/*.vue'
      ]
    }),
    viteStylelint({
      cache: true
    })
  ],
  css: {
    preprocessorOptions: {
      less: {
        modifyVars: {
          hack: `true; @import (reference) "${path.resolve('src/style/variable.less')}";`
        },
        javascriptEnabled: true
      }
    }
  },
  resolve: {
    extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.less', '.css'],
    alias: [
      {
        find: '@',
        replacement: `${pathResolve('src')}`
      }
    ]
  },
  optimizeDeps: {
    esbuildOptions: {
      // 开发依赖预构建不会经过常规 Rollup 解析，需要保持与生产构建一致
      plugins: [createG2LayoutOptimizerPlugin(root)]
    },
    include: [
      'vue',
      'vue-router',
      'vue-types',
      // 组件样式由按需插件注入，提前预构建以避免访问懒加载路由时刷新依赖哈希
      'element-plus-secondary/es/components/**/style/css',
      'element-plus-secondary/es/locale/lang/zh-cn',
      'element-plus-secondary/es/locale/lang/en',
      '@vueuse/core',
      'axios'
    ]
  }
}
