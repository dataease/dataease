const { test, expect } = require("@playwright/test");

const first = "[orders_id] - [items_id]";
const second = "([orders_id] - [items_id]) / [orders_id]";
const manual = "[items_id] * 2";
const expressions = [first, second, "1", "2", "3", "4"];
const button = (scope, name) =>
  scope.getByRole("button", { name, exact: true });

async function serveTurns(page, lifetime = 300000) {
  const state = {
    opens: [],
    checks: [],
    closed: [],
    replied: [],
    beforeResult: null,
    expressions: [...expressions],
  };
  await page.route("**/fixture-api/ai/**", async (route) => {
    const request = route.request();
    const path = new URL(request.url()).pathname;
    const reply = (json) => route.fulfill({ json });
    if (path.endsWith("/availability")) return reply({ available: true });
    if (path.endsWith("/conversations")) {
      const opening = request.postDataJSON();
      const index = state.opens.push(opening);
      return reply({
        editorId: opening.editorId,
        resourceId: opening.resourceId,
        resourceName: "Fixture MySQL",
        resourceVersion: "r1",
        sessionId: "s" + index,
        contextVersion: "v" + index,
        draftVersion: "d" + index,
        expiresAt: Date.now() + lifetime,
      });
    }
    const sessionId = path.split("/")[4];
    const n = Number(sessionId.slice(1));
    const executionId = "e" + n;
    const opening = state.opens[n - 1];
    if (request.method() === "DELETE") {
      state.closed.push(sessionId);
      return reply({});
    }
    if (path.endsWith("/executions"))
      return reply({ executionId, status: "RUNNING" });
    const result = state.clarification && n === 1 ? {
      type: "EXPLANATION",
      text: state.clarification,
    } : {
      type: "CALC_FIELD_SUGGESTION",
      text: "已生成公式。",
      calcField: {
        expression: state.expressions[n - 1],
        name: "建议名" + n,
        groupType: "q",
        deType: 3,
        validation: "DETAIL_PASS",
      },
      resourceId: opening.resourceId,
      resourceVersion: "r1",
      editorId: opening.editorId,
      draftVersion: "d" + n,
    };
    if (path.endsWith("/check-application")) {
      state.checks.push(sessionId);
      return reply(result);
    }
    if (path.endsWith("/events")) {
      if (state.beforeResult) await state.beforeResult(n);
      await reply({
        sessionId,
        executionId,
        contextVersion: "v" + n,
        status: "COMPLETED",
        cursor: 1,
        events: [],
        result,
      });
      state.replied.push(sessionId);
      return;
    }
    throw Error("Unexpected AI call: " + path);
  });
  return state;
}

async function openDrawer(page) {
  await button(page, "新建计算字段").click();
  const drawer = page.locator(".calc-field-drawer");
  await button(drawer, "计算字段助手").click();
  return drawer;
}

async function ask(drawer, question) {
  await drawer.locator("textarea").fill(question);
  await button(drawer, "发送").click();
}

async function completed(drawer, index) {
  const turn = drawer.getByRole("article").nth(index - 1);
  await expect(button(turn, "应用到当前草稿")).toBeEnabled();
  return turn;
}

test("clarification accepts a free-text calculation period before suggesting a formula", async ({ page }) => {
  const state = await serveTurns(page);
  state.clarification = "增长率应对比哪个期间？\n1. 上月\n2. 去年同期";
  await page.goto("/");
  const drawer = await openDrawer(page);
  await ask(drawer, "算增长率");
  const question = drawer.getByRole("article").first();
  await expect(button(question, "上月")).toBeEnabled();
  await expect(button(question, "应用到当前草稿")).toHaveCount(0);
  const input = drawer.locator("textarea");
  await input.fill("使用上周的订单量");
  await input.press("Enter");
  await completed(drawer, 2);
  await expect(button(question, "上月")).toBeDisabled();
  await expect(button(question, "上月")).toHaveAttribute("aria-pressed", "false");
  expect(state.opens[1].previousSessionId).toBe("s1");
});

