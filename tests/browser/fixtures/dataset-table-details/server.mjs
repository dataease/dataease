import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const root = fileURLToPath(new URL('.', import.meta.url))
const frontend = path.resolve(root, '../../../../core/core-frontend')
const require = createRequire(path.join(frontend, 'package.json'))
const { createServer } = await import(require.resolve('vite'))
const { default: vue } = await import(require.resolve('@vitejs/plugin-vue'))
const { default: svgLoader } = await import(require.resolve('vite-svg-loader'))
const bridge = path.join(root, 'bridge.js')

const server = await createServer({
  configFile: false,
  root,
  cacheDir: path.resolve(root, '../../output/dataset-table-details-vite'),
  plugins: [vue(), svgLoader({ svgo: false, defaultImport: 'component' })],
  optimizeDeps: {
    noDiscovery: true,
    include: ['vue', 'element-plus-secondary', '@element-plus/icons-vue']
  },
  css: {
    preprocessorOptions: {
      less: {
        javascriptEnabled: true,
        additionalData: `@import "${path.join(frontend, 'src/style/variable.less')}";`
      }
    }
  },
  resolve: {
    alias: [
      { find: '@/api/dataset', replacement: bridge },
      { find: '@/hooks/web/useQueryScope', replacement: bridge },
      { find: '@/hooks/web/useI18n', replacement: bridge },
      {
        find: 'dataset-table-details',
        replacement: path.join(
          frontend,
          'src/views/visualized/data/dataset/form/DatasetTableDetails.vue'
        )
      },
      { find: '@', replacement: path.join(frontend, 'src') },
      { find: 'vue', replacement: path.join(frontend, 'node_modules/vue/dist/vue.esm-bundler.js') },
      {
        find: '@element-plus/icons-vue',
        replacement: path.join(frontend, 'node_modules/@element-plus/icons-vue')
      },
      {
        find: 'element-plus-secondary',
        replacement: path.join(frontend, 'node_modules/element-plus-secondary')
      }
    ]
  },
  server: {
    host: '127.0.0.1',
    port: 4199,
    strictPort: true,
    fs: { allow: [root, frontend] }
  }
})

await server.listen()
for (const signal of ['SIGINT', 'SIGTERM'])
  process.on(signal, async () => {
    await server.close()
    process.exit()
  })
