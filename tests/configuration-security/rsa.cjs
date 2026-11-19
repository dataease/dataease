const fs = require("node:fs");
const path = require("node:path");
const Module = require("node:module");
const assert = require("node:assert/strict");
const [frontend, inputFile, outputFile] = process.argv.slice(2);
const ts = require(path.join(frontend, "node_modules/typescript"));
const input = JSON.parse(fs.readFileSync(inputFile, "utf8"));
const source = path.join(frontend, "src/utils/encryption.ts");
const cache = new Map();
const notices = [];
global.window = global;
const mod = new Module(source);
mod.filename = source;
mod.paths = Module._nodeModulePaths(path.dirname(source));
const originalRequire = mod.require.bind(mod);
let requests = 0;
mod.require = (id) => {
  if (id === "element-plus-secondary")
    return { ElMessage: { error: (msg) => notices.push(msg) } };
  if (id === "@/hooks/web/useI18n")
    return { useI18n: () => ({ t: (key) => key }) };
  if (id === "@/hooks/web/useCache")
    return {
      useCache: () => ({
        wsCache: {
          get: (key) => cache.get(key),
          set: (key, value) => cache.set(key, value),
          delete: (key) => cache.delete(key),
        },
      }),
    };
  if (id === "@/store/modules/app")
    return { useAppStoreWithOut: () => ({ getDekey: "DataEaseKey" }) };
  if (id === "@/api/login")
    return {
      queryDekey: async () => {
        requests++;
        return { data: input.publicKey };
      },
    };
  return originalRequire(id);
};
mod._compile(
  ts.transpileModule(fs.readFileSync(source, "utf8"), {
    compilerOptions: {
      module: ts.ModuleKind.CommonJS,
      target: ts.ScriptTarget.ES2020,
      esModuleInterop: true,
    },
  }).outputText,
  source
);
(async () => {
  const { isPublicKey, ensureDekey, rsaEncryp } = mod.exports;
  assert.equal(isPublicKey(input.publicKey), true);
  for (const invalid of [
    null,
    {},
    "MI" + "A".repeat(390),
    "old-encrypted-key-separator-secret",
  ]) {
    assert.equal(isPublicKey(invalid), false);
  }
  cache.set("DataEaseKey", "old-encrypted-key-separator-secret");
  await ensureDekey();
  await ensureDekey();
  assert.equal(requests, 1);
  await ensureDekey(true);
  assert.equal(requests, 2);
  assert.ok(rsaEncryp("a".repeat(input.bits / 8 - 11)));
  assert.throws(
    () => rsaEncryp("a".repeat(input.bits / 8 - 10)),
    /rsa.encryption_failed/
  );
  assert.throws(
    () => rsaEncryp("中".repeat(Math.ceil((input.bits / 8 - 10) / 3))),
    /rsa.encryption_failed/
  );
  fs.writeFileSync(
    outputFile,
    JSON.stringify({ ciphertext: rsaEncryp("历史登录-password") })
  );
})().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
