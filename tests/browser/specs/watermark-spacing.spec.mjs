import { readFileSync } from 'node:fs'
import { test, expect } from '@playwright/test'
import ts from '../../../core/core-frontend/node_modules/typescript/lib/typescript.js'

const source = readFileSync(
  new URL('../../../core/core-frontend/src/components/watermark/watermark.ts', import.meta.url),
  'utf8'
)
const ast = ts.createSourceFile('watermark.ts', source, ts.ScriptTarget.Latest, true)
// Execute the production DOM renderer without initializing stores or calling the user API.
const renderer = ast.statements.find(
  node => ts.isFunctionDeclaration(node) && node.name?.text === 'watermark'
)
const compiled = ts.transpileModule(renderer.getText(ast), {
  compilerOptions: {
    module: ts.ModuleKind.CommonJS,
    target: ts.ScriptTarget.ES2022
  }
}).outputText
const script = `(() => { const exports = {}; ${compiled}; window.renderWatermark = exports.watermark })()`

const cases = [
  { name: 'single computed column', width: 350, height: 900 },
  { name: 'single computed row', width: 1440, height: 276 },
  { name: 'single row and column', width: 350, height: 276 },
  { name: 'container smaller than one watermark', width: 100, height: 100 },
  {
    name: 'explicit single row and column',
    width: 350,
    height: 276,
    settings: { watermark_rows: 1, watermark_cols: 1 }
  },
  { name: 'normal canvas', width: 1440, height: 900 },
  { name: 'long dashboard', width: 1440, height: 12000 }
]

for (const scenario of cases) {
  test(scenario.name, async ({ page }) => {
    await page.setContent(
      `<div id="watermark" style="position:relative;width:${scenario.width}px;height:${scenario.height}px"></div>`
    )
    await page.addScriptTag({ content: script })
    await page.evaluate(settings => {
      window.renderWatermark({ watermark_txt: 'Watermark regression', ...settings }, 'watermark')
    }, scenario.settings || {})
    const positions = await page.locator('.mask_div').evaluateAll(elements =>
      elements.map(element => ({
        left: element.style.left,
        top: element.style.top
      }))
    )
    expect(positions.length).toBeGreaterThanOrEqual(4)
    expect(positions[0]).toEqual({ left: '20px', top: '20px' })
    for (const position of positions) {
      expect(Number.isFinite(Number.parseFloat(position.left))).toBe(true)
      expect(Number.isFinite(Number.parseFloat(position.top))).toBe(true)
    }
    if (scenario.name === 'normal canvas') {
      expect(positions).toHaveLength(24)
      expect(positions.at(-1)).toEqual({ left: '1208px', top: '760px' })
    }
    if (scenario.name === 'long dashboard') {
      expect(new Set(positions.map(position => position.top)).size).toBeGreaterThan(60)
      expect(Number.parseFloat(positions.at(-1).top)).toBeGreaterThan(11800)
    }
  })
}
