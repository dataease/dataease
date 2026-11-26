const { test: base, expect } = require("@playwright/test");

const test = base.extend({
  page: async ({ page }, use) => {
    const opened = [];
    const closed = new Set();
    page.on("response", (response) => {
      if (!response.ok()) return;
      const method = response.request().method();
      if (method === "POST" && response.url().endsWith("/ai/conversations"))
        opened.push(response);
      if (method === "DELETE") closed.add(response.url());
    });

    await use(page);

    // Both browser suites share the same backend actor across editor lifetimes.
    for (const response of opened) {
      const { sessionId } = await response.json();
      expect(sessionId).toBeTruthy();
      const url = response.url() + "/" + encodeURIComponent(sessionId);
      if (closed.has(url)) continue;
      await expect(await page.request.delete(url)).toBeOK();
      closed.add(url);
    }
  },
});

module.exports = { test, expect };
