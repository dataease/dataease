import { test, expect } from "@playwright/test";
import {
  randomBytes,
  createCipheriv,
  createPublicKey,
  publicEncrypt,
  constants,
  createHmac,
} from "node:crypto";
const secret = Buffer.from("synthetic-plugin-hmac-fixture-key");
async function gateway(page, options = {}) {
  const requests = [];
  await page.route("**/*", async (route) => {
    const req = route.request(),
      url = new URL(req.url());
    if (!url.pathname.includes("/de2api")) return route.continue();
    if (req.method() === "OPTIONS")
      return route.fulfill({
        status: 204,
        headers: {
          "access-control-allow-origin": "*",
          "access-control-allow-headers": "*",
          "access-control-allow-methods": "*",
        },
      });
    const respond = (data) =>
      route.fulfill({
        json: { code: 0, data },
        headers: { "access-control-allow-origin": "*" },
      });
    if (url.pathname.endsWith("/xpackModel")) return respond(true);
    if (url.pathname.endsWith("/hmac/info")) {
      if (options.fail)
        return route.fulfill({ status: 503, body: "synthetic unavailable" });
      return respond({ version: 2, enabled: options.enabled !== false });
    }
    if (url.pathname.endsWith("/hmac/exchange")) {
      const { publicKey, requestId } = req.postDataJSON();
      const wrapping = randomBytes(32),
        iv = randomBytes(12);
      const cipher = createCipheriv("aes-256-gcm", wrapping, iv);
      cipher.setAAD(Buffer.from(`2\n${requestId}\ntrue`));
      const ciphertext = Buffer.concat([
        cipher.update(secret),
        cipher.final(),
        cipher.getAuthTag(),
      ]);
      const key = createPublicKey({
        key: Buffer.from(publicKey, "base64"),
        type: "spki",
        format: "der",
      });
      return respond({
        version: 2,
        enabled: true,
        requestId,
        iv: iv.toString("base64"),
        ciphertext: ciphertext.toString("base64"),
        encryptedKey: publicEncrypt(
          {
            key,
            padding: constants.RSA_PKCS1_OAEP_PADDING,
            oaepHash: "sha256",
          },
          wrapping
        ).toString("base64"),
      });
    }
    const headers = await req.allHeaders();
    const path =
      url.pathname.replace(/\/(casbi|oidcbi)(?=\/de2api(?:\/|$))/, "") +
      url.search;
    const signature = createHmac("sha256", secret)
      .update(
        `dataease-key\n${req.method()} ${path}\nX-Date: ${headers["x-date"]}\n`
      )
      .digest("base64");
    const valid =
      headers.authorization?.includes(`signature="${signature}"`) || false;
    requests.push({
      path: url.pathname + url.search,
      headers,
      body: req.postData(),
      valid,
    });
    if (options.enabled !== false && !valid)
      return route.fulfill({
        status: 401,
        body: "synthetic signature rejected",
        headers: { "access-control-allow-origin": "*" },
      });
    return respond({ ok: true });
  });
  await page.goto("/");
  await page.waitForFunction(() => window.ready);
  return requests;
}

test("legacy interceptors run before final signature; response envelope remains unchanged", async ({
  page,
}) => {
  const seen = await gateway(page);
  const value = await page.evaluate(async () => {
    const client = window.AxiosDe.create({ baseURL: "./de2api" });
    client.interceptors.request.use((c) => {
      c.url = "/visualizationBackground/findAll";
      c.params = { q: "区域 &", count: 2 };
      c.headers.Authorization =
        'Signature keyId="dataease-key",signature="obsolete"';
      c.headers["X-DE-TOKEN"] = "stale-plugin-token";
      return c;
    });
    client.interceptors.response.use((r) => ({
      envelope: r.data,
      status: r.status,
    }));
    return client.get("/placeholder");
  });
  expect(value).toEqual({
    envelope: { code: 0, data: { ok: true } },
    status: 200,
  });
  expect(seen).toHaveLength(1);
  expect(seen[0].valid).toBe(true);
  expect(seen[0].headers["x-de-token"]).toBe("host-test-token");
  expect(seen[0].path).toContain("count=2");
});

for (const prefix of ["/dataease/casbi/", "/dataease/oidcbi/", "/"])
  test(`sign final path under ${prefix}`, async ({ page }) => {
    const seen = await gateway(page);
    await page.evaluate(
      (prefix) => history.replaceState(null, "", prefix),
      prefix
    );
    await page.evaluate(() =>
      window.AxiosDe.create({ baseURL: "./de2api" }).get(
        "/visualizationBackground/findAll"
      )
    );
    expect(seen[0].valid).toBe(true);
    expect(seen[0].path).toBe(
      prefix + "de2api/visualizationBackground/findAll"
    );
  });

test("div host rebases legacy API and supplies embedded and proxy context", async ({
  page,
}) => {
  const seen = await gateway(page);
  await page.evaluate(async () => {
    window.fixture.embedded = {
      baseUrl: "http://de-fixture.test/dataease/",
      token: "embedded-test-token",
    };
    window.fixture.cache["user.proxyInfo"] = {
      proxy: true,
      proxySecret: "proxy-test-secret",
    };
    await window.AxiosDe.create({ baseURL: "./de2api" }).get(
      "/visualizationBackground/findAll"
    );
  });
  expect(seen[0].path).toBe("/dataease/de2api/visualizationBackground/findAll");
  expect(seen[0].headers["x-embedded-token"]).toBe("embedded-test-token");
  expect(seen[0].headers["x-de-admin-proxy"]).toBe("proxy-test-secret");
  expect(seen[0].valid).toBe(true);
});

