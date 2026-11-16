import { createRequire } from "node:module";
import { readFile, writeFile, mkdir } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";
const root = path.resolve(
  path.dirname(fileURLToPath(import.meta.url)),
  "../../../.."
);
const frontend = path.join(root, "core/core-frontend");
const require = createRequire(path.join(frontend, "package.json"));
const { parse, compileScript } = require("@vue/compiler-sfc");
const { build } = require("esbuild");
const source = await readFile(
  path.join(frontend, "src/views/menu/system/auth/DynamicColumn.vue"),
  "utf8"
);
const { descriptor } = parse(source);
const component = compileScript(descriptor, {
  id: "menu-check",
  inlineTemplate: true,
}).content;
// Exercise the actual resource selection functions with synthetic tree and API boundaries.
const resource = await readFile(
  path.join(frontend, "src/views/menu/system/auth/ResourceConfig.vue"),
  "utf8"
);
const first = resource.slice(
  resource.indexOf("const findFirstValidNode ="),
  resource.indexOf("const selectFirstItem =")
);
const change = resource.slice(
  resource.indexOf("const menuIdChange ="),
  resource.indexOf("const resourceIdChange =")
);
const { transform } = require("esbuild");
const selection = (await transform(first + change, { loader: "ts" })).code;
const output = path.join(root, "tests/browser/output/menu-authorization");
await mkdir(output, { recursive: true });
await build({
  stdin: {
    contents:
      component.replace("export default", "const Component =") +
      `
import { createApp, h, reactive } from 'vue'
import ElementPlus from 'element-plus-secondary'
const row = reactive({ id: '30', leaf: true, disabled: true, value2: false })
let writes = 0
createApp({ render: () => h(Component, { activeAuth: 'menu', scope: { row }, item: { weightLevel: 2 }, onRowWeightChanged: () => writes++ }) }).use(ElementPlus).mount('#app')
const selectedMenuId = { value: 'allowed' }
let loaded = 0
const selectedResourceOid = { value: undefined }
const menuTreeRef = { value: { setCurrentKey() {} } }
const loadPermission = () => loaded++
const getSubjectType = () => 0
const uncommittedTips = async () => true
const ElMessage = { warning() {} }
${selection}
window.fixture = { row, select: menuIdChange, first: findFirstValidNode, result: () => ({ selected: selectedMenuId.value, loaded, writes }) }
`,
    resolveDir: frontend,
    loader: "ts",
  },
  bundle: true,
  outfile: path.join(output, "fixture.js"),
  plugins: [
    {
      name: "boundaries",
      setup(build) {
        build.onResolve({ filter: /^@\// }, (args) => ({
          path: args.path,
          namespace: "boundary",
        }));
        build.onLoad({ filter: /.*/, namespace: "boundary" }, () => ({
          contents: `
      export const useI18n = () => ({ t: key => key })
      export const propTypes = { string: { def: value => ({ type: String, default: value }) }, bool: { def: value => ({ type: Boolean, default: value }) } }
    `,
        }));
      },
    },
  ],
});
await writeFile(
  path.join(output, "index.html"),
  '<!doctype html><div id="app"></div><script src="fixture.js"></script>'
);
console.log("Menu authorization fixture built");
