import { createServer } from '../../../../core/core-frontend/node_modules/vite/dist/node/index.js'
import { fileURLToPath } from 'node:url'
const root = fileURLToPath(new URL('.', import.meta.url))
const repo = fileURLToPath(new URL('../../../../', import.meta.url))
const stub = root + 'boundary.js'
const server = await createServer({
  configFile: false,
  root,
  resolve: {
    alias: [
      ...[
        '@/hooks/web/useCache',
        '@/store/modules/embedded',
        '@/store/modules/link',
        '@/store/modules/user',
        '@/config/axios',
        '@/views/visualized/data/dataset/form/util.js',
        'element-plus-secondary'
      ].map((find) => ({ find, replacement: stub })),
      { find: '@', replacement: repo + 'core/core-frontend/src' },
      {
        find: 'vue',
        replacement:
          repo +
          'core/core-frontend/node_modules/vue/dist/vue.runtime.esm-bundler.js'
      }
    ]
  },
  server: {
    host: '127.0.0.1',
    port: 4186,
    strictPort: true,
    fs: { allow: [repo] }
  },
  logLevel: 'error'
})
await server.listen()
for (const signal of ['SIGINT', 'SIGTERM'])
  process.on(signal, async () => {
    await server.close()
    process.exit()
  })
