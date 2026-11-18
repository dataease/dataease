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
async function ask(page, question = "按已确认支付口径汇总实付金额") {
  await control(page, "input")
    .locator("textarea")
    .or(page.locator('textarea[data-testid="dataset-sql-assistant-input"]'))
    .fill(question);
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
  const completedResults = new Map();
  await page.route("**/fixture-api/dataset/previewSql", async (route) => {
    state.previews.push(route.request().postDataJSON());
    return route.fulfill({
      json: {
        data: {
          data: [{ total: state.previewTotals?.shift() ?? "200" }],
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
      if (body.previousSessionId && state.denyHistory)
        return route.fulfill({
          status: 403,
          json: { messageKey: "i18n_ai_forbidden" },
        });
      if (state.onOpen) await state.onOpen();
      const id = "session-" + state.opens.length;
      const scope = {
        sessionId: id,
        contextVersion: "context-" + id,
        scene: body.scene,
        resourceId: body.resourceId,
        resourceName:
          body.resourceId === "second" ? "Warehouse B" : "Fixture MySQL",
        editorId: body.editorId,
        resourceVersion: "source-v1",
        draftVersion: "draft-" + id,
        expiresAt: Date.now() + (state.lifetime ?? 300000),
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
      sql: state.explain
        ? null
        : sc.resourceId === "second"
        ? "SELECT 2"
        : state.query || proposal,
      evidence: state.evidence,
    };
    if (path.endsWith("/executions")) {
      state.starts = (state.starts ?? 0) + 1;
      return reply(run);
    }
    if (path.endsWith("/check-application")) {
      state.checks++;
      state.lastCheck = { id, target: body.targetResourceId };
      if (state.checkDenied)
        return route.fulfill({
          status: 403,
          json: { messageKey: "forbidden" },
        });
      return reply(completedResults.get(id));
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
      completedResults.set(id, result);
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
        {
          scene: "dataset-sql",
          resourceId: "source",
          sql: initial,
          isCross: cross,
        },
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
test("the next suggestion follows the prior suggestion until the author edits the draft", async ({
  page,
}) => {
  const state = await backend(page);
  await enter(page);
  await expand(page);
  await ask(page);
  await expect(control(page, "apply")).toHaveCount(1);
  await ask(page);
  await expect(control(page, "apply")).toHaveCount(2);
  expect(state.opens[1].sql).toBe(proposal);
  await expect(control(page, "panel").locator("article").nth(1)).toContainText(
    "建议 #1"
  );
  await editor(page).locator(".cm-content").fill("SELECT 77");
  await ask(page);
  await expect(control(page, "apply")).toHaveCount(3);
  expect(state.opens[2].sql).toBe("SELECT 77");
  await assertUnrun(page, state);
});

test("older suggestions can be copied to another source after a combined edit warning", async ({
  page,
}) => {
  const state = await backend(page);
  await enter(page);
  await expand(page);
  await ask(page);
  await expect(control(page, "apply")).toBeVisible();
  await editor(page).locator(".cm-content").fill("SELECT 77");
  await editor(page).locator(".ds-list").click();
  await page.getByRole("treeitem", { name: "Warehouse B" }).click();
  await ask(page);
  await expect(control(page, "apply")).toHaveCount(2);
  expect(state.opens[1]).toMatchObject({
    resourceId: "second",
    sql: "SELECT 77",
    previousSessionId: "session-1",
  });
  const old = control(page, "apply").first();
  await old.click();
  const dialog = page.locator(".ed-message-box:visible");
  await expect(dialog).toContainText("Fixture MySQL");
  await expect(dialog).toContainText("Warehouse B");
  await expect(dialog).toContainText("覆盖");
  await dialog.getByRole("button", { name: "取消", exact: true }).click();
  await expect(editor(page).locator(".cm-content")).toHaveText("SELECT 77");
  await old.click();
  await dialog.getByRole("button", { name: "确定", exact: true }).click();
  await expect(editor(page).locator(".cm-content")).toHaveText(proposal);
  expect(state.lastCheck).toEqual({ id: "session-1", target: "second" });
  expect(state.checks).toBe(1);
  await assertUnrun(page, state);
});

for (const scope of ["user", "workspace", "proxy", "expiry"]) {
  test(scope + " invalidates the prior editor activity", async ({ page }) => {
    const state = await backend(page, {
      lifetime: scope === "expiry" ? 1500 : 300000,
    });
    await enter(page);
    await expand(page);
    await ask(page);
    await expect(control(page, "apply")).toBeVisible();
    if (scope !== "expiry")
      await page.evaluate((scope) => window.aiSwitchUser(scope), scope);
    await expect(control(page, "apply")).toHaveCount(0);
    await expect(editor(page).locator(".cm-content")).toHaveText(initial);
    expect(state.checks).toBe(0);
  });
}

test("the author can choose an older basis or start another query with the same composer", async ({
  page,
}) => {
  const state = await backend(page);
  await enter(page);
  await expand(page);
  await ask(page);
  await expect(control(page, "apply")).toBeVisible();
  await editor(page).locator(".cm-content").fill("SELECT 77");
  await control(page, "reference").first().click();
  await ask(page);
  await expect(control(page, "apply")).toHaveCount(2);
  expect(state.opens[1].sql).toBe(proposal);
  await control(page, "new-query").click();
  await ask(page);
  await expect(control(page, "apply")).toHaveCount(3);
  expect(state.opens[2]).toMatchObject({
    sql: "",
    previousSessionId: "session-2",
  });
  await expect(control(page, "panel").locator("article").last()).toContainText(
    "另写查询"
  );
});

test("a revoked source cannot supply history or deliver an older suggestion into the new draft", async ({
  page,
}) => {
  const state = await backend(page);
  await enter(page);
  await expand(page);
  await ask(page);
  await expect(control(page, "apply")).toBeVisible();
  await editor(page).locator(".ds-list").click();
  await page.getByRole("treeitem", { name: "Warehouse B" }).click();
  state.denyHistory = true;
  await ask(page);
  await expect.poll(() => state.opens.length).toBe(3);
  expect(state.opens[2]).toMatchObject({ resourceId: "second", sql: initial });
  expect(state.opens[2].previousSessionId).toBeUndefined();
  await expect(control(page, "apply")).toHaveCount(1);
  await expect(control(page, "panel").getByRole("alert")).toContainText(
    "先前对话"
  );
  await assertUnrun(page, state);
});

test("a written reference to the first version overrides a later hand edit", async ({
  page,
}) => {
  const state = await backend(page);
  await enter(page);
  await expand(page);
  await ask(page);
  await expect(control(page, "apply")).toBeVisible();
  await editor(page).locator(".cm-content").fill("SELECT 77");
  await control(page, "input")
    .locator("textarea")
    .or(page.locator('textarea[data-testid="dataset-sql-assistant-input"]'))
    .fill("基于第一版建议再加日期");
  await control(page, "send").click();
  await expect(control(page, "apply")).toHaveCount(2);
  expect(state.opens[1].sql).toBe(proposal);
});

test("each suggestion expands its own table and field evidence", async ({
  page,
}) => {
  const state = await backend(page, {
    evidence: [
      {
        table: "orders",
        inspected: true,
        description: "销售订单",
        grain: "每笔订单一行",
        relations: [{
          name: "下单客户",
          leftTable: "orders",
          rightTable: "customers",
          cardinality: "MANY_TO_ONE",
          joinType: "LEFT",
          actual: { leftTable: "customers", joinType: "INNER", rightTable: "orders" },
          conditions: [
            {leftField: "tenant_id", rightField: "tenant_id"},
            {leftField: "buyer_id", rightField: "cid"},
          ],
        }],
        fields: [
          {
            name: "pay_code",
            type: "CHAR",
            businessName: "支付状态",
            values: [{ value: "P", meaning: "已支付" }],
          },
        ],
      },
    ],
  });
  await enter(page);
  await expand(page);
  await ask(page);
  const cards = control(page, "panel").locator("article");
  await expect(cards.first().getByText("每笔订单一行")).toBeHidden();
  const evidence = cards.locator("details").filter({
    has: page.locator("summary", { hasText: "用表与字段依据" }),
  });
  await evidence.first().locator("summary").click();
  await expect(cards.first()).toContainText("pay_code");
  await expect(cards.first()).toContainText("P → 已支付");
  for (const label of ["下单客户", "orders LEFT JOIN customers", "customers INNER JOIN orders", "MANY_TO_ONE",
    "orders.tenant_id = customers.tenant_id", "orders.buyer_id = customers.cid"]) {
    await expect(evidence.first()).toContainText(label);
  }
  state.query = "SELECT count(*) FROM items";
  state.evidence = [{ table: "items", inspected: false, fields: [] }];
  await ask(page);
  await evidence.nth(1).locator("summary").click();
  await expect(evidence.nth(1)).toContainText("items");
  await expect(evidence.nth(1)).not.toContainText("pay_code");
  await expect(cards.first()).toContainText("每笔订单一行");
  await assertUnrun(page, state);
});

test("system variable suggestions send only editor names and run with mapped identifiers", async ({
  page,
}) => {
  await page.addInitScript(() => {
    window.fixtureSystemVariables = [
      {
        id: "449000000000000003",
        name: "订单月份",
        type: "text",
        value: "private-fixture-value",
      },
    ];
  });
  const query =
    "SELECT SUM(actual_amt) AS total FROM orders WHERE $DE_PARAM{order_month = $f2cde[订单月份]} AND $DE_PARAM{pay_code = $f2cde[账号]}";
  const state = await backend(page, {
    query,
    previewTotals: ["100"],
  });
  await enter(page, "&assistantBlank=1");
  await expand(page);
  await ask(page, "按订单月份和当前账号筛选");
  await control(page, "apply").click();
  await expect(editor(page).locator(".cm-content")).toContainText("订单月份");
  const opening = state.opens[0];
  expect(opening.systemVariableNames).toEqual(
    expect.arrayContaining(["账号", "订单月份"])
  );
  expect(JSON.stringify(opening)).not.toMatch(
    /private-fixture-value|449000000000000003/
  );
  await assertUnrun(page, state);
  await editor(page).getByRole("button", { name: "运行", exact: true }).click();
  await expect(editor(page).locator(".table-scroll")).toContainText("100");
  expect(Buffer.from(state.previews[0].sql, "base64").toString()).toBe(
    query
      .replace("订单月份", "449000000000000003")
      .replace("账号", "sysParams.userId")
  );
});

for (const legacy of [false, true]) {
  for (const cross of [false, true]) {
    test(`dashboard parameters reuse settings and preview defaults (legacy=${legacy}, cross=${cross})`, async ({
      page,
    }) => {
      const draft =
        "SELECT SUM(actual_amt) AS total FROM orders WHERE " +
        (legacy ? "oid >= ${minimum}" : "$DE_PARAM{oid >= $[minimum]}");
      const query =
        draft +
        " AND " +
        (legacy ? "pay_code = ${payment}" : "$DE_PARAM{pay_code = $[payment]}");
      const state = await backend(page, {
        query,
        previewTotals: ["140", "100"],
      });
      await enter(page, cross ? "&cross=1" : "");
      await editor(page).locator(".cm-content").fill(draft);
      const settings = page.locator(".sql-dataset-drawer .ed-drawer:visible");
      const openSettings = () =>
        editor(page)
          .getByRole("button", { name: "参数设置", exact: true })
          .click();
      const confirmSettings = () =>
        settings.getByRole("button", { name: "确认", exact: true }).click();
      await openSettings();
      const minimum = settings.getByRole("row").filter({ hasText: "minimum" });
      await minimum.locator(".select-type").click();
      await page
        .locator(".cascader-panel:visible")
        .getByText("数值", { exact: true })
        .click();
      await minimum.locator('input[type="number"]').fill("2");
      await confirmSettings();
      await expand(page);
      await ask(page, "保留最低订单号，增加支付状态仪表板参数");
      await control(page, "apply").click();
      await page
        .locator(".ed-message-box:visible")
        .getByRole("button", { name: "确定", exact: true })
        .click();
      await expect(editor(page).locator(".cm-content")).toHaveText(query);
      await assertUnrun(page, state);
      expect(state.opens[0].sql).toBe(draft);
      const run = () =>
        editor(page).getByRole("button", { name: "运行", exact: true }).click();
      await run();
      await expect(editor(page).locator(".table-scroll")).toContainText("140");
      const details = JSON.parse(state.previews[0].sqlVariableDetails);
      expect(details).toMatchObject([
        { variableName: "minimum", type: ["LONG"], defaultValue: "2" },
        { variableName: "payment", type: ["TEXT"], defaultValue: "" },
      ]);
      await openSettings();
      const payment = settings.getByRole("row").filter({ hasText: "payment" });
      await payment
        .locator('input[type="text"]:not([readonly])')
        .last()
        .fill("P");
      await confirmSettings();
      await run();
      await expect(editor(page).locator(".table-scroll")).toContainText("100");
      expect(JSON.parse(state.previews[1].sqlVariableDetails)[1]).toMatchObject(
        {
          variableName: "payment",
          type: ["TEXT"],
          defaultValue: "P",
          defaultValueScope: "EDIT",
        }
      );
      expect(await page.evaluate(() => window.saved)).toBeUndefined();
    });
  }
}

test("system variable catalog follows workspace changes in an open editor", async ({
  page,
}) => {
  await page.addInitScript(() => {
    window.fixtureSystemVariables = [
      { name: "旧空间变量", id: "449000000000000003", type: "text" },
    ];
  });
  const state = await backend(page);
  await enter(page);
  await expand(page);
  await ask(page, "哪些系统变量可供查询？");
  await expect(control(page, "apply")).toBeVisible();
  expect(state.opens[0].systemVariableNames).toContain("旧空间变量");
  await page.evaluate(() => {
    window.fixtureSystemVariables = [
      { name: "新空间变量", id: "449000000000000004", type: "text" },
    ];
    window.aiSwitchUser("workspace");
  });
  await expand(page);
  await ask(page, "哪些系统变量可供查询？");
  await expect.poll(() => state.opens.length).toBe(2);
  expect(state.opens[1].systemVariableNames).toContain("新空间变量");
  expect(state.opens[1].systemVariableNames).not.toContain("旧空间变量");
});

test("a manual preview failure can be repaired after editing and manually rerun", async ({
  page,
}) => {
  const state = await backend(page);
  const failedSql = "SELECT misspelled_amt FROM orders";
  await page.route("**/fixture-api/dataset/previewSql", async (route) => {
    const request = route.request().postDataJSON();
    const sql = Buffer.from(request.sql, "base64").toString();
    if (sql !== failedSql) return route.fallback();
    state.previews.push(request);
    return route.fulfill({
      status: 400,
      json: {
        msg: "Unknown column 'misspelled_amt' in 'field list'; password=error-private",
      },
    });
  });
  await enter(page);
  const draft = editor(page).locator(".cm-content");
  await draft.fill(failedSql);
  const run = editor(page).getByRole("button", { name: "运行", exact: true });
  await run.click();
  await expect.poll(() => state.previews.length).toBe(1);
  await draft.fill("SELECT 99");
  await expand(page);
  await ask(page, "刚才为什么报错");
  await expect(control(page, "apply")).toHaveCount(1);
  expect(state.opens[0]).toMatchObject({
    sql: failedSql,
    preview: {
      outcome: "UNKNOWN_COLUMN",
      resourceId: "source",
      isCross: false,
    },
  });
  expect(state.opens[0].preview.revision).toBeLessThan(
    state.opens[0].preview.currentRevision
  );
  expect(JSON.stringify(state.opens)).not.toContain("error-private");
  await expect(control(page, "panel").locator("article").first()).toContainText(
    "失败预览"
  );
  await expect(control(page, "panel").locator("article").first()).toContainText(
    "草稿已修改"
  );
  await control(page, "apply").click();
  await page
    .locator(".ed-message-box:visible")
    .getByRole("button", { name: "确定", exact: true })
    .click();
  await expect(draft).toHaveText(proposal);
  expect(state.previews).toHaveLength(1);
  await run.click();
  await expect(editor(page).locator(".table-scroll")).toContainText("200");
  state.explain = true;
  await ask(page, "为什么订单重复");
  await expect(control(page, "panel").locator("article")).toHaveCount(2);
  await expect.poll(() => state.opens.length).toBe(2);
  expect(state.opens[1]).toMatchObject({
    sql: proposal,
    preview: { outcome: "SUCCESS" },
  });
  expect(JSON.stringify(state.opens[1])).not.toContain("200");
  expect(state.previews).toHaveLength(2);
  expect(await page.evaluate(() => window.saved)).toBeUndefined();
});

test("disabling AI during a manual preview preserves its table result", async ({
  page,
}) => {
  const state = await backend(page);
  let release;
  const gate = new Promise((resolve) => (release = resolve));
  let pending = false;
  await page.route("**/fixture-api/dataset/previewSql", async (route) => {
    pending = true;
    await gate;
    await route.fallback();
  });
  await enter(page);
  await expand(page);
  await editor(page).getByRole("button", { name: "运行", exact: true }).click();
  await expect.poll(() => pending).toBe(true);
  try {
    state.available = false;
    await page.evaluate(() => window.dispatchEvent(new Event("focus")));
    await expect(control(page, "toggle")).toHaveCount(0);
  } finally {
    release();
  }
  await expect(editor(page).locator(".table-scroll")).toContainText("200");
});

test("a late older preview cannot replace the newer result or cross datasource boundaries", async ({
  page,
}) => {
  const state = await backend(page);
  const staleSql = "SELECT delayed_column FROM orders";
  let release,
    started = false;
  const gate = new Promise((resolve) => (release = resolve));
  await page.route("**/fixture-api/dataset/previewSql", async (route) => {
    const req = route.request().postDataJSON();
    if (Buffer.from(req.sql, "base64").toString() !== staleSql)
      return route.fallback();
    started = true;
    await gate;
    await route.fulfill({
      status: 400,
      json: { msg: "Unknown column 'delayed_column'" },
    });
  });
  await enter(page);
  const draft = editor(page).locator(".cm-content");
  const run = editor(page).getByRole("button", { name: "运行", exact: true });
  await draft.fill(staleSql);
  await run.click();
  await expect.poll(() => started).toBe(true);
  const lateResponse = page.waitForResponse(
    (r) => r.url().endsWith("/previewSql") && r.status() === 400
  );
  try {
    await draft.fill(proposal);
    await run.click();
    await expect(editor(page).locator(".table-scroll")).toContainText("200");
  } finally {
    release();
  }
  await lateResponse;
  await expand(page);
  await ask(page, "为什么订单重复");
  await expect(control(page, "apply")).toBeVisible();
  expect(state.opens[0]).toMatchObject({
    sql: proposal,
    preview: { outcome: "SUCCESS" },
  });
  await expect(draft).toHaveText(proposal);
  await editor(page).locator(".ds-list").click();
  await page.getByRole("treeitem", { name: "Warehouse B" }).click();
  await draft.fill("SELECT 2");
  state.denyHistory = true;
  await ask(page, "刚才为什么报错");
  await expect.poll(() => state.opens.length).toBe(3);
  for (const open of state.opens.slice(1)) {
    expect(open).toMatchObject({ resourceId: "second", sql: "SELECT 2" });
    expect(open.preview).toBeUndefined();
  }
  expect(state.opens[2].previousSessionId).toBeUndefined();
});

test("old failed preview is repaired on its own source after switching the editor", async ({ page }) => {
  const state = await backend(page);
  await page.route("**/fixture-api/dataset/previewSql", route => route.fulfill({
    status: 400, json: { msg: "Unknown column 'missing_col'" }
  }));
  await enter(page);
  const draft = editor(page).locator(".cm-content");
  await draft.fill("SELECT missing_col FROM orders");
  await editor(page).getByRole("button", { name: "运行", exact: true }).click();
  await editor(page).locator(".ds-list").click();
  await page.getByRole("treeitem", { name: "Warehouse B" }).click();
  await draft.fill("SELECT 2");
  await expand(page);
  await ask(page, "刚才为什么报错");
  await expect(control(page, "apply")).toBeVisible();
  expect(state.opens[0]).toMatchObject({ sql: "SELECT missing_col FROM orders",
    preview: { outcome: "UNKNOWN_COLUMN", currentResourceId: "second" } });
  expect(state.opens[0].resourceId).toBe(state.opens[0].preview.resourceId);
  await control(page, "apply").click();
  await expect(page.locator(".ed-message-box:visible")).toContainText("Warehouse B");
  await page.locator(".ed-message-box:visible").getByRole("button", { name: /Cancel|取消/ }).click();
  await expect(draft).toHaveText("SELECT 2");
  await ask(page, "解释当前草稿");
  await expect.poll(() => state.opens.length).toBe(2);
  expect(state.opens[1]).toMatchObject({ resourceId: "second", sql: "SELECT 2" });
});

test("stop during continuation keeps the new idle context for the following question", async ({ page }) => {
  const state = await backend(page);
  await enter(page);
  await expand(page);
  await ask(page);
  await expect(control(page, "apply")).toHaveCount(1);
  let release;
  const pending = new Promise(resolve => release = resolve);
  state.onOpen = () => pending;
  await ask(page, "continue");
  await expect.poll(() => state.opens.length).toBe(2);
  await control(page, "stop").click();
  release();
  await expect(control(page, "send")).toBeVisible();
  await ask(page, "which table did we use?");
  await expect(control(page, "apply")).toHaveCount(2);
  expect(state.opens[2].previousSessionId).toBe("session-2");
  expect(state.starts).toBe(2);
  expect(state.closes).toBe(0);
});
