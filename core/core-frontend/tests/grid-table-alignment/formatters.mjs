import fs from 'node:fs'
import { fileURLToPath } from 'node:url'
import { parse } from '@vue/compiler-sfc'
import ts from 'typescript'

export const files = [
  'src/views/pages/menu/system/sysuser/index.vue',
  'src/views/pages/menu/system/log/index.vue',
  'src/views/pages/menu/setting/plugin/index.vue',
  'src/views/menu/system/sync/ds/SourceTable.vue',
  'src/views/menu/system/sync/ds/TargetTable.vue',
  'src/views/menu/user-center/ApiKey.vue'
]
export const formatterSource =
  files
    .map((file, index) => {
      const filename = fileURLToPath(new URL('../../' + file, import.meta.url))
      const { descriptor } = parse(fs.readFileSync(filename, 'utf8'), { filename })
      const ast = ts.createSourceFile(
        filename + '.ts',
        descriptor.scriptSetup.content,
        ts.ScriptTarget.Latest
      )
      const declaration = ast.statements
        .filter(ts.isVariableStatement)
        .flatMap(node => [...node.declarationList.declarations])
        .find(node => node.name.getText(ast) === 'timestampFormatDate')
      if (!declaration) throw new Error(`Missing timestampFormatDate: ${file}`)
      return `const format${index} = ${declaration.initializer.getText(ast)}`
    })
    .join('\n') +
  '\nexport const formatters = [' +
  files
    .map(
      (file, index) =>
        `{ name: ${JSON.stringify(file)}, format: ${
          index === 5 ? '(value) => format5(null, null, value)' : 'format' + index
        } }`
    )
    .join(',') +
  ']'
