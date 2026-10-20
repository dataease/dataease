// Run: node --test tests/distributed-preload/regression.mjs
import assert from 'node:assert/strict'
import { mkdtemp, mkdir, writeFile, rm, realpath } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { test } from 'node:test'
import { build, loadConfigFromFile } from 'vite'

const frontend = fileURLToPath(new URL('../..', import.meta.url))
const loaded = await loadConfigFromFile(
  { command: 'build', mode: 'distributed' },
  path.join(frontend, 'config/distributed.ts')
)
assert.ok(loaded, 'Production distributed config loads')

const fixtureBuild = async (base, disablePreload = false) => {
  const root = await realpath(await mkdtemp(path.join(tmpdir(), 'dataease-preload-')))
  try {
    await mkdir(path.join(root, 'src'))
    await mkdir(path.join(root, 'panel'))
    const files = {
      'index.html': '<script type="module">import "/src/main.js"; window.page = "index"</script>',
      'panel/index.html':
        '<script type="module">import "/src/main.js"; window.page = "panel"</script>',
      'src/main.js': `import { core } from './core.js'
window.core = core
window.loadRoute = () => import('./route.js')
window.loadExtension = () => import('extensions/widget.js')`,
      'src/core.js': 'export const core = "core-value"',
      'src/route.js': `import { vendor } from './vendor.js'
import './route.css'
export const route = vendor`,
      'src/vendor.js': 'export const vendor = "lazy-vendor-value"',
      'src/route.css': '.lazy-route { color: red }'
    }
    await Promise.all(
      Object.entries(files).map(([name, content]) => writeFile(path.join(root, name), content))
    )
    const result = await build({
      configFile: false,
      root,
      base,
      logLevel: 'silent',
      build: {
        ...loaded.config.build,
        ...(disablePreload ? { modulePreload: { resolveDependencies: () => [] } } : {}),
        write: false,
        minify: false,
        rollupOptions: {
          ...loaded.config.build.rollupOptions,
          input: {
            index: path.join(root, 'index.html'),
            panel: path.join(root, 'panel/index.html')
          },
          output: {
            ...loaded.config.build.rollupOptions.output,
            manualChunks: {
              core: [path.join(root, 'src/core.js')],
              lazyVendor: [path.join(root, 'src/vendor.js')]
            }
          }
        }
      }
    })
    return result.output
  } finally {
    await rm(root, { recursive: true, force: true })
  }
}

for (const base of ['./', '/dataease/']) {
  test(`distributed preload artifacts with base=${base}`, async () => {
    const output = await fixtureBuild(base)
    const core = output.find(file => file.type === 'chunk' && file.name === 'core')
    const vendor = output.find(file => file.type === 'chunk' && file.name === 'lazyVendor')
    const route = output.find(file => file.type === 'chunk' && file.name === 'route')
    const css = output.find(file => file.type === 'asset' && file.fileName.endsWith('.css'))
    const entry = output.find(
      file => file.type === 'chunk' && file.dynamicImports.includes(route.fileName)
    )
    assert.ok(core && vendor && route && css && entry, 'Fixture emits all dependency types')
    for (const name of ['index.html', 'panel/index.html']) {
      const html = output.find(file => file.fileName === name).source.toString()
      assert.match(html, /rel="modulepreload"/)
      const prefix = base === './' ? (name.startsWith('panel/') ? '../' : './') : base
      assert.ok(html.includes(`${prefix}${core.fileName}`), `${name}: core preload path resolves`)
      assert.ok(!html.includes(vendor.fileName), 'Lazy vendor stays out of initial HTML')
      assert.ok(!html.includes('extensions/'), 'External extension is not preloaded by HTML')
    }
    assert.ok(entry.code.includes(path.basename(vendor.fileName)), 'Lazy vendor is in preload map')
    assert.ok(entry.code.includes(path.basename(css.fileName)), 'Lazy CSS is in preload map')
    assert.match(entry.code, /import\(['"]extensions\/widget\.js['"]\)/)
    assert.ok(route.imports.includes(vendor.fileName), 'Route retains its actual vendor import')
    if (base === './') assert.match(entry.code, /import\.meta\.url/)
  })
}

test('old empty dependency override removes script preloads while retaining dynamic CSS', async () => {
  const output = await fixtureBuild('./', true)
  const html = output.find(file => file.fileName === 'index.html').source.toString()
  const route = output.find(file => file.type === 'chunk' && file.name === 'route')
  const vendor = output.find(file => file.type === 'chunk' && file.name === 'lazyVendor')
  const entry = output.find(
    file => file.type === 'chunk' && file.dynamicImports.includes(route.fileName)
  )
  const css = output.find(file => file.type === 'asset' && file.fileName.endsWith('.css'))
  assert.doesNotMatch(html, /rel="modulepreload"/)
  assert.ok(!entry.code.includes(path.basename(vendor.fileName)))
  assert.ok(entry.code.includes(path.basename(css.fileName)))
})
