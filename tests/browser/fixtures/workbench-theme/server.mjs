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
  '@/hooks/web/useCache',
  '@/store/modules/permission',
  '@/store/modules/request',
  '@/store/modules/interactive',
  '@/store/modules/user',
  '@/store/modules/embedded',
  '@/store/modules/app',
  '@/store/modules/share',
  '@/api/templateMarket',
  '@/api/visualization/dataVisualization',
  '@/views/workbranch/ShortcutTable.vue',
  '@/views/workbranch/TemplateBranchItem.vue',
  '@/views/workbranch/TemplateBranchItemSkeleton.vue',
  '@/views/common/DeResourceCreateOptV2.vue',
  'vue-router_2',
  'js-base64'
]

const server = await createServer({
  configFile: false,
  root,
  cacheDir: path.resolve(root, '../../output/workbench-theme-vite'),
  plugins: [
    vue(),
    {
      name: 'workbench-theme-boundaries',
      enforce: 'pre',
      load(id) {
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
      {
        find: 'workbranch-real',
        replacement: path.join(frontend, 'src/views/pages/workbranch/index.vue')
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
    port: 4196,
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
