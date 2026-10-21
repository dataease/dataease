import assert from 'node:assert/strict'
import { build } from 'esbuild'
import vm from 'node:vm'

// Exercise the production carousel and formatter, without starting a WebGL scene.
const bundle = await build({
  stdin: {
    contents: `export { CarouselManager } from './src/views/chart/components/js/panel/charts/map/tooltip-carousel'; export { mapTooltipValue } from './src/views/chart/components/js/panel/charts/map/mapLegend'`,
    resolveDir: process.cwd()
  },
  bundle: true,
  write: false,
  format: 'cjs',
  platform: 'node',
  plugins: [
    {
      name: 'browser-boundaries',
      setup(build) {
        build.onResolve(
          {
            filter:
              /^(@antv\/|@\/hooks\/web\/useI18n$|@\/utils\/utils$|@\/views\/chart\/components\/js\/util$)/
          },
          args => ({ path: args.path, namespace: 'stub' })
        )
        build.onLoad({ filter: /.*/, namespace: 'stub' }, ({ path }) => ({
          contents: path.includes('useI18n')
            ? `export const useI18n=()=>({t:k=>({'chart.unit_ten_thousand':'万'}[k]||k)})`
            : path.endsWith('/js/util')
            ? `export const parseJson=v=>typeof v==='string'?JSON.parse(v):v`
            : path.endsWith('/utils/utils')
            ? `export const getLocale=()=> 'zh-CN'; export const deepCopy=v=>structuredClone(v); export const sanitizeTooltipHtml=v=>v`
            : `export class Popup{}; export class Plot{}; export class Scene{}`
        }))
      }
    }
  ]
})
const context = { module: { exports: {} }, exports: {} }
vm.runInNewContext(bundle.outputFiles[0].text, context)
const { CarouselManager, mapTooltipValue } = context.module.exports
const original = {
  type: 'value',
  unitLanguage: 'ch',
  unit: 1,
  suffix: '元',
  decimalCount: 2,
  thousandSeparator: false
}
const chart = {
  type: 'map',
  yAxis: [{ formatterCfg: original }],
  customStyle: {
    legend: {
      map: {
        formatMode: 'custom',
        syncTooltip: true,
        prefix: '¥',
        formatterCfg: { ...original, unit: 10000, decimalCount: 1 }
      }
    }
  },
  customAttr: {
    tooltip: {
      seriesTooltipFormatter: [
        { id: 'main', show: true, name: '主指标', formatterCfg: original },
        { id: 'extra', show: true, name: '其他指标', formatterCfg: original }
      ]
    }
  }
}
const manager = Object.create(CarouselManager.prototype)
manager.chart = chart
const data = {
  value: 15000,
  quotaList: [{ id: 'main' }],
  dynamicTooltipValue: [{ fieldId: 'extra', value: 123 }]
}
const items = () => manager.getTooltipItems(data)
assert.equal(items()[0].value, '¥1.5万元')
assert.equal(items()[0].value, mapTooltipValue(chart, 15000, original))
assert.equal(items()[1].value, '123.00元')
chart.customStyle.legend.map.syncTooltip = false
assert.equal(items()[0].value, '15000.00元')
chart.customStyle.legend.map.syncTooltip = true
chart.customStyle.legend.map.formatMode = 'inherit'
assert.equal(items()[0].value, '15000.00元')
chart.customStyle.legend.map.formatMode = 'custom'
chart.customStyle.legend.map.formatterCfg = { ...original, type: 'percent', decimalCount: 1 }
assert.equal(items()[0].value, mapTooltipValue(chart, 15000, original))
chart.customStyle.legend.map.formatMode = 'legacy'
assert.equal(items()[0].value, '15000')
chart.type = 'symbolic-map'
assert.equal(items()[0].value, '15000.00元')
chart.customAttr.tooltip.seriesTooltipFormatter[0].show = false
assert.equal(items().length, 1)
chart.customAttr.tooltip.seriesTooltipFormatter[1].show = false
assert.equal(items().length, 0)
console.log(
  'PASS: carousel sync on/off, hover parity, custom units/prefix, inherit, percent, legacy, other fields, non-region map, hidden fields'
)