test("share context takes precedence over embedded context", async ({
  page,
}) => {
  const seen = await gateway(page);
  await page.evaluate(async () => {
    window.fixture.link = "share-test-token";
    window.fixture.embedded.token = "embedded-test-token";
    await window.AxiosDe.get("/de2api/echo");
  });
  expect(seen[0].headers["x-de-link-token"]).toBe("share-test-token");
  expect(seen[0].headers["x-embedded-token"]).toBeUndefined();
});

test("absolute API URL, callable root and nested create retain compatibility", async ({
  page,
}) => {
  const seen = await gateway(page);
  await page.evaluate(async () => {
    const client = window.AxiosDe.create({ baseURL: "./de2api" }).create({
      headers: { "X-Plugin": "child" },
    });
    await client(location.origin + "/de2api/absolute");
    await window.AxiosDe.default({ url: "/de2api/root" });
  });
  expect(seen).toHaveLength(2);
  expect(seen.every((r) => r.valid)).toBe(true);
  expect(seen[0].headers["x-plugin"]).toBe("child");
});

test("disabled HMAC removes obsolete signature without changing normal requests", async ({
  page,
}) => {
  const seen = await gateway(page, { enabled: false });
  await page.evaluate(() =>
    window.AxiosDe.create({ baseURL: "./de2api" }).get("/echo", {
      headers: {
        Authorization: 'Signature keyId="dataease-key",signature="old"',
        "X-Date": "old",
      },
    })
  );
  expect(seen[0].headers.authorization).toBeUndefined();
  expect(seen[0].headers["x-date"]).toBeUndefined();
  expect(seen[0].headers["x-de-token"]).toBe("host-test-token");
});

test("bootstrap failure never dispatches unsigned business request", async ({
  page,
}) => {
  const seen = await gateway(page, { fail: true });
  const error = await page.evaluate(() =>
    window.AxiosDe.get("/de2api/echo").catch((e) => e.code)
  );
  expect(error).toBe("ERR_HMAC_INITIALIZATION");
  expect(seen).toHaveLength(0);
});

test("external destinations and API-prefix lookalikes receive no DataEase credentials", async ({
  page,
}) => {
  const seen = await gateway(page, { enabled: false });
  const external = [];
  await page.route("http://outside.test/**", async (route) => {
    external.push(await route.request().allHeaders());
    await route.fulfill({
      json: { ok: true },
      headers: {
        "access-control-allow-origin": "*",
        "access-control-allow-headers": "*",
      },
    });
  });
  await page.evaluate(async () => {
    const client = window.AxiosDe.create({ baseURL: "./de2api" });
    client.interceptors.request.use((c) => {
      c.headers["X-DE-TOKEN"] = "stale";
      c.headers["X-EMBEDDED-TOKEN"] = "stale";
      c.headers.Authorization =
        'Signature keyId="dataease-key",signature="old"';
      return c;
    });
    await client.get("http://outside.test/echo");
    await client.get(location.origin + "/de2api-lookalike/echo");
  });
  for (const headers of [...external, ...seen.map((r) => r.headers)]) {
    expect(headers["x-de-token"]).toBeUndefined();
    expect(headers["x-embedded-token"]).toBeUndefined();
    expect(headers.authorization).toBeUndefined();
  }
  expect(await page.evaluate(() => window.fixture.contextCalls)).toBe(0);
});

test("FormData and blob response survive final signing", async ({ page }) => {
  const seen = await gateway(page);
  const type = await page.evaluate(async () => {
    const file = new FormData();
    file.append("file", new Blob(["synthetic text"]), "sample.txt");
    const result = await window.AxiosDe.post("/de2api/upload", file, {
      responseType: "blob",
    });
    return result.data instanceof Blob;
  });
  expect(type).toBe(true);
  expect(seen[0].valid).toBe(true);
  expect(seen[0].headers["content-type"]).toContain(
    "multipart/form-data; boundary="
  );
  expect(seen[0].body).toContain("synthetic text");
});

test("custom adapters still pass through final signing", async ({ page }) => {
  await gateway(page);
  const result = await page.evaluate(async () => {
    let calls = 0;
    const client = window.AxiosDe.create({ baseURL: "./de2api" });
    const response = await client.get("/echo", {
      adapter: async (config) => {
        calls++;
        return {
          data: { signed: !!config.headers.get("Authorization") },
          status: 200,
          statusText: "OK",
          headers: {},
          config,
        };
      },
    });
    return { calls, data: response.data };
  });
  expect(result).toEqual({ calls: 1, data: { signed: true } });
});

test("cancellation during async context preparation never dispatches", async ({
  page,
}) => {
  const seen = await gateway(page, { enabled: false });
  const result = await page.evaluate(async () => {
    window.fixture.waitContext = true;
    const source = window.AxiosDe.CancelToken.source();
    const result = window.AxiosDe.get("/de2api/echo", {
      cancelToken: source.token,
    }).catch((e) => window.AxiosDe.isCancel(e));
    while (!window.releaseContext)
      await new Promise((resolve) => setTimeout(resolve, 0));
    source.cancel("synthetic cancellation");
    window.releaseContext();
    return result;
  });
  expect(result).toBe(true);
  expect(seen).toHaveLength(0);
});

test("unadapted legacy behavior reproduces unsigned 401", async ({ page }) => {
  const seen = await gateway(page);
  const status = await page.evaluate(async () => {
    const client = window.rawAxios.create({ baseURL: "./de2api" });
    client.interceptors.request.use(async (c) => {
      try {
        const info = await window.rawAxios.get("./de2api/perSetting/hmac/info");
        info.data.data.substring(0, 16);
      } catch {
        /* Old bundles swallow initialization failures. */
      }
      return c;
    });
    return client
      .get("/visualizationBackground/findAll")
      .catch((e) => e.response.status);
  });
  expect(status).toBe(401);
  expect(seen[0].valid).toBe(false);
});
