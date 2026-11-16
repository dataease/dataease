import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const root = fileURLToPath(new URL('.', import.meta.url))
const frontend = path.resolve(root, '../../../../core/core-frontend')
const require = createRequire(path.join(frontend, 'package.json'))
const { createServer } = await import(require.resolve('vite'))
const server = await createServer({
  configFile: false,
  root,
  plugins: [
    {
      name: 'fixture-univer-dependencies',
      resolveId(source, importer) {
        if (source.startsWith('@univerjs/') && importer?.startsWith(root)) {
          return this.resolve(source, path.join(frontend, 'package.json'), { skipSelf: true })
        }
      }
    }
  ],
  resolve: {
    alias: [
      {
        find: 'preview-controller',
        replacement: `${frontend}/src/views/menu/spreadsheet/plugins/DataEasePreviewModePlugin/controllers/preview-sheet-tab-context-menu.controller.ts`
      },
      {
        find: 'spreadsheet-mode',
        replacement: `${frontend}/src/views/menu/spreadsheet/services/spreadsheet-mode.service.ts`
      }
    ]
  },
  esbuild: { tsconfigRaw: { compilerOptions: { experimentalDecorators: true } } },
  optimizeDeps: {
    esbuildOptions: { tsconfigRaw: { compilerOptions: { experimentalDecorators: true } } }
  },
  server: { host: '127.0.0.1', port: 4192, strictPort: true, fs: { allow: [root, frontend] } }
})
await server.listen()
for (const signal of ['SIGINT', 'SIGTERM']) {
  process.on(signal, async () => {
    await server.close()
    process.exit()
  })
}
