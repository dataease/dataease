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
  configFile: false, root, cacheDir: path.resolve(root, '../../output/threshold-tags-vite'),
  plugins: [vue(), {
    name: 'threshold-tags-boundaries',
    enforce: 'pre',
    resolveId(id, importer) {
      if (importer?.endsWith('/ThresholdWarnForm.vue') && ['../../../menu/system/report/api', '../api'].includes(id)) return bridge
    },
    load(id) { if (id.endsWith('.svg')) return 'export default { render() { return null } }' }
  }],
  optimizeDeps: { noDiscovery: true, include: ['vue', 'element-plus-secondary', '@element-plus/icons-vue'] },
  resolve: { alias: [
    ...['@/api/auth', '@/hooks/web/useI18n', '@/utils/utils', '@/components/icon-custom', '@/components/rich-text/TinymacEditorAlarm.vue'].map(find => ({ find, replacement: bridge })),
    { find: 'threshold-form', replacement: path.join(frontend, 'src/views/component/threshold-warning/form/ThresholdWarnForm.vue') },
    { find: '@', replacement: path.join(frontend, 'src') },
    { find: 'vue', replacement: path.join(frontend, 'node_modules/vue/dist/vue.esm-bundler.js') },
    { find: '@element-plus/icons-vue', replacement: path.join(frontend, 'node_modules/@element-plus/icons-vue') },
    { find: 'element-plus-secondary', replacement: path.join(frontend, 'node_modules/element-plus-secondary') }
  ] },
  server: { host: '127.0.0.1', port: 4194, strictPort: true, fs: { allow: [root, frontend] } }
})
await server.listen()
for (const signal of ['SIGINT', 'SIGTERM']) process.on(signal, async () => { await server.close(); process.exit() })