test("long conversations scroll inside the calculated field drawer", async ({
  page,
}) => {
  await page.setViewportSize({ width: 1280, height: 720 });
  const state = await serveTurns(page);
  await page.goto("/");
  const drawer = await openDrawer(page);
  const history = drawer.getByRole("log");
  const editor = drawer.locator(".cm-content");
  const editorTop = (await editor.boundingBox()).y;
  for (let i = 1; i <= 5; i++) {
    await ask(drawer, "第 " + i + " 次调整计算公式");
    await completed(drawer, i);
  }
  expect(
    await history.evaluate((el) => el.scrollHeight - el.clientHeight)
  ).toBeGreaterThan(0);
  const latest = history.getByRole("article").last();
  await expect(button(latest, "应用到当前草稿")).toBeInViewport({ ratio: 1 });
  await expect(drawer.locator("textarea")).toBeInViewport({ ratio: 1 });
  await expect(button(drawer, "发送")).toBeInViewport({ ratio: 1 });
  await drawer.screenshot({
    path: test.info().outputPath("calc-field-conversation.png"),
  });

  let release;
  const hold = new Promise((resolve) => {
    release = resolve;
  });
  state.beforeResult = () => hold;
  await ask(drawer, "继续调整公式");
  const oldest = history.getByRole("article").first();
  await oldest.scrollIntoViewIfNeeded();
  release();
  await completed(drawer, 6);
  await expect(oldest).toBeInViewport({ ratio: 1 });
  await expect(drawer.locator("textarea")).toBeInViewport({ ratio: 1 });
  expect((await editor.boundingBox()).y).toBe(editorTop);
  const header = drawer.locator("header");
  for (const action of ["计算字段助手", "取消", "校验", "确认"]) {
    await expect(button(header, action)).toBeInViewport({ ratio: 1 });
  }
});

test("the current formula or any selected turn is frozen as the basis of each question", async ({
  page,
}) => {
  const state = await serveTurns(page);
  await page.goto("/");
  const drawer = await openDrawer(page);
  await ask(drawer, "逐行生成差额");
  const initial = await completed(drawer, 1);
  await button(drawer, "计算字段助手").click();
  await expect(drawer.getByRole("log")).toBeHidden();
  await button(drawer, "计算字段助手").click();
  await ask(drawer, "再改成毛利率");
  const followUp = await completed(drawer, 2);
  expect(state.opens[1]).toMatchObject({ previousSessionId: "s1", sql: first });
  await followUp.locator("summary").click();
  await expect(followUp.locator("details pre")).toHaveText(first);
  await drawer.locator(".cm-content").fill(manual);
  await ask(drawer, "保留两位小数");
  const manualTurn = await completed(drawer, 3);
  expect(state.opens[2].sql).toBe(manual);
  await button(initial, "基于此建议追问").click({ timeout: 3000 });
  await ask(drawer, "除以十");
  await completed(drawer, 4);
  expect(state.opens[3]).toMatchObject({ previousSessionId: "s3", sql: first });
  await drawer.getByRole("checkbox", { name: "本轮引用" }).uncheck();
  await ask(drawer, "每条记录计为一");
  await completed(drawer, 5);
  expect(state.opens[4].sql).toBe("");
  await ask(drawer, "根据第二轮保留三位小数");
  await completed(drawer, 6);
  expect(state.opens[5].sql).toBe(second);
  expect(state.checks).toEqual(["s1", "s1", "s2"]);
  await manualTurn.locator("summary").click();
  await expect(manualTurn.locator("details pre")).toHaveText(manual);
  await expect(drawer.locator(".cm-content")).toHaveText(manual);
});

