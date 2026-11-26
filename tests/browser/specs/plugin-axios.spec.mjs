import { test, expect } from "@playwright/test";
test.beforeEach(async ({ page }) => {
  await page.goto("/");
  await page.waitForFunction(() => window.ready);
});
test("callable, default, nested create and static tools preserve response and raw isolation", async ({
  page,
}) => {
  const result = await page.evaluate(async () => {
    const a = window.AxiosDe;
    const adapter = async (config) => ({
      data: { ok: true },
      status: 200,
      statusText: "OK",
      headers: {},
      config,
    });
    const configs = [];
    for (const client of [a, a.default, a.create().create()]) {
      const response = await client({
        url: "/findAll",
        baseURL: "./de2api",
        adapter,
      });
      configs.push([
        response.status,
        response.data.ok,
        response.config.headers.has("Authorization"),
      ]);
    }
    const raw = await window.rawAxios({
      url: "/findAll",
      baseURL: "./de2api",
      adapter,
    });
    return {
      configs,
      rawSigned: raw.config.headers.has("Authorization"),
      staticTool: typeof a.CancelToken.source,
    };
  });
  expect(result).toEqual({
    configs: [
      [200, true, true],
      [200, true, true],
      [200, true, true],
    ],
    rawSigned: false,
    staticTool: "function",
  });
});
test("signs final interceptor destination and serializes parameters once", async ({
  page,
}) => {
  const result = await page.evaluate(async () => {
    const a = window.AxiosDe.create();
    let calls = 0;
    a.interceptors.request.use((c) => {
      c.url = "/final";
      return c;
    });
    const r = await a.get("/initial", {
      baseURL: "./de2api",
      params: { x: 1 },
      paramsSerializer: () => `x=${++calls}`,
      adapter: async (config) => ({
        config,
        data: null,
        status: 200,
        headers: {},
      }),
    });
    return {
      calls,
      target: window.rawAxios.getUri(r.config),
      signed: window.fixture.signatures[0],
    };
  });
  expect(result.calls).toBe(1);
  expect(result.target).toBe(result.signed);
  expect(result.target).toContain("/de2api/final?x=1");
});
test("external destinations lose DataEase credentials and retain third party auth", async ({
  page,
}) => {
  const result = await page.evaluate(async () => {
    const a = window.AxiosDe.create();
    a.interceptors.request.use((c) => {
      c.url = "https://example.org/de2api/findAll";
      c.headers.set("X-DE-TOKEN", "stale");
      return c;
    });
    const r = await a.get("/findAll", {
      baseURL: "./de2api",
      headers: {
        Authorization: "Bearer third-party",
        "X-DE-LINK-TOKEN": "stale",
        "X-EMBEDDED-TOKEN": "stale",
        "X-DE-ADMIN-PROXY": "stale",
      },
      adapter: async (config) => ({
        config,
        data: null,
        status: 200,
        headers: {},
      }),
    });
    return {
      headers: r.config.headers.toJSON(),
      signatures: window.fixture.signatures.length,
    };
  });
  expect(result.signatures).toBe(0);
  expect(result.headers.Authorization).toBe("Bearer third-party");
  for (const key of Object.keys(result.headers))
    expect(key.toLowerCase()).not.toMatch(/^x-(de|embedded)-/);
});
test("share and embedded identity come from host state", async ({ page }) => {
  const result = await page.evaluate(async () => {
    const send = async () =>
      (
        await window.AxiosDe.get("/findAll", {
          baseURL: "./de2api",
          adapter: async (config) => ({
            config,
            data: null,
            status: 200,
            headers: {},
          }),
        })
      ).config.headers.toJSON();
    window.fixture.embedded = {
      baseUrl: "https://embed.example/context/",
      token: "synthetic-embed",
    };
    const embedded = await send();
    window.fixture.link = "synthetic-link";
    const shared = await send();
    return { embedded, shared, targets: window.fixture.signatures };
  });
  expect(result.embedded["X-EMBEDDED-TOKEN"]).toBe("synthetic-embed");
  expect(result.shared["X-DE-LINK-TOKEN"]).toBe("synthetic-link");
  expect(result.shared["X-EMBEDDED-TOKEN"]).toBeUndefined();
  expect(
    result.targets.every((u) =>
      u.startsWith("https://embed.example/context/de2api/")
    )
  ).toBe(true);
});
test("cancellation during host initialization prevents dispatch", async ({
  page,
}) => {
  const result = await page.evaluate(async () => {
    window.fixture.waitContext = true;
    let dispatched = false;
    const controller = new AbortController();
    const promise = window.AxiosDe.get("/findAll", {
      baseURL: "./de2api",
      signal: controller.signal,
      adapter: async () => {
        dispatched = true;
        return { data: null, status: 200, headers: {} };
      },
    }).catch((e) => window.AxiosDe.isCancel(e));
    await new Promise((r) => setTimeout(r, 20));
    controller.abort();
    window.releaseContext();
    return { cancelled: await promise, dispatched };
  });
  expect(result).toEqual({ cancelled: true, dispatched: false });
});
test("disabled signer retains V2 unsigned behavior", async ({ page }) => {
  const signed = await page.evaluate(async () => {
    window.fixture.enabled = false;
    const r = await window.AxiosDe.get("/findAll", {
      baseURL: "./de2api",
      adapter: async (config) => ({
        config,
        data: null,
        status: 200,
        headers: {},
      }),
    });
    return r.config.headers.has("Authorization");
  });
  expect(signed).toBe(false);
});
test("standard browser adapter preserves multipart upload and blob download", async ({
  page,
}) => {
  let signed = false;
  await page.route("**/de2api/upload", async (route) => {
    const headers = route.request().headers();
    signed =
      !!headers.authorization &&
      /multipart\/form-data; boundary=/.test(headers["content-type"]);
    await route.fulfill({
      body: "synthetic-file",
      contentType: "application/octet-stream",
    });
  });
  const body = await page.evaluate(async () => {
    const form = new FormData();
    form.append("file", new Blob(["fixture"]), "fixture.txt");
    const r = await window.AxiosDe.post("/upload", form, {
      baseURL: "./de2api",
      responseType: "blob",
    });
    return r.data.text();
  });
  expect(signed).toBe(true);
  expect(body).toBe("synthetic-file");
});
