import { createRequire } from "node:module";
import { fileURLToPath } from "node:url";
import path from "node:path";
const root = fileURLToPath(new URL(".", import.meta.url));
const frontend = path.resolve(root, "../../../../core/core-frontend");
const require = createRequire(path.join(frontend, "package.json"));
const { createServer } = await import(require.resolve("vite"));
const bridge = path.join(root, "bridge.mjs");
const server = await createServer({
  root,
  configFile: false,
  cacheDir: path.join(root, "node_modules/.vite"),
  define: { "import.meta.env.VITE_API_BASEPATH": JSON.stringify("./de2api") },
  resolve: {
    alias: [
      ...[
        "@/config/axios/refresh",
        "@/store/modules/embedded",
        "@/store/modules/link",
        "@/hooks/web/useCache",
        "@/hooks/web/useI18n",
        "@/utils/utils",
        "element-plus-secondary",
      ].map((find) => ({ find, replacement: bridge })),
      { find: "@", replacement: path.join(frontend, "src") },
    ],
  },
  server: {
    host: "127.0.0.1",
    port: 4196,
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
