import { randomInt } from 'node:crypto'
import ts from 'typescript'
import { transform } from 'esbuild'
import type { Plugin } from 'vite'

// A build-only deterrent to static string searches, not a secrecy boundary.
// Keep module specifiers, properties and the public wire protocol unchanged.
export const hmacObfuscation = (): Plugin => ({
  name: 'hmac-obfuscation',
  apply: 'build',
  enforce: 'post',
  async transform(source, id) {
    if (
      !/\/src\/(views\/tools\/(?:HmacTool|hmacCrypto)|config\/axios\/hmacTransport)\.ts$/.test(id)
    )
      return
    const mask = randomInt(1, 256)
    const table: number[][] = []
    const file = ts.createSourceFile(id, source, ts.ScriptTarget.Latest, true, ts.ScriptKind.JS)
    const strings = new Set([
      'RSA-OAEP',
      'AES-GCM',
      'HMAC',
      'SHA-256',
      'spki',
      'raw',
      'unwrapKey',
      'sign',
      'dataease-key'
    ])
    const result = ts.transform(file, [
      context => node => {
        const visit: ts.Visitor = child => {
          if (ts.isStringLiteral(child) && strings.has(child.text)) {
            const index = table.push(Array.from(child.text, char => char.charCodeAt(0) ^ mask)) - 1
            return ts.factory.createCallExpression(
              ts.factory.createIdentifier('__deHmacString'),
              undefined,
              [ts.factory.createNumericLiteral(index)]
            )
          }
          return ts.visitEachChild(child, visit, context)
        }
        return ts.visitNode(node, visit) as ts.SourceFile
      }
    ])
    try {
      const code = ts.createPrinter().printFile(result.transformed[0])
      const decoder = `const __deHmacTable=${JSON.stringify(
        table
      )};const __deHmacString=i=>String.fromCharCode(...__deHmacTable[i].map(v=>v^${mask}));`
      return {
        code: (
          await transform(decoder + code, {
            loader: 'js',
            minify: true,
            sourcemap: false,
            target: 'es2020'
          })
        ).code,
        map: null
      }
    } finally {
      result.dispose()
    }
  }
})
