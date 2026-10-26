import { defineConfig } from '../../node_modules/vite/dist/node/index.js'
import vue from '../../node_modules/@vitejs/plugin-vue/dist/index.mjs'
import svgLoader from '../../node_modules/vite-svg-loader/index.js'
import { resolve } from 'node:path'
import { tmpdir } from 'node:os'
const host = resolve(__dirname, '../..')
const mock = resolve(__dirname, 'ui-mock.ts')
export default defineConfig({
  root: __dirname,
  cacheDir: resolve(tmpdir(), 'dataease-report-delivery-vite'),
  plugins: [vue(), svgLoader({ svgo: false, defaultImport: 'component' })],
  optimizeDeps: { include: ['@element-plus/icons-vue'] },
  resolve: {
    alias: [
      { find: '@/hooks/web/useI18n', replacement: mock },
      { find: '@/plugins/vue-i18n', replacement: mock },
      { find: '@/config/axios', replacement: mock },
      {
        find: /^@element-plus\/icons-vue$/,
        replacement: resolve(host, 'node_modules/@element-plus/icons-vue/dist/index.js')
      },
      { find: /^vue$/, replacement: resolve(host, 'node_modules/vue/dist/vue.esm-bundler.js') },
      {
        find: /^element-plus-secondary$/,
        replacement: resolve(host, 'node_modules/element-plus-secondary/es/index.mjs')
      },
      { find: /^dayjs$/, replacement: resolve(host, 'node_modules/dayjs/dayjs.min.js') },
      { find: /^vue-uuid$/, replacement: resolve(host, 'node_modules/vue-uuid/dist/index.esm.js') },
      {
        find: /^vue-i18n$/,
        replacement: resolve(host, 'node_modules/vue-i18n/dist/vue-i18n.esm-bundler.js')
      },
      { find: /^mitt$/, replacement: resolve(host, 'node_modules/mitt/dist/mitt.mjs') },
      { find: '@', replacement: resolve(host, 'src') }
    ],
    dedupe: ['vue']
  },
  server: {
    host: '127.0.0.1',
    port: 7096,
    strictPort: true,
    fs: { allow: [resolve(__dirname, '../../../..')] }
  }
})
