import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import ts from '../../core/core-frontend/node_modules/typescript/lib/typescript.js'
const root = new URL('../../core/core-frontend/src/views/chart/components/js/', import.meta.url)
const moduleUrl = source => `data:text/javascript;base64,${Buffer.from(ts.transpileModule(source, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 }
}).outputText).toString('base64')}`
// Use the real formatter; only replace environment hooks so the regression can run without Vue.
const formatter = readFileSync(new URL('formatter.ts', root), 'utf8')
  .replace("from 'lodash-es'", `from '${new URL('../../core/core-frontend/node_modules/lodash-es/lodash.js', import.meta.url)}'`)
  .replace("import { useI18n } from '@/hooks/web/useI18n'", "const useI18n = () => ({t: key => ({'chart.unit_ten_thousand':'万'}[key] || key)})")
  .replace("import { getLocale } from '@/utils/utils'", "const getLocale = () => 'zh-CN'")
const source = readFileSync(new URL('panel/charts/map/mapLegend.ts', root), 'utf8')
  .replace("'../../../formatter'", JSON.stringify(moduleUrl(formatter)))
  .replace("import { parseJson } from '@/views/chart/components/js/util'", 'const parseJson = value => typeof value === "string" ? JSON.parse(value) : value')
const { mapLegendLabel: label, mapLegendFormatter, mapTooltipValue } = await import(moduleUrl(source))
let checks = 0
const eq = (actual, expected) => { assert.equal(actual, expected); checks++ }
const cfg = {type:'value',unitLanguage:'ch',unit:10000,suffix:'元',decimalCount:2,thousandSeparator:true}
const chart = {type:'map',yAxis:[{formatterCfg:cfg}],customStyle:{legend:{}},customAttr:{misc:{mapAutoLegend:false,mapLegendRangeType:'custom'}}}
eq(label(chart, [12500.25,25600.75]), '12500-25601')
chart.customStyle.legend.map = {formatMode:'inherit'}
eq(label(chart, [12500.25,25600.75]), '1.25万元 - 2.56万元')
const snapshot = JSON.stringify(chart)
label(chart,[1,2]);eq(JSON.stringify(chart),snapshot)
const custom = chart.customStyle.legend.map = {formatMode:'custom',prefix:'¥',formatterCfg:{...cfg,decimalCount:1},rangeLabels:{'[0,20000]':'低风险'}}
eq(label(chart,[20000,50000]), '¥2.0万元 - ¥5.0万元')
eq(label(chart,[0,20000]),'低风险')
eq(label(chart,[0,30000]),'¥0.0万元 - ¥3.0万元')
chart.drill=true;eq(label(chart,[0,20000]),'¥0.0万元 - ¥2.0万元');chart.drill=false
chart.customAttr.misc.mapAutoLegend=true;eq(label(chart,[0,20000]),'¥0.0万元 - ¥2.0万元')
eq(label(chart,[-Infinity,20000]),'< ¥2.0万元')
eq(label(chart,[20000,Infinity]),'≥ ¥2.0万元')
eq(label(chart,[20000,20000]),'¥2.0万元')
for (const v of [NaN, Infinity, null, undefined, '', ' ', true, {}, 'invalid']) eq(mapLegendFormatter(chart)(v),'-')
eq(label(chart,[-25000,-10000]),'¥-2.5万元 - ¥-1.0万元')
eq(mapTooltipValue(chart,12500.25,cfg),'1.25万元')
custom.syncTooltip=true;eq(mapTooltipValue(chart,12500.25,cfg),'¥1.3万元')
chart.type='bubble-map';eq(mapTooltipValue(chart,12500.25,cfg),'1.25万元')
custom.formatterCfg={...cfg,unit:1,decimalCount:2};eq(label(chart,1234567.89),'¥1,234,567.89元')
custom.formatterCfg={...cfg,type:'percent',unit:1,decimalCount:1,suffix:''};eq(label(chart,0.125),'¥12.5%')
console.log(`PASS: ${checks} map legend assertions`)
const rangeSource = readFileSync(new URL('editor/editor-style/components/mapRange.ts', new URL('../../core/core-frontend/src/views/chart/components/', import.meta.url)), 'utf8')
const {validMapBoundaries, mapBoundaryMidpoint, insertMapBoundary, removeMapBoundary} = await import(moduleUrl(rangeSource))
const bounds = [0,20,50,100]
assert.deepEqual(insertMapBoundary(bounds,1),[0,20,35,50,100])
assert.deepEqual(bounds,[0,20,50,100])
assert.deepEqual(removeMapBoundary([0,20,35,50,100],1),[0,35,50,100])
eq(removeMapBoundary(bounds,0),undefined)
eq(removeMapBoundary(bounds,3),undefined)
eq(validMapBoundaries([0,0,1]),false)
eq(validMapBoundaries([0,undefined,1]),false)
eq(validMapBoundaries([-5,0,1.5]),true)
eq(validMapBoundaries([0,Infinity]),false)
eq(mapBoundaryMidpoint(0.1,0.2),0.15)
eq(mapBoundaryMidpoint(-20,-10),-15)
eq(mapBoundaryMidpoint(Number.MAX_VALUE / 2,Number.MAX_VALUE)>0,true)
eq(mapBoundaryMidpoint(1,1+Number.EPSILON),undefined)
eq(insertMapBoundary([0,1,2,3,4,5,6,7,8,9],0),undefined)
eq(insertMapBoundary(bounds,4),undefined)
eq(removeMapBoundary([0,100],1),undefined)
console.log('PASS: boundary insertion, deletion, ordering, limits and numeric precision checks')
