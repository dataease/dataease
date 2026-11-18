/* eslint-disable @typescript-eslint/no-var-requires -- Node CommonJS 测试入口使用 require。 */
const fs = require('node:fs')
const path = require('node:path')
const ts = require('typescript')
const { parse } = require('@vue/compiler-sfc')

const filename = path.resolve(__dirname, '../../src/views/dashboard/MobileConfigPanel.vue')
const { descriptor } = parse(fs.readFileSync(filename, 'utf8'), { filename })
const ast = ts.createSourceFile(
  filename + '.ts',
  descriptor.scriptSetup.content,
  ts.ScriptTarget.Latest
)
const script = ts.transpileModule(
  ast.statements
    .filter(node => !ts.isImportDeclaration(node))
    .map(node => node.getText(ast))
    .join('\n'),
  { compilerOptions: { target: ts.ScriptTarget.ES2020 } }
).outputText

const fixtureScript = (baseUrl = '') => `
const fixtureMounted=[],fixtureUnmounted=[],fixtureEvents=[];
const ref=value=>({value}),unref=value=>value?.value??value;
const computed=get=>({get value(){return get()}});
const onMounted=fn=>fixtureMounted.push(fn),onBeforeUnmount=fn=>fixtureUnmounted.push(fn);
const defineEmits=()=>type=>fixtureEvents.push(type),debounce=fn=>fn,deepCopy=value=>JSON.parse(JSON.stringify(value));
const fixtureState={
 componentData:ref([{id:'chart',component:'UserView',inMobile:true}]),
 canvasStyleData:ref({width:100}),canvasViewInfo:ref({chart:{customAttr:{},customStyle:{}}}),dvInfo:ref({id:'dashboard'})
};
const fixtureEmbedded={baseUrl:${JSON.stringify(baseUrl)},token:'test-only-token'};
const fixtureStore={...fixtureState,setMobileInPc(){},setCurComponent(){},setCurComponentMobileConfig:value=>fixtureEvents.push('select')};
const dvMainStoreWithOut=()=>fixtureStore,storeToRefs=()=>fixtureState,useEmbedded=()=>fixtureEmbedded;
const fixtureSnapshot={recordSnapshotCacheToMobile:()=>fixtureEvents.push('sync'),resetStyleChangeTimes(){}};
const snapshotStoreWithOut=()=>fixtureSnapshot,useEmitt=()=>{},useI18n=()=>({t:key=>key});
const canvasSave=callback=>{fixtureEvents.push('save');callback()},backCanvasData=()=>{},findComponentById=id=>fixtureState.componentData.value.find(c=>c.id===id);
const ElMessage={success(){}},ElMessageBox={confirm:()=>Promise.resolve()};
${script}
globalThis.fixture={state:fixtureState,events:fixtureEvents,embedded:fixtureEmbedded,mounted:fixtureMounted,unmounted:fixtureUnmounted,handler:hanedleMessage,newWindow,mobileStatusChange,origin:iframeOrigin};
`

module.exports = { fixtureScript, filename, descriptor }