test("an edit made during generation is preserved until the author confirms an older suggestion", async ({
  page,
}) => {
  const state = await serveTurns(page);
  await page.goto("/?dependency=1");
  await page.getByRole("tab", { name: "批量管理" }).click();
  const row = page.getByRole("row", { name: /计算字段/ });
  await row.locator("td").last().getByRole("button").last().click();
  const drawer = page.locator(".calc-field-drawer");
  const code = drawer.locator(".cm-content");
  await expect(code).toHaveText("[orders_id]");
  await button(drawer, "计算字段助手").click();
  let release;
  const hold = new Promise((resolve) => {
    release = resolve;
  });
  state.beforeResult = () => hold;
  await ask(drawer, "生成毛利");
  await expect.poll(() => state.opens.length).toBe(1);
  await code.fill(manual);
  const basis = drawer.getByRole("article").first().locator("details").first();
  await basis.locator("summary").click({ timeout: 3000 });
  await expect(basis.locator("pre")).toHaveText("[orders_id]");
  release();
  const original = await completed(drawer, 1);
  await expect(code).toHaveText(manual);
  await ask(drawer, "改成整数");
  await completed(drawer, 2);
  expect(state.opens[1].sql).toBe(manual);
  const applyOld = button(original, "应用到当前草稿");
  await applyOld.click();
  const confirm = page.locator(".ed-message-box:visible");
  await expect(confirm).toContainText("这会覆盖发问后对草稿的修改。");
  await button(confirm, "取消").click();
  await expect(code).toHaveText(manual);
  expect(state.checks).toEqual([]);
  await applyOld.click();
  await button(confirm, "确定").click();
  await expect(code).toHaveText(first);
  expect(state.checks).toEqual(["s1"]);
  await expect(drawer.getByPlaceholder("请输入字段名称")).toHaveValue(
    "计算字段"
  );
  await expect(button(drawer, "维度")).toHaveClass(/is-active/);
  await button(drawer, "确认").click();
  await button(page, "保存").click();
  await expect
    .poll(() =>
      page.evaluate(() => window.saved?.allFields.find((f) => f.id === "calc"))
    )
    .toMatchObject({
      name: "计算字段",
      groupType: "d",
      deType: 0,
      originName: "[orders1] - [items1]",
    });
});

test("closing a pending drawer drops its late result and starts another editing activity", async ({
  page,
}) => {
  const state = await serveTurns(page);
  let release, received;
  const hold = new Promise((resolve) => {
    release = resolve;
  });
  const pending = new Promise((resolve) => {
    received = resolve;
  });
  state.beforeResult = async (n) => {
    if (n === 1) {
      received();
      await hold;
    }
  };
  await page.goto("/");
  const drawer = await openDrawer(page);
  await ask(drawer, "稍后返回的建议");
  await pending;
  await button(drawer, "取消").click();
  await expect(drawer).toBeHidden();
  await openDrawer(page);
  await drawer.locator(".cm-content").fill(manual);
  await ask(drawer, "新字段的建议");
  await completed(drawer, 1);
  release();
  await expect.poll(() => state.replied).toContain("s1");
  await expect(drawer.getByRole("article")).toHaveCount(1);
  await expect(drawer.getByRole("log")).not.toContainText("稍后返回的建议");
  await expect(drawer.locator(".cm-content")).toHaveText(manual);
  expect(state.opens[1].previousSessionId).toBeUndefined();
  expect(state.opens[1].editorId).not.toBe(state.opens[0].editorId);
  expect(state.closed).toContain("s1");
});

for (const scope of ["user", "expiry"]) {
  test(
    scope + " clears formula suggestions while preserving the author's draft",
    async ({ page }) => {
      const state = await serveTurns(page, scope === "expiry" ? 1800 : 300000);
      await page.goto("/");
      const drawer = await openDrawer(page);
      await drawer.locator(".cm-content").fill(manual);
      await ask(drawer, "建议一个比例");
      await completed(drawer, 1);
      if (scope === "user") await page.evaluate(() => window.aiSwitchUser());
      await expect(drawer.getByRole("article")).toHaveCount(0);
      await expect(drawer.locator(".cm-content")).toHaveText(manual);
      await expect.poll(() => state.closed).toEqual(["s1"]);
    }
  );
}

