import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import path from 'node:path'
import fs from 'node:fs/promises'
const root = fileURLToPath(new URL('.', import.meta.url))
const frontend = path.resolve(root, '../../../../core/core-frontend')
const require = createRequire(path.join(frontend, 'package.json'))
const { createServer } = await import(require.resolve('vite'))
const { default: vue } = await import(require.resolve('@vitejs/plugin-vue'))
const server = await createServer({
  configFile: false,
  root: frontend,
  plugins: [
    vue(),
    {
      name: 'ai-fixture-entry',
      configureServer(server) {
        server.middlewares.use(async (request, response, next) => {
          if (request.url?.split('?')[0] !== '/') return next()
          const html = (await fs.readFile(path.join(root, 'index.html'), 'utf8')).replace(
            './main.js',
            '/@fs/' + path.join(root, 'main.js')
          )
          response.setHeader('Content-Type', 'text/html')
          response.end(await server.transformIndexHtml('/', html))
        })
      }
    }
  ],
  optimizeDeps: { entries: [path.join(root, 'main.js')] },
  cacheDir: path.join(root, '../../output/ai-vite-cache'),
  resolve: {
    alias: [
      ...['@/config/axios', '@/hooks/web/useI18n', '@/store/modules/user'].map((find) => ({
        find,
        replacement: path.join(root, 'bridge.js')
      })),
      {
        find: 'ai-service-page',
        replacement: path.join(frontend, 'src/views/system/ai-service/index.vue')
      },
      { find: '@', replacement: path.join(frontend, 'src') },
      { find: 'vue', replacement: path.join(frontend, 'node_modules/vue/dist/vue.esm-bundler.js') },
      {
        find: 'vue-router_2',
        replacement: path.join(frontend, 'node_modules/vue-router_2/dist/vue-router.mjs')
      },
      {
        find: 'element-plus-secondary',
        replacement: path.join(frontend, 'node_modules/element-plus-secondary')
      }
    ]
  },
  server: { host: '127.0.0.1', port: 4198, strictPort: true, fs: { allow: [root, frontend] } }
})
await server.listen()
for (const signal of ['SIGINT', 'SIGTERM'])
  process.on(signal, async () => {
    await server.close()
    process.exit()
  })
