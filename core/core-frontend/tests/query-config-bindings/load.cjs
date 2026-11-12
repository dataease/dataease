/* eslint-disable @typescript-eslint/no-var-requires -- Node 回归夹具加载真实 TypeScript 纯函数。 */
const fs = require('node:fs')
const path = require('node:path')
const ts = require('typescript')

const source = fs.readFileSync(
  path.resolve(__dirname, '../../src/custom-component/v-query/config-bindings.ts'),
  'utf8'
)
const code = ts.transpileModule(source, {
  compilerOptions: { target: ts.ScriptTarget.ES2020, module: ts.ModuleKind.CommonJS }
}).outputText
new Function('exports', code)(module.exports)
