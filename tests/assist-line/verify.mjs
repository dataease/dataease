import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import ts from '../../core/core-frontend/node_modules/typescript/lib/typescript.js'
const source = readFileSync(new URL('../../core/core-frontend/src/views/chart/components/js/panel/types/impl/g2-assist-line.ts', import.meta.url), 'utf8').replace("import { parseJson } from '../../../util'", 'const parseJson = value => typeof value === "string" ? JSON.parse(value) : value')
const compiled = ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 } }).outputText
const { createAssistLineVisibility } = await import(`data:text/javascript;base64,${Buffer.from(compiled).toString('base64')}`)
const fixed = {name:'limit', field:'0', boundFieldId:'1', followField:true}
const dynamic = {name:'average',field:'1',fieldId:'1',summary:'avg',followField:true}
const chart = { senior:{assistLineCfg:{assistLine:[fixed,dynamic]}}, yAxis:[{id:'1'},{id:'2'}],data:{data:[{category:'renamed-A',quotaList:[{id:'1'}]},{category:'renamed-B',quotaList:[{id:'1'}]},{category:'other',quotaList:[{id:'2'}]}]} }
const s = createAssistLineVisibility(chart)
assert.equal(s.visible(fixed),true)
s.select(['renamed-A','other']);assert.equal(s.visible(fixed),true)
s.select(['other']);assert.equal(s.visible(fixed),false)
assert.equal(s.visible({...dynamic,followField:undefined,value:'20'}),false)
assert.equal(s.visible({...fixed,followField:false}),true)
assert.equal(s.visible({...fixed,followField:undefined}),true)
assert.equal(s.visible({...fixed,boundFieldId:''}),true)
assert.equal(s.visible({...fixed,boundFieldId:'removed'}),false)
s.select([]);assert.equal(s.visible(fixed),false)
s.select();assert.equal(s.visible(fixed),true)
assert.equal(s.visible(dynamic),true)
const dual = createAssistLineVisibility({...chart, yAxisExt:[{id:'1'}], data:{left:{data:[{category:'left',quotaList:[{id:'1'}]}]},right:{data:[{category:'right',quotaList:[{id:'1'}]}]}}})
dual.select(['left']);assert.equal(dual.visible({...fixed,yAxisType:'right'}),false)
assert.equal(dual.visible({...fixed,yAxisType:'left'}),true)
const nested = createAssistLineVisibility({...chart,data:{left:{data:[{data:[{category:'amount',quotaList:[{id:'1'}]}]}]}}})
nested.select(['other']);assert.equal(nested.visible(fixed),false)
nested.select(['amount']);assert.equal(nested.visible(fixed),true)
console.log('PASS: 15 auxiliary-line visibility assertions')