test("a changed calculation parameter invalidates a proposal's field bindings", async ({
  page,
}) => {
  const state = await serveTurns(page);
  state.expressions[0] = "[orders_id] * [比例]";
  await page.goto("/");
  const drawer = await openDrawer(page);
  await drawer.locator(".hover-icon_quota").click();
  const param = page.locator(".create-dialog:visible");
  await param.getByRole("textbox").fill("比例");
  await param.getByRole("spinbutton").fill("3");
  await button(param, "确认").click();
  await ask(drawer, "按比例计算差额");
  const old = await completed(drawer, 1);
  const item = drawer.locator(".item-quota").filter({ hasText: /^比例$/ });
  await item.hover();
  await item.locator(".icon-right .hover-icon").first().click();
  await param.getByRole("textbox").fill("新比例");
  await button(param, "确认").click();
  await button(old, "应用到当前草稿").click();
  const warning = page.locator(".ed-message-box:visible");
  if (await warning.isVisible()) await button(warning, "确定").click();
  await expect(drawer.getByRole("alert")).toBeVisible();
  await expect(drawer.locator(".cm-content")).toHaveText("");
  expect(state.checks).toEqual([]);
});

test("a single blank basis keeps the discussion and recomputes the next formula reference", async ({
  page,
}) => {
  const state = await serveTurns(page);
  await page.goto("/");
  const drawer = await openDrawer(page);
  await drawer.locator(".cm-content").fill(manual);
  const checkbox = drawer.getByRole("checkbox", { name: "本轮引用" });
  const reference = drawer.getByRole("button", { name: /查看完整引用/ });
  await expect(reference).toContainText(manual);
  await ask(drawer, "按订单统计");
  const original = await completed(drawer, 1);
  await expect(reference).toContainText("建议 #1");
  await expect(reference).toContainText(first);
  await checkbox.uncheck();
  await ask(drawer, "沿用这个业务口径");
  const without = await completed(drawer, 2);
  expect(state.opens[1]).toMatchObject({ previousSessionId: "s1", sql: "" });
  await expect(without.getByText(/生成依据/)).toHaveCount(0);
  await expect(checkbox).toBeChecked();
  await expect(reference).toContainText(second);
  await button(original, "基于此建议追问").click();
  await ask(drawer, "请从头写，沿用刚才确定的口径");
  const scratch = await completed(drawer, 3);
  expect(state.opens[2]).toMatchObject({ previousSessionId: "s2", sql: "" });
  await expect(scratch.getByText(/生成依据/)).toHaveCount(0);
});

test("new conversation closes a running formula reply and starts from the current field", async ({
  page,
}) => {
  const state = await serveTurns(page);
  let release;
  const pending = new Promise((resolve) => {
    release = resolve;
  });
  let oldReply;
  state.beforeResult = async (n) => {
    if (n === 1) {
      oldReply = true;
      await pending;
    }
  };
  await page.goto("/");
  const drawer = await openDrawer(page);
  await ask(drawer, "旧对话的问题");
  await expect.poll(() => oldReply).toBe(true);
  await drawer.locator(".cm-content").fill(manual);
  try {
    await button(drawer, "新对话").click({ timeout: 3000 });
    await expect(drawer.getByRole("article")).toHaveCount(0);
    await expect.poll(() => state.closed).toEqual(["s1"]);
    await expect(drawer.locator(".cm-content")).toHaveText(manual);
    await expect(
      drawer.getByRole("checkbox", { name: "本轮引用" })
    ).toBeChecked();
    await ask(drawer, "新的问题");
    await completed(drawer, 1);
    expect(state.opens[1]).toMatchObject({
      sql: manual,
      resourceId: state.opens[0].resourceId,
    });
    expect(state.opens[1].previousSessionId).toBeUndefined();
    expect(state.opens[1].editorId).not.toBe(state.opens[0].editorId);
  } finally {
    release();
  }
  await expect.poll(() => state.replied).toContain("s1");
  await expect(drawer.getByRole("article")).toHaveCount(1);
  await expect(drawer.getByRole("log")).not.toContainText("旧对话的问题");
});

