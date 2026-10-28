import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import path from 'node:path'
const fixture = fileURLToPath(new URL('.', import.meta.url))
const frontend = path.resolve(fixture, '../../../../core/core-frontend')
const require = createRequire(path.join(frontend, 'package.json'))
const { createServer } = await import(require.resolve('vite'))
const { default: vue } = await import(require.resolve('@vitejs/plugin-vue'))
const server = await createServer({
  configFile: false,
  root: frontend,
  cacheDir: path.resolve(fixture, '../../output/report-resend/vite'),
  plugins: [
    vue(),
    {
      name: 'resend-fixture',
      enforce: 'pre',
      resolveId(id, importer) {
        if (id === './api' && importer?.endsWith('/ReportDeliveryDetails.vue'))
          return '\0delivery-api'
      },
      load(id) {
        if (id === '\0delivery-api')
          return `export const reportDeliveryApi = async () => ({data: await (await fetch('/fixture-delivery')).json()}); export const reportResendApi = () => { throw new Error('Real sending is forbidden'); };`
        if (id.endsWith('.svg')) return 'export default {render() {return null}}'
      },
      configureServer(server) {
        server.middlewares.use('/__resend', (_req, res) => {
          res.setHeader('Content-Type', 'text/html')
          res.end(
            `<div id="app"></div><script type="module" src="/@fs/${fixture}main.js"></script>`
          )
        })
      }
    }
  ],
  resolve: {
    alias: [
      { find: /^@\/plugins\/vue-i18n$/, replacement: path.join(fixture, 'i18n.js') },
      { find: '@', replacement: path.join(frontend, 'src') }
    ],
    dedupe: ['vue', 'vue-i18n', 'element-plus-secondary']
  },
  optimizeDeps: {
    noDiscovery: true,
    include: ['vue', 'vue-i18n', 'element-plus-secondary', 'dayjs', 'mitt']
  },
  server: { host: '127.0.0.1', port: 4196, strictPort: true, fs: { allow: [frontend, fixture] } }
})
await server.listen()
for (const signal of ['SIGINT', 'SIGTERM'])
  process.on(signal, async () => {
    await server.close()
    process.exit()
  })
