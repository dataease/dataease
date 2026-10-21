import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import ts from '../../core/core-frontend/node_modules/typescript/lib/typescript.js'
const base = new URL('../../core/core-frontend/src/views/chart/components/js/', import.meta.url)
const mapText = readFileSync(new URL('panel/charts/map/map.ts', base), 'utf8')
const utilText = readFileSync(new URL('util.ts', base), 'utf8')
const mapAst = ts.createSourceFile('map.ts', mapText, ts.ScriptTarget.Latest, true)
const utilAst = ts.createSourceFile('util.ts', utilText, ts.ScriptTarget.Latest, true)
const mapClass = mapAst.statements.find(n => ts.isClassDeclaration(n) && n.name.text === 'Map')
const members = ['configBasicStyle', 'calculateAutoLegendNumber'].map(name => mapClass.members.find(n => n.name?.getText(mapAst) === name).getText(mapAst)).join('\n')
const helpers = ['getDynamicColorScale','getMaxAndMinValueByData','filterChartDataByRange'].map(name => utilAst.statements.find(n => ts.isVariableStatement(n) && n.declarationList.declarations.some(d => d.name.getText(utilAst) === name)).getText(utilAst)).join('\n')
const wrapper = mapAst.statements.find(n => ts.isClassDeclaration(n) && n.name.text === 'ColorWrapper').getText(mapAst)
// Isolate DOM/geographic styling; retain the actual renderer method and actual range calculations.
const source = `const parseJson = x => x; const handleGeoJson = () => {}; const hexColorToRGBA = x => x;
${helpers}\n${wrapper}\nexport class TestMap { ${members} }`
const js = ts.transpileModule(source, {compilerOptions:{module:ts.ModuleKind.ESNext,target:ts.ScriptTarget.ES2022}}).outputText
const {TestMap} = await import(`data:text/javascript;base64,${Buffer.from(js).toString('base64')}`)
const renderer = new TestMap()
const data = [12500.25,25600.75,42500.25,78900.5].map((value,i)=>({value,field:String(i)}))
for (const [min,max] of [[0,0],[null,null],[null,100000],[0,null],[12500.25,78900.5]]) {
  for (const count of [1,3,5,9]) {
    const misc = {mapAutoLegend:false,mapLegendNumber:count,mapLegendMin:min,mapLegendMax:max,mapLegendRangeType:'quantize'}
    const chart = {data:{data},senior:{},customStyle:{legend:{show:true}},customAttr:{misc,basicStyle:{colors:Array(9).fill('#123456')},label:{show:false}}}
    const result=renderer.configBasicStyle(chart,{source:{data}},{drawOption:{areaId:'156'},geoJson:{features:[{properties:{name:'0'}}]}})
    assert.equal(result.color.value.length,count,`range ${min},${max}, expected ${count}`)
    assert.equal(misc.mapLegendNumber,count)
  }
}
console.log('PASS: 20 map renderer count cases, including first inferred range and initialized range')
