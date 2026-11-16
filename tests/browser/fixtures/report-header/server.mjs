import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import path from 'node:path'
const root = fileURLToPath(new URL('.', import.meta.url))
const frontend = path.resolve(root, '../../../../core/core-frontend')
const require = createRequire(path.join(frontend, 'package.json'))
const { createServer } = await import(require.resolve('vite'))
const { default: vue } = await import(require.resolve('@vitejs/plugin-vue'))
const bridge = path.join(root, 'bridge.js')
const server = await createServer({
  configFile: false,
  root,
  cacheDir: path.resolve(root, '../../output/report-header-vite'),
  plugins: [
    vue(),
    {
      name: 'report-header-boundaries',
      enforce: 'pre',
      resolveId(id, importer) {
        if (id === '@/utils/CanvasInfoTransUtils')
          return path.join(root, 'filters.js')
        if (
          importer?.endsWith('/BaseForm.vue') &&
          ['/formUtil', '/api', '/ConditionDefaultSet.vue'].some((suffix) =>
            id.endsWith(suffix)
          )
        )
          return bridge
        if (id === '@/hooks/web/useI18n') return bridge
      },
      load(id) {
        if (id.endsWith('.svg'))
          return 'export default { render() { return null } }'
      },
    },
  ],
  optimizeDeps: {
    noDiscovery: true,
    include: [
      'dayjs',
      'dayjs/locale/zh-cn',
      'dayjs/plugin/quarterOfYear',
      'dayjs/plugin/updateLocale',
      'vue',
      'element-plus-secondary',
      '@element-plus/icons-vue',
    ],
  },
  resolve: {
    alias: [
      {
        find: '@/utils/CanvasInfoTransUtils',
        replacement: path.join(root, 'filters.js'),
      },
      ...[
        '@/api/visualization/dataVisualization',
        '@/components/rich-text/TinymceEditor.vue',
      ].map((find) => ({ find, replacement: bridge })),

      ...[
        '@/hooks/web/useI18n',
        '@/utils/utils',
        '@/components/icon-custom',
      ].map((find) => ({ find, replacement: bridge })),
      { find: '@', replacement: path.join(frontend, 'src') },
      { find: 'dayjs', replacement: path.join(frontend, 'node_modules/dayjs') },
      {
        find: 'vue',
        replacement: path.join(
          frontend,
          'node_modules/vue/dist/vue.esm-bundler.js'
        ),
      },
      {
        find: '@element-plus/icons-vue',
        replacement: path.join(
          frontend,
          'node_modules/@element-plus/icons-vue'
        ),
      },
      {
        find: 'element-plus-secondary',
        replacement: path.join(frontend, 'node_modules/element-plus-secondary'),
      },
    ],
  },
  server: {
    host: '127.0.0.1',
    port: 4198,
    strictPort: true,
    fs: { allow: [root, frontend] },
  },
})
await server.listen()
for (const signal of ['SIGINT', 'SIGTERM'])
  process.on(signal, async () => {
    await server.close()
    process.exit()
  })
