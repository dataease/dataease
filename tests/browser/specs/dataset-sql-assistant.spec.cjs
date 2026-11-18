const { test, expect } = require("@playwright/test");
const proposal =
  "SELECT SUM(actual_amt) AS total FROM orders WHERE pay_code = 'P'";
const initial = "select 1";
const control = (page, part) =>
  page.getByTestId("dataset-sql-assistant-" + part);
const editor = (page) => page.locator(".sql-drawer-fullscreen:visible");
async function enter(page, options = "") {
  await page.goto("/?sqlNode=1" + options);
  await expect(page.locator(".sql-result .preview-data")).toContainText(
    "orders:"
  );
  await page.getByRole("button", { name: "数据表关联", exact: true }).click();
  await reopen(page);
}
async function reopen(page) {
  const row = page
    .locator(".node-items .node-item")
    .filter({ has: page.getByRole("button", { name: "items", exact: true }) });
  await row.locator(".hover-icon").click();
  await page
    .locator(".menu-more_popper:visible")
    .getByRole("menuitem", { name: "编辑SQL", exact: true })
    .click();
  await expect(editor(page).locator(".cm-content")).toBeVisible();
}
async function expand(page) {
  await control(page, "toggle").click();
  await expect(control(page, "panel")).toBeVisible();
}
async function ask(page) {
  await control(page, "input")
    .locator("textarea")
    .or(page.locator('textarea[data-testid="dataset-sql-assistant-input"]'))
    .fill("按已确认支付口径汇总实付金额");
  await control(page, "send").click();
}
async function closeEditor(page) {
  await editor(page).locator(".save-or-cancel > .hover-icon").click();
  const confirm = page.locator(".ed-message-box:visible");
  if (await confirm.isVisible())
    await confirm.getByRole("button", { name: /^(OK|确定)$/ }).click();
  await expect(editor(page).locator(".cm-content")).toHaveCount(0);
}
async function backend(page, options = {}) {
  const state = {
    available: true,
    opens: [],
    checks: 0,
    stops: 0,
    closes: 0,
    reads: 0,
    previews: [],
    ...options,
  };
  const sessions = new Map();
  await page.route("**/fixture-api/dataset/previewSql", async (route) => {
    state.previews.push(route.request().postDataJSON());
    return route.fulfill({
      json: {
        data: {
          data: [{ total: "200" }],
          fields: [{ originName: "total", deType: 2 }],
        },
      },
    });
  });
  await page.route("**/fixture-api/ai/**", async (route) => {
    const req = route.request(),
      path = new URL(req.url()).pathname;
    const body = req.postDataJSON() || {};
    const reply = (json) => route.fulfill({ json });
    if (path === "/fixture-api/ai/availability")
      return reply({ available: state.available, revision: "r1" });
    if (path === "/fixture-api/ai/conversations") {
      state.opens.push(body);
      const id = "session-" + state.opens.length;
      const scope = {
        sessionId: id,
        contextVersion: "context-" + id,
        scene: body.scene,
        resourceId: body.resourceId,
        resourceName: "Fixture MySQL",
        editorId: body.editorId,
        resourceVersion: "source-v1",
        draftVersion: "draft-" + id,
        expiresAt: Date.now() + 300000,
      };
      sessions.set(id, scope);
      return reply(scope);
    }
    const id = path.split("/")[4],
      sc = sessions.get(id);
    if (req.method() === "DELETE") {
      state.closes++;
      return reply({});
    }
    if (!sc) throw Error("Unexpected session: " + path);
    const run = {
      ...sc,
      executionId: "run-" + id,
      status: "RUNNING",
      deadline: Date.now() + 60000,
    };
    const result = {
      ...sc,
      type: state.explain ? "EXPLANATION" : "SQL_SUGGESTION",
      text: "已核对两笔已支付订单。",
      sql: state.explain ? null : proposal,
    };
    if (path.endsWith("/executions")) return reply(run);
    if (path.endsWith("/check-application")) {
      state.checks++;
      if (state.checkDenied)
        return route.fulfill({
          status: 403,
          json: { messageKey: "forbidden" },
        });
      return reply(result);
    }
    if (path.endsWith("/stop")) {
      state.stops++;
      return reply({ ...run, status: "CANCELLED", cursor: 1, events: [] });
    }
    if (path.endsWith("/events")) {
      state.reads++;
      if (state.onRead) await state.onRead();
      if (state.slow) return reply({ ...run, cursor: 0, events: [] });
      if (state.failed)
        return reply({
          ...run,
          status: "FAILED",
          cursor: 1,
          events: [{ sequence: 1, type: "FAILED", error: "model_unavailable" }],
        });
      return reply({
        ...run,
        status: "COMPLETED",
        cursor: 1,
        events: [{ sequence: 1, type: "COMPLETED" }],
        result,
      });
    }
    throw Error("Unexpected API: " + path);
  });
  return state;
}
async function assertUnrun(page, state) {
  expect(state.previews).toEqual([]);
  expect(await page.evaluate(() => window.saved)).toBeUndefined();
}
for (const cross of [false, true]) {
  test(
    "SQL assistant suggestion is applied and manually previewed (cross=" +
      cross +
      ")",
    async ({ page }) => {
      const state = await backend(page);
      await enter(page, cross ? "&cross=1" : "");
      await expand(page);
      await ask(page);
      await expect(control(page, "apply")).toBeVisible();
      expect(state.opens).toMatchObject([
        { scene: "dataset-sql", resourceId: "source", sql: initial },
      ]);
      await expect(editor(page).locator(".cm-content")).toHaveText(initial);
      await assertUnrun(page, state);
      await control(page, "toggle").click();
      await expect(control(page, "panel")).toBeHidden();
      await expand(page);
      await control(page, "apply").click();
      const confirm = page.locator(".ed-message-box:visible");
      await confirm.getByRole("button", { name: "取消", exact: true }).click();
      expect(state.checks).toBe(0);
      await expect(editor(page).locator(".cm-content")).toHaveText(initial);
      await control(page, "apply").click();
      await confirm.getByRole("button", { name: "确定", exact: true }).click();
      await expect(editor(page).locator(".cm-content")).toHaveText(proposal);
      expect(state.checks).toBe(1);
      await assertUnrun(page, state);
      await editor(page)
        .getByRole("button", { name: "运行", exact: true })
        .click();
      await expect(editor(page).locator(".table-scroll")).toContainText("200");
      expect(state.previews).toMatchObject([
        {
          isCross: cross,
          datasourceId: "source",
          sql: Buffer.from(proposal).toString("base64"),
        },
      ]);
      await editor(page)
        .getByRole("button", { name: "保存", exact: true })
        .click();
      await closeEditor(page);
      await reopen(page);
      await expect(editor(page).locator(".cm-content")).toHaveText(proposal);
    }
  );
}
test("only a suggestion applies to a blank draft", async ({ page }) => {
  const state = await backend(page, { explain: true });
  await enter(page, "&assistantBlank=1");
  await expand(page);
  await ask(page);
  await expect(control(page, "panel")).toContainText("已核对两笔已支付订单。");
  await expect(control(page, "apply")).toHaveCount(0);
  await assertUnrun(page, state);
  state.explain = false;
  await ask(page);
  await control(page, "apply").click();
  await expect(editor(page).locator(".cm-content")).toHaveText(proposal);
  expect(state.opens[1].previousSessionId).toBe("session-1");
  expect(state.checks).toBe(1);
  await assertUnrun(page, state);
});
test("availability and reauthorization control the editor entry and application", async ({
  page,
}) => {
  const state = await backend(page, { available: false, checkDenied: true });
  await enter(page, "&assistantBlank=1");
  await expect(control(page, "toggle")).toHaveCount(0);
  state.available = true;
  await page.evaluate(() => window.dispatchEvent(new Event("focus")));
  await expand(page);
  await ask(page);
  await control(page, "apply").click();
  await expect(control(page, "panel").getByRole("alert")).toBeVisible();
  await expect(editor(page).locator(".cm-content")).toHaveText("");
  state.available = false;
  await page.evaluate(() => window.dispatchEvent(new Event("focus")));
  await expect(control(page, "toggle")).toHaveCount(0);
  expect(state.checks).toBe(1);
  await assertUnrun(page, state);
});
test("failed and stopped executions preserve the active draft", async ({
  page,
}) => {
  const state = await backend(page, { failed: true });
  await enter(page);
  await expand(page);
  await ask(page);
  await expect(control(page, "panel").getByRole("alert")).toBeVisible();
  await expect(control(page, "apply")).toHaveCount(0);
  state.failed = false;
  state.slow = true;
  await ask(page);
  await expect.poll(() => state.reads).toBeGreaterThan(1);
  await control(page, "stop").click();
  await expect.poll(() => state.stops).toBe(1);
  await expect(control(page, "send")).toBeVisible();
  await expect(editor(page).locator(".cm-content")).toHaveText(initial);
  await assertUnrun(page, state);
});
test("closing the real drawer discards its late result and any old turn", async ({
  page,
}) => {
  let release;
  const held = new Promise((resolve) => (release = resolve));
  const state = await backend(page, { onRead: () => held });
  await enter(page);
  await expand(page);
  await ask(page);
  await expect.poll(() => state.reads).toBe(1);
  try {
    await closeEditor(page);
    await expect.poll(() => state.closes).toBe(1);
    await reopen(page);
    await expand(page);
  } finally {
    release();
  }
  await expect(control(page, "panel").getByRole("log")).toBeEmpty();
  state.onRead = null;
  await ask(page);
  await expect(control(page, "apply")).toHaveCount(1);
  expect(state.opens[1].editorId).not.toBe(state.opens[0].editorId);
  expect(state.opens[1].previousSessionId).toBeUndefined();
  await assertUnrun(page, state);
});
