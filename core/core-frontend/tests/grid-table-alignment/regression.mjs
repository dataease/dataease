// Run: node --test tests/grid-table-alignment/regression.mjs
import assert from 'node:assert/strict'
import fs from 'node:fs'
import vm from 'node:vm'
import { test } from 'node:test'
import ts from 'typescript'
import { parse, compileScript, compileTemplate } from '@vue/compiler-sfc'
import { files, formatterSource } from './formatters.mjs'

const transpile = source =>
  ts.transpileModule(source, {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2020 }
  }).outputText
const utils = {}
vm.runInNewContext(
  transpile(fs.readFileSync(new URL('../../src/utils/DateUtil.ts', import.meta.url), 'utf8')),
  { exports: utils }
)
const exports = {}
vm.runInNewContext(transpile(formatterSource), { exports, formatDate: utils.formatDate, Date })

files.forEach((file, index) =>
  test(`${file}: formats without Date prototype extension`, () => {
    assert.equal(Date.prototype.format, undefined)
    const format = exports.formatters[index].format
    assert.equal(format(new Date(2026, 9, 20, 12, 34, 56).getTime()), '2026-10-20 12:34:56')
    for (const empty of [null, undefined, '']) assert.equal(format(empty), '-')
    const { descriptor, errors } = parse(
      fs.readFileSync(new URL('../../' + file, import.meta.url), 'utf8')
    )
    assert.deepEqual(errors, [])
    const script = compileScript(descriptor, { id: `date-${index}` })
    assert.equal(script.bindings.formatDate, 'setup-maybe-ref')
    const template = compileTemplate({
      source: descriptor.template.content,
      filename: file,
      id: `date-${index}`,
      compilerOptions: { bindingMetadata: script.bindings }
    })
    assert.deepEqual(template.errors, [])
    assert.equal(Date.prototype.format, undefined)
  })
)
