import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const root = fileURLToPath(new URL('.', import.meta.url))
const frontend = path.resolve(root, '../../../../core/core-frontend')
const require = createRequire(path.join(frontend, 'package.json'))
const { createServer } = await import(require.resolve('vite'))
const { default: vue } = await import(require.resolve('@vitejs/plugin-vue'))

const bridge = path.join(root, 'bridge.js')
const bridgeImports = [
  '@/hooks/web/useI18n',
  '@/api/dataset',
  '@/api/variable',
  '@/store/modules/user',
  '@/components/icon-custom',
  '@/components/empty-background',
  '@/components/grid-table/src/GridTable.vue',
  '@/components/icon-group/field-list',
  '@/utils/attr',
  '@/utils/sqlUtil',
  '@/utils/ModelUtil'
]

const clipboard = path.join(root, 'clipboard.js')

const server = await createServer({
  configFile: false,
  root,
  cacheDir: path.resolve(root, '../../output/dataset-sql-fields-vite'),
  plugins: [
    vue(),
    {
      name: 'dataset-sql-fields-boundaries',
      enforce: 'pre',
      resolveId(id, importer) {
        if (!importer?.split('?')[0].endsWith('/AddSql.vue')) return
        if (id === './DatasetEditorAssistant.vue') {
          return '\0dataset-sql-assistant-stub'
        }
        if (['.', './util', './CodeMirror.vue'].includes(id)) {
          return bridge
        }
      },
      load(id) {
        if (id === '\0dataset-sql-assistant-stub') {
          return 'export default { name: "DatasetSqlAssistantStub", render() { return null } }'
        }
        if (id.endsWith('.svg')) {
          return 'export default { name: "SvgStub", render() { return null } }'
        }
      }
    }
  ],
  optimizeDeps: {
    noDiscovery: true,
    include: ['vue', 'element-plus-secondary', '@element-plus/icons-vue']
  },
  resolve: {
    alias: [
      ...bridgeImports.map(find => ({ find, replacement: bridge })),
      { find: 'vue-clipboard3', replacement: clipboard },
      { find: 'add-sql-real', replacement: path.join(frontend, 'src/views/visualized/data/dataset/form/AddSql.vue') },
      { find: '@', replacement: path.join(frontend, 'src') },
      { find: 'vue', replacement: path.join(frontend, 'node_modules/vue/dist/vue.esm-bundler.js') },
      { find: 'js-base64', replacement: path.join(frontend, 'node_modules/js-base64') },
      { find: 'lodash-es', replacement: path.join(frontend, 'node_modules/lodash-es') },
      { find: '@element-plus/icons-vue', replacement: path.join(frontend, 'node_modules/@element-plus/icons-vue') },
      { find: 'element-plus-secondary', replacement: path.join(frontend, 'node_modules/element-plus-secondary') }
    ]
  },
  server: {
    host: '127.0.0.1',
    port: 4198,
    strictPort: true,
    fs: { allow: [root, frontend] }
  }
})

await server.listen()

for (const signal of ['SIGINT', 'SIGTERM']) {
  process.on(signal, async () => {
    await server.close()
    process.exit()
  })
}
