import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import path from 'node:path'
const root = fileURLToPath(new URL('.', import.meta.url))
const frontend = path.resolve(root, '../../../../core/core-frontend')
const require = createRequire(path.join(frontend, 'package.json'))
const { createServer } = await import(require.resolve('vite'))
const { default: vue } = await import(require.resolve('@vitejs/plugin-vue'))
const server = await createServer({
  configFile: false,
  root,
  cacheDir: path.resolve(
    root,
    `../../output/sync-datasource-import-vite-${process.env.SYNC_IMPORT_PORT || 4213}`
  ),
  plugins: [
    vue({ compiler: require('vue/compiler-sfc') }),
    {
      name: 'fixture-svg',
      load(id) {
        if (id.endsWith('.svg')) return 'export default { render() { return null } }'
      }
    }
  ],
  optimizeDeps: { noDiscovery: true, include: ['vue', 'element-plus-secondary'] },
  resolve: {
    alias: [
      ...['@/hooks/web/useI18n', '@/api/datasource', '@/hooks/web/useCache'].map((find) => ({
        find,
        replacement: path.join(root, 'bridge.js')
      })),
      {
        find: 'sync-import-form',
        replacement:
          process.env.SYNC_IMPORT_FORM ||
          path.join(frontend, 'src/views/menu/system/sync/ds/SyncForm.vue')
      },
      { find: 'js-base64', replacement: path.join(frontend, 'node_modules/js-base64/base64.mjs') },
      { find: '@', replacement: path.join(frontend, 'src') },
      {
        find: 'vue',
        replacement: path.join(frontend, 'node_modules/vue/dist/vue.esm-bundler.js')
      },
      {
        find: 'element-plus-secondary',
        replacement: path.join(frontend, 'node_modules/element-plus-secondary')
      }
    ]
  },
  server: {
    host: '127.0.0.1',
    port: Number(process.env.SYNC_IMPORT_PORT || 4213),
    strictPort: true,
    fs: {
      allow: [
        root,
        frontend,
        ...(process.env.SYNC_IMPORT_FORM ? [path.dirname(process.env.SYNC_IMPORT_FORM)] : [])
      ]
    }
  }
})
await server.listen()
for (const signal of ['SIGINT', 'SIGTERM'])
  process.on(signal, async () => {
    await server.close()
    process.exit()
  })
