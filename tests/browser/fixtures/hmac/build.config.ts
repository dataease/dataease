import path from 'node:path'
import { hmacObfuscation } from '../../../../core/core-frontend/config/hmac-obfuscation'

const frontend = path.resolve(__dirname, '../../../../core/core-frontend')
export default {
  plugins: [hmacObfuscation()],
  resolve: { alias: { '@': path.join(frontend, 'src') } },
  build: {
    outDir: process.env.HMAC_FIXTURE_BUILD_DIR || '/private/tmp/dataease-hmac-browser-build',
    sourcemap: false,
    lib: {
      entry: path.join(frontend, 'src/views/tools/HmacTool.ts'),
      formats: ['es'],
      fileName: () => 'hmac.js'
    },
    minify: 'esbuild'
  }
}
