const fs = require("node:fs/promises"),
  path = require("node:path"),
  { createRequire } = require("node:module"),
  { pathToFileURL } = require("node:url");
const frontend = path.resolve(__dirname, "../../../../core/core-frontend"),
  req = createRequire(frontend + "/package.json");
let server, dir, url;
(async () => {
  dir = await fs.realpath(
    await fs.mkdtemp(
      path.join(require("node:os").tmpdir(), "dataset-step-fixture-")
    )
  );
  const bridge = dir + "/bridge.ts";
  await fs.writeFile(
    bridge,
    `
import {ref} from 'vue';
import zh from '${frontend}/src/locales/zh-CN';
export const useI18n=()=>({t:(key)=>key.split('.').reduce((v,k)=>v?.[k],zh)||key});
export const useAppStoreWithOut=()=>({});export const useEmbedded=()=>({});export const useAppearanceStoreWithOut=()=>({});
export const useCache=()=>({wsCache:{set(){}}});export const useEmitt=()=>({emitter:{emit(){}}});
export const useRoute=()=>({query:location.search.includes('create')?{datasourceId:'source'}:{id:'dataset'},params:{}});export const useRouter=()=>({push(value){window.navigations??=[];window.navigations.push(value)}});export const cancelMap={};
export const getCSSVariable=()=> '#3370ff';
const field=(id,name,node)=>({id,name,originName:name,description:'字段说明 '+name,type:'VARCHAR',deType:0,deExtractType:0,extField:0,groupType:'d',checked:true,datasetTableId:node,datasourceId:'source'});
const nodes=['orders','items'].map((name,i)=>({currentDs:{id:name,tableName:name,datasourceId:'source',type:'db',info:JSON.stringify({table:name,sql:''})},currentDsFields:[field(name+'1',name+'_id',name),field(name+'2',name+'_name',name)],childrenDs:[],unionToParent:{unionType:'left',unionFields:[]}}));nodes[1].unionToParent={unionType:'inner',unionFields:[{parentField:nodes[0].currentDsFields[0],currentField:nodes[1].currentDsFields[0]}]};
if(location.search.includes('sqlNode')){nodes[1].currentDs.type='sql';nodes[1].currentDs.info=JSON.stringify({table:'items',sql:'c2VsZWN0IDE='});nodes[1].currentDs.sqlVariableDetails=JSON.stringify([{variableName:'amount',type:'LONG',defaultValue:'1'}])}
nodes[0].childrenDs=[nodes[1]];
if(location.search.includes('singleSql')){nodes.splice(1);nodes[0].childrenDs=[];nodes[0].currentDs.type='sql';nodes[0].currentDs.sqlVariableDetails='[]'}
export const barInfoApi=async()=>({id:'dataset'});export const getDatasetDetails=async()=>JSON.parse(sessionStorage.getItem('savedDataset')||'null')||JSON.parse(JSON.stringify({id:'dataset',name:'测试数据集',pid:'0',union:[nodes[0]],allFields:[...nodes.flatMap(n=>n.currentDsFields),...(location.search.includes('dependency')?[{...field('calc','计算字段','orders'),extField:2,originName:'[orders1]',params:[]}]:[])]}));
export const getDatasourceList=async()=>[];export const getTables=async()=>['orders','items'].map(tableName=>({tableName,type:'db',datasourceId:'source',name:tableName}));export const singleVal=async()=> 'false';export const enumValueDs=async()=>[];
export const getTableField=async(data)=>{window.fieldRequests??=[];window.fieldRequests.push(data.id);window.fieldRequestDetails??=[];window.fieldRequestDetails.push(data);if(window.failFields){window.failFields=false;throw Error('synthetic field failure')}if(window.holdFieldRequests){await new Promise(resolve=>{window.pendingFields??=[];window.pendingFields.push(resolve)})}else await new Promise(r=>setTimeout(r,data.id==='orders'?120:10));return ['_id','_name','_extra'].map((suffix,i)=>field('',(['orders','items'].includes(data.id)?data.id:data.tableName||data.id)+suffix,data.id))};
export const getPreviewData=async(data)=>{window.previewRequests??=[];window.previewRequests.push(JSON.parse(JSON.stringify(data)));await new Promise(r=>setTimeout(r,window.previewDelays?.shift()??(data.union[0].currentDs.id==='orders'?180:20)));window.previewCompleted??=[];window.previewCompleted.push(data.union[0].currentDs.id);return {data:{fields:data.allFields.map(f=>({...f,dataeaseName:f.id})),data:[Object.fromEntries(data.allFields.map(f=>[f.id,data.union[0].currentDs.tableName+':'+f.name]))]}}};
export const saveDatasetTree=async(data)=>{window.saved=JSON.parse(JSON.stringify(data));sessionStorage.setItem('savedDataset',JSON.stringify(data))};
`
  );
  await fs.writeFile(
    dir + "/stub.vue",
    `<script setup>defineExpose({clearState(){},initState(){}})</script><template><span/></template>`
  );
  await fs.writeFile(
    dir + "/sql-stub.vue",
    `<script setup>
const props=defineProps({sqlNode:Object});const emit=defineEmits(['save','close']);
const save=()=>{emit('save',{...props.sqlNode,sql:props.sqlNode.sql+' /* edited */',sqlVariableDetails:'[]',changeFlag:true},()=>{});emit('close')}
</script><template><div data-testid="sql-editor">{{ JSON.stringify(sqlNode) }}<button @click="save">保存合成 SQL</button><button @click="emit('close')">关闭合成 SQL</button></div></template>`
  );
  await fs.writeFile(
    dir + "/main.ts",
    `import {createApp} from 'vue';import ElementPlus from 'element-plus-secondary';import 'element-plus-secondary/dist/index.css';import '${frontend}/src/style/index.less';import App from '${frontend}/src/views/pages/visualized/data/dataset/form/index.vue';import {useI18n} from './bridge';history.replaceState({back:null},'');const app=createApp(App);app.config.globalProperties.$t=useI18n().t;app.use(ElementPlus,{namespace:'ed'});app.component('Icon',{template:'<span><slot/></span>'});app.mount('#app');`
  );
  await fs.writeFile(
    dir + "/index.html",
    '<style>*{box-sizing:border-box}body{margin:0}</style><div id="app"></div><script type="module" src="/main.ts"></script>'
  );
  const { createServer } = await import(
    pathToFileURL(req.resolve("vite")).href
  );
  const { default: vue } = await import(
    pathToFileURL(req.resolve("@vitejs/plugin-vue")).href
  );
  const { default: jsx } = await import(
    pathToFileURL(
      req.resolve("@vitejs/plugin-vue-jsx").replace("index.cjs", "index.mjs")
    ).href
  );
  const modules = [
    "@/api/dataset",
    "@/hooks/web/useI18n",
    "@/store/modules/app",
    "@/store/modules/embedded",
    "@/store/modules/appearance",
    "@/hooks/web/useCache",
    "@/hooks/web/useEmitt",
    "@/config/axios/service",
    "@/utils/color",
    "vue-router_2",
  ];
  server = await createServer({
    configFile: false,
    root: dir,
    cacheDir: dir + "/cache",
    css: {
      preprocessorOptions: {
        less: {
          additionalData: `@import "${frontend}/src/style/variable.less";`,
        },
      },
    },
    plugins: [
      {
        name: "boundaries",
        enforce: "pre",
        resolveId(id) {
          if (id.endsWith("/AddSql.vue")) return dir + "/sql-stub.vue";
          if (
            id.endsWith(".vue") &&
            ![
              "index.vue",
              "DatasetUnion.vue",
              "DatasetNodeDetails.vue",
              "DatasetTableDetails.vue",
              "DatasetNodeMenu.vue",
              "DatasetPanelFullscreen.vue",
              "HandleMore.vue",
              "UnionFieldList.vue",
            ].includes(path.basename(id))
          )
            return dir + "/stub.vue";
        },
        load(id) {
          if (id.endsWith(".svg"))
            return "export default {render(){return null}}";
        },
      },
      vue(),
      jsx(),
    ],
    resolve: {
      alias: [
        ...modules.map((find) => ({ find, replacement: bridge })),
        { find: "@", replacement: frontend + "/src" },
        ...[
          "vue",
          "element-plus-secondary",
          "lodash-es",
          "@vueuse/core",
          "dayjs",
          "snowflake-id",
        ].map((find) => ({
          find,
          replacement: ["element-plus-secondary", "dayjs"].includes(find)
            ? frontend + "/node_modules/" + find
            : req.resolve(find),
        })),
      ],
      dedupe: ["vue"],
    },
    server: {
      host: "127.0.0.1",
      port: 4205,
      strictPort: true,
      fs: { allow: [dir, frontend] },
    },
  });
  await server.listen();
  url = "http://127.0.0.1:" + server.httpServer.address().port;
  console.log("Dataset editor fixture ready: " + url);
  process.on("SIGTERM", async () => {
    await server.close();
    await fs.rm(dir, { recursive: true, force: true });
    process.exit();
  });
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
