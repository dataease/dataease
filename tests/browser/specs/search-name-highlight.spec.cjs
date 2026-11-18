/* eslint-disable @typescript-eslint/no-var-requires */
const { test, expect } = require(process.env.PLAYWRIGHT_TEST_MODULE ||
  "@playwright/test");
const fs = require("node:fs/promises");
const path = require("node:path");
const os = require("node:os");
const { createRequire } = require("node:module");
const { pathToFileURL } = require("node:url");
const frontend = path.resolve(__dirname, "../../../core/core-frontend");
const requireFrontend = createRequire(path.join(frontend, "package.json"));
let server, dir, url;

test.beforeAll(async () => {
  dir = await fs.realpath(
    await fs.mkdtemp(path.join(os.tmpdir(), "dataease-search-name-"))
  );
  const utils = await fs.readFile(
    path.join(frontend, "src/utils/utils.ts"),
    "utf8"
  );
  const helper = utils.slice(
    utils.indexOf("export const setColorName ="),
    utils.indexOf("export interface HighlightSegment")
  );
  const variable = await fs.readFile(
    path.join(frontend, "src/views/menu/setting/variable/VariableManage.vue"),
    "utf8"
  );
  const filter = variable.slice(
    variable.indexOf("const triggerFilterRole ="),
    variable.indexOf("const initialValue =")
  );
  const label = variable.match(
    /<span v-if="role.colorName"[\s\S]*?<span v-else>[\s\S]*?<\/span>/
  )[0];
  await fs.writeFile(
    path.join(dir, "highlight.ts"),
    `import DOMPurify from 'dompurify';import {escape} from 'lodash-es';${helper}`
  );
  await fs.writeFile(
    path.join(dir, "Probe.vue"),
    `<script setup>
import {reactive,ref} from 'vue';import {setColorName} from './highlight';
const roleKeyword=ref('');
const state=reactive({roleData:[{children:[
{id:'tag',name:'qa<b>词</b>'},
{id:'entity',name:'qa&lt;'},
{id:'escaped',name:'qa&amp;lt;'},
{id:'special',name:'qa <img src=x onerror="alert(1)">词 & "引号"'},
{id:'plain',name:'普通AbC名称'}]}]});
${filter}
const clear=()=>{roleKeyword.value='';triggerFilterRole()};
</script><template><input aria-label="搜索" v-model="roleKeyword" @change="triggerFilterRole"/><button @click="clear">清空</button><div v-for="role in state.roleData[0].children" :key="role.id" :data-testid="role.id" v-show="!role.hidden">${label}</div></template>`
  );
  await fs.writeFile(
    path.join(dir, "main.ts"),
    `import {createApp} from 'vue';import VueDOMPurifyHTML from 'vue-dompurify-html';import Probe from './Probe.vue';import {setColorName} from './highlight';createApp(Probe).use(VueDOMPurifyHTML).mount('#app');window.highlightForTest=setColorName;`
  );
  await fs.writeFile(
    path.join(dir, "index.html"),
    '<div id="app"></div><script type="module" src="/main.ts"></script>'
  );
  const { createServer } = await import(
    pathToFileURL(requireFrontend.resolve("vite")).href
  );
  const { default: vue } = await import(
    pathToFileURL(requireFrontend.resolve("@vitejs/plugin-vue")).href
  );
  server = await createServer({
    configFile: false,
    root: dir,
    cacheDir: path.join(dir, "cache"),
    plugins: [vue()],
    optimizeDeps: {
      include: ["vue", "dompurify", "lodash-es", "vue-dompurify-html"],
    },
    resolve: {
      alias: Object.fromEntries(
        ["vue", "dompurify", "lodash-es", "vue-dompurify-html"].map((name) => [
          name,
          requireFrontend.resolve(name),
        ])
      ),
      dedupe: ["vue"],
    },
    server: { host: "127.0.0.1", port: 0, fs: { allow: [dir, frontend] } },
  });
  await server.listen();
  url = `http://127.0.0.1:${server.httpServer.address().port}`;
});
test.afterAll(async () => {
  await server?.close();
  if (dir) await fs.rm(dir, { recursive: true, force: true });
});

for (const scenario of [
  { id: "tag", name: "qa<b>词</b>", keyword: "词" },
  { id: "entity", name: "qa&lt;", keyword: "&lt;" },
  { id: "escaped", name: "qa&amp;lt;", keyword: "&amp;lt;" },
  {
    id: "special",
    name: 'qa <img src=x onerror="alert(1)">词 & "引号"',
    keyword: "词",
  },
  { id: "plain", name: "普通AbC名称", keyword: "AbC" },
]) {
  test(`搜索和清空保持名称原文：${scenario.id}`, async ({ page }) => {
    const errors = [];
    page.on("pageerror", (error) => errors.push(error.message));
    await page.goto(url);
    const row = page.getByTestId(scenario.id);
    await expect(row).toHaveText(scenario.name);
    await page
      .getByRole("textbox", { name: "搜索", exact: true })
      .fill(scenario.keyword);
    await page.getByRole("textbox", { name: "搜索", exact: true }).press("Tab");
    await expect(row).toBeVisible();
    await expect(row).toHaveText(scenario.name);
    await expect(row.locator(".search-key-span")).toHaveText(scenario.keyword);
    await expect(row.locator("b,img,script")).toHaveCount(0);
    await page.getByRole("button", { name: "清空", exact: true }).click();
    await expect(row).toHaveText(scenario.name);
    await expect(row.locator(".search-key-span")).toHaveCount(0);
    expect(errors).toEqual([]);
  });
}

test("公共高亮保留自定义字段、忽略大小写和无匹配清理语义", async ({ page }) => {
  await page.goto(url);
  const result = await page.evaluate(() => {
    const item = { account: "<ABC>&lt;" };
    window.highlightForTest(item, "abc", "account", "colorAccount");
    const element = document.createElement("div");
    element.innerHTML = item.colorAccount;
    const text = element.textContent;
    const match = element.querySelector(".search-key-span").textContent;
    window.highlightForTest(item, "missing", "account", "colorAccount");
    const missing = item.colorAccount;
    window.highlightForTest(item, "", "account", "colorAccount");
    return {
      text,
      match,
      missing,
      empty: item.colorAccount,
      original: item.account,
    };
  });
  expect(result).toEqual({
    text: "<ABC>&lt;",
    match: "ABC",
    missing: null,
    empty: null,
    original: "<ABC>&lt;",
  });
});

test("大小写不同的搜索仍展示并高亮原名，未命中隐藏且清空恢复", async ({
  page,
}) => {
  await page.goto(url);
  const row = page.getByTestId("plain");
  const input = page.getByRole("textbox", { name: "搜索", exact: true });
  for (const keyword of ["abc", "ABC"]) {
    await input.fill(keyword);
    await input.press("Tab");
    await expect(row).toBeVisible();
    await expect(row).toHaveText("普通AbC名称");
    await expect(row.locator(".search-key-span")).toHaveText("AbC");
  }
  await input.fill("不存在的变量");
  await input.press("Tab");
  await expect(row).toBeHidden();
  await page.getByRole("button", { name: "清空", exact: true }).click();
  await expect(row).toBeVisible();
  await expect(row).toHaveText("普通AbC名称");
  await expect(row.locator(".search-key-span")).toHaveCount(0);
});
