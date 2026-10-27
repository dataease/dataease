// Run: node --test tests/dev-first-navigation/regression.mjs
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import { test } from 'node:test'
import { resolve } from 'node:path'
import { loadConfigFromFile } from 'vite'

const configPath = resolve('vite.config.ts')
const scenarios = [
  ['dashboard', 'first-navigation-deps.json'],
  ['datasource', 'datasource-navigation-deps.json'],
  ['startup reoptimization', 'reoptimization-deps.json']
]

for (const [name, file] of scenarios) {
  test(`dev optimizer covers dependencies observed on the first ${name} navigation`, async () => {
    const observed = JSON.parse(await readFile(new URL(file, import.meta.url)))
    const { config } = await loadConfigFromFile({ command: 'serve', mode: 'dev' }, configPath)
    const include = new Set(config.optimizeDeps.include)
    // Captured from cold-cache FULL_RELOAD reproductions; contains package IDs only.
    for (const id of observed) assert.ok(include.has(id), `missing first-navigation dependency: ${id}`)
    assert.ok(include.has('element-plus-secondary/es/components/tree-v2/style/css'))
    assert.ok(include.has('element-plus-secondary/es/components/select-v2/style/css'))
  })
}

for (const mode of ['base', 'distributed', 'lib']) {
  test(`${mode} build does not enable the dev-only dependency additions`, async () => {
    const { config } = await loadConfigFromFile({ command: 'build', mode }, configPath)
    assert.ok(!config.optimizeDeps?.include?.includes('tinymce/plugins/pagebreak'))
    assert.ok(!config.optimizeDeps?.include?.includes('vant/es/popup/style'))
    assert.ok(!config.optimizeDeps?.include?.includes('vue3-ace-editor'))
    assert.ok(!config.optimizeDeps?.include?.includes('vue-router_2'))
    assert.ok(!config.optimizeDeps?.include?.includes('@antv/g2'))
  })
}
