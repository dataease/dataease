import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath } from 'node:url'
import { tmpdir } from 'node:os'
import path from 'node:path'
const root = fileURLToPath(new URL('.', import.meta.url))
const frontend = fileURLToPath(new URL('../..', import.meta.url))
const bridge = root + 'bridge.ts'
export default defineConfig({
  root,
  cacheDir: path.join(tmpdir(), 'dataease-mfa-unbind-vite-cache'),
  plugins: [vue()],
  resolve: {
    dedupe: ['vue'],
    alias: [
      ...['@/hooks/web/useI18n', '@/store/modules/user', '@/config/axios', '@/api/user'].map(
        find => ({ find, replacement: bridge })
      ),
      { find: '../../component/login/MfaStep.vue', replacement: root + 'MfaStepStub.vue' },
      { find: '@', replacement: frontend + 'src' }
    ]
  },
  server: { host: '127.0.0.1', port: 7098, strictPort: true, fs: { allow: [root, frontend] } }
})
