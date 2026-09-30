import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import ts from '../../core/core-frontend/node_modules/typescript/lib/typescript.js'

const source = readFileSync(new URL('../../core/core-frontend/src/views/chart/components/js/panel/common/tableImage.ts', import.meta.url), 'utf8')
const compiled = ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.ESNext } }).outputText
globalThis.window = { location: { origin: 'https://dataease.example' } }
const { tableImageUrl } = await import(`data:text/javascript;base64,${Buffer.from(compiled).toString('base64')}`)
for (const value of [null, undefined, '', {}, 'javascript:alert(1)', 'java\nscript:alert(1)', 'file:///tmp/a.png', 'data:text/html;base64,PHNjcmlwdD4=', 'data:image/svg+xml;base64,PHN2Zz4=', '//other.example/a.png', 'https://user:secret@example.com/a.png']) {
  assert.equal(tableImageUrl(value), '', String(value))
}
assert.equal(tableImageUrl(' https://example.com/a.png?q=1 '), 'https://example.com/a.png?q=1')
assert.equal(tableImageUrl('http://example.com/a.jpg'), 'http://example.com/a.jpg')
assert.equal(tableImageUrl('/api/static/a.png'), 'https://dataease.example/api/static/a.png')
assert.equal(tableImageUrl('data:image/png;base64,YQ=='), 'data:image/png;base64,YQ==')
console.log('PASS: table image URL allowlist (15 cases)')