test("formula correction shows both validation outcomes and keeps the detail being read", async ({
  page,
}) => {
  await serveTurns(page);
  const tool = (sequence, callId, status, expression, verdict) => ({
    sequence,
    type: "TOOL",
    tool: {
      callId,
      name: "propose_calc_field",
      status,
      parameters: { expression },
      result: verdict && JSON.stringify(verdict),
    },
  });
  let finish, entered;
  const hold = new Promise((resolve) => {
    finish = resolve;
  });
  const waiting = new Promise((resolve) => {
    entered = resolve;
  });
  await page.route("**/executions/e1/events**", async (route) => {
    const after = Number(
      new URL(route.request().url()).searchParams.get("after")
    );
    const scope = { sessionId: "s1", contextVersion: "v1", executionId: "e1" };
    if (!after)
      return route.fulfill({
        json: {
          ...scope,
          status: "RUNNING",
          cursor: 5,
          events: [
            { sequence: 1, type: "TEXT", text: "检查原公式。" },
            tool(2, "one", "STARTED", "[missing]"),
            tool(3, "one", "SUCCEEDED", "[missing]", {
              status: "INVALID",
              message: "字段不存在",
            }),
            { sequence: 4, type: "TEXT", text: "改用订单字段。" },
            tool(5, "two", "RUNNING", "[orders_id]"),
          ],
        },
      });
    entered();
    await hold;
    return route.fulfill({
      json: {
        ...scope,
        status: "COMPLETED",
        cursor: 8,
        metering: { elapsedMillis: 2670, usageStatus: "PARTIAL", inputTokens: null, outputTokens: 12 },
        events: [
          tool(6, "two", "SUCCEEDED", "[orders_id]", { status: "DETAIL_PASS" }),
          { sequence: 7, type: "TEXT", text: "已校验修正后的公式。" },
          { sequence: 8, type: "COMPLETED" },
        ],
        result: {
          type: "CALC_FIELD_SUGGESTION",
          text: "已校验修正后的公式。",
          calcField: { expression: "[orders_id]", validation: "DETAIL_PASS" },
        },
      },
    });
  });
  await page.goto("/");
  const drawer = await openDrawer(page);
  await ask(drawer, "检查订单公式");
  const process = drawer
    .locator("details")
    .filter({ has: page.locator("summary", { hasText: "执行过程" }) })
    .first();
  const rejected = drawer
    .locator("details")
    .filter({ has: page.locator("summary", { hasText: "校验公式 · 未通过" }) })
    .last();
  const running = drawer
    .locator("details")
    .filter({ has: page.locator("summary", { hasText: "校验公式 · 执行中" }) })
    .last();
  try {
    await expect(rejected.locator("summary")).toBeVisible({ timeout: 3000 });
    await expect(rejected).not.toHaveAttribute("open", "");
    await expect(running).toHaveAttribute("open", "");
    await rejected.locator("summary").click();
    await expect(rejected).toContainText("字段不存在");
    await waiting;
  } finally {
    finish();
  }
  await expect(button(drawer, "应用到当前草稿")).toBeEnabled();
  await expect(process).toHaveAttribute("open", "");
  await expect(rejected).toHaveAttribute("open", "");
  await expect(process.locator(":scope > summary")).toContainText("总耗时 2.67 s");
  const footer = drawer.getByRole("article").locator("footer");
  await expect(footer).toContainText("输入 Token —");
  await expect(footer).toContainText("输出 Token 12");
  await expect(footer).toContainText("用量不完整");
  await expect(process.locator("details")).toHaveCount(2);
  const passed = process.locator("details").last();
  await expect(passed.locator("summary")).toHaveText("校验公式 · 通过");
  await expect(passed).not.toHaveAttribute("open", "");
  await expect(process).not.toContainText("已校验修正后的公式。");
  await expect(
    drawer.getByText("已校验修正后的公式。", { exact: true })
  ).toBeVisible();
});
