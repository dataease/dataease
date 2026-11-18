const fs = require('node:fs')
const path = require('node:path')
const Module = require('node:module')
const [frontend, challengeFile, outputFile] = process.argv.slice(2)
const ts = require(path.join(frontend, 'node_modules/typescript'))
const source = path.join(frontend, 'src/config/axios/configurationTransport.ts')
const moduleUnderTest = new Module(source)
moduleUnderTest.filename = source
moduleUnderTest.paths = Module._nodeModulePaths(path.dirname(source))
moduleUnderTest._compile(
  ts.transpileModule(fs.readFileSync(source, 'utf8'), {
    compilerOptions: {
      module: ts.ModuleKind.CommonJS,
      target: ts.ScriptTarget.ES2020,
      esModuleInterop: true
    }
  }).outputText,
  source
)
// Exercise the HTTP-compatible branch: secure randomness, without crypto.subtle.
Object.defineProperty(globalThis, 'crypto', {
  value: {
    getRandomValues: (array) =>
      require('node:crypto').webcrypto.getRandomValues(array)
  }
})
const input = JSON.parse(fs.readFileSync(challengeFile, 'utf8'))
moduleUnderTest.exports
  .sealConfiguration(input.data, input.path, input.challenge)
  .then((envelope) => fs.writeFileSync(outputFile, JSON.stringify(envelope)))
  .catch((error) => {
    console.error(error)
    process.exitCode = 1
  })
