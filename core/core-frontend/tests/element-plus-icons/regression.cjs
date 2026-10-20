// Run: node --test tests/element-plus-icons/regression.cjs
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const { test } = require('node:test')
const ts = require('typescript')
const { parse, compileScript, compileTemplate } = require('@vue/compiler-sfc')
const { baseParse } = require('@vue/compiler-dom')
const { isHTMLTag, isSVGTag } = require('@vue/shared')
const root = path.resolve(__dirname, '../..')
const iconComponents = require('@element-plus/icons-vue')
const icons = new Set(Object.keys(iconComponents))
const { h } = require('vue')
const { renderToString } = require('@vue/server-renderer')
const files = []
const collect = directory => {
  for (const item of fs.readdirSync(directory, { withFileTypes: true })) {
    const file = path.join(directory, item.name)
    if (item.isDirectory()) collect(file)
    else if (/\.(vue|tsx?)$/.test(file)) files.push(file)
  }
}
collect(path.join(root, 'src'))
const read = file => fs.readFileSync(file, 'utf8')

test('named icon imports render SVG without global registration', async () => {
  const used = new Set()
  for (const file of files) {
    let source = read(file)
    if (file.endsWith('.vue')) {
      const { descriptor } = parse(source)
      source = [descriptor.script?.content, descriptor.scriptSetup?.content]
        .filter(Boolean)
        .join('\n')
    }
    const ast = ts.createSourceFile(
      'source.tsx',
      source,
      ts.ScriptTarget.Latest,
      true,
      ts.ScriptKind.TSX
    )
    for (const node of ast.statements) {
      if (ts.isImportDeclaration(node) && node.moduleSpecifier.text === '@element-plus/icons-vue') {
        assert.ok(
          node.importClause?.namedBindings && ts.isNamedImports(node.importClause.namedBindings),
          file
        )
        for (const specifier of node.importClause.namedBindings.elements) {
          used.add((specifier.propertyName || specifier.name).text)
        }
      }
    }
  }
  for (const name of used) {
    assert.ok(iconComponents[name], name)
    assert.match(await renderToString(h(iconComponents[name])), /<svg[\s>]/, name)
  }
  for (const entry of ['index', 'panel', 'mobile', 'lib']) {
    assert.doesNotMatch(
      read(path.join(root, `src/pages/${entry}/main.ts`)),
      /setupElementPlusIcons/
    )
  }
})

test('templates resolve icons locally, including icon props, and icon consumers compile', () => {
  const failures = []
  const warning = console.warn
  console.warn = () => {} // Existing macro import warnings are unrelated to icon resolution.
  try {
    for (const file of files.filter(file => file.endsWith('.vue'))) {
      const source = read(file)
      const { descriptor } = parse(source)
      if (!descriptor.template) continue
      const references = new Set()
      const ast = baseParse(descriptor.template.content)
      const walk = node => {
        if (node.type === 1) {
          const name = node.tag.replace(/(^|-)([a-z])/g, (_, dash, letter) => letter.toUpperCase())
          if (!isHTMLTag(node.tag) && !isSVGTag(node.tag) && icons.has(name)) references.add(name)
          for (const prop of node.props) {
            if (prop.type === 6 && /(^|-)icon$/.test(prop.name) && icons.has(prop.value?.content)) {
              failures.push(`${file}: string icon ${prop.value.content}`)
            }
            if (
              prop.type === 7 &&
              prop.arg &&
              /(^|-)icon$|^is$/.test(prop.arg.content) &&
              prop.exp
            ) {
              const expression = ts.createSourceFile(
                'expression.ts',
                prop.exp.content,
                ts.ScriptTarget.Latest,
                true
              )
              const visit = child => {
                if (
                  ts.isIdentifier(child) &&
                  icons.has(child.text) &&
                  !(ts.isPropertyAccessExpression(child.parent) && child.parent.name === child)
                )
                  references.add(child.text)
                ts.forEachChild(child, visit)
              }
              visit(expression)
            }
          }
        }
        for (const child of node.children || []) walk(child)
      }
      walk(ast)
      if (!references.size && !source.includes('@element-plus/icons-vue')) continue
      const script = compileScript(descriptor, { id: file })
      for (const name of references)
        if (!script.bindings[name]) failures.push(`${file}: missing local binding for ${name}`)
      const template = compileTemplate({
        source: descriptor.template.content,
        filename: file,
        id: file,
        compilerOptions: { bindingMetadata: script.bindings }
      })
      assert.deepEqual(template.errors, [], file)
    }
  } finally {
    console.warn = warning
  }
  assert.deepEqual(failures, [])
})
