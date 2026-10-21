import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const here = path.dirname(fileURLToPath(import.meta.url))
const frontend = path.resolve(here, '../../../../core/core-frontend')
const require = createRequire(`${frontend}/package.json`)
const { createServer } = await import(require.resolve('vite'))
const vue = (await import(require.resolve('@vitejs/plugin-vue'))).default
const server = await createServer({
  configFile: false,
  root: here,
  plugins: [vue(), {
    name: 'fixture-svg',
    enforce: 'pre',
    load(id) {
      if (id.endsWith('.svg')) return 'export default { render() { return null } }'
    }
  }],
  resolve: {
    alias: [
      { find: '@/config/axios', replacement: `${here}/request.mjs` },
      { find: '@/hooks/web/useI18n', replacement: `${here}/i18n.mjs` },
      { find: '@', replacement: `${frontend}/src` },
      ...['vue', 'element-plus-secondary', 'vue-clipboard3', 'vue-types'].map(name => ({
        find: name, replacement: require.resolve(name)
      }))
    ],
    dedupe: ['vue']
  },
  server: { host: '127.0.0.1', port: 4194, strictPort: true, fs: { allow: [path.resolve(here, '../../../..')] } }
})
await server.listen()
console.log('Ticket component fixture: http://127.0.0.1:4194 (mock HTTP only)')
