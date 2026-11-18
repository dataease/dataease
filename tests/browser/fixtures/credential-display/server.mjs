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
  cacheDir: path.join(root, 'node_modules/.vite'),
  plugins: [
    vue(),
    {
      name: 'fixture-svg',
      load(id) {
        if (id.endsWith('.svg'))
          return 'export default { render() { return null } }'
      }
    }
  ],
  resolve: {
    alias: [
      ...['@/hooks/web/useI18n', '@/api/sensitive'].map((find) => ({
        find,
        replacement: path.join(root, 'bridge.js')
      })),
      { find: '@', replacement: path.join(frontend, 'src') },
      {
        find: 'vue',
        replacement: path.join(
          frontend,
          'node_modules/vue/dist/vue.esm-bundler.js'
        )
      },
      {
        find: 'element-plus-secondary',
        replacement: path.join(frontend, 'node_modules/element-plus-secondary')
      }
    ]
  },
  server: {
    host: '127.0.0.1',
    port: 4207,
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
