import { createRequire } from "node:module";
import { fileURLToPath } from "node:url";
import path from "node:path";
const root = fileURLToPath(new URL(".", import.meta.url));
const frontend = path.resolve(root, "../../../../core/core-frontend");
const require = createRequire(path.join(frontend, "package.json"));
const { createServer } = await import(require.resolve("vite"));
const { default: vue } = await import(require.resolve("@vitejs/plugin-vue"));
const server = await createServer({
  configFile: false,
  root,
  cacheDir: path.join(root, "node_modules/.vite"),
  plugins: [vue()],
  resolve: {
    alias: [
      {
        find: "@/store/modules/interactive",
        replacement: path.join(root, "bridge.mjs"),
      },
      { find: "@", replacement: path.join(frontend, "src") },
      {
        find: "vue",
        replacement: path.join(
          frontend,
          "node_modules/vue/dist/vue.esm-bundler.js"
        ),
      },
    ],
  },
  server: {
    host: "127.0.0.1",
    port: 4198,
    strictPort: true,
    fs: { allow: [root, frontend] },
  },
});
await server.listen();
for (const signal of ["SIGINT", "SIGTERM"])
  process.on(signal, async () => {
    await server.close();
    process.exit();
  });
