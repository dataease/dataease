import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import path from 'node:path'
import { execFile } from 'node:child_process'
import { promisify } from 'node:util'
const run = promisify(execFile)
const root = fileURLToPath(new URL('.', import.meta.url))
const frontend = path.resolve(root, '../../../../core/core-frontend')
const built = process.env.HMAC_FIXTURE_BUILD_DIR
const require = createRequire(path.join(frontend, 'package.json'))
const { createServer } = await import(require.resolve('vite'))
const server = await createServer({
  configFile: false, root,
  define: { 'import.meta.env.VITE_API_BASEPATH': JSON.stringify('/api') },
  resolve: { alias: [
    ...['@/utils/loading', '@/store/modules/permission', '@/store/modules/embedded', '@/store/modules/link',
      '@/config/axios/refresh', '@/utils/utils', '@/hooks/web/useI18n', '@/store/modules/request',
      '@/utils/cacheUtil', '@/router', '@/hooks/web/useCache', 'element-plus-secondary']
      .map(find => ({ find, replacement: path.join(root, 'error-bridge.mjs') })),
    { find: './refresh', replacement: path.join(root, 'error-bridge.mjs') },
    { find: '/hmac-module', replacement: built ? path.join(built, 'hmac.js') : path.join(frontend, 'src/views/tools/HmacTool.ts') },
    { find: '@', replacement: path.join(frontend, 'src') },
    { find: 'axios', replacement: path.join(frontend, 'node_modules/axios/index.js') }
  ] },
  plugins: [{ name: 'java-envelope-fixture', configureServer(server) {
    server.middlewares.use('/api/sysParameter/requestTimeOut', (_req, res) => {
      res.setHeader('Content-Type', 'application/json')
      res.end(JSON.stringify({ code: 0, data: 60000 }))
    })
    server.middlewares.use('/fixture-envelope', async (req, res) => {
      try {
        let body = ''
        for await (const chunk of req) body += chunk
        const request = JSON.parse(body)
        const { stdout } = await run('java', ['-cp', process.env.HMAC_EXCHANGE_FIXTURE_CP, 'EnvelopeFixture', request.publicKey, request.requestId])
        res.setHeader('Content-Type', 'application/json')
        res.end(stdout)
      } catch { res.statusCode = 500; res.end('fixture failed') }
    })
  } }],
  server: { host: '127.0.0.1', port: 4197, strictPort: true, allowedHosts: ['hmac-http.test'], fs: { allow: [root, frontend, ...(built ? [built] : [])] } }
})
await server.listen()
for (const signal of ['SIGINT', 'SIGTERM']) process.on(signal, async () => { await server.close(); process.exit() })
