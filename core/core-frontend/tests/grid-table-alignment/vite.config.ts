import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath } from 'node:url'
import { formatterSource } from './formatters.mjs'

const frontend = fileURLToPath(new URL('../..', import.meta.url))
const stub = fileURLToPath(new URL('./stubs.ts', import.meta.url))
export default defineConfig({
  root: fileURLToPath(new URL('.', import.meta.url)),
  plugins: [
    vue(),
    {
      name: 'production-date-formatters',
      resolveId: id => (id === 'virtual:date-formatters' ? '\0date-formatters' : undefined),
      load: id =>
        id === '\0date-formatters'
          ? `import { formatDate } from '@/utils/DateUtil';\n${formatterSource}`
          : undefined
    }
  ],
  resolve: {
    alias: [
      { find: '@/hooks/web/useI18n', replacement: stub },
      { find: '@/components/empty-background/src/EmptyBackground.vue', replacement: stub },
      { find: '@', replacement: frontend + '/src' }
    ]
  },
  server: { host: '127.0.0.1', port: 7091, strictPort: true, fs: { allow: [frontend] } }
})
