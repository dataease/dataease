/* eslint-disable @typescript-eslint/no-var-requires -- Node 回归入口。 */
const fs = require('node:fs')
const vm = require('node:vm')
const ts = require('typescript')
const load = (file, globals = {}) => {
  const exports = {}
  vm.runInNewContext(
    ts.transpileModule(fs.readFileSync(file, 'utf8'), {
      compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2020 }
    }).outputText,
    {
      exports,
      require: name => require(name === 'lodash-es' ? 'lodash' : name),
      AbortController,
      setTimeout,
      clearTimeout,
      console,
      ...globals
    }
  )
  return exports
}
module.exports = {
  loadCandidateLoader: globals =>
    load('src/custom-component/v-query/candidate-loader.ts', globals).createCandidateLoader,
  createQueryScope: () => load('src/config/axios/queryScope.ts').createQueryScope()
}
